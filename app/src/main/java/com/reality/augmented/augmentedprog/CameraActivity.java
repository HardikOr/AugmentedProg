package com.reality.augmented.augmentedprog;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.view.Surface;
import android.view.TextureView;

import java.nio.ByteBuffer;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 6/2/17.
 */

public class CameraActivity extends Activity {
    String cameraId;

    private Surface mCameraRecieverSurface;

    private final CameraCaptureSession.StateCallback mCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                CaptureRequest.Builder requestBuilder = session.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                requestBuilder.addTarget(mCameraRecieverSurface);
                System.out.println("CONFIGURING...");
                //set to null - image data will be produced but will not receive metadata
                session.setRepeatingRequest(requestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                        super.onCaptureStarted(session, request, timestamp, frameNumber);
                    }


                    public void process(CaptureResult result) {

                        System.out.println("PROCESSING..." + Math.random());
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
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };


    TextureView texView;
    Surface texSurface;

    CameraDevice mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.test_image_layout);
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            cameraId = manager.getCameraIdList()[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
            }

            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice cameraDevice) {
                    texView = (TextureView) findViewById(R.id.testTextureView);
                    mCamera = cameraDevice;

                    texView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                        @Override
                        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                            List<Surface> surfaceList = new ArrayList<Surface>();
                            mCameraRecieverSurface = new Surface(surfaceTexture);
                            try {
                                surfaceList.add(mCameraRecieverSurface);
                                mCamera.createCaptureSession(surfaceList, mCaptureSessionStateCallback, null);
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
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

                @Override
                public void onDisconnected(@NonNull CameraDevice cameraDevice) {

                }

                @Override
                public void onError(@NonNull CameraDevice cameraDevice, int i) {

                }

                @Override
                public void onClosed(@NonNull CameraDevice camera) {
                    super.onClosed(camera);
                }
            }, null);
            System.out.println("CAMERA OPEN SUCCESED");

        }
        catch (Exception e) {
            System.out.println("CAMERA OPEN FAILED");
            e.printStackTrace();
        }
    }
}
