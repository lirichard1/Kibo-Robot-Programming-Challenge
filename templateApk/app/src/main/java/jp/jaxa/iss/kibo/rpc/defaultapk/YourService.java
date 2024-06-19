package jp.jaxa.iss.kibo.rpc.defaultapk;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import gov.nasa.arc.astrobee.Result;
import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.CvType;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import static java.lang.Thread.sleep;

import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class YourService extends KiboRpcService {
    final String
            TAG = "ROT",
            SIM = "SIMULATOR";
    int counter = 0;

    //ArucoTagDetector arTagDetector = new ArucoTagDetector();

    @Override
    protected void runPlan1(){

        api.startMission();
        Result result;
        final int LOOP_MAX = 5;
        final int img_MAX = 5;
        int loopCounter = 0;
        int imgRetries = 0;

        Point point = new Point(11.029d, -9.98828d, 5.2817d);
        Point point1 = new Point(11.343d, -9.2814d, 5.3594d);
        Point point2 = new Point(10.974d, -8.8799d, 4.6309d);
        //Point point3 = new Point(11.303d, -8.7276d, 4.5397d);
        Point point4 = new Point(10.924d, -7.9268d,4.5397d);
        Point point5 = new Point(10.563d, -7.4084d,4.5397d);
        Point point6 = new Point(10.557d, -6.8833d,4.8351d);

        Point astronaut = new Point(11.143d, -6.7607d, 4.9654d);

        double increment = 0.2;

        Quaternion quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
        Quaternion quaternion1 = new Quaternion(0f, 0.707f, 0f, 0.707f);
        Quaternion quaternion2= new Quaternion(0f,0f,1f,0f);
        Quaternion quaternion3 = new Quaternion(0f, 0f, 0.707f, 0.707f);

        Quaternion testIncrement = new Quaternion(0.383f, 0f, 0f, 0.924f);
        Quaternion incrementX = new Quaternion(0.0261f,0.01f,0.01f,0.9996f);
        Quaternion incrementY = new Quaternion(0f,0.0261f,0f,0.9996f);
        Quaternion incrementZ = new Quaternion(0f,0f,0.0261f,0.9996f);


        ArrayList<String> predictions = new ArrayList<String>();
        try {
            result = api.moveTo(point, quaternion, true);
            while (!result.hasSucceeded() && loopCounter < LOOP_MAX) {
                result = api.moveTo(point, quaternion, true);
                ++loopCounter;

            }
            api.flashlightControlFront(0.01f);
            Thread.sleep(2000);

            
            Bitmap image_bmp = api.getBitmapNavCam();
            Log.i(TAG, "Bitmap gotten success!");
            Mat image = api.getMatNavCam();
            Log.i(TAG, "Mat successful");
            api.flashlightControlFront(0.0f);
            if (image == null) {
                while (image == null && imgRetries < img_MAX) {
                    api.flashlightControlFront(0.5f);
                    Thread.sleep(2000);
                    image_bmp = api.getBitmapNavCam();
                    image = api.getMatNavCam();
                    api.flashlightControlFront(0.0f);
                    imgRetries++;
                }
            } else {
                api.saveMatImage(image, "point.png");
            }

            Log.i(TAG, "ABOUT TO DO PREDICTIONS");
            String pred = imageRecognition(image);
            Log.i(TAG, "FINISHED IMAGE RECOGNITION, START COUNT OBJECTS");
            int numObjects = countObjects(image);
            Log.i(TAG, "FINISHED COUNT OBJECTS");

            Log.i(TAG, "TESTING OPENCV MOBILE");
            OpenCVModel cvModel = new OpenCVModel(this);
            Log.i(TAG, "LOADED OPENCV MOBILE, STARTING INFERENCES");
            pred = cvModel.inference(api.getMatNavCam());
            Log.i(TAG, "prediction 0 " + pred);
            Log.i(TAG, "FINISHED INFERENCE");

            if (pred != null) {
                api.setAreaInfo(1, pred);
            }
            if (numObjects!=0) {
                api.setAreaInfo(1, pred, numObjects);
            }

            result = api.moveTo(point1, quaternion, true);
            while (!result.hasSucceeded() && loopCounter < LOOP_MAX) {
                result = api.moveTo(point1, quaternion, true);
                ++loopCounter;

            }

            result = api.moveTo(point2, quaternion1, true);
            while (!result.hasSucceeded() && loopCounter < LOOP_MAX) {
                result = api.moveTo(point2, quaternion1, true);
                ++loopCounter;

            }
            api.flashlightControlFront(0.5f);
            Thread.sleep(2000);
            Mat image1 = api.getMatNavCam();
            api.flashlightControlFront(0.0f);
            if (image1 == null) {
                while (image1 == null && imgRetries < img_MAX) {
                    api.flashlightControlFront(0.05f);
                    Thread.sleep(2000);
                    image1 = api.getMatNavCam();
                    api.flashlightControlFront(0.0f);
                    imgRetries++;
                }
            } else {
                api.saveMatImage(image1, "point_1.png");
            }


            String pred1 = imageRecognition(image1);
            int numObjects1 = countObjects(image1);

            Log.i(TAG, "TESTING OPENCV MOBILE");
            Log.i(TAG, "LOADED OPENCV MOBILE, STARTING INFERENCES");
            pred1 = cvModel.inference(api.getMatNavCam());
            Log.i(TAG, "prediction 1 " + pred1);
            Log.i(TAG, "FINISHED INFERENCE");

            if (pred1!=null) {
                api.setAreaInfo(2, pred1);
            }
            if (numObjects1!=0) {
                api.setAreaInfo(2, pred1, numObjects1);
            }

            result = api.moveTo(point4, quaternion1, true);
            while (!result.hasSucceeded() && loopCounter < LOOP_MAX) {
                result = api.moveTo(point4, quaternion1, true);
                ++loopCounter;

            }
            api.flashlightControlFront(0.5f);
            Thread.sleep(2000);
            Mat image2 = api.getMatNavCam();
            api.flashlightControlFront(0.0f);
            if (image2 == null) {
                while (image2 == null && imgRetries < img_MAX) {
                    api.flashlightControlFront(0.05f);
                    Thread.sleep(2000);
                    image2 = api.getMatNavCam();
                    api.flashlightControlFront(0.0f);
                    imgRetries++;
                }
            } else {

                api.saveMatImage(image2, "point_2.png");
            }

            //ArrayList corners2 = arTagDetector.detect(image2);

            //Log.i(TAG, "FINISHED ARUCO DETECT");
            //String joined2 = TextUtils.join(", ", corners2);
            //Log.i(TAG, joined2);

            String pred2 = imageRecognition(image2);
            int numObjects2 = countObjects(image2);

            Log.i(TAG, "TESTING OPENCV MOBILE");
            Log.i(TAG, "LOADED OPENCV MOBILE, STARTING INFERENCES");
            pred2 = cvModel.inference(api.getMatNavCam());
            Log.i(TAG, "prediction 2 " + pred2);
            Log.i(TAG, "FINISHED INFERENCE");

            if (pred2!=null) {
                api.setAreaInfo(3, pred2);
            }
            if (numObjects2!=0) {
                api.setAreaInfo(3, pred2, numObjects2);
            }
//            shiftXLeftRight(point4, quaternion1, increment);
//            shiftYInOut(point4, quaternion1, increment);
//            shiftZUpDown(point4, quaternion1, increment);
            //predictions.add(doInference(image2, model));


            result = api.moveTo(point5, quaternion1, true);
            while (!result.hasSucceeded() && loopCounter < LOOP_MAX) {
                result = api.moveTo(point5, quaternion1, true);
                ++loopCounter;

            }

            result = api.moveTo(point6, quaternion2, true);
            while (!result.hasSucceeded() && loopCounter < LOOP_MAX) {
                result = api.moveTo(point6, quaternion2, true);
                ++loopCounter;

            }
            api.flashlightControlFront(0.5f);
            Thread.sleep(2000);
            Mat image3 = api.getMatNavCam();
            api.flashlightControlFront(0.0f);
            if (image3 == null) {
                while (image3 == null && imgRetries < img_MAX) {
                    api.flashlightControlFront(0.05f);
                    Thread.sleep(2000);
                    image3 = api.getMatNavCam();
                    api.flashlightControlFront(0.0f);
                    imgRetries++;
                }
            } else {

                api.saveMatImage(image3, "point_3.png");
            }

            //ArrayList corners3 = arTagDetector.detect(image3);

            //Log.i(TAG, "FINISHED ARUCO DETECT");
            //String joined3 = TextUtils.join(", ", corners3);
            //Log.i(TAG, joined3);

            String pred3 = imageRecognition(image3);
            int numObjects3 = countObjects(image3);

            Log.i(TAG, "TESTING OPENCV MOBILE");
            Log.i(TAG, "LOADED OPENCV MOBILE, STARTING INFERENCES");
            pred1 = cvModel.inference(api.getMatNavCam());
            Log.i(TAG, "prediction 3 " + pred3);
            Log.i(TAG, "FINISHED INFERENCE");

            if (pred3!=null) {
                api.setAreaInfo(4, pred3);
            }
            if (numObjects3!=0) {
                api.setAreaInfo(4, pred3, numObjects3);
            }

            result = api.moveTo(astronaut, quaternion3, true);
            while (!result.hasSucceeded() && loopCounter < LOOP_MAX) {
                result = api.moveTo(astronaut, quaternion3, true);
                ++loopCounter;

            }

            api.reportRoundingCompletion();

            api.flashlightControlFront(0.5f);
            Thread.sleep(2000);
            Mat target_item = api.getMatNavCam();
            api.flashlightControlFront(0.0f);
            if (target_item == null) {
                while (target_item == null && imgRetries < img_MAX) {
                    api.flashlightControlFront(0.05f);
                    Thread.sleep(2000);
                    target_item = api.getMatNavCam();
                    api.flashlightControlFront(0.0f);
                    imgRetries++;
                }
            } else {
                api.saveMatImage(target_item, "target_item.png");
            }

        }
        catch (Exception e) {
            Log.d(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }



    @SuppressWarnings("UnusedReturnValue")
//    private void moveTo(Point point, Quaternion quaternion) throws InterruptedException {
//        final int LOOP_MAX = 10;
//
//        Log.i(TAG, "Moving to: " + point.getX() + ", " + point.getY() + ", " + point.getZ());
//        long start = System.currentTimeMillis();
//
//        Result result = api.moveTo(point, quaternion, true);
//
//        api.flashlightControlFront(0.01f);
//        Thread.sleep(5000);
//        Mat image = api.getMatNavCam();
//        api.flashlightControlFront(0.0f);
//        api.saveMatImage(image, "ImageData_"+counter+".png");
//        counter++;
//        long end = System.currentTimeMillis();
//        long elapsedTime = end - start;
//        Log.i(TAG, "[0] moveTo finished in : " + elapsedTime/1000 + " seconds");
//        Log.i(TAG, "[0] hasSucceeded : " + result.hasSucceeded());
//
//        int loopCounter = 1;
//        while (!result.hasSucceeded() && loopCounter <= LOOP_MAX) {
//
//            Log.i(TAG, "[" + loopCounter + "] " + "Calling moveTo function");
//            start = System.currentTimeMillis();
//
//            result = api.moveTo(point, quaternion, true);
//
//            end = System.currentTimeMillis();
//            elapsedTime = end - start;
//            Log.i(TAG, "[" + loopCounter + "] " + "moveTo finished in : " + elapsedTime / 1000 +
//                    " seconds");
//            Log.i(TAG, "[" + loopCounter + "] " + "hasSucceeded : " + result.hasSucceeded());
//
//            loopCounter++;
//        }
//    }

//    private void shiftXLeftRight(Point point, Quaternion quaternion, double increment) throws InterruptedException {
//        moveTo(new Point(point.getX()+increment, point.getY(),point.getZ()),quaternion);
//        api.moveTo(point, quaternion, true);
//        moveTo(new Point(point.getX()-increment, point.getY(),point.getZ()),quaternion);
//        api.moveTo(point, quaternion, true);
//    }
//
//    public void shiftYInOut(Point point, Quaternion quaternion, double increment) throws InterruptedException {
//        moveTo(new Point(point.getX(), point.getY()+increment,point.getZ()),quaternion);
//        api.moveTo(point, quaternion, true);
//        moveTo(new Point(point.getX(), point.getY()-increment,point.getZ()),quaternion);
//        api.moveTo(point, quaternion, true);
//
//    }
//
//    public void shiftZUpDown(Point point, Quaternion quaternion, double increment) throws InterruptedException {
//        moveTo(new Point(point.getX(), point.getY(),point.getZ()+increment),quaternion);
//        api.moveTo(point, quaternion, true);
//        moveTo(new Point(point.getX(), point.getY(),point.getZ()-increment),quaternion);
//        api.moveTo(point, quaternion, true);
//    }

    private String imageRecognition(Mat image) {
        // Load images
        Log.i(TAG, "STARTING IMAGE RECOGNITION");
        Bitmap templateBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.template);
        Mat templateImage = new Mat();
        Utils.bitmapToMat(templateBitmap, templateImage);

        Mat mainImage = image;

        if (templateImage.empty() || mainImage.empty()) {
            Log.e(TAG, "Cannot load images!");
            return null;
        }
        Log.i(TAG, "IMAGE LOADING SUCCESSFUL");
        // Crop the sub-images
        List<Mat> templates = new ArrayList<>();
        List<String> templatesName = new ArrayList<>();

        templates.add(templateImage.submat(0, 150, 50, 220));
        templates.add(templateImage.submat(0, 150, 330, 500));
        templates.add(templateImage.submat(0, 150, 580, 750));
        templates.add(templateImage.submat(210, 360, 50, 220));
        templates.add(templateImage.submat(210, 360, 330, 500));
        templates.add(templateImage.submat(210, 360, 580, 750));
        templates.add(templateImage.submat(420, 570, 50, 220));
        templates.add(templateImage.submat(420, 570, 330, 500));
        templates.add(templateImage.submat(420, 570, 580, 750));
        templates.add(templateImage.submat(630, 780, 50, 220));

        Log.i(TAG, "TEMPLATE CROPPING SUCCESSFUL");

        templatesName.add("kapton");
        templatesName.add("top");
        templatesName.add("screw");
        templatesName.add("beaker");
        templatesName.add("hammer");
        templatesName.add("pipette");
        templatesName.add("wrench");
        templatesName.add("thermometer");
        templatesName.add("watch");
        templatesName.add("goggle");

        int binaryThresh = 100;

        // Convert the image to grayscale (if not already done)
//        Mat grayImage = new Mat();
//        Imgproc.cvtColor(mainImage, grayImage, Imgproc.COLOR_BGR2GRAY);

        // Apply binary threshold
        Mat mainThresh = new Mat();
        Imgproc.threshold(mainImage, mainThresh, binaryThresh, 255, Imgproc.THRESH_BINARY);

        // Detect edges using Canny
//        Mat mainEdges = new Mat();
//        Imgproc.Canny(mainThresh, mainEdges, 100, 200);
//
//        // Find contours
//        List<MatOfPoint> contoursMain = new ArrayList<>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(mainEdges, contoursMain, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        // Filter contours based on minimum area
//        double minAreaThreshold = 20.0;
//        List<MatOfPoint> filteredContoursMain = new ArrayList<>();
//        for (MatOfPoint contour : contoursMain) {
//            if (Imgproc.contourArea(contour) >= minAreaThreshold) {
//                filteredContoursMain.add(contour);
//            }
//        }
//
//        // Get the number of objects (contours) found, subtracting 1
//        int numObjects = filteredContoursMain.size() - 1;
//
//        // Print the result (for debugging purposes)
//        Log.i(TAG, "Number of objects: " + numObjects);

        ORB orb = ORB.create();
        //Mat mainThresh = new Mat();
        //Imgproc.threshold(mainImage, mainThresh, 100, 255, Imgproc.THRESH_BINARY);

        Log.i(TAG, "THRESHOLDING SUCCESSFUL");

        BFMatcher bf = BFMatcher.create(Core.NORM_HAMMING, true);
        Log.i(TAG, "BFMATCHER SUCCESSFUL");
        String bestTemplatePath = null;
        int bestNumGoodMatches = 0;
        int goodMatchThreshold = 35;
        int index = 0;

        for (int i = 0; i < templates.size(); i++) {
            Mat template = templates.get(i);
            Mat templateThresh = new Mat();
            Imgproc.threshold(template, templateThresh, 100, 255, Imgproc.THRESH_BINARY);

            MatOfKeyPoint kp1 = new MatOfKeyPoint();
            MatOfKeyPoint kp2 = new MatOfKeyPoint();
            Mat des1 = new Mat();
            Mat des2 = new Mat();

            orb.detectAndCompute(templateThresh, new Mat(), kp1, des1);
            orb.detectAndCompute(mainThresh, new Mat(), kp2, des2);

            MatOfDMatch matches = new MatOfDMatch();
            bf.match(des1, des2, matches);

            List<DMatch> matchesList = matches.toList();
            Collections.sort(matchesList, new Comparator<DMatch>() {
                @Override
                public int compare(DMatch o1, DMatch o2) {
                    return Double.compare(o1.distance, o2.distance);
                }
            });

            int numGoodMatches = 0;
            for (DMatch match : matchesList) {
                if (match.distance < goodMatchThreshold) {
                    numGoodMatches++;
                }
            }

            if (numGoodMatches > bestNumGoodMatches) {
                bestNumGoodMatches = numGoodMatches;
                bestTemplatePath = templatesName.get(i);
                index = i;
            }

            Mat resultImage = new Mat();
            Features2d.drawMatches(template, kp1, mainImage, kp2, matches, resultImage);

            Log.d(TAG, "Template: " + templatesName.get(i));
            Log.d(TAG, "Number of matches: " + numGoodMatches);
            // Display the images using OpenCV's HighGui (for demonstration purposes only)
            // HighGui.imshow("Main Image", mainImage);
            // HighGui.imshow("Match Image", resultImage);
            // HighGui.waitKey(0);
        }

        if (bestTemplatePath != null) {
            Log.i(TAG, "Best matching template: " + bestTemplatePath);
            Log.i(TAG, "Number of good matches: " + bestNumGoodMatches);
        } else {
            Log.i(TAG, "No good matches found.");
        }
        return bestTemplatePath;
    }

    public int countObjects(Mat image) {
        int binaryThresh = 100;
        int numObjects = 0;
        // Convert the image to grayscale (if not already done)
        //Mat grayImage = new Mat();
        //Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        //Log.i(TAG, "CONVERTED TO GRAYSCALE");
        //GRAYSCALE CAUSING ISSUES

        // Apply binary threshold
        Mat mainThresh = new Mat();
        Imgproc.threshold(image, mainThresh, binaryThresh, 255, Imgproc.THRESH_BINARY);

        Log.i(TAG, "APPLIED BINARY THRESHOLDING");

        // Detect edges using Canny
        Mat mainEdges = new Mat();
        Imgproc.Canny(mainThresh, mainEdges, 100, 200);

        Log.i(TAG, "DETECTED EDGES USING CANNY");

        // Find contours
        List<MatOfPoint> contoursMain = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mainEdges, contoursMain, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Log.i(TAG, "FOUND CONTOURS");

        // Filter contours based on minimum area
        double minAreaThreshold = 20.0;
        List<MatOfPoint> filteredContoursMain = new ArrayList<>();
        for (MatOfPoint contour : contoursMain) {
            if (Imgproc.contourArea(contour) >= minAreaThreshold) {
                filteredContoursMain.add(contour);
            }
        }

        Log.i(TAG, "FILTERED CONTOURS");

        // Get the number of objects (contours) found, subtracting 1
        numObjects = filteredContoursMain.size() - 1;

        // Print the result (for debugging purposes)
        Log.i(TAG, "Number of objects: " + numObjects);

        return numObjects;
    }

    public Mat decimateImage(Mat image, double scaleFactor) {
        int originalWidth = image.width();
        int originalHeight = image.height();

        int newWidth = (int) (originalWidth * scaleFactor);
        int newHeight = (int) (originalHeight * scaleFactor);

        Mat decimatedImage = new Mat();
        Imgproc.resize(image, decimatedImage, new Size(newWidth, newHeight), 0, 0, Imgproc.INTER_AREA);

        return decimatedImage;
    }

//    public List<MatOfPoint> mergeContours(List<MatOfPoint> contours, double proximityThreshold) {
//        // Function to calculate the distance between two bounding rectangles
//        private double rectDistance(Rect rect1, Rect rect2) {
//            double dx = Math.max(0, Math.max(rect1.x - (rect2.x + rect2.width), rect2.x - (rect1.x + rect1.width)));
//            double dy = Math.max(0, Math.max(rect1.y - (rect2.y + rect2.height), rect2.y - (rect1.y + rect1.height)));
//            return Math.sqrt(dx * dx + dy * dy);
//        }
//
//        // Calculate bounding rectangles for all contours
//        List<Rect> boundingRects = new ArrayList<>();
//        for (MatOfPoint contour : contours) {
//            boundingRects.add(Imgproc.boundingRect(contour));
//        }
//
//        boolean merged = true;
//        while (merged) {
//            merged = false;
//            List<MatOfPoint> newContours = new ArrayList<>();
//            boolean[] used = new boolean[contours.size()];
//
//            for (int i = 0; i < contours.size(); i++) {
//                if (used[i]) continue;
//                Rect currentRect = boundingRects.get(i);
//                MatOfPoint currentContour = contours.get(i);
//
//                for (int j = i + 1; j < contours.size(); j++) {
//                    if (used[j]) continue;
//                    if (rectDistance(currentRect, boundingRects.get(j)) < proximityThreshold) {
//                        currentRect = Imgproc.boundingRect(new MatOfPoint2f(currentContour.toArray(), contours.get(j).toArray()));
//                        currentContour.push_back(new MatOfPoint(contours.get(j)));
//                        used[j] = true;
//                        merged = true;
//                    }
//                }
//                newContours.add(currentContour);
//                used[i] = true;
//            }
//
//            contours = newContours;
//            boundingRects.clear();
//            for (MatOfPoint contour : contours) {
//                boundingRects.add(Imgproc.boundingRect(contour));
//            }
//        }
//
//        return contours;
//    }

    public List<DMatch> matchTemplate(Mat mainImage, Mat templateImage, ORB orb, BFMatcher bf) {
        MatOfKeyPoint kp1 = new MatOfKeyPoint(), kp2 = new MatOfKeyPoint();
        Mat des1 = new Mat(), des2 = new Mat();

        orb.detectAndCompute(templateImage, new Mat(), kp1, des1);
        orb.detectAndCompute(mainImage, new Mat(), kp2, des2);

        MatOfDMatch matches = new MatOfDMatch();
        bf.match(des1, des2, matches);

        List<DMatch> matchesList = matches.toList();
        Collections.sort(matchesList, new Comparator<DMatch>() {
            @Override
            public int compare(DMatch o1, DMatch o2) {
                return Double.compare(o1.distance, o2.distance);
            }
        });

        return matchesList;
    }

//    public Mat detectAndWarpAruco(Mat image) {
//        Mat gray = new Mat();
//        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
//
//        Dictionary arucoDict = Aruco.getPredefinedDictionary(Aruco.DICT_5X5_250);
//        ArucoDetector detector = new ArucoDetector(arucoDict, new DetectorParameters());
//        List<Mat> corners = new ArrayList<>();
//        Mat ids = new Mat();
//
//        detector.detectMarkers(gray, corners, ids);
//
//        if (corners.size() > 0) {
//            Mat markerCorners = corners.get(0);
//            double arucoCx = Core.mean(markerCorners.col(0)).val[0];
//            double arucoCy = Core.mean(markerCorners.col(1)).val[0];
//
//            // Find the contours of the image
//            Mat edges = new Mat();
//            Imgproc.Canny(gray, edges, 50, 150);
//
//            List<MatOfPoint> contours = new ArrayList<>();
//            Imgproc.findContours(edges, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//
//            contours.sort((c1, c2) -> Double.compare(Imgproc.contourArea(c2), Imgproc.contourArea(c1)));
//
//            for (MatOfPoint contour : contours) {
//                MatOfPoint2f approx = new MatOfPoint2f();
//                Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), approx, 0.02 * Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true), true);
//
//                if (approx.total() == 4) {
//                    List<Point> points = new ArrayList<>(Arrays.asList(approx.toArray()));
//                    Collections.sort(points, (p1, p2) -> Double.compare(p1.x + p1.y, p2.x + p2.y));
//
//                    Point topLeft = points.get(0);
//                    Point topRight = points.get(1);
//                    Point bottomRight = points.get(2);
//                    Point bottomLeft = points.get(3);
//
//                    double width = Math.max(topRight.x - topLeft.x, bottomRight.x - bottomLeft.x);
//                    double height = Math.max(bottomLeft.y - topLeft.y, bottomRight.y - topRight.y);
//
//                    Mat srcPts = new MatOfPoint2f(topLeft, topRight, bottomRight, bottomLeft);
//                    Mat dstPts = new MatOfPoint2f(new Point(0, 0), new Point(width, 0), new Point(width, height), new Point(0, height));
//
//                    Mat M = Imgproc.getPerspectiveTransform(srcPts, dstPts);
//                    Mat warped = new Mat();
//                    Imgproc.warpPerspective(image, warped, M, new Size(width, height));
//
//                    if (width < height) {
//                        Core.rotate(warped, warped, Core.ROTATE_90_CLOCKWISE);
//                    }
//
//                    return warped;
//                }
//            }
//        }
//        return image;
//    }
//    private MappedByteBuffer loadModelFile() throws IOException {
//        AssetFileDescriptor fileDescriptor=this.getAssets().openFd("resnet.tflite");
//        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
//        FileChannel fileChannel=inputStream.getChannel();
//        long startOffset=fileDescriptor.getStartOffset();
//        long declareLength=fileDescriptor.getDeclaredLength();
//        Log.i(TAG, "LOADED MODEL FILE");
//        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declareLength);
//    }



//    private ImageClassifier loadModelFile() {
//        ImageClassifier imageClassifier;
//        String modelName = "resnet.tflite";
//        File modelFile = new File("app/src/main/assets/resnet.tflite");
//        try {
//            imageClassifier =
//                    ImageClassifier.createFromFile(modelFile);
//            return imageClassifier;
//        } catch (IOException e) {
//            Log.e(TAG, "TFLite failed to load model with error: "
//                    + e.getMessage());
//            return null;
//        }
//    }

//    private String doInference(Bitmap image, ImageClassifier tflite) {
//        if (tflite == null) {
//            loadModelFile();
//        }
//        long inferenceTime = SystemClock.uptimeMillis();
//
//
//
//        Log.i(TAG, "DO INFERENCE");
//        String[] classes = {"beaker","google","hammer", "kapton-tape", "pipette", "screwdriver", "thermometer", "top", "watch", "wrench"};
//        int index = 0;
//        float[] output=new float[10];
//        float max = output[0];
//
//
//        //image.convertTo(image, CvType.CV_32F);
//       // Imgproc.resize(image, image, size);
//        Size size = new Size(224, 224);
//        //Bitmap bitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
//        //int[] shape = {1, image.rows(), image.cols(), image.channels()};
//        //TensorBuffer tensorBuffer = TensorBuffer.createFixedSize(shape, DataType.FLOAT32);
//        TensorImage tensorImage;
//        //tensorImage.load(bitmap);
//
//        ImageProcessor imageProcessor =
//                new ImageProcessor.Builder()
//                        .add(new ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
//                        .build();
//
//
//        tensorImage = imageProcessor.process(TensorImage.fromBitmap(image));
//        //int buff[] = new int[(int) (image.total() * image.channels())];
//        //image.get(0, 0, buff);
//        //tensorImage.load(buff);
//
//        //TensorBuffer inputBuffer = TensorBuffer.createDynamic(DataType.FLOAT32);
//        //float[] inputData = inputBuffer.getFloatArray();
//        //image.get(0, 0, inputData);
//        Log.i(TAG, String.valueOf(tensorImage.getHeight()));
//        Log.i(TAG, String.valueOf(tensorImage.getWidth()));
//        Log.i(TAG, String.valueOf(tensorImage.getDataType()));
//        // Convert the Mat image to Bitmap
//        //Utils.matToBitmap(image, bitmap);
//        //TensorImage tensorImage = new TensorImage();
//        //tensorImage.load(bitmap);
//        tflite.classify(tensorImage);
//        Log.i(TAG, "DONE INFERENCE");
//        List<Classifications> result = tflite.classify(tensorImage);
//
//        inferenceTime = SystemClock.uptimeMillis() - inferenceTime;
//
//        Log.i(TAG, "INFERENCE TIME: "+inferenceTime);
////        tflite.run(tensorImage,output);
//        Log.i(TAG, "RUN INFERENCES WITH MODEL");
//
//        for (int i = 1; i<output.length; i++) {
//            if (output[i]>max) {
//                max = output[i];
//                index = i;
//            }
//        }
//
//        //Log.d(TAG, output[0]);
//        return classes[index];
//    }
    
}







