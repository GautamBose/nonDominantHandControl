package com.gautam_bose.goodDrawing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import processing.core.PApplet;
import processing.android.PFragment;
import processing.android.CompatUtils;
import processing.event.TouchEvent;


public class mainCanvas extends AppCompatActivity {
    private PApplet sketch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frame = new FrameLayout(this);
        frame.setId(CompatUtils.getUniqueViewId());
        setContentView(frame, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        sketch = new Sketch();
        PFragment fragment = new PFragment(sketch);
        fragment.setView(frame, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (sketch != null) {
            sketch.onRequestPermissionsResult(
                    requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (sketch != null) {
            sketch.onNewIntent(intent);
        }
    }
}

// Sketch.java



class Sketch extends PApplet {
    public void settings() {
        fullScreen();
//        background(255);
    }

    public void setup() { }

    public void draw() {
        background(255);
//        for (int i = 0; i < touches.length; i++) {
//            float d = 100;
//            fill(0, 255 * touches[i].pressure);
//            ellipse(touches[i].x, touches[i].y, d, d);
//            fill(255, 0, 0);
//            text(touches[i].id, touches[i].x, touches[i].y);

//        if (touches.length == 3) {
//            line(touches[0].x, touches[0].y, touches[1].x, touches[1].y);
//            line(touches[1].x, touches[1].y, touches[2].x, touches[2].y);
//        }

    }

}

class Tool {
    private boolean isActive;
    private float x1, y1, x2, y2;
    private int radius1, radius2, radius3;
    Tool() {

    }

    void drawTool() {

    }

    void positionTool(TouchEvent touchEvent) {

    }

    void isSelected() {

    }
}
