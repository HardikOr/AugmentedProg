//
// Created by zheka on 03.06.2017.
//


#include <jni.h>
#include <string>
#include <math.h>

#include "native-lib.h"

extern "C"
//JNIEXPORT jstring JNICALL

#define MINOR_DIFFERENCE_THRESHOLD 30
#define MAJOR_DIFFERENCE_THRESHOLD 75
#define SPOT_RADIUS 4


void Java_com_reality_augmented_augmentedprog_MainActivity_nativeCaptureProcessor(
        JNIEnv* env,
        jobject, jintArray _pixels, int w, int h) {

    jint *pixels = env->GetIntArrayElements(_pixels, 0);

    for (int x = 0; x < w - 1; x++) {
        for (int y = 0; y < h - 1; y++) {
            // get brigtness of both pixels
            int pixel1 = colorVal(pixels[x + y * w]);
            int pixel2 = colorVal(pixels[(x + 1) + (y + 1) * w]);

            // 1st filter
            // if there is significant difference between them, proceed
            if (fabs(pixel1 - pixel2) > MINOR_DIFFERENCE_THRESHOLD) {
                // check difference in larger range
                int p1 = colorVal(pixels[(x - SPOT_RADIUS) + (y - SPOT_RADIUS) * w]);
                int p2 = colorVal(pixels[(x + SPOT_RADIUS) + (y - SPOT_RADIUS) * w]);
                int p3 = colorVal(pixels[(x - SPOT_RADIUS) + (y + SPOT_RADIUS) * w]);
                int p4 = colorVal(pixels[(x + SPOT_RADIUS) + (y + SPOT_RADIUS) * w]);

                // 2nd filter
                if (fabs(p1 - p4) > MAJOR_DIFFERENCE_THRESHOLD && fabs(p2 - p3) > MAJOR_DIFFERENCE_THRESHOLD) {
                    pixels[x + y * w] = (255 << 24) + (255 << 16);
                }
                else {
                    pixels[x + y * w] = (255 << 24) + 255;
                }
            }
            else {
                pixels[x + y * w] = 255 << 24; // set to black
            }
            // =  + (fabs((p1 & 255) - (p2 & 255)) > 20 ? 255 : 0);
        }
    }

    env->ReleaseIntArrayElements(_pixels, pixels, 0);
}
/*
JNIEXPORT jstring JNICALL
Java_com_reality_augmented_augmentedprog_MainActivity_processImageTest(JNIEnv* env, jobject) {
    printf("PROCESSING TEST");
}*/

double colorVal(int color) {
    // r = color << 16
    // g = color << 8
    // b = color << 0

    return ((color << 16) & 255) + ((color << 8) & 255) + (color & 255);
}

