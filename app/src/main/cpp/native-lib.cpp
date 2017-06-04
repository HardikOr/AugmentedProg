//
// Created by zheka on 03.06.2017.
//


#include <jni.h>
#include <string>
#include <math.h>

#include "native-lib.h"

int DEF_DEBUG_COLOR = (255 << 24) + (128 << 8);

/* numbers -> colors to display N number of FAST algorithm */
int TEST_COLORS[16] = {
        0, // 0 - impossible
        0, // black 1
        255 << 16, // red 2
        255 << 8, // green 3
        255, // blue 4
        128 << 16, // dark red 5
        128 << 8,  // dark green 6
        128, // dark blue 7
        (255 << 16) + (255 << 8), // yellow 8
        (255 << 16) + (255), // purple 9
        (255 << 8) + (255), // light blue 10
        (255 << 16) + (128 << 8), // orange 11
        (255 << 16) + (128 << 8) + (128), // pink 12
        (128 << 16) + (255 << 8) + (128), // light green 13
        (128 << 16) + (128 << 8) + (128), // gray 14
        (255 << 16) + (255 << 8) + (255), // white 15
};

extern "C"
//JNIEXPORT jstring JNICALL

#define MINOR_DIFFERENCE_THRESHOLD 15
#define MAJOR_DIFFERENCE_THRESHOLD 25
#define SPOT_RADIUS 2

void Java_com_reality_augmented_augmentedprog_MainActivity_nativeCaptureProcessor(
        JNIEnv* env,
        jobject, jintArray _pixels, int w, int h) {

    jint *pixels = env->GetIntArrayElements(_pixels, 0);

    grayscale(pixels, w * h);

    jint *pixels2 = (jint*) malloc(w * h * sizeof(jint));


    for (int x = 10; x < w - 10; x++) {
        for (int y = 10; y < h - 10; y++) {
            // get brightness of both pixels
            int pixel1 = pixels[x + y * w];
            int pixel2 = pixels[(x - 1) + (y - 1) * w];

            if (pixels2[x + y * w] != DEF_DEBUG_COLOR){
                int val = (int)(pixels[x + y * w] / 3);
                pixels2[x + y * w] = (255 << 24) + (val << 16) + (val << 8) + val;
            }

            // 1st filter
            // if there is significant difference between them, proceed
            if (fabs(pixel1 - pixel2) > MINOR_DIFFERENCE_THRESHOLD) {
                if (pixels2[x + y * w] != DEF_DEBUG_COLOR) {
                    pixels2[x + y * w] = (255 << 24) + 255;
                }
                /*
                 * F.A.S.T. detector - main detector, tracks corners and straight lines
                 * trajkovic detector - removes linear segments (must do this, but for now it is not working)
                */
                if (corner_detector_fast(pixels, w, h, x, y, 12, 20)) {
                //if (corner_detector_fast(pixels, w, h, x, y, 9, 20) && corner_detector_trajkovic(pixels, w, h, x, y, 800)){
                //if (corner_detector_trajkovic(pixels, w, h, x, y, 1200)){
                    debug_select_significant_point(pixels2, w, h, x, y);
                }
            }
        }
    }

    env->ReleaseIntArrayElements(_pixels, pixels2, 0);
}

void debug_select_significant_point(jint *pixels, int w, int h, int x, int y) {
    int c = DEF_DEBUG_COLOR;

    int s = 12;
    int hs = s / 2;

    for (int i = 0; i < s; i++) {
        pixels[(x + i - hs) + (y - hs) * w] = c;
        pixels[(x + i - hs) + (y + hs) * w] = c;
        pixels[(x - hs) + (y - hs + i) * w] = c;
        pixels[(x + hs) + (y - hs + i) * w] = c;
    }
}

void grayscale(jint *pixels, int size) {
    for (int i = 0; i < size; i++) {
        pixels[i] = (jint) colorVal(pixels[i]);
    }
}

int colorVal(int color) {
    // r = color << 16
    // g = color << 8
    // b = color << 0

    return ((color << 16) & 255) + ((color << 8) & 255) + (color & 255);
}

double colorDiff(int color1, int color2) {
    double r = fabs(((color1 << 16) & 255) - ((color2 << 16) & 255));
    double g = fabs(((color1 << 8) & 255) - ((color2 << 8) & 255));
    double b = fabs((color1 & 255) - (color2 & 255));

    return fabs(color1 - color2); //fmax(r, fmax(b, g));
}





