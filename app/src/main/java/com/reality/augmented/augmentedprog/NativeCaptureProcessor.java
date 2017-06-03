package com.reality.augmented.augmentedprog;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.TextureView;
import android.widget.ImageView;

/**
 * Created by zheka on 03.06.2017.
 */

public class NativeCaptureProcessor implements ICaptureProcessor {
    ImageView debugView;

    public NativeCaptureProcessor(ImageView view) {
        debugView = view;
    }

    public NativeCaptureProcessor() {

    }

    Bitmap bitmap;

    @Override
    public void process(Bitmap bmp, TextureView view, Activity parentActivity) {
        int w = bmp.getWidth(), h = bmp.getHeight();
        int[] colors = new int[w * h];
        bmp.getPixels(colors, 0, w, 0, 0, w, h);

        MainActivity.nativeCaptureProcessor(colors, w, h);

        bmp.setPixels(colors, 0, w, 0, 0, w, h);

        if (debugView != null) {
            bitmap = bmp;
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    debugView.setImageBitmap(bitmap);
                }
            });
        }
    }
}
