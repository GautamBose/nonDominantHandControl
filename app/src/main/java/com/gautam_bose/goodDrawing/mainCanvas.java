package com.gautam_bose.goodDrawing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
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
//        frameRate(240);
//        background(255);
    }

    public void setup() {
    }

    public void draw() {
        background(255);
        tool.drawTool();
//        tool.positionTool();
//        for (int i = 0; i < touches.length; i++) {
//            float d = 100;
//            fill(0, 255 * touches[i].pressure);
//            ellipse(touches[i].x, touches[i].y, d, d);
//            fill(255, 0, 0);
//            text(touches[i].id, touches[i].x, touches[i].y);
//        }
//        if (touches.length == 3) {
//            line(touches[0].x, touches[0].y, touches[1].x, touches[1].y);
//            line(touches[1].x, touches[1].y, touches[2].x, touches[2].y);
//        }

    }

    @Override
    public boolean surfaceTouchEvent(MotionEvent motionEvent) {
        tool.positionTool();
        return super.surfaceTouchEvent(motionEvent);
    }

    @Override
    public void touchStarted() {
        if (tool.numFingsInTool() == 2) {
            tool.makeThirdButtonAvaliable();
        }

        tool.setActiveButtons();
    }

    @Override
    public void touchEnded() {
//        println(touches.length);
        println("called");
        if (touches.length <= 2) {
            tool.removeThirdButton();
        }
        tool.setActiveButtons();

    }


    class ToolButton {
        float x, y, radius;
        boolean isActive;

        ToolButton(float x, float y, int radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            isActive = true;
        }

        ToolButton(float x, float y, int radius, boolean isActive) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.isActive = isActive;
        }

        void setIsActive(boolean isDown) {
            this.isActive = isDown;
        }

        boolean isOver(float mx, float my) {
//            boolean isOver;
            float distance = this.distance(mx, my, this.x, this.y);
            return (distance < radius);

        }

        float getX() {
            return x;
        }

        float getY() {
            return y;
        }

        void moveButton(float x, float y) {
            this.x = x;
            this.y = y;
        }

        void render() {
            if (isActive){
                fill(0);
            }
            else {
                fill(255, 0, 0);
            }

            ellipse(this.x, this.y, radius, radius);
        }

        float distance(float x1, float y1, float x2, float y2) {
            return sqrt((float) (Math.pow((x1 - x2), 2.0) + Math.pow((y1 - y2), 2.0)));
        }
    }


    class Tool {
        private int buttRadius;
        private ArrayList<ToolButton> buttonList;
        private int numFingersDown;

        Tool() {
            buttRadius = 150;
            buttonList = new ArrayList<>();
            buttonList.add(new ToolButton(100, 200, buttRadius));
            buttonList.add(new ToolButton(500, 500, buttRadius, false));
        }

        void makeThirdButtonAvaliable() {
            if (touches.length == 2 && buttonList.size() < 3) {
//                println("called");
                ToolButton auxButton0 = buttonList.get(0);
                ToolButton auxButton1 = buttonList.get(1);
                buttonList.add(new ToolButton((auxButton1.x + auxButton0.x) / 2 - 200, (auxButton1.y + auxButton0.y) / 2 + 200, buttRadius, false));
            }
        }

        void removeThirdButton() {
//            println("wefwef");
            if (buttonList.size() > 2) {
                buttonList.remove(buttonList.size() - 1);
            }
        }

        int numFingsInTool() {
            int numButtonsInTool = 0;
            for (TouchEvent.Pointer currpointer : touches) {
                for (ToolButton currButton : buttonList) {
                    if (currButton.isOver(currpointer.x, currpointer.y)) {
                        numButtonsInTool++;
                    }
                }
            }
            return numButtonsInTool;
        }


        void drawTool() {
            for (ToolButton currButton : buttonList) {
                currButton.render();
            }
        }

        float[] calculateOffset(ToolButton b1, ToolButton b2) {
            float x1 = b1.getX();
            float x2 = b2.getX();
            float y1 = b1.getY();
            float y2 = b2.getY();
            float[] retArray = new float[]{0, 0};
            retArray[0] = x1 - x2;
            retArray[1] = y1 - y2;

            return retArray;

        }

        void positionTool() {
            for (TouchEvent.Pointer currpointer : touches) {
                for (ToolButton currButton : buttonList) {
                    if (currButton.isOver(currpointer.x, currpointer.y)) {
                        currButton.moveButton(currpointer.x, currpointer.y);
//                        println(currButton.isActive);
                    }

                }
            }
        }

        public void setActiveButtons() {
            for (ToolButton currButton: buttonList) {
                currButton.setIsActive(false);
                for (TouchEvent.Pointer currPointer: touches) {
                    if (currButton.isOver(currPointer.x, currPointer.y)) {
                        currButton.setIsActive(true);
                        break;
                    }
                }

            }
        }
    }
}






