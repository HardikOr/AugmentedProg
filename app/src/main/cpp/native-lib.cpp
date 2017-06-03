//
// Created by zheka on 03.06.2017.
//


#include <jni.h>
#include <string>
#include <math.h>

#include "test.h"

extern "C"
//JNIEXPORT jstring JNICALL

void Java_com_reality_augmented_augmentedprog_MainActivity_nativeTest(
        JNIEnv* env,
        jobject, jintArray _pixels, int w, int h) {

    jint *pixels = env->GetIntArrayElements(_pixels, 0);

    for (int x = 0; x < w - 1; x++) {
        for (int y = 0; y < h - 1; y++) {
            int p1 = pixels[x + y * w];
            int p2 = pixels[(x + 1) + (y + 1) * w];

            pixels[x + y * w] = (255 << 24) + (fabs((p1 & 255) - (p2 & 255)) > 20 ? 255 : 0);
        }
    }

    env->ReleaseIntArrayElements(_pixels, pixels, 0);
}
/*
JNIEXPORT jstring JNICALL
Java_com_reality_augmented_augmentedprog_MainActivity_processImageTest(JNIEnv* env, jobject) {
    printf("PROCESSING TEST");
}*/

