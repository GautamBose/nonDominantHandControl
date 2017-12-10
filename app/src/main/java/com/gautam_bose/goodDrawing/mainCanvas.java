package com.gautam_bose.goodDrawing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;

import processing.android.CompatUtils;
import processing.android.PFragment;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.TouchEvent;

/*
@todo make inactive buttons follow
@todo visual cleanup
@todo code regarding pens <= break this down more
@todo color changing
@todo stroke changing
@todo fix placmeent of third orb
@todo canvas zooming and such
@todo finish three finger tool picker
*/

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
    private TouchDelegate delegate;
//    boolean isCurrentGesture;

    public void settings() {
        fullScreen();
        tool = new Tool();
        //eventually this will take in a canvas param as well :)
        delegate = new TouchDelegate(tool);

    }

    public void setup() {
//        isCurrentGesture = false;
    }

    public void draw() {
        background(255);
        tool.drawTool();
    }

    @Override
    public boolean surfaceTouchEvent(MotionEvent motionEvent) {
        delegate.touchEvent();
        return super.surfaceTouchEvent(motionEvent);
    }

    @Override
    public void touchStarted() {
//        if (tool.fingsInTool().size() == 2) {
//            tool.makeThirdButtonAvaliable();
//        }
//        tool.setActiveButtons(true);

        delegate.touchStarted();
    }

    @Override
    public void touchEnded() {
        delegate.touchEnded();
//        if (touches.length <= 2) {
//            tool.removeThirdButton();
//        }
//        tool.setActiveButtons(false);

    }

    //this class delegates touches to either the tool or the drawing canvas
    class TouchDelegate {
        Tool tool;
        //DrawingCanvas canvas;
        boolean isCurrentGesture;

        TouchDelegate(Tool tool) {
            this.tool = tool;
            isCurrentGesture = false;
            //this.canvas = canvas;
        }

        void touchEnded() {
            if (tool.fingsInTool().size() <= 2) {
                tool.removeThirdButton();
            }
            tool.setActiveButtons(false);

        }

        void touchStarted() {
            if (tool.fingsInTool().size() == 2) {
                tool.makeThirdButtonAvaliable();
            }
            tool.setActiveButtons(true);

        }

        void touchEvent() {
            ArrayList<TouchEvent.Pointer> canvTouches = new ArrayList<>();
            ArrayList<TouchEvent.Pointer> toolTouches = new ArrayList<>();
            ArrayList<Integer> toolTouchIndexes = tool.fingsInTool();

            for (int i = 0; i < touches.length; i++) {
                if (toolTouchIndexes.contains(i)) {
                    toolTouches.add(touches[i]);
                }
                else {
                    canvTouches.add(touches[i]);
                }
            }
            tool.positionTool(toolTouches);

            if (tool.fingsInTool().size() == 3) {
                tool.radialActivation(isCurrentGesture);
                isCurrentGesture = true;
            }
            else {
                isCurrentGesture = false;
            }

        }


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
        private ToolCircle circle;
        private PVector initialVec, currVec;
        private float oldDegrees, degrees, degreesPerFrame;
        private boolean selectionActive;

        Tool() {
            buttRadius = 150;
            buttonList = new ArrayList<>();
            buttonList.add(new ToolButton(100, 200, buttRadius, false));
            buttonList.add(new ToolButton(500, 500, buttRadius, false));
            circle = new ToolCircle(buttonList);
            selectionActive = false;
        }

        void makeThirdButtonAvaliable() {
            if (this.fingsInTool().size() == 2 && buttonList.size() < 3) {
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

        //returns the indexes in the touches array of the fingers currently on the tool
        ArrayList<Integer> fingsInTool() {
            ArrayList<Integer> touchIndexesOnTool = new ArrayList<>();
            for (int i = 0; i < touches.length; i++ ) {
                TouchEvent.Pointer currpointer = touches[i];
                for (ToolButton currButton : buttonList) {
                    if (currButton.isOver(currpointer.x, currpointer.y)) {
                        touchIndexesOnTool.add(i);
                    }
                }
            }
            return touchIndexesOnTool;
        }

        int getButtRadius() {
            return buttRadius;
        }

        void drawTool() {
            int i = 0;
            textSize(76);
            textAlign(LEFT, TOP);
            fill(0);
            text("Degrees:  " + degrees, 20, 20);
            for (ToolButton currButton : buttonList) {
                currButton.render();
                fill(255);
                text("" + i, currButton.x, currButton.y);
                i++;

            }

            if (this.fingsInTool().size() == 3) {
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
                    rotate(degrees + initialVec.heading());
                    line(0, 0, 0, circle.getRenderRadius() / 2);
                    popMatrix();

                    pushMatrix();
                    translate(circle.cX, circle.cY);
                    rotate(radians(90));
                    fill(0,255,0);
                    noStroke();
                    if (selectionActive) {
                        if (initialVec.heading() < currVec.heading())
                            arc(0, 0, circle.getRenderRadius(), circle.getRenderRadius(), initialVec.heading(), currVec.heading(), PIE);
                        else
                            arc(0, 0, circle.getRenderRadius(), circle.getRenderRadius(), currVec.heading(), initialVec.heading(), PIE);
                    }
                    popMatrix();
                }

            }
            else circle.resetTransparency();
        }

        //for making nonactve buttons glide w/ curr button
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

        void positionTool(ArrayList<TouchEvent.Pointer> touches) {
            for (TouchEvent.Pointer currpointer : touches) {
                for (ToolButton currButton : buttonList) {
                    if (currButton.isOver(currpointer.x, currpointer.y)) {
                        currButton.moveButton(currpointer.x, currpointer.y);
                    }

                }
            }
        }

        void setActiveButtons(boolean isTouchStarted) {
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

         void radialActivation(boolean b) {
            ToolButton indexButton = buttonList.get(1);
            ToolButton thumbButton = buttonList.get(2);

            if (!b) {
                initialVec = new PVector((indexButton.x - thumbButton.x), (indexButton.y - thumbButton.y));
                oldDegrees = initialVec.heading();
                selectionActive = false;

            }
            else {
                currVec = new PVector(indexButton.x - thumbButton.x, (indexButton.y- thumbButton.y));
                degrees =  (currVec.heading() - initialVec.heading());

                degreesPerFrame = abs(degrees(degrees - oldDegrees));
//                println(degreesPerFrame);
                if (!selectionActive) {
                    if ((degreesPerFrame > 3 && degreesPerFrame < 50) || (degrees(degrees) > 15 || degrees(degrees) < -15)) {

                        selectionActive = true;
                    }
                }
                oldDegrees = degrees;
            }

        }
    }

//    class Canvas

//    @todo implement these classes
    class TwoFingerCircle extends ToolCircle {
        TwoFingerCircle(ArrayList<ToolButton> buttonList) {
            super(buttonList);
        }


    }

    class FourFingerCircle extends ToolCircle {
        FourFingerCircle(ArrayList<ToolButton> buttonList) {
            super(buttonList);
        }
    }
    //@todo implement easy circle for two fingers c is the mdipoint and r is the r.
    class ToolCircle implements Runnable {
        ArrayList<ToolButton> buttonList;
        float cX, cY, x1, y1, x2, y2, x3, y3, radius, renderRadius;
        float selectionBonus;
        ToolCircle(ArrayList<ToolButton> buttonList) {
            this.buttonList = buttonList;
            this.calculateCircle();
            selectionBonus = 0;
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

        void resetTransparency() {
            selectionBonus = 0;
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
//            strokeWeight(strokeWeight);
            renderRadius = radius * 2 + tool.getButtRadius() + selectionBonus;
            ellipse(cX, cY, renderRadius, renderRadius);
            if (tool.selectionActive && selectionBonus <= 200) (new Thread(this)).start();

        }

        @Override
        public void run() {
            if (selectionBonus == 200) return;
            if (tool.selectionActive) selectionBonus += 10;
//            selectionBonus -= 10;

        }
    }
}






