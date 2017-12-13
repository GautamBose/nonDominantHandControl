package com.gautam_bose.goodDrawing;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.method.Touch;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.reflect.Array;
import java.util.ArrayList;

import processing.android.CompatUtils;
import processing.android.PFragment;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import processing.event.TouchEvent;

/*
@todo make inactive buttons follow
@todo visual cleanup
@todo code regarding pens <= break this down more
@todo color changing
@todo stroke changing
@todo canvas zooming and such
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
    private DrawingCanvas canvas;
    private TouchDelegate delegate;
//    boolean isCurrentGesture;

    public void settings() {
        fullScreen(P3D);
        //P3D renderer for improved performance


    }

    public void setup() {
        background(255);
        tool = new Tool();
        canvas = new DrawingCanvas();
        delegate = new TouchDelegate(tool, canvas);
    }

    public void draw() {
        background(255);

        canvas.renderToTexture();
        canvas.renderToScreen();
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

    }

    abstract class Brush {
        int color, brushMinSize, brushMaxSize, brushSize;
        boolean brushSizeVariable, isOpaque;
        PImage brushIcon;
        

        void setColor(int color) {
            this.color = color;
        }
    }

    class Eraser extends Brush {
        Eraser() {
            this.color = color(255, 255, 255);
            brushSizeVariable = false;
            brushSize = 40;
            isOpaque = true;
//            brushIcon = loadImage(Environment.getExternalStorageDirectory().getPath());
        }
    }

    class Pen extends Brush {
        Pen() {
            this.color = color(0,0,0);
            brushSizeVariable = true;
            brushMinSize = 7;
            brushMaxSize = 20;
            isOpaque = true;
        }
    }

    class Marker extends Brush {
        Marker() {
            this.color = color(150,150,150);
            brushSizeVariable = true;
            brushMinSize = 20;
            brushMaxSize = 40;
            isOpaque = true;

        }
    }

    class DrawingCanvas {
        PGraphics canvars;
        boolean isCurrentStroke;
        boolean internalCurrentStroke;
        ArrayList<PVector> drawingPoints;
        PVector oldC, oldD, p4;
        ArrayList<Float> velocities;
        ArrayList<PVector> smoothedPoints;
        ArrayList<Brush> brushes;
        Brush currBrush;

        DrawingCanvas() {
            canvars = createGraphics(displayWidth, displayHeight, P3D);
            isCurrentStroke = false;
            internalCurrentStroke = false;
            drawingPoints = new ArrayList<>();
            velocities = new ArrayList<>();
            brushes = new ArrayList<>();
            brushes.add(new Eraser());
            brushes.add(new Pen());
            brushes.add(new Marker());
            this.setCurrentBrush(tool.getSelector().brushZone);

            oldC = new PVector();
            oldD = new PVector();

        }

        void setCurrentBrush(int brushZone) {
            currBrush = brushes.get(brushZone);

        }
        void addStroke(boolean currStroke, TouchEvent.Pointer penTip) {
            if (currBrush.brushSizeVariable) {
                if (drawingPoints.size() >= 2) {
                    PVector p0 = drawingPoints.get(drawingPoints.size() - 2);
                    PVector p1 = drawingPoints.get(drawingPoints.size() - 1);

//            find perpendicular vector to direction of points
                    PVector direction = new PVector(p0.x - p1.x, p0.y - p1.y);
                    drawingPoints.add(new PVector(penTip.x, penTip.y, this.getSize(direction)));

                } else {
                    drawingPoints.add(new PVector(penTip.x, penTip.y));
                }
            }

            else {
                drawingPoints.add(new PVector(penTip.x, penTip.y, currBrush.brushSize));
            }
//                println("calculateSmoothedPoints called");
             smoothedPoints = calculateSmoothLinePoints();

        }

        void setIsCurrentStroke(boolean b) {
            this.isCurrentStroke = b;
        }

        void renderToTexture() {

            if (smoothedPoints == null || !(smoothedPoints.size() >= 2)) return;
//            println(smoothedPoints.size());
            PVector p0 = smoothedPoints.get(smoothedPoints.size() - 2);
            PVector p1 = smoothedPoints.get(smoothedPoints.size() - 1);

//            find perpendicular vector to direction of points
            PVector direction = new PVector(p0.x - p1.x, p0.y -p1.y);
            PVector perpendicular = new PVector(-direction.y, direction.x);
            perpendicular.normalize();

            //multiply by width;
            PVector A = PVector.add(p0, PVector.mult(perpendicular, p0.z));
            PVector B = PVector.sub(p0, PVector.mult(perpendicular, p0.z));
            PVector C = PVector.add(p1, PVector.mult(perpendicular, p1.z));
            PVector D = PVector.sub(p1, PVector.mult(perpendicular, p1.z));

            if (internalCurrentStroke) {
                A = oldC;
                B = oldD;
            }

            //render to canvars, a pgraphics object.
            canvars.beginDraw();
            canvars.fill(currBrush.color);
            canvars.noStroke();
            canvars.triangle(A.x, A.y, B.x, B.y, C.x, C.y);
            canvars.triangle(B.x, B.y, C.x, C.y, D.x, D.y);
            if (currBrush.isOpaque) {
                canvars.ellipse(p4.x, p4.y, p4.z * 2, p4.z * 2);
            }

            canvars.endDraw();

            oldC = C.copy();
            oldD = D.copy();
            internalCurrentStroke = true;



        }

        ArrayList<PVector> calculateSmoothLinePoints() {
            if (drawingPoints.size() > 2) {
                ArrayList<PVector> smoothedPoints = new ArrayList<>();
                for (int i = 2; i < drawingPoints.size(); i++) {
                    //retrive last 3 points in the array
                    PVector prev2 = drawingPoints.get(i - 2);
                    PVector prev1 = drawingPoints.get(i - 1);
                    PVector currP = drawingPoints.get(i);
                    
                    //calculate the midpoint by finding the vectors between them and multiplying by scalar 0.5
                    PVector m1 = PVector.add(prev1, prev2).mult((float) 0.5);
                    PVector m2 = PVector.add(currP, prev1).mult((float) 0.5);

                    int numSegments = 2;
                    float distance = abs(new PVector(m1.x - m2.x, m1.y - m2.y).mag());
                    //here we define how many points we want based on the distance the touch traveled.
                    int numberOfSegments = min(128, max(floor(distance / numSegments), 64));
                    float t = 0;
                    float step = 1 / numberOfSegments;
                    for (int j = 0; j < numberOfSegments; j++) {
                        PVector newPoint;
                        //here we create a vector at a critical point using the quadratic bezier formula.
                        newPoint = PVector.add(PVector.add(PVector.mult(m1, pow(1-t, 2)), PVector.mult(prev1, 2 * (1-t) * t)), PVector.mult(m2, t * t));

                        //here we see if we need change the brush due to speed size depending on if its vairable or not
                        if (currBrush.brushSizeVariable) {
                            newPoint.z = pow(1 - t, 2) * ((prev1.z + prev2.z) * (float) 0.5) + 2 * (1 - t) * t * prev1.z + t * t * ((currP.z + prev1.z) * (float) 0.5);
                        }
                        else {newPoint.z = currBrush.brushSize; }

                        smoothedPoints.add(newPoint);
                        t += step;
                    }

                    //here we write the final point
                    p4 = new PVector();
                    p4.x = m2.x;
                    p4.y = m2.y;
                    p4.z = (currP.z + prev1.z) * (float) 0.5;
                    smoothedPoints.add(p4);

                }
                //we remove from the original drawing points all the points we just drew
                drawingPoints.subList(0, drawingPoints.size() - 2).clear();
                return smoothedPoints;

            }

            else {
                return null;
            }
        }

        //this function calculates the width of the stroke based on speed.
        float getSize(PVector direction) {
            float speed = direction.mag();
            float size = speed / 30;
            size = constrain(size, 1, 40);

            //we weight the size based on the previous velocity in the array so that we don't get jumps in stroke size
            if (velocities.size() > 1) {
                size *= 0.2 + velocities.get(velocities.size() - 1) * 0.8;
            }

            size = constrain(size, currBrush.brushMinSize, currBrush.brushMaxSize);

            velocities.add(size);

            return size;
        }

        //code for drawing to screen
        void renderToScreen() {
            image(canvars,0, 0);

            if (!(drawingPoints.size() >= 2) || isCurrentStroke) return;
            internalCurrentStroke = false;
            drawingPoints.clear();
            velocities.clear();
            if (smoothedPoints!= null) smoothedPoints.clear();
        }
    }
    //this class delegates touches to either the tool or the drawing canvas
    class TouchDelegate {
        Tool tool;
        DrawingCanvas canvas;
        boolean isCurrentGesture;
        boolean isCurrentStroke;

        TouchDelegate(Tool tool, DrawingCanvas canvas) {
            this.tool = tool;
            isCurrentGesture = false;
            isCurrentStroke = false;
            this.canvas = canvas;
        }

        void touchEnded() {
            if (tool.fingsInTool().size() <= 2) {
                tool.removeThirdButton();
            }

            tool.setActiveButtons(false);

//            if (tool.fingsInTool().size() == touches.length) {
//                canvas.setIsCurrentStroke(false);
//            }


        }

        void touchStarted() {
            if (tool.fingsInTool().size() == 2) {
                tool.makeThirdButtonAvaliable();
            }

        }

        void touchEvent() {
            //here we sort touches for the canvas and the tool.
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

            if (canvTouches.size() > 0) {
                canvas.setIsCurrentStroke(true);
                canvas.addStroke(isCurrentStroke, canvTouches.get(0));
            }
            else {
                canvas.setIsCurrentStroke(false);
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
//            if (isActive){
//                fill(0);
//            }
//            else {
//                fill(1, 0, 0);
//            }

            fill(106,153,183);
            noStroke();
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
        private boolean selectionActive, colorMode;
        protected BrushSelector selector;

        Tool() {
            buttRadius = 150;
            buttonList = new ArrayList<>();
            buttonList.add(new ToolButton(100, 200, buttRadius, false));
            buttonList.add(new ToolButton(500, 500, buttRadius, false));
            circle = new ToolCircle(buttonList);
            selector = new BrushSelector(circle);
            selectionActive = false;

        }

        BrushSelector getSelector() {
            return selector;
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

            for (ToolButton currButton : buttonList) {
                currButton.render();

                i++;

            }

            if (this.fingsInTool().size() == 3) {
                circle.calculateCircle();
                circle.render();

                if (initialVec != null && currVec != null) {


                    if (selectionActive) {
                        //render the selector here!
                        selector.render(initialVec, circle.getcX(), circle.getcY());
                        selector.updateSelectedBrush(circle.getcX(), circle.getcY(), degrees);
                        if (!(canvas.currBrush instanceof Eraser)) {
                            colorMode(HSB, 360, 100, 100);
                            canvas.currBrush.setColor(color(map(circle.getcY(), 0, height, 0, 360), 70, 100));
                            colorMode(RGB, 255, 255, 255);
                        }
                    }

                    pushMatrix();
                    translate(circle.cX, circle.cY);
                    strokeWeight(4);
                    rotate(degrees + initialVec.heading());
                    line(0, 0, 0, circle.getRenderRadius() / 2);
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


        void resetButtonPos() {
            buttonList.get(1).x = buttonList.get(0).x + buttRadius + 100;
            buttonList.get(1).y = buttonList.get(0).y + buttRadius + 100;
        }

        void positionTool(ArrayList<TouchEvent.Pointer> touches) {
            for (TouchEvent.Pointer currpointer : touches) {
                for (ToolButton currButton : buttonList) {
                    if (currButton.isOver(currpointer.x, currpointer.y) ) {
                        if (buttonList.get(0).distance(buttonList.get(0).getX(), buttonList.get(0).getY(), buttonList.get(1).getX(), buttonList.get(0).getY()) <= buttRadius) {
                            resetButtonPos();
                        }
                        else {
                            currButton.moveButton(currpointer.x, currpointer.y);
                        }

                    }

                }
            }
        }

        void setActiveButtons(boolean isTouchStarted) {
            for (ToolButton currButton: buttonList) {
                currButton.setIsActive(false);
                int finalIndex = fingsInTool().size();
                ArrayList<TouchEvent.Pointer> toolTouches = new ArrayList<>();

                for (int i: fingsInTool()) {
                    toolTouches.add(touches[i]);
                    }

                //if the call is coming from touchEnded, ignore the last element of touches because it no longer is on the screen :<
//                if (isTouchStarted == false) finalIndex--;

                for (int i = 0; i <finalIndex; i++) {
                    TouchEvent.Pointer currPointer = toolTouches.get(i);
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

            //if this is the first time reading, set the initial vector and old vector
            if (!b) {

                //perpendicular vector to the one between index and thumb
                initialVec = new PVector((indexButton.x - thumbButton.x), (indexButton.y - thumbButton.y));
                oldDegrees = initialVec.heading();
                selectionActive = false;

            }

            else {

                currVec = new PVector(indexButton.x - thumbButton.x, (indexButton.y- thumbButton.y));
                degrees =  (currVec.heading() - initialVec.heading());

                //here we use the difference in rotation between frames to determine if the user intends to open
                //the menue
                degreesPerFrame = abs(degrees(degrees - oldDegrees));


                if (!selectionActive) {
                    //determine if the menue should open if rotational velocity is high enough or if you turn enough
                    if ((degreesPerFrame > 3 && degreesPerFrame < 50) || (degrees(degrees) > 15 || degrees(degrees) < -15)) {

                        selectionActive = true;
                    }
                }
                oldDegrees = degrees;
            }

        }
    }


    class BrushSelector {
        private ToolCircle toolCircle;
        private float separationAngle;
        private float maxRenderRadius;
        float[][] brushRegions;
        int brushZone;

        BrushSelector(ToolCircle toolCircle) {
            this.toolCircle = toolCircle;
            separationAngle = radians(-40);
            maxRenderRadius = 0;
            brushRegions = new float[3][2];
            brushZone = 1;

        }

        void updateSelectedBrush(float x, float y, float degrees) {
            if (radians(20) > degrees && degrees > radians(-20)) {
               brushZone = 1;

            }

            else if (radians(20) < degrees) {
                brushZone = 2;
            }

            else if (radians(-20) > degrees) {
                brushZone = 0;
            }
            canvas.setCurrentBrush(brushZone);


        }

        void resetRenderRadius() {
            if (maxRenderRadius != 0) maxRenderRadius = 0;
        }
        void render(PVector initialVector, float x, float y) {

            maxRenderRadius = toolCircle.getRenderRadius() / 2;

            pushMatrix();
            translate(x, y);
            float currRotation = radians(180) + separationAngle * 3  + initialVector.heading();
            rotate(currRotation);
            strokeWeight(4);
            fill(0,255,0);
            if (brushZone == 2) {
                rotate(radians(90) + separationAngle);
                fill(canvas.currBrush.color);
                arc(0, 0, toolCircle.getRenderRadius(), toolCircle.getRenderRadius(), 0, -separationAngle, PIE);
                rotate(radians(-90) - separationAngle);
            }
            line(0, 0, 0, maxRenderRadius);


            rotate(separationAngle);
            if (brushZone == 1) {
                rotate(radians(90) + separationAngle);
                fill(canvas.currBrush.color);
                arc(0, 0, toolCircle.getRenderRadius(), toolCircle.getRenderRadius(), 0, -separationAngle, PIE);
                rotate(radians(-90) - separationAngle);
            }
            line(0,0, 0, maxRenderRadius);


            rotate(separationAngle);
            if (brushZone == 0) {

                rotate(radians(90) + separationAngle);
                fill(canvas.currBrush.color);
                arc(0, 0, toolCircle.getRenderRadius(), toolCircle.getRenderRadius(), 0, -separationAngle, PIE);
                rotate(radians(-90) - separationAngle);
            }
            line(0,0, 0, maxRenderRadius);
            rotate(separationAngle);
            stroke(0);
            line(0, 0, 0, maxRenderRadius);
            popMatrix();

        }
    }


    //@todo implement easy circle for two fingers c is the mdipoint and r is the r.
    class ToolCircle {
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
//            strokeWeight(strokeWeight);
            stroke(50, 50, 50);
            renderRadius = radius * 2 + tool.getButtRadius() + selectionBonus;
            ellipse(cX, cY, renderRadius, renderRadius);
            if (tool.selectionActive && selectionBonus <= 300) selectionBonus += 15;

        }


    }
}






