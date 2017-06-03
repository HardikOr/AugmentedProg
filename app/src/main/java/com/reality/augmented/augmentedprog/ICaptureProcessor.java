package com.reality.augmented.augmentedprog;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.TextureView;

/**
 * Created by zheka on 03.06.2017.
 */

public interface ICaptureProcessor {
    void process (Bitmap bmp, TextureView view, Activity parentActivity);
}
