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
import processing.core.PVector;
import processing.event.TouchEvent;

import static processing.core.PVector.angleBetween;


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
    private Tool tool;
    boolean isCurrentGesture;

    public void settings() {
        fullScreen();
        tool = new Tool();
//        frameRate(240);
//        background(255);
    }

    public void setup() {
        isCurrentGesture = false;
    }

    public void draw() {
        background(255);
        tool.drawTool();
    }

    @Override
    public boolean surfaceTouchEvent(MotionEvent motionEvent) {

        tool.positionTool();
        if (tool.numFingsInTool() == 3) {
//            tool.threeFingGesture == true;
            tool.radialActivation(isCurrentGesture);
            isCurrentGesture = true;
        }
        else {
            isCurrentGesture = false;
        }
        return super.surfaceTouchEvent(motionEvent);
    }

    @Override
    public void touchStarted() {
        if (tool.numFingsInTool() == 2) {
            tool.makeThirdButtonAvaliable();
        }
        tool.setActiveButtons(true);
    }

    @Override
    public void touchEnded() {
        println("endedtouches");
        println(touches.length);
//        println("called");
        if (touches.length <= 2) {
            tool.removeThirdButton();
        }
        tool.setActiveButtons(false);

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
        private ToolCircle circle;
        private PVector initialVec, currVec, actualVec;
        float degrees;

        Tool() {
            buttRadius = 150;
            buttonList = new ArrayList<>();
            buttonList.add(new ToolButton(100, 200, buttRadius, false));
            buttonList.add(new ToolButton(500, 500, buttRadius, false));
            circle = new ToolCircle(buttonList);
        }

        void makeThirdButtonAvaliable() {
            if (touches.length == 2 && buttonList.size() < 3) {
//                ToolButton auxButton0 = buttonList.get(0);
                ToolButton auxButton1 = buttonList.get(1);
                buttonList.add(new ToolButton(auxButton1.x, auxButton1.y + 300, buttRadius, false));
            }
        }

        void removeThirdButton() {
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

        int getButtRadius() {
            return buttRadius;
        }

        void drawTool() {
            int i = 0;
            textSize(26);
            fill(0);
            text("Degrees" + degrees(degrees), 20, 20);

            for (ToolButton currButton : buttonList) {
                currButton.render();
                fill(255);
                text("" + i, currButton.x, currButton.y);
                i++;

            }

            if (touches.length == 3) {
                circle.calculateCircle();
                circle.render();

                if (initialVec != null && currVec != null) {

                    pushMatrix();
                    translate(circle.cX, circle.cY);
                    strokeWeight(4);
                    rotate(initialVec.heading());
                    line(0, 0, 0, circle.getRenderRadius() / 2);
                    popMatrix();

                    pushMatrix();
                    translate(circle.cX, circle.cY);
                    strokeWeight(4);
                    rotate(currVec.heading());
                    line(0, 0, 0, circle.getRenderRadius() / 2);
                    popMatrix();

                    pushMatrix();
                    translate(circle.cX, circle.cY);
                    rotate(radians(90));
                    fill(0,255,0);
                    noStroke();

                    if (initialVec.heading() < currVec.heading()) arc(0, 0,  circle.getRenderRadius(), circle.getRenderRadius(), initialVec.heading(), currVec.heading(), PIE);
                    else arc(0, 0,  circle.getRenderRadius(), circle.getRenderRadius(), currVec.heading(), initialVec.heading(), PIE);
                    popMatrix();
                }

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
                    }

                }
            }
        }

        public void setActiveButtons(boolean isTouchStarted) {
            for (ToolButton currButton: buttonList) {
                currButton.setIsActive(false);
                int finalIndex = touches.length;
                //if the call is coming from touchEnded, ignore the last element of touches because it no longer is on the screen :<
                if (isTouchStarted == false) finalIndex--;

                for (int i = 0; i <finalIndex; i++) {
                    TouchEvent.Pointer currPointer = touches[i];
                    if (currButton.isOver(currPointer.x, currPointer.y)) {
                        currButton.setIsActive(true);

                        break;
                    }
                }

            }
        }

        public void radialActivation(boolean b) {
            ToolButton indexButton = buttonList.get(1);
            ToolButton thumbButton = buttonList.get(2);

            if (b == false) {
                initialVec = new PVector((indexButton.x - thumbButton.x), (indexButton.y - thumbButton.y));


            }
            else {
                currVec = new PVector(indexButton.x - thumbButton.x, (indexButton.y- thumbButton.y));
                degrees = angleBetween(initialVec, currVec);
            }

        }
    }

    class ToolCircle {
        ArrayList<ToolButton> buttonList;
        float cX, cY, x1, y1, x2, y2, x3, y3, radius, renderRadius;
        ToolCircle(ArrayList<ToolButton> buttonList) {
            this.buttonList = buttonList;
            this.calculateCircle();
        }

        private void calculateCircle() {
            //array safety
            if (buttonList.size() < 3) return;

            ToolButton b1 = buttonList.get(0);
            ToolButton b2 = buttonList.get(1);
            ToolButton b3 = buttonList.get(2);

            x1 = b1.getX();
            y1 = b1.getY();
            x2 = b2.getX();
            y2 = b2.getY();
            x3 = b3.getX();
            y3 = b3.getY();

            float slopeA = (y2 - y1) / (x2 - x1);
            float slopeB = (y3 - y2) / (x3 - x2);

            cX = ((((slopeA * slopeB) * (y1 - y3)) + (slopeB * (x1 + x2))) - (slopeA * (x2 + x3))) / (2 * (slopeB - slopeA));
            cY = (-1 / slopeB) * (cX - (x2 + x3)/2) + ((y3 + y2) / 2);

            radius = b1.distance(cX, cY, x1, y1);

        }

        float getcX() {
            return cX;
        }

        float getcY() {
            return cY;
        }

        float getRenderRadius() {
            return renderRadius;
        }

        void render() {
            noFill();
            stroke(255,0, 0);
            renderRadius = radius * 2 + tool.getButtRadius();
            ellipse(cX, cY, renderRadius, renderRadius);
        }
    }
}






