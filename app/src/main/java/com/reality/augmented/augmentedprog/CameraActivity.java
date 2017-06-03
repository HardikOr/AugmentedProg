package com.reality.augmented.augmentedprog;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageView;

import java.nio.ByteBuffer;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RunnableFuture;

/**
 * Created by user on 6/2/17.
 */

public class CameraActivity extends Activity {
    ImageView imgView;
    TextureView texView;

    int threadsRunning = 0;

    CameraActivity current;

    Bitmap bmp;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.test_image_layout);
        imgView = (ImageView) findViewById(R.id.testImageView);
        texView = (TextureView) findViewById(R.id.testTextureView);

        CameraHelper cameraHelper = new CameraHelper(this, texView);

        cameraHelper.setCaptureProcessor(new NativeCaptureProcessor(imgView));

        cameraHelper.setTargetResolution(300, 300);
        cameraHelper.startCameraProcessing();
    }

}
