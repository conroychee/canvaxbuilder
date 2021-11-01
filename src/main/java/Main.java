
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


/*

Command 		Description
C w h           Should create a new canvas of width w and height h.
L x1 y1 x2 y2   Should create a new line from (x1,y1) to (x2,y2). Currently only
                horizontal or vertical lines are supported. Horizontal and vertical lines
                will be drawn using the 'x' character.
R x1 y1 x2 y2   Should create a new rectangle, whose upper left corner is (x1,y1) and
                lower right corner is (x2,y2). Horizontal and vertical lines will be drawn
                using the 'x' character.
B x y c         Should fill the entire area connected to (x,y) with "colour" c. The
                behavior of this is the same as that of the "bucket fill" tool in paint
                programs.
Q               Should quit the program.


Assumption:

It is assumed that paint (color) cannot be placed on the 'x' which is the line representative.
It is assumed that the line can be placed on the painted area.
It is assumed that the line can be placed overlap to other line.

 */

public class Main {
    TreeMap<Integer, ArrayList<String>> pointMap = new TreeMap<>();
    int maxCanvasX = 0;
    int maxCanvasY = 0;

    public static void main(String args[]) throws Exception {

        boolean isContinue = true;
        Main main = new Main();
        do {
            System.out.print("Enter command: ");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String input = in.readLine();
            isContinue = main.inputCheck(input);
            main.printCanvas();
        } while (isContinue);
    }


    /**
     * Validate the input and call the responding function e.g. createLine, createRectangle or filling (painting color)
     *
     * @param input
     */
    boolean inputCheck(String input) {
        ArrayList<String> inputList = new ArrayList<String>();
        Collections.addAll(inputList, input.trim().split("\\s+"));
        String action = inputList.get(0);
        if (action.equals("C")) {
            if (inputList.size() != 3) {
                System.err.println("Error: To create a canvas you must enter C followed by 2 digits");
                return true;
            }
            isNumeric(1, 2, inputList);
            maxCanvasX = Integer.parseInt(inputList.get(1));
            maxCanvasY = Integer.parseInt(inputList.get(2));
            if (maxCanvasX <= 0) {
                System.err.println("Error: Canvas width cannot be less than 0");
                return true;
            } else if (maxCanvasY <= 0) {
                System.err.println("Error: Canvas height cannot be less than 0");
                return true;
            }
            pointMap.clear();
            createCanvas();
        } else if (action.equals("L")) { // it can be assumed someone draw a dot, so not checking equality of begin and end coordinate
            if (inputList.size() != 5) {
                System.err.println("Error: To draw a line you must enter L followed by 4 digits");
                return true;
            }
            if (!isCanvasExist()) {
                return true;
            }
            if (!isNumeric(1, 4, inputList)) {
                return true;
            }
            int beginX = Integer.parseInt(inputList.get(1));
            int beginY = Integer.parseInt(inputList.get(2));
            int endX = Integer.parseInt(inputList.get(3));
            int endY = Integer.parseInt(inputList.get(4));
            if (beginX != endX && beginY != endY) {
                System.err.println("Error: Slope line is not supported");
                return true;
            }
            if (!isWithinBoundary(beginX, beginY)) {
                return true;
            }
            if (!isWithinBoundary(endX, endY)) {
                return true;
            }
            createLine(beginX, beginY, endX, endY);
        } else if (action.equals("R")) {
            if (inputList.size() != 5) {
                System.err.println("Error: To draw a line you must enter R followed by 4 digits");
                return true;
            }
            if (!isCanvasExist()) {
                return true;
            }
            if (!isNumeric(1, 4, inputList)) {
                return true;
            }
            int beginX = Integer.parseInt(inputList.get(1));
            int beginY = Integer.parseInt(inputList.get(2));
            int endX = Integer.parseInt(inputList.get(3));
            int endY = Integer.parseInt(inputList.get(4));
            if (!isWithinBoundary(beginX, beginY)) {
                return true;
            }
            if (!isWithinBoundary(endX, endY)) {
                return true;
            }
            String errorMsg = "";
            if (beginX == endX) {
                errorMsg = "Error: To draw a rectangle a point X must not equal to another point X.\n";
            }
            if (beginY == endY) {
                errorMsg += "Error: To draw a rectangle a point Y must not equal to another point Y.\n";
            }
            if (!errorMsg.isEmpty()) {
                System.err.println(errorMsg);
                return true;
            }
            createRectangle(beginX, beginY, endX, endY);
        } else if (action.equals("B")) { // it is assumed that the line has higher priority than color, the line can be drawn on top of color
            if (inputList.size() != 4) {
                System.err.println("Error: To paint an area you must enter B followed by 2 digits, and finally the color you want to paint.");
                return true;
            }
            if (!isCanvasExist()) {
                return true;
            }
            if (!isNumeric(1, 2, inputList)) {
                return true;
            }
            int pointX = Integer.parseInt(inputList.get(1));
            int pointY = Integer.parseInt(inputList.get(2));
            String color = inputList.get(3);
            if (!color.matches("^[a-zA-Z]$")) {
                System.err.println("Error: You must only enter single alphabet character except X for filling.");
                return true;
            }
            if (color.matches("^[x]$")) {
                System.err.println("Error: Do not use x for for filling. They represent line.");
                return true;
            }
            if (pointMap.get(pointY).get(pointX).equals("x") || pointMap.get(pointY).get(pointX).equals("X")) {
                System.err.println("Error: You painted on line. Enter another filling point.");
                return true;
            }
            filling(pointX, pointY, color);

        } else if (action.equals("Q") && inputList.size() == 1) {
            return false;
        } else {
            System.err.println("You have enter incorrect input");
            return true;
        }
        return true;
    }

