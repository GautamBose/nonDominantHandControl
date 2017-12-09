package com.gautam_bose.goodDrawing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;

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
    Tool tool;
    public void settings() {
        fullScreen();
        tool = new Tool();
//        background(255);
    }

    public void setup() { }

    public void draw() {
        background(255);
        tool.drawTool();
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

    @Override
    public void mouseDragged() {
        super.mouseDragged();
//        @todo add some checking here for when the pen is down
    }

    class ToolButton {
        float x, y, radius;

        ToolButton(int x, int y, int radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }

        boolean isOver(float mx, float my) {
            boolean isOver;
            float distance = this.distance(mx, my, this.x, this.y);
            return (distance > radius);

        }

        void moveButton(float x, float y) {
            this.x = x;
            this.y = y;
        }

        float distance(float x1, float y1, float x2, float y2) {
            return sqrt((float) (Math.pow((x1 - x2),2.0) + Math.pow((y1 - y2),2.0)));
        }
    }
    class Tool {
        private boolean isActive;
        private float x1, y1, x2, y2;
        private int radius1, radius2, radius3;

        private ArrayList<ToolButton> buttonList;
        Tool() {

            buttonList = new ArrayList<>();
            buttonList.add(new ToolButton(width/4, height / 4, 100));
            buttonList.add(new ToolButton(width / 2, height / 2, 100));


        }



        void drawTool() {
            ellipse(x1, y1, radius1, radius1);
            line(x1,y1, x2, y2);
            ellipse(x2, y2, radius2, radius2);
        }

        void positionTool(TouchEvent touchEvent) {

        }

        void isSelected() {
            for (TouchEvent.Pointer touch: touches) {

            }
        }
    }



}