/*
 * ----------------------------------------------
 * CORNER DETECTION
 * ----------------------------------------------
 */


/*
  ###
 #   #
#     #
#  @  #
#     #
 #   #
  ###
*/

int positions_FAST12[16][2] = {
        {0, -3},
        {1, -3},
        {2, -2},
        {3, -1},
        {3, 0},
        {3, 1},
        {2, 2},
        {1, 3},
        {0, 3},
        {-1, 3},
        {-2, 2},
        {-3, 1},
        {-3, 0},
        {-3, -1},
        {-2, -2},
        {-1, -3}
};

int positions_FAST9[8][2] = {
        {0, 1},
        {1, 1},
        {1, 0},
        {1, -1},
        {0, -1},
        {-1,-1},
        {-1, 0},
        {-1, 1}
};

/* this data was used for FAST12 tests */
int test_FAST12[16] = {
        -1,
        1,
        0,
        0,
        0,
        0,
        0,
        0,
        -1,
        1,
        1,
        1,
        1,
        1,
        -1,
        -1
};

/*
 ###
#   #
# @ #
#   #
 ###
*/

int positions_trajkovic[12][2] = {
    {0, -2},
    {1, -2},
    {2, -1},
    {2, 0},
    {2, 1},
    {1, 2},
    {0, 2},
    {-1, 1},
    {-1, 0},
    {-1, -1}
};

bool corner_detector_trajkovic(jint* pixels, int w, int h, int x, int y, int t) {
    int px, py;
    int minCRN = 99999;

    jint In = pixels[x + y * w];

    for (int i = 1; i < 6; i++) {
        px = x + positions_trajkovic[i][0];
        py = y + positions_trajkovic[i][1];
        jint Ip1 = pixels[px + py * w];
        px = x + positions_trajkovic[(i + 6) % 12][0];
        py = y + positions_trajkovic[(i + 6) % 12][1];
        jint Ip2 = pixels[px + py * w];

        int CRN = (In - Ip1) * (In - Ip1) + (In - Ip2) * (In - Ip2);
        if (CRN < minCRN)
            minCRN = CRN;
    }

    return minCRN > t;
}

int corner_detector_fast(jint *pixels, int w, int h, int x, int y, int N, int t) {
    int px, py;

    int Ip = (int) pixels[x + y * w];

/*
    int checker[4] = {
            pixels[x + positions_FAST12[0][0] + (y + positions_FAST12[0][1]) * w],
            pixels[x + positions_FAST12[4][0] + (y + positions_FAST12[4][1]) * w],
            pixels[x + positions_FAST12[8][0] + (y + positions_FAST12[8][1]) * w],
            pixels[x + positions_FAST12[12][0] + (y + positions_FAST12[12][1]) * w]
    };

    if (fabs(checker[0] - checker[2]) < t && fabs(checker[1] - checker[3]) < t)
        return false;
    int type1 = 0, type2 = 0;
    for (int i = 0; i < 4; i++) {
        if (checker[i] > Ip + t)
            type1++;
        if (checker[i] < Ip - t)
            type2++;
    }

    if (type1 != 3 && type2 != 3)
        return false;
*/

    /*mode values: -1 - none, 0 - darker, 1 - brighter */
    int maxStreak = 0, curStreak = 0, curMode = -1;
    int firstMode = -1, firstStreak = 0;

    int mode, p;

    for (int i = 0; i < 16; i++) {
        px = x + positions_FAST12[i][0];
        py = y + positions_FAST12[i][1];

        p = (int) pixels[px + py * w];
        mode = p > Ip + t ? 1 : (p < Ip - t ? 0 : -1);

        if (curMode == mode)
            curStreak++;
        else {
            if (curMode != -1) {
                if (maxStreak < curStreak)
                    maxStreak = curStreak;
                if (firstStreak == 0) {
                    firstStreak = curStreak;
                    firstMode = curMode;
                }
            }
            curStreak = 1;
        }

        curMode = mode;
    }

    if (curStreak > maxStreak)
        maxStreak = curStreak;
    if (curMode != -1 && curMode == firstMode && firstStreak + curStreak > maxStreak)
        maxStreak = firstStreak + curStreak;

    return maxStreak >= N;
}