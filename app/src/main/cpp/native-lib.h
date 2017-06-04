//
// Created by zheka on 03.06.2017.
//

#ifndef AUGMENTEDPROG_NATIVE_LIB_H_H
#define AUGMENTEDPROG_NATIVE_LIB_H_H


void debug_select_significant_point(jint *pInt, int w, int h, int x, int y);

void grayscale(int *pixels, int size);

double colorDiff(int color1, int color2);
int colorVal(int color);

bool corner_detector_trajkovic(jint* pixels, int w, int h, int x, int y, int t);
bool corner_detector_fast(jint *pixels, int w, int h, int x, int y, int N, int t);


#endif //AUGMENTEDPROG_NATIVE_LIB_H_H
