package com.reality.augmented.augmentedprog;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Activity current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        current = this;

        ((Button) findViewById(R.id.startButton)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(current, CameraActivity.class);
                startActivity(intent);
            }
        });
    }

    static {
        System.loadLibrary("native-lib");
    }

    public static native void nativeTest(int[] pixels, int w, int h);
}
