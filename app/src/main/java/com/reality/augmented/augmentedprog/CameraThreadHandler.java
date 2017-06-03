package com.reality.augmented.augmentedprog;

import android.graphics.Bitmap;
import android.os.Process;
import android.renderscript.RenderScript;

import java.util.ArrayList;

/**
 * Created by zheka on 03.06.2017.
 */

public class CameraThreadHandler {
    private Thread thread = null;
    private ArrayList<Bitmap> queue = new ArrayList<Bitmap>();

    private CameraHelper cameraHelper;
    private ICaptureProcessor captureProcessor;
    public CameraThreadHandler(CameraHelper cameraHelper) {
        this.cameraHelper = cameraHelper;
    }

    private int skippedFrames = 0;
    public void Queue(Bitmap bmp) {
        if (queue.size() < 2) {
            if (skippedFrames > 0) {
                System.out.println("skipped frames: " + skippedFrames);
                skippedFrames = 0;
            }
            queue.add(bmp);
        }
        else {
            skippedFrames++;
        }

        if (thread == null) {
            startThread();
        }
    }

    private boolean isRunning = false;
    private int threadCount = 4;

    public void setThreadCount(int count) {
        threadCount = count;
    }

    private void startThread() {
        this.captureProcessor = cameraHelper.getCaptureProcessor();
        for (int i = 0; i < threadCount; i++) {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bmp;
                    Process.setThreadPriority(19);
                    while (isRunning) {
                        if (queue.size() > 0) {
                            try {
                                bmp = queue.get(0);
                                queue.remove(0);
                            } catch (IndexOutOfBoundsException e) {
                                continue;
                            }
                            if (bmp != null)
                                captureProcessor.process(bmp, cameraHelper.targetView, cameraHelper.parentActivity);
                        }
                    }
                }
            });
            isRunning = true;
            thread.start();
        }
    }
}
