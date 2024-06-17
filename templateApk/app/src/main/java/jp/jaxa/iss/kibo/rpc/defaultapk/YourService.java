package jp.jaxa.iss.kibo.rpc.defaultapk;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import java.util.ArrayList;

import gov.nasa.arc.astrobee.Result;
import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

import org.opencv.android.Utils;
import org.opencv.aruco.Aruco;
import org.opencv.aruco.Dictionary;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import static java.lang.Thread.sleep;

import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;



/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class YourService extends KiboRpcService {
    final String
            TAG = "ROT",
            SIM = "SIMULATOR";
    int counter = 0;

    ArucoTagDetector arTagDetector = new ArucoTagDetector();

    //String[] classNames = {"beaker", "goggle", "hammer", "kapton-tape", "pipette", "screwdriver", "thermometer", "top", "watch", "wrench"};
    int numObjects = 0;
    @Override
    protected void runPlan1(){

        api.startMission();
        Result result;
        final int LOOP_MAX = 5;
        final int img_MAX = 5;
        int loopCounter = 0;
        int imgRetries = 0;

        //ModelInterpreter modelInterpreter = new ModelInterpreter(this);

        Point point = new Point(11.029d, -9.98828d, 5.2817d);
        Point point1 = new Point(11.343d, -9.2814d, 5.3594d);
        Point point2 = new Point(10.974d, -8.8799d, 4.6309d);
        //Point point3 = new Point(11.303d, -8.7276d, 4.5397d);
        Point point4 = new Point(10.924d, -7.9268d,4.5397d);
        Point point5 = new Point(10.563d, -7.4084d,4.5397d);
        Point point6 = new Point(10.557d, -6.8833d,4.8351d);

        Point astronaut = new Point(11.143d, -6.7607d, 4.9654d);



        double increment = 0.2;

//        Point incrementXRight;
//        Point incrementXLeft;
//        Point incrementYUp;
//        Point incrementYDown;
//        Point incrementZRight;
//        Point incrementZLeft;

        Quaternion quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
        Quaternion quaternion1 = new Quaternion(0f, 0.707f, 0f, 0.707f);
        Quaternion quaternion2= new Quaternion(0f,0f,1f,0f);
        Quaternion quaternion3 = new Quaternion(0f, 0f, 0.707f, 0.707f);

        Quaternion testIncrement = new Quaternion(0.383f, 0f, 0f, 0.924f);
        Quaternion incrementX = new Quaternion(0.0261f,0.01f,0.01f,0.9996f);
        Quaternion incrementY = new Quaternion(0f,0.0261f,0f,0.9996f);
        Quaternion incrementZ = new Quaternion(0f,0f,0.0261f,0.9996f);


        ArrayList<Integer> templatePaths = new ArrayList<Integer>();
        templatePaths.add(R.drawable.beaker_template);
        templatePaths.add(R.drawable.goggle_template);
        templatePaths.add(R.drawable.hammer_template);
        templatePaths.add(R.drawable.kapton_tape_template);
        templatePaths.add(R.drawable.pipette_template);
        templatePaths.add(R.drawable.screwdriver_template);
        templatePaths.add(R.drawable.thermometer_template);
        templatePaths.add(R.drawable.top_template);
        templatePaths.add(R.drawable.watch_template);
        templatePaths.add(R.drawable.wrench_template);


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

//              api.saveBitmapImage(image_bmp, "point.png");
                api.saveMatImage(image, "point.png");
            }

//            ArrayList corners = arTagDetector.detect(image);
//
//            Log.i(TAG, "FINISHED ARUCO DETECT");
//            String joined = TextUtils.join(", ", corners);
//            Log.i(TAG, joined);

            Log.i(TAG, "ABOUT TO DO PREDICTIONS");
            String pred = processImage(image, templatePaths);
            Log.i(TAG, "FINISHED IMAGE RECOGNITION, START COUNT OBJECTS");
            Log.i(TAG, "FINISHED COUNT OBJECTS");

            if (pred != null) {
                api.setAreaInfo(1, pred);
                predictions.add(pred);
            }
            if (numObjects!=0) {
                api.setAreaInfo(1, pred, numObjects);
            }

            Log.i(TAG, "TESTING OPENCV MOBILE");
//            OpenCVModel cvModel = new OpenCVModel(this);
            Log.i(TAG, "LOADED OPENCV MOBILE, STARTING INFERENCES");
//            Log.i(TAG, "prediction " + cvModel.inference(api.getMatNavCam()));
            Log.i(TAG, "FINISHED INFERENCE");

//            shiftXLeftRight(point, quaternion, increment);
//            shiftYInOut(point, quaternion, increment);
//            shiftZUpDown(point, quaternion, increment);



//            incrementXRight = new Point(point.getX()+increment, point.getY(), point.getZ());
//            incrementXLeft = new Point(point.getX()-increment, point.getY(), point.getZ());
//            moveTo(incrementXRight, quaternion);
//            api.moveTo(point, quaternion, true);
//            moveTo(incrementXLeft, quaternion);
//            api.moveTo(
//            moveTo(point, incrementY);
//            api.moveTo(point, quaternion, true);
//            moveTo(point, incrementZ);
//            api.moveTo(point, quaternion, true);


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

            ArrayList corners1 = arTagDetector.detect(image1);

            Log.i(TAG, "FINISHED ARUCO DETECT");
            String joined1 = TextUtils.join(", ", corners1);
            Log.i(TAG, joined1);

            String pred1 = processImage(image1, templatePaths);
            //int numObjects1 = countObjects(image1);
            if (pred1!=null) {
                api.setAreaInfo(2, pred1);
                predictions.add(pred1);
            }
            if (numObjects!=0) {
                api.setAreaInfo(2, pred1, numObjects);
            }


            //predictions.add(doInference(image1, model));

//            shiftXLeftRight(point2, quaternion1, increment);
//            shiftYInOut(point2, quaternion1, increment);
//            shiftZUpDown(point2, quaternion1, increment);
//
//            result = api.moveTo(point3, quaternion1, true);
//            while (!result.hasSucceeded() && loopCounter < LOOP_MAX) {
//                result = api.moveTo(point3, quaternion, true);
//                ++loopCounter;
//
//            }

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

            ArrayList corners2 = arTagDetector.detect(image2);

            Log.i(TAG, "FINISHED ARUCO DETECT");
            String joined2 = TextUtils.join(", ", corners2);
            Log.i(TAG, joined2);

            String pred2 = processImage(image2, templatePaths);
            //int numObjects2 = countObjects(image2);
            if (pred2!=null) {
                api.setAreaInfo(3, pred2);
                predictions.add(pred2);
            }
            if (numObjects!=0) {
                api.setAreaInfo(3, pred2, numObjects);
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

            ArrayList corners3 = arTagDetector.detect(image3);

            Log.i(TAG, "FINISHED ARUCO DETECT");
            String joined3 = TextUtils.join(", ", corners3);
            Log.i(TAG, joined3);

            String pred3 = processImage(image3, templatePaths);
            //int numObjects3 = countObjects(image3);
            if (pred3!=null) {
                api.setAreaInfo(4, pred3);
                predictions.add(pred3);
            }
            if (numObjects!=0) {
                api.setAreaInfo(4, pred3, numObjects);
            }

//            shiftXLeftRight(point6, quaternion2, increment);
//            shiftYInOut(point6, quaternion2, increment);
//            shiftZUpDown(point6, quaternion2, increment);
            //predictions.add(doInference(image3, model));

//            String[] args = {"astrobee_seperated.png","template.jpg"};
//            new MatchTemplate().run(args);
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

            String targetClass = processImage(target_item, templatePaths);
            api.notifyRecognitionItem();

            for (int i = 0;i<predictions.size();i++) {
                if (predictions.get(i).equals(targetClass)) {
                    if (i==0) {
                        api.moveTo(point5, quaternion,true);
                        api.moveTo(point4, quaternion,true);
                        api.moveTo(point2, quaternion,true);
                        api.moveTo(point1, quaternion,true);
                        api.moveTo(point, quaternion, true);
                        api.takeTargetItemSnapshot();

                    }
                    if (i == 1) {
                        api.moveTo(point5, quaternion1,true);
                        api.moveTo(point4, quaternion1,true);
                        api.moveTo(point2, quaternion1,true);
                        api.takeTargetItemSnapshot();

                    }
                    if ( i ==2) {
                        api.moveTo(point5, quaternion2,true);
                        api.moveTo(point4, quaternion2,true);
                        api.takeTargetItemSnapshot();
                    }
                    if (i == 3) {
                        api.moveTo(point6, quaternion2,true);
                        api.takeTargetItemSnapshot();
                    }
                }
            }







        }
        catch (Exception e) {
            Log.d(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
//        result = api.moveTo(point3, quaternion, true);
//        while (!result.hasSucceeded() && loopCounter < LOOP_MAX) {
//            result = api.moveTo(point3, quaternion, true);
//            ++loopCounter;
//
//        }

//        api.moveTo(point1, quarternion, false);
//        Mat image = api.getMatNavCam();
//
//        api.moveTo(point2, quarternion, false);
//        Mat image = api.getMatNavCam();
//
//        api.moveTo(point3, quarternion, false);
//        Mat image = api.getMatNavCam();

//        Mat image = api.getMatNavCam();
//        api.setAreaInfo(1, "item_name", 1);
//        api.reportRoundingCompletion();
//        api.notifyRecognitionItem();
//        api.takeTargetItemSnapshot();
    }

    @Override
    protected void runPlan2(){
        // write your plan 2 here
    }

    @Override
    protected void runPlan3(){

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

//    private String imageRecognition(Mat image) {
//        // Load images
//        Log.i(TAG, "STARTING IMAGE RECOGNITION");
//        Bitmap templateBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.template);
//        Mat templateImage = new Mat();
//        Utils.bitmapToMat(templateBitmap, templateImage);
//
//        Mat mainImage = image;
//
//        if (templateImage.empty() || mainImage.empty()) {
//            Log.e(TAG, "Cannot load images!");
//            return null;
//        }
//        Log.i(TAG, "IMAGE LOADING SUCCESSFUL");
//        // Crop the sub-images
//        List<Mat> templates = new ArrayList<>();
//        List<String> templatesName = new ArrayList<>();
//
//        templates.add(templateImage.submat(0, 150, 50, 220));
//        templates.add(templateImage.submat(0, 150, 330, 500));
//        templates.add(templateImage.submat(0, 150, 580, 750));
//        templates.add(templateImage.submat(210, 360, 50, 220));
//        templates.add(templateImage.submat(210, 360, 330, 500));
//        templates.add(templateImage.submat(210, 360, 580, 750));
//        templates.add(templateImage.submat(420, 570, 50, 220));
//        templates.add(templateImage.submat(420, 570, 330, 500));
//        templates.add(templateImage.submat(420, 570, 580, 750));
//        templates.add(templateImage.submat(630, 780, 50, 220));
//
//        Log.i(TAG, "TEMPLATE CROPPING SUCCESSFUL");
//
//        templatesName.add("kapton");
//        templatesName.add("top");
//        templatesName.add("screw");
//        templatesName.add("beaker");
//        templatesName.add("hammer");
//        templatesName.add("pipette");
//        templatesName.add("wrench");
//        templatesName.add("thermometer");
//        templatesName.add("watch");
//        templatesName.add("goggle");
//
//        int binaryThresh = 100;
//
//        // Convert the image to grayscale (if not already done)
////        Mat grayImage = new Mat();
////        Imgproc.cvtColor(mainImage, grayImage, Imgproc.COLOR_BGR2GRAY);
//
//        // Apply binary threshold
//        Mat mainThresh = new Mat();
//        Imgproc.threshold(mainImage, mainThresh, binaryThresh, 255, Imgproc.THRESH_BINARY);
//
//        // Detect edges using Canny
////        Mat mainEdges = new Mat();
////        Imgproc.Canny(mainThresh, mainEdges, 100, 200);
////
////        // Find contours
////        List<MatOfPoint> contoursMain = new ArrayList<>();
////        Mat hierarchy = new Mat();
////        Imgproc.findContours(mainEdges, contoursMain, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
////
////        // Filter contours based on minimum area
////        double minAreaThreshold = 20.0;
////        List<MatOfPoint> filteredContoursMain = new ArrayList<>();
////        for (MatOfPoint contour : contoursMain) {
////            if (Imgproc.contourArea(contour) >= minAreaThreshold) {
////                filteredContoursMain.add(contour);
////            }
////        }
////
////        // Get the number of objects (contours) found, subtracting 1
////        int numObjects = filteredContoursMain.size() - 1;
////
////        // Print the result (for debugging purposes)
////        Log.i(TAG, "Number of objects: " + numObjects);
//
//        ORB orb = ORB.create();
//        //Mat mainThresh = new Mat();
//        //Imgproc.threshold(mainImage, mainThresh, 100, 255, Imgproc.THRESH_BINARY);
//
//        Log.i(TAG, "THRESHOLDING SUCCESSFUL");
//
//        BFMatcher bf = BFMatcher.create(Core.NORM_HAMMING, true);
//        Log.i(TAG, "BFMATCHER SUCCESSFUL");
//        String bestTemplatePath = null;
//        int bestNumGoodMatches = 0;
//        int goodMatchThreshold = 35;
//        int index = 0;
//
//        for (int i = 0; i < templates.size(); i++) {
//            Mat template = templates.get(i);
//            Mat templateThresh = new Mat();
//            Imgproc.threshold(template, templateThresh, 100, 255, Imgproc.THRESH_BINARY);
//
//            MatOfKeyPoint kp1 = new MatOfKeyPoint();
//            MatOfKeyPoint kp2 = new MatOfKeyPoint();
//            Mat des1 = new Mat();
//            Mat des2 = new Mat();
//
//            orb.detectAndCompute(templateThresh, new Mat(), kp1, des1);
//            orb.detectAndCompute(mainThresh, new Mat(), kp2, des2);
//
//            MatOfDMatch matches = new MatOfDMatch();
//            bf.match(des1, des2, matches);
//
//            List<DMatch> matchesList = matches.toList();
//            Collections.sort(matchesList, new Comparator<DMatch>() {
//                @Override
//                public int compare(DMatch o1, DMatch o2) {
//                    return Double.compare(o1.distance, o2.distance);
//                }
//            });
//
//            int numGoodMatches = 0;
//            for (DMatch match : matchesList) {
//                if (match.distance < goodMatchThreshold) {
//                    numGoodMatches++;
//                }
//            }
//
//            if (numGoodMatches > bestNumGoodMatches) {
//                bestNumGoodMatches = numGoodMatches;
//                bestTemplatePath = templatesName.get(i);
//                index = i;
//            }
//
//            Mat resultImage = new Mat();
//            Features2d.drawMatches(template, kp1, mainImage, kp2, matches, resultImage);
//
//            Log.d(TAG, "Template: " + templatesName.get(i));
//            Log.d(TAG, "Number of matches: " + numGoodMatches);
//            // Display the images using OpenCV's HighGui (for demonstration purposes only)
//            // HighGui.imshow("Main Image", mainImage);
//            // HighGui.imshow("Match Image", resultImage);
//            // HighGui.waitKey(0);
//        }
//
//        if (bestTemplatePath != null) {
//            Log.i(TAG, "Best matching template: " + bestTemplatePath);
//            Log.i(TAG, "Number of good matches: " + bestNumGoodMatches);
//        } else {
//            Log.i(TAG, "No good matches found.");
//        }
//        return bestTemplatePath;
//    }

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

    public static Mat decimateImage(Mat image, double scaleFactor) {
        // Get the original dimensions of the image
        int originalHeight = image.height();
        int originalWidth = image.width();

        // Calculate the new dimensions
        int newWidth = (int) (originalWidth * scaleFactor);
        int newHeight = (int) (originalHeight * scaleFactor);

        // Resize the image
        Mat decimatedImage = new Mat();
        Imgproc.resize(image, decimatedImage, new Size(newWidth, newHeight), 0, 0, Imgproc.INTER_AREA);

        return decimatedImage;
    }

    public static List<MatOfPoint> mergeContours(List<MatOfPoint> contours, int proximityThreshold) {
        // Calculate bounding rectangles for all contours
        List<Rect> boundingRects = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            boundingRects.add(Imgproc.boundingRect(contour));
        }

        boolean merged = true;
        while (merged) {
            merged = false;
            List<MatOfPoint> newContours = new ArrayList<>();
            boolean[] used = new boolean[contours.size()];

            for (int i = 0; i < contours.size(); i++) {
                if (used[i]) {
                    continue;
                }
                Rect currentRect = boundingRects.get(i);
                MatOfPoint currentContour = contours.get(i);
                for (int j = i + 1; j < contours.size(); j++) {
                    if (used[j]) {
                        continue;
                    }
                    if (rectDistance(currentRect, boundingRects.get(j)) < proximityThreshold) {
                        Rect newRect = Imgproc.boundingRect(mergeContours(currentContour, contours.get(j)));
                        currentRect = newRect;
                        currentContour.push_back(contours.get(j));
                        used[j] = true;
                        merged = true;
                    }
                }
                newContours.add(currentContour);
                used[i] = true;
            }

            contours = newContours;
            boundingRects.clear();
            for (MatOfPoint contour : contours) {
                boundingRects.add(Imgproc.boundingRect(contour));
            }
        }

        return contours;
    }

    private static double rectDistance(Rect rect1, Rect rect2) {
        double dx, dy;
        if (rect1.x + rect1.width < rect2.x) {  // rect1 is to the left of rect2
            dx = rect2.x - (rect1.x + rect1.width);
        } else if (rect2.x + rect2.width < rect1.x) {  // rect2 is to the left of rect1
            dx = rect1.x - (rect2.x + rect2.width);
        } else {  // rects overlap in x-axis
            dx = 0;
        }

        if (rect1.y + rect1.height < rect2.y) {  // rect1 is above rect2
            dy = rect2.y - (rect1.y + rect1.height);
        } else if (rect2.y + rect2.height < rect1.y) {  // rect2 is above rect1
            dy = rect1.y - (rect2.y + rect2.height);
        } else {  // rects overlap in y-axis
            dy = 0;
        }

        return Math.sqrt(dx * dx + dy * dy);
    }

    private static MatOfPoint mergeContours(MatOfPoint contour1, MatOfPoint contour2) {
        MatOfPoint mergedContour = new MatOfPoint();
        List<org.opencv.core.Point> points = new ArrayList<>();
        points.addAll(contour1.toList());
        points.addAll(contour2.toList());
        mergedContour.fromList(points);
        return mergedContour;
    }

    private MatOfDMatch matchTemplate(Mat mainImage, Mat templateImage, ORB orb, BFMatcher bf) {
        MatOfKeyPoint kp1 = new MatOfKeyPoint();
        MatOfKeyPoint kp2 = new MatOfKeyPoint();
        Mat des1 = new Mat();
        Mat des2 = new Mat();
        orb.detectAndCompute(templateImage, new Mat(), kp1, des1);
        orb.detectAndCompute(mainImage, new Mat(), kp2, des2);
        MatOfDMatch matches = new MatOfDMatch();
        bf.match(des1, des2, matches);
        return matches;
    }


    private String processImage(Mat mainImage, List<Integer> templatePaths) {
          String[] classNames = {"beaker", "goggle", "hammer", "kapton-tape", "pipette", "screwdriver", "thermometer", "top", "watch", "wrench"};
//        File folder = new File(folderPath);
//        File[] files = folder.listFiles();

              Mat gray = mainImage;
              //Mat gray = new Mat();
              //Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

              Dictionary arucoDict = Aruco.getPredefinedDictionary(Aruco.DICT_5X5_250);
              Mat ids = new Mat();
              List<Mat> corners = new ArrayList<>();

              Aruco.detectMarkers(gray, arucoDict, corners, ids);

              Mat image = new Mat();
              Mat image_markers=new Mat();
              Imgproc.cvtColor(gray, image, Imgproc.COLOR_GRAY2BGR);
              Imgproc.cvtColor(gray, image_markers, Imgproc.COLOR_GRAY2BGR);
              Aruco.drawDetectedMarkers(image_markers, corners, ids);
              api.saveMatImage(image_markers, "aruco_marker.png");
                // Initialize variables for minimum distance calculation
              double mindist = 6000;
              int m = 0;

              // Iterate through detected markers
              for (int i = 0; i < ids.rows(); i++) {
                  MatOfPoint2f cornerMat = new MatOfPoint2f(corners.get(i));
                  org.opencv.core.Point[] points = cornerMat.toArray();
                  double sumX = 0, sumY = 0;
                  for (org.opencv.core.Point point : points) {
                      sumX += point.x;
                      sumY += point.y;
                  }
                  double centerX = sumX / points.length;
                  double centerY = sumY / points.length;

                  // Calculate distance from center of image (assuming image size is 1280x960)
                  double dist = Math.sqrt(Math.pow(centerX - 640, 2) + Math.pow(centerY - 480, 2));
                  if (dist < mindist) {
                      mindist = dist;
                      m = i;
                  }
              }

              // Retrieve the closest marker corners
              Mat aruco_corners = corners.get(m);
              // Convert aruco_corners to 4x2 matrix
              MatOfPoint2f arucoCorners = new MatOfPoint2f(aruco_corners);
              org.opencv.core.Point[] arucoCornersArray = arucoCorners.toArray();

              Mat edges = new Mat();
              Imgproc.Canny(image, edges, 50, 150);


              Mat hierarchy = new Mat();
              List<MatOfPoint> contoursList = new ArrayList<>();
              Imgproc.findContours(edges, contoursList, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
              // Convert contours to MatOfPoint2f
              List<MatOfPoint2f> contours = new ArrayList<>();
              for (MatOfPoint contour : contoursList) {
                  MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
                  contours.add(contour2f);
              }
              // Sort contours by area
              Collections.sort(contours, new Comparator<MatOfPoint2f>() {
                  @Override
                  public int compare(MatOfPoint2f contour1, MatOfPoint2f contour2) {
                      double area1 = Imgproc.contourArea(contour1);
                      double area2 = Imgproc.contourArea(contour2);
                      return Double.compare(area2, area1);
                  }
              });
              boolean found = false;
              for (MatOfPoint2f contour : contours) {
                  double epsilon = 0.02 * Imgproc.arcLength(contour, true);
                  MatOfPoint2f approx = new MatOfPoint2f();
                  Imgproc.approxPolyDP(contour, approx, epsilon, true);

                  if (approx.total() == 4) {
                      Mat imageContours = image.clone();

                      List<MatOfPoint> contourList1 = new ArrayList<>();
                      contourList1.add(new MatOfPoint(approx.toArray())); // Convert back to MatOfPoint
                      Imgproc.drawContours(imageContours, contourList1, -1, new Scalar(0, 255, 0), 3);


                      org.opencv.core.Point[] cornersArray = approx.toArray();
                      org.opencv.core.Point top_left = cornersArray[0];
                      org.opencv.core.Point top_right = cornersArray[1];
                      org.opencv.core.Point bottom_right = cornersArray[2];
                      org.opencv.core.Point bottom_left = cornersArray[3];

                      // Determine the width and height of the detected rectangle
                      double width = Math.sqrt(Math.pow(top_right.x - top_left.x, 2) + Math.pow(top_right.y - top_left.y, 2));
                      double height = Math.sqrt(Math.pow(top_right.x - bottom_right.x, 2) + Math.pow(top_right.y - bottom_right.y, 2));

                      // Create destination points for perspective transform
                      MatOfPoint2f dst_pts = new MatOfPoint2f(
                              new  org.opencv.core.Point(0, 0),
                              new  org.opencv.core.Point(width, 0),
                              new  org.opencv.core.Point(width, height),
                              new  org.opencv.core.Point(0, height));

                      // Get the perspective transform matrix
                      Mat M = Imgproc.getPerspectiveTransform(approx, dst_pts);

                      // Apply the perspective transformation to get the top-down view
                      Mat warped = new Mat();
                      Imgproc.warpPerspective(image, warped, M, new org.opencv.core.Size(width, height));

                      if (width < height) {
                          Core.rotate(warped, warped, Core.ROTATE_90_CLOCKWISE);
                          found = true;
                      }
                      break;
                  }
              }

              if (!found && arucoCorners != null) {
                  org.opencv.core.Point[] corners1 = arucoCorners.toArray();
                  org.opencv.core.Point top_left = corners1[0];
                  org.opencv.core.Point top_right = corners1[1];
                  org.opencv.core.Point bottom_right = corners1[2];
                  org.opencv.core.Point bottom_left = corners1[3];

                  double k1 = 5;
                  double k2 = 2;
                  double k3 = 0.5;

                  org.opencv.core.Point top_left1 = new org.opencv.core.Point(top_left.x + k1 * (top_left.x - top_right.x) + k3 * (top_left.x - bottom_left.x), top_left.y + k1 * (top_left.y - top_right.y) + k3 * (top_left.y - bottom_left.y));
                  org.opencv.core.Point bottom_right1 = new org.opencv.core.Point(bottom_right.x + k2 * (bottom_right.x - top_right.x), bottom_right.y + k2 * (bottom_right.y - top_right.y));
                  org.opencv.core.Point bottom_left1 = new org.opencv.core.Point(bottom_right1.x + k1 * (top_left.x - top_right.x), bottom_right1.y + k1 * (top_left.y - top_right.y));
                  org.opencv.core.Point top_right1 = top_right;

                  MatOfPoint2f new_corners = new MatOfPoint2f(top_left1, top_right1, bottom_right1, bottom_left1);
                  double width1 = Math.sqrt(Math.pow(top_right1.x - top_left1.x, 2) + Math.pow(top_right1.y - top_left1.y, 2));
                  double height1 = Math.sqrt(Math.pow(top_right1.x - bottom_right1.x, 2) + Math.pow(top_right1.y - bottom_right1.y,2));
                  // Create destination points for perspective transform
                   MatOfPoint2f dst_pts1 = new MatOfPoint2f(
                                  new  org.opencv.core.Point(0, 0),
                                  new  org.opencv.core.Point(width1, 0),
                                  new  org.opencv.core.Point(width1, height1),
                                  new  org.opencv.core.Point(0, height1));

                  // Get the perspective transform matrix
                  Mat M1 = Imgproc.getPerspectiveTransform(new_corners, dst_pts1);
                  // Apply the perspective transformation to get the top-down view
                  Mat warped = new Mat();
                  Imgproc.warpPerspective(image, warped, M1, new org.opencv.core.Size(width1, height1));
                  api.saveMatImage(warped, "warped.png");
                  int bestIndex=0;
                      int binaryThreshold = 150;
                      double minAreaThreshold = 10;
                      Mat main_thresh = new Mat();
                      Imgproc.threshold(warped, main_thresh, binaryThreshold, 255, Imgproc.THRESH_BINARY);

                      Mat mainEdges = new Mat();
                      Imgproc.Canny(main_thresh, mainEdges, 50, 200);

                      List<MatOfPoint> contoursMain = new ArrayList<>();
                      Mat hierarchy1 = new Mat();
                      Imgproc.findContours(mainEdges, contoursMain, hierarchy1, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

                      List<MatOfPoint> filteredMergedContours = new ArrayList<>();
                      List<MatOfPoint> mergedContours = mergeContours(contoursMain, 10);
                      for (MatOfPoint main_contour : mergedContours) {
                          Log.i(TAG, "LOOPING THROUGH MERGED CONTOURS, STARTING FILTER");
                          if (Imgproc.contourArea(main_contour) >= minAreaThreshold) {
                              filteredMergedContours.add(main_contour);
                          }
                      }
                      numObjects = filteredMergedContours.size() - 2;
                      Log.i(TAG, "Number of objects: " + numObjects);
                      // Initialize ORB detector
                      ORB orb = ORB.create();

                      // Use BFMatcher to find matches between descriptors
                      BFMatcher bf = BFMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING, true);

                      String bestTemplatePath = null;
                      MatOfDMatch bestMatches = null;
                      int bestNumGoodMatches = 0;
                      int goodMatchThreshold = 65;
                      int index=0;
                      for (Integer templatePath : templatePaths) {
                          Log.i(TAG, "LOOPING THROUGH TEMPLATE PATHS");
                          Bitmap templateBitmap = BitmapFactory.decodeResource(getResources(), templatePath);
                          Mat template = new Mat();
                          Utils.bitmapToMat(templateBitmap, template);
                          Mat grayTemplate = new Mat();
                          Imgproc.cvtColor(template, grayTemplate, Imgproc.COLOR_BGR2GRAY);
                          Mat templateThresh= new Mat();
                          Imgproc.threshold(grayTemplate, templateThresh, binaryThreshold, 255, Imgproc.THRESH_BINARY);
                          MatOfDMatch matches = matchTemplate(main_thresh, templateThresh, orb, bf);
                          List<MatOfDMatch> matchesList = new ArrayList<>();
                          matchesList.add(matches);
                          List<DMatch> goodMatches = new ArrayList<>();
                          for (DMatch match : matches.toList()) {
                              Log.i(TAG, "LOOPING THROUGH MATCHES, LOOKING FOR GOOD MATCHES"+String.valueOf(match.distance));
                              if (match.distance < goodMatchThreshold) {
                                  Log.i(TAG, "ADDING GOOD MATCHES");
                                  goodMatches.add(match);
                              }
                          }

                          int numGoodMatches = goodMatches.size();
                          if (numGoodMatches > bestNumGoodMatches) {
                              Log.i(TAG, "NUM MATCHES BETTER THAN BEST MATCHES, OVERRIDING");
                              bestNumGoodMatches = numGoodMatches;
                              //bestTemplatePath = (templatePath);
                              bestMatches = new MatOfDMatch();
                              bestMatches.fromList(goodMatches);
                              bestIndex = index;
                          }
                          index++;

                      }
                      Log.i(TAG, "bestIndex: " + bestIndex);

                      return classNames[bestIndex];


                  }


        return null;
    };
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