    boolean isNumeric(int beginInputIndex, int endInputIndex, ArrayList<String> inputList) {
        LinkedList<String> numErrorList = new LinkedList<>();
        for (int i = beginInputIndex; i <= endInputIndex; i++) {
            if (!StringUtils.isNumeric(inputList.get(i))) {
                numErrorList.add(inputList.get(i));
            }
        }
        if (numErrorList.size() > 0) {
            System.err.println("Error: There is non-digit entered: " + numErrorList);
            return false;
        }
        return true;
    }

    boolean isWithinBoundary(int x, int y) {
        String errorMsg = "";

        if (x > maxCanvasX) {
            errorMsg = "Error: The x value is higher than canvax max width\n";
        }
        if (y > maxCanvasY) {
            errorMsg += "Error: The y value is higher than canvax max height";
        }
        if (!errorMsg.isEmpty()) {
            System.err.println(errorMsg);
            return false;
        }
        return true;
    }

    /**
     * This is to check whether the canvas does exist. If it does not exist, it
     */
    boolean isCanvasExist() {
        if (maxCanvasY == 0 || maxCanvasX == 0) {
            System.err.println("Error: Canvas does not exist. You need to create a canvas first");
            return false;
        }
        return true;
    }

    /**
     * Create canvas
     */
    void createCanvas() {
        int width = maxCanvasX + 1;
        int height = maxCanvasY + 1;

        for (int y = 0; y <= height; y++) {
            pointMap.put(y, new ArrayList<>());
            for (int x = 0; x <= width; x++) {
                if (y == 0 || y == height) {
                    pointMap.get(y).add("-");
                } else if (x == 0 || x == width) {
                    pointMap.get(y).add("|");
                } else {
                    pointMap.get(y).add(" ");
                }
                if (x == width) {
                    pointMap.get(y).add("\n");
                }
            }
        }
    }

    /**
     * Create line
     *
     * @param beginX a point x
     * @param beginY a point y
     * @param endX   another point x
     * @param endY   another point y
     */
    void createLine(int beginX, int beginY, int endX, int endY) {
        if (beginX == endX) { // vertical line, bidirectional drawing
            int startY = Math.min(beginY, endY);
            int stopY = Math.max(endY, beginY);
            for (int i = startY; i <= stopY; i++) {
                pointMap.get(i).set(beginX, "x");
            }
        }
        if (beginY == endY) { // horizontal line, bidirectional drawing
            int startX = Math.min(beginX, endX);
            int stopX = Math.max(beginX, endX);
            for (int i = startX; i <= stopX; i++) {
                pointMap.get(beginY).set(i, "x");
            }
        }
    }

    /**
     * Recursive check and fill the point
     *
     * @param x
     * @param y
     * @param color
     */
    void filling(int x, int y, String color) {
        if (x != 0 && x < pointMap.get(0).size() - 2 && y != 0 && y < pointMap.size() - 1 && !pointMap.get(y).get(x).equals("x") && !pointMap.get(y).get(x).equals(color)) {
            pointMap.get(y).set(x, color);
            filling(x - 1, y, color);
            filling(x + 1, y, color);
            filling(x, y + 1, color);
            filling(x, y - 1, color);
        }
    }

    /**
     * Call createLine function to draw rectangle
     *
     * @param beginX a point x
     * @param beginY a point y
     * @param endX   another point x
     * @param endY   another point y
     */
    void createRectangle(int beginX, int beginY, int endX, int endY) {
        createLine(beginX, beginY, endX, beginY);
        createLine(beginX, endY, endX, endY);
        createLine(beginX, beginY, beginX, endY);
        createLine(endX, beginY, endX, endY);
    }


    /**
     * Print canvas
     */
    void printCanvas() {
        AtomicReference<String> canvas = new AtomicReference<>("");

        pointMap.keySet().forEach(k -> {
            for (int x = 0; x < pointMap.get(k).size(); x++) {
                canvas.set(canvas + pointMap.get(k).get(x));
            }
        });

        System.out.println(canvas);
    }


}