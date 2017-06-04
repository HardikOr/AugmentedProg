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
    int frameSkip = 1;

    public NativeCaptureProcessor(ImageView view, int skip) {
        debugView = view;
        frameSkip = skip;
    }

    public NativeCaptureProcessor(ImageView view) {
        debugView = view;
    }

    public NativeCaptureProcessor() {

    }

    Bitmap bitmap;
    int frame = 0;

    @Override
    public void process(Bitmap bmp, TextureView view, Activity parentActivity) {
        if (frame++ % frameSkip != 0)
            return;

        int w = bmp.getWidth(), h = bmp.getHeight();
        int[] colors = new int[w * h];
        bmp.getPixels(colors, 0, w, 0, 0, w, h);

        MainActivity.nativeCaptureProcessor(colors, w, h);

        if (debugView != null) {
            if (bitmap == null)
                bitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);

            bitmap.setPixels(colors, 0, w, 0, 0, w, h);

            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    debugView.setImageBitmap(bitmap);
                }
            });
        }
    }
}
