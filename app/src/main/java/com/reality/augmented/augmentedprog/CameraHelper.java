package com.reality.augmented.augmentedprog;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zheka on 03.06.2017.
 */

public class CameraHelper {
    private TextureView targetView;
    private Surface targetSurface;

    private String cameraId;
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;

    private Activity parentActivity;

    public CameraHelper(Activity parentActivity, TextureView targetView) {
        this.parentActivity = parentActivity;
        this.targetView = targetView;

        cameraManager = (CameraManager) parentActivity.getSystemService(Context.CAMERA_SERVICE);

        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            System.out.println("CANNOT RECEIVE CAMERA ID LIST");
            e.printStackTrace();
            return;
        }
    }

    /**
     * checks camera permission and asks for it
    */
    private boolean checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(parentActivity, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
            return false;
        }
        return true;
    }

    /* capture processor interface instance*/
    private ICaptureProcessor captureProcessor;
    /* result capture bitmap size */
    private int captureW = 100, captureH = 100;

    /**
     * sets capture processor
     */
    public void setCaptureProcessor(ICaptureProcessor processor) {
        captureProcessor = processor;
    }

    /**
     * sets result capture resolution
     */
    public void setTargetResolution(int w, int h) {
        captureW = w;
        captureH = h;
    }

    /**
     * opens and configures camera, starts capture, that will be translated to capture processor
    */
    public void startCameraProcessing() {
        this.checkCameraPermission();

        try {
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    onCameraOpened();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {

                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    System.out.println("CAMERA ERROR OCCURRED: " + error);
                }

                @Override
                public void onClosed(@NonNull CameraDevice camera) {
                    super.onClosed(camera);
                }
            }, null);
        }
        catch (Exception e) {
            System.out.println("CAMERA FAILED TO OPEN");
            e.printStackTrace();
        }
    }

    /* called after camera is opened, waits for surface texture */
    private void onCameraOpened() {
        SurfaceTexture texture = targetView.getSurfaceTexture();

        if (texture != null) {
            targetSurface = new Surface(texture);
            onSurfaceReceived();
        }
        else {
            targetView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                    targetSurface = new Surface(surfaceTexture);
                    onSurfaceReceived();
                }
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

                }
            });
        }
    }

    /* called after surface texture is received, starts capture session */
    private void onSurfaceReceived() {
        List<Surface> surfaceList = new ArrayList<Surface>();
        surfaceList.add(targetSurface);
        try {
            cameraDevice.createCaptureSession(surfaceList, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    onSurfaceConfigured(session);
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    System.out.println("CAMERA CONFIGURE FAILED");
                }
            }, null);
        } catch(Exception e) {
            System.out.println("FAILED TO START CAPTURE SESSION");
            e.printStackTrace();
        }
    }

    /* called after capture and surface are configured, starts capture and adds processing callback */
    private void onSurfaceConfigured(CameraCaptureSession session) {
        try {
            CaptureRequest.Builder requestBuilder = session.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            requestBuilder.addTarget(targetSurface);

            session.setRepeatingRequest(requestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                public void process(CaptureResult result) {
                    onImageProcessed();
                }

                @Override
                public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                                @NonNull CaptureRequest request,
                                                @NonNull CaptureResult partialResult) {
                    process(partialResult);
                }

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    process(result);
                }
            }, new Handler());

        } catch (CameraAccessException e) {
            System.out.println("CAPTURE CONFIGURE FAILED");
            e.printStackTrace();
        }
    }

    /* contains captured bitmap */
    private Bitmap captureBitmap;
    /* updates captured bitmap and calls capture processor */
    private void onImageProcessed() {
        /* creating random bitmap and then using in TextureView.getBitmap(bitmap) instead of simple TextureView.getBitmap() will not cause lags & memory leaks */
        if (captureBitmap == null) {
            captureBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        }
        captureBitmap = Bitmap.createScaledBitmap(targetView.getBitmap(captureBitmap), captureW, captureH, false);

        if (captureProcessor != null) {
            captureProcessor.process(captureBitmap, targetView, parentActivity);
        }
    }
}
