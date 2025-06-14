package jp.jaxa.iss.kibo.rpc.defaultapk;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import gov.nasa.arc.astrobee.Result;
import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

//import org.opencv.aruco.Aruco;
//import org.opencv.aruco.Dictionary;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.objdetect.ArucoDetector;
import org.opencv.objdetect.Dictionary;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import static java.lang.Thread.sleep;

import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgproc.Imgproc;
import org.opencv.calib3d.Calib3d;


import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.objdetect.Objdetect;

//import org.opencv.core.Moments;
import java.io.File;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */


public class YourService extends KiboRpcService {
    final String
            TAG = "ROT",
            SIM = "SIMULATOR";
    int counter = 0;

    //ArucoTagDetector arTagDetector = new ArucoTagDetector();

    //String[] classNames = {"beaker", "goggle", "hammer", "kapton-tape", "pipette", "screwdriver", "thermometer", "top", "watch", "wrench"};
    int numObjects = 0;
    double cx=0;
    double cz =0;
    public void goToPoint(Point point, Quaternion quaternion) {
        Result result = api.moveTo(point, quaternion, true);
        int loopCounter = 0;
        final int LOOP_MAX = 5;
        while (!result.hasSucceeded() && loopCounter < LOOP_MAX) {
            result = api.moveTo(point, quaternion, true);
            ++loopCounter;
        }


    }

//    public String assetFilePath(String assetName, Context context) throws IOException {
//        File file = new File(context.getFilesDir(), assetName);
//        try (InputStream is = context.getAssets().open(assetName)) {
//            try (OutputStream os = new FileOutputStream(file)) {
//                byte[] buffer = new byte[4 * 1024];
//                int read;
//                while ((read = is.read(buffer)) != -1) {
//                    os.write(buffer, 0, read);
//                }
//                os.flush();
//            }
//            Log.i("ROT", "OPEN FILE SUCCCESS!");
//            return file.getAbsolutePath();
//        } catch (Exception e) {
//            Log.e("ROT", e.getMessage());
//            Log.i("ROT", "ERROR writing asset path");
//            return null;
//        }
//    }

    @Override
    protected void runPlan1() {

        api.startMission();
        Result result;
        final int LOOP_MAX = 5;
        final int img_MAX = 5;
        int loopCounter = 0;
        int imgRetries = 0;

        //ModelInterpreter modelInterpreter = new ModelInterpreter(this);
        //
            //old kibo rpc points
        //
        Point point = new Point(11.029d, -9.98828d, 5.2817d);
//        Point point1 = new Point(11.343d, -9.2814d, 5.3594d);
        Point point1 = new Point(10.974d, -8.8799d, 4.6309d);
//        //Point point3 = new Point(11.303d, -8.7276d, 4.5397d);
        Point point2 = new Point(10.924d, -7.9268d, 4.5397d);
//        Point point5 = new Point(10.563d, -7.4084d, 4.5397d);
        Point point3 = new Point(10.557d, -6.8833d, 4.8351d);

//        Point point = new Point(11.026d, -9.564d, 4.8945);
//        Point point1 = new Point(11.178d, -8.9585d, 5.3901d);
//        Point point2 = new Point(10.81d, -7.9477d, 5.3935d);
//        Point point3 = new Point(11.367d, -6.8833d, 4.8593d);


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
        Quaternion quaternion2 = new Quaternion(0f, 0f, 1f, 0f);
        Quaternion quaternion3 = new Quaternion(0f, 0f, 0.707f, 0.707f);

        Quaternion testIncrement = new Quaternion(0.383f, 0f, 0f, 0.924f);
        Quaternion incrementX = new Quaternion(0.0261f, 0.01f, 0.01f, 0.9996f);
        Quaternion incrementY = new Quaternion(0f, 0.0261f, 0f, 0.9996f);
        Quaternion incrementZ = new Quaternion(0f, 0f, 0.0261f, 0.9996f);


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


        ArrayList<String[]> predictions = new ArrayList<String[]>();


        try {
//            result = api.moveTo(point, quaternion, true);
//            while (!result.hasSucceeded() && loopCounter < LOOP_MAX) {
//                result = api.moveTo(point, quaternion, true);
//                ++loopCounter;
//
//            }

            Net net = Dnn.readNetFromONNX(assetFilePath("my_model_50_wende.onnx", this));

            goToPoint(point, quaternion);


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
            int destination = 0;
            Log.i(TAG, "ABOUT TO DO PREDICTIONS");
            PredictionResult predictionResult = processImage(image, net, destination);
            String[] strPreds = predictionResult.getLabels();
            Log.i("ROT", "PREDICTIONS FOR AREA 1: "+Arrays.toString(strPreds));
            int num_Objects = predictionResult.getNumObjects();
            String finalPred = "coin";
            Log.i(TAG, "FINISHED IMAGE RECOGNITION, START COUNT OBJECTS");
            Log.i(TAG, "FINISHED COUNT OBJECTS");

            if (predictionResult != null) {
                predictions.add(strPreds);
                api.saveMatImage(predictionResult.getBlob(), "inference.png");
            } else {
                Log.i(TAG, "Prediction is null for area 1, guessing");
                api.setAreaInfo(1, "Beaker", 1);
            }
            if (num_Objects > 0) {
                for (int i = 0;i<strPreds.length;i++) {
                    Log.i("ROT", "num_Objects>0");
                    if (strPreds[i].equals("crystal")||strPreds[i].equals("diamond")||strPreds[i].equals("emerald")) {
                        num_Objects--;
                        continue;
                    }
                    else {
                        finalPred = strPreds[i];
                    }

                }
                api.setAreaInfo(1, finalPred, num_Objects);
            }else {
                for (int i = 0;i<strPreds.length;i++) {
                    Log.i("ROT", "num_Objects=0");
                    if (strPreds[i].equals("crystal")||strPreds[i].equals("diamond")||strPreds[i].equals("emerald")) {
                        continue;
                    }
                    api.setAreaInfo(1, strPreds[i], 2);

                }
            }

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


//            result = api.moveTo(point1, quaternion, true);
//            while (!result.hasSucceeded() && loopCounter < LOOP_MAX) {
//                result = api.moveTo(point1, quaternion, true);
//                ++loopCounter;
//            }
            goToPoint(point1, quaternion1);
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

            //ArrayList corners1 = arTagDetector.detect(image1);

            //Log.i(TAG, "FINISHED ARUCO DETECT");
            //String joined1 = TextUtils.join(", ", corners1);
            //Log.i(TAG, joined1); done
            destination++;
            PredictionResult predictionResult1 = processImage(image1, net, destination);
            String[] strPreds1 = predictionResult1.getLabels();
            Log.i("ROT", "PREDICTIONS FOR AREA 2: "+Arrays.toString(strPreds1));
            int numObjects1 = predictionResult1.getNumObjects();
            String finalPred1 = "coin";
            //int numObjects1 = countObjects(image1);
            if (predictionResult1 != null) {
                api.saveMatImage(predictionResult1.getBlob(), "inference1.png");
                predictions.add(strPreds1);
            } else {
                Log.i(TAG, "Prediction is null for area 2, guessing");
                api.setAreaInfo(2, "Beaker", 2);
            }
            if (numObjects1 > 0) {
                for (int i = 0;i<strPreds1.length;i++) {
                    Log.i("ROT", "numObjects1>0");
                    if (strPreds1[i].equals("crystal")||strPreds1[i].equals("diamond")||strPreds1[i].equals("emerald")) {
                        numObjects1--;
                        continue;
                    }
                    else {
                        finalPred1 = strPreds1[i];
                    }
                }
                api.setAreaInfo(2, finalPred1, numObjects1);
            }else {
                for (int i = 0;i<strPreds1.length;i++) {
                    Log.i("ROT", "numObjects1=0");
                    if (strPreds1[i].equals("crystal")||strPreds1[i].equals("diamond")||strPreds1[i].equals("emerald")) {
                        continue;
                    }
                    api.setAreaInfo(2, strPreds1[i], 3);
                }

            }
//            result = api.moveTo(point2, quaternion1, true);
//            while (!result.hasSucceeded() && loopCounter < LOOP_MAX) {
//                result = api.moveTo(point2, quaternion1, true);
//                ++loopCounter;
//
//            }


            goToPoint(point2, quaternion1);

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

            //ArrayList corners1 = arTagDetector.detect(image1);

            //Log.i(TAG, "FINISHED ARUCO DETECT");
            //String joined1 = TextUtils.join(", ", corners1);
            //Log.i(TAG, joined1);
            destination++;
            PredictionResult predictionResult2 = processImage(image2, net, destination);
            String[] strPreds2 = predictionResult2.getLabels();
            Log.i("ROT", "PREDICTIONS FOR AREA 3: "+Arrays.toString(strPreds2));
            int numObjects2 = predictionResult2.getNumObjects();
            String finalPred2 = "coin";
            //int numObjects1 = countObjects(image1);
            if (predictionResult2 != null) {
                api.saveMatImage(predictionResult2.getBlob(), "inference2.png");
                predictions.add(strPreds2);
            } else {
                Log.i(TAG, "Prediction is null for area 2, guessing");
                api.setAreaInfo(3, "Beaker", 2);
            }
            if (numObjects2 > 0) {
                for (int i = 0;i<strPreds2.length;i++) {
                    Log.i("ROT", "numObjects2>0");
                    if (strPreds2[i].equals("crystal")||strPreds2[i].equals("diamond")||strPreds2[i].equals("emerald")) {
                        numObjects2--;
                        continue;
                    }
                    else {
                        finalPred2 = strPreds2[i];
                    }
                }
                api.setAreaInfo(3, finalPred2, numObjects2);
            }else {
                for (int i = 0;i<strPreds2.length;i++) {
                    Log.i("ROT", "numObjects2=0");
                    if (strPreds2[i].equals("crystal")||strPreds2[i].equals("diamond")||strPreds2[i].equals("emerald")) {
                        continue;
                    }
                    api.setAreaInfo(3, strPreds2[i], 4);
                }
            }


            goToPoint(point3, quaternion2);

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

            //ArrayList corners1 = arTagDetector.detect(image1);

            //Log.i(TAG, "FINISHED ARUCO DETECT");
            //String joined1 = TextUtils.join(", ", corners1);
            //Log.i(TAG, joined1);
            destination++;
            PredictionResult predictionResult3 = processImage(image3, net, destination);
            String[] strPreds3 = predictionResult3.getLabels();
            int numObjects3 = predictionResult3.getNumObjects();
            String finalPred3 = "coin";
            //int numObjects1 = countObjects(image1);
            if (predictionResult3 != null) {
                api.saveMatImage(predictionResult3.getBlob(), "inference3.png");
                predictions.add(strPreds3);
            } else {
                Log.i(TAG, "Prediction is null for area 2, guessing");
                api.setAreaInfo(4, "Beaker", 2);
            }
            if (numObjects3 > 0) {
                for (int i = 0;i<strPreds3.length;i++) {
                    Log.i("ROT", "numObjects3>0");
                    if (strPreds3[i].equals("crystal")||strPreds3[i].equals("diamond")||strPreds3[i].equals("emerald")) {
                        numObjects3--;
                        continue;
                    }
                    else {
                        finalPred3 = strPreds3[i];
                    }
                }
                api.setAreaInfo(4, finalPred3, numObjects3);
            }else {
                for (int i = 0;i<strPreds3.length;i++) {

                    if (strPreds3[i].equals("crystal")||strPreds3[i].equals("diamond")||strPreds3[i].equals("emerald")) {
                        continue;
                    }
                    api.setAreaInfo(4, strPreds3[i], 4);
                }
            }
//            result = api.moveTo(point4, quaternion1, true);
//            while (!result.hasSucceeded() && loopCounter < LOOP_MAX) {
//                result = api.moveTo(point4, quaternion1, true);
//                ++loopCounter;
//            }
//            goToPoint(point4, quaternion1);
//            api.flashlightControlFront(0.5f);
//            Thread.sleep(2000);
//            Mat image2 = api.getMatNavCam();
//            api.flashlightControlFront(0.0f);
//            if (image2 == null) {
//                while (image2 == null && imgRetries < img_MAX) {
//                    api.flashlightControlFront(0.05f);
//                    Thread.sleep(2000);
//                    image2 = api.getMatNavCam();
//                    api.flashlightControlFront(0.0f);
//                    imgRetries++;
//                }
//            } else {
//
//                api.saveMatImage(image2, "point_2.png");
//            }
//
//            //ArrayList corners2 = arTagDetector.detect(image2);
//
////            Log.i(TAG, "FINISHED ARUCO DETECT");
////            String joined2 = TextUtils.join(", ", corners2);
////            Log.i(TAG, joined2);
//            destination++;
//            String pred2 = processImage(image2, templatePaths, destination);
//            //int numObjects2 = countObjects(image2);
//            if (pred2 != null) {
//                api.setAreaInfo(3, pred2);
//                predictions.add(pred2);
//            } else {
//                Log.i(TAG, "Prediction is null for area 2, guessing");
//                api.setAreaInfo(3, "Beaker", 3);
//            }
//            if (numObjects > 0) {
//                api.setAreaInfo(3, pred2, numObjects);
//            } else
//            {
//                api.setAreaInfo(3, pred2, 4);
//            }
//
//
////            result = api.moveTo(point5, quaternion1, true);
////            while (!result.hasSucceeded() && loopCounter < LOOP_MAX) {
////                result = api.moveTo(point5, quaternion1, true);
////                ++loopCounter;
////
////            }
//            goToPoint(point5, quaternion1);
//
////            result = api.moveTo(point6, quaternion2, true);
////            while (!result.hasSucceeded() && loopCounter < LOOP_MAX) {
////                result = api.moveTo(point6, quaternion2, true);
////                ++loopCounter;
////
////            }
//            goToPoint(point6, quaternion2);
//
//
//            api.flashlightControlFront(0.5f);
//            Thread.sleep(2000);
//            Mat image3 = api.getMatNavCam();
//            api.flashlightControlFront(0.0f);
//            if (image3 == null) {
//                while (image3 == null && imgRetries < img_MAX) {
//                    api.flashlightControlFront(0.05f);
//                    Thread.sleep(2000);
//                    image3 = api.getMatNavCam();
//                    api.flashlightControlFront(0.0f);
//                    imgRetries++;
//                }
//            } else {
//
//                api.saveMatImage(image3, "point_3.png");
//            }
//
//            //ArrayList corners3 = arTagDetector.detect(image3);
//
////            Log.i(TAG, "FINISHED ARUCO DETECT");
////            String joined3 = TextUtils.join(", ", corners3);
////            Log.i(TAG, joined3);
//            destination++;
//            String pred3 = processImage(image3, templatePaths, destination);
//            //int numObjects3 = countObjects(image3);
//            if (pred3 != null) {
//                api.setAreaInfo(4, pred3);
//                predictions.add(pred3);
//            } else {
//                Log.i(TAG, "Prediction is null for area 4, guessing");
//                api.setAreaInfo(4, "Beaker", 1);
//            }
//            if (numObjects > 0) {
//                api.setAreaInfo(4, pred3, numObjects);
//            }else {
//                api.setAreaInfo(4, pred3, 4);
//            }

//            result = api.moveTo(astronaut, quaternion3, true);
//            while (!result.hasSucceeded() && loopCounter < LOOP_MAX) {
//                result = api.moveTo(astronaut, quaternion3, true);
//                ++loopCounter;
//
//            }
            goToPoint(astronaut, quaternion3);

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
            destination++;
            PredictionResult targetResult = processImage(target_item, net, destination);
            api.saveMatImage(targetResult.getBlob(), "inference_target.png");
            api.notifyRecognitionItem();
            Log.i("ROT", "targetClass:"+ Arrays.toString(targetResult.getLabels()));
            Log.i("ROT", "Predictions Array is");
            Log.i("ROT", TextUtils.join(", ", predictions));
            String targetItem = "diamond"; //default value
            for (String item:targetResult.getLabels()) {
                if (item.equals("crystal")||item.equals("diamond")||item.equals("emerald")) {
                    targetItem = item;
                }
            }
            Log.i("TARGET ITEM: ", targetItem);
            int found_target=0;
            for (int i = 0; i < predictions.size(); i++) {
                for (String prediction:predictions.get(i))
                    if (prediction.equals(targetItem)) {
                        Log.i("ROT", "Going to target at" + String.valueOf(i));
                        if (i == 0) {
    //                        api.moveTo(point5, quaternion, true);
    //                        api.moveTo(point4, quaternion, true);
    //                        api.moveTo(point2, quaternion, true);
    //                        api.moveTo(point1, quaternion, true);
    //                        api.moveTo(point, quaternion, true);
                           //goToPoint(point5, quaternion);
                            //goToPoint(point4, quaternion);
                            goToPoint(point2, quaternion);
                            goToPoint(point1, quaternion);
                            goToPoint(point, quaternion);
                            goToPoint(new Point(point.getX()+cx, point.getY(),point.getZ()+cz), quaternion);
                            api.saveMatImage(api.getMatNavCam(), "final_image.png");
                            api.takeTargetItemSnapshot();
                        } else if (i == 1) {
    //                        api.moveTo(point5, quaternion1, true);
    //                        api.moveTo(point4, quaternion1, true);
    //                        api.moveTo(point2, quaternion1, true);
                            //goToPoint(point5, quaternion1);
                            //goToPoint(point4, quaternion1);
                            goToPoint(point1, quaternion1);
                            goToPoint(new Point(point2.getX()+cx, point2.getY(),point2.getZ()+cz), quaternion1);
                            api.saveMatImage(api.getMatNavCam(), "final_image.png");
                            api.takeTargetItemSnapshot();
                        } else if (i == 2) {
    //                        api.moveTo(point5, quaternion2, true);
    //                        api.moveTo(point4, quaternion2, true);
                            //goToPoint(point5, quaternion1);
                            //goToPoint(point4, quaternion1);
                            //goToPoint(new Point(point4.getX()+cx, point4.getY(),point4.getZ()+cz), quaternion1);
                            goToPoint(point2, quaternion1);
                            api.saveMatImage(api.getMatNavCam(), "final_image.png");
                            api.takeTargetItemSnapshot();
                        } else if (i == 3) {
                            //api.moveTo(point6, quaternion2, true);
                            //goToPoint(point6, quaternion2);
                            //goToPoint(new Point(point6.getX()+cx, point6.getY(),point6.getZ()+cz), quaternion2);
                            goToPoint(point3, quaternion2);
                            api.saveMatImage(api.getMatNavCam(), "final_image.png");
                            api.takeTargetItemSnapshot();
                        }
                    }
                }
            if(found_target==0){
                    // go to 3 (since that is most common)
                    //api.moveTo(point5, quaternion2, true);
                    //api.moveTo(point4, quaternion2, true);
                    //goToPoint(point5, quaternion1);
                    //goToPoint(point4, quaternion1);
                    //goToPoint(new Point(point4.getX()+cx, point4.getY(),point4.getZ()+cz), quaternion1);
                    goToPoint(point3, quaternion2);
                    api.saveMatImage(api.getMatNavCam(), "final_image.png");
                    api.takeTargetItemSnapshot();
                }



        } catch (Exception e) {
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
    protected void runPlan2() {
        // write your plan 2 here
    }

    @Override
    protected void runPlan3() {

    }

    public String assetFilePath(String assetName, Context context) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            Log.i("ROT", "OPEN FILE SUCCCESS!");
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e("ROT", e.getMessage());
            Log.i("ROT", "ERROR writing asset path");
            return null;
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

//    public int countObjects(Mat image) {
//        int binaryThresh = 100;
//        int numObjects = 0;
//        // Convert the image to grayscale (if not already done)
//        //Mat grayImage = new Mat();
//        //Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
//        //Log.i(TAG, "CONVERTED TO GRAYSCALE");
//        //GRAYSCALE CAUSING ISSUES
//
//        // Apply binary threshold
//        Mat mainThresh = new Mat();
//        Imgproc.threshold(image, mainThresh, binaryThresh, 255, Imgproc.THRESH_BINARY);
//
//        Log.i(TAG, "APPLIED BINARY THRESHOLDING");
//
//        // Detect edges using Canny
//        Mat mainEdges = new Mat();
//        Imgproc.Canny(mainThresh, mainEdges, 100, 200);
//
//        Log.i(TAG, "DETECTED EDGES USING CANNY");
//
//        // Find contours
//        List<MatOfPoint> contoursMain = new ArrayList<>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(mainEdges, contoursMain, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        Log.i(TAG, "FOUND CONTOURS");
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
//        Log.i(TAG, "FILTERED CONTOURS");
//
//        // Get the number of objects (contours) found, subtracting 1
//        numObjects = filteredContoursMain.size() - 1;
//
//        // Print the result (for debugging purposes)
//        Log.i(TAG, "Number of objects: " + numObjects);
//
//        return numObjects;
//    }
//


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

    public boolean isArucoInsideRectangle(org.opencv.core.Point[] innerVertices, org.opencv.core.Point[] outerVertices) {
        // Check if all vertices of the inner rectangle are inside the outer rectangle

        for (org.opencv.core.Point vertex : innerVertices) {
            if (!isPointInsideBoundingBox(vertex, outerVertices)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPointInsideBoundingBox(org.opencv.core.Point p, org.opencv.core.Point[] boundingBox) {
        // Use cross product to determine if point is inside the bounding box
        return isPointInTriangle(p, boundingBox[0], boundingBox[1], boundingBox[2]) ||
                isPointInTriangle(p, boundingBox[0], boundingBox[2], boundingBox[3]);
    }

    private boolean isPointInTriangle(org.opencv.core.Point p, org.opencv.core.Point v1, org.opencv.core.Point v2, org.opencv.core.Point v3) {
        double d1 = vectorCrossProduct(new org.opencv.core.Point(p.x - v1.x, p.y - v1.y), new org.opencv.core.Point(v2.x - v1.x, v2.y - v1.y));
        double d2 = vectorCrossProduct(new org.opencv.core.Point(p.x - v2.x, p.y - v2.y), new org.opencv.core.Point(v3.x - v2.x, v3.y - v2.y));
        double d3 = vectorCrossProduct(new org.opencv.core.Point(p.x - v3.x, p.y - v3.y), new org.opencv.core.Point(v1.x - v3.x, v1.y - v3.y));

        boolean hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(hasNeg && hasPos);
    }

    private double vectorCrossProduct(org.opencv.core.Point v1, org.opencv.core.Point v2) {
        return v1.x * v2.y - v1.y * v2.x;
    }
    /*private Mat extractPaperImage(Mat originalImage, org.opencv.core.Point[] cornersArray) {
        // Calculate bounding box of the sub-image
        double minX = Math.min(Math.min(cornersArray[0].x, cornersArray[1].x), Math.min(cornersArray[2].x, cornersArray[3].x));
        double minY = Math.min(Math.min(cornersArray[0].y, cornersArray[1].y), Math.min(cornersArray[2].y, cornersArray[3].y));
        double maxX = Math.max(Math.max(cornersArray[0].x, cornersArray[1].x), Math.max(cornersArray[2].x, cornersArray[3].x));
        double maxY = Math.max(Math.max(cornersArray[0].y, cornersArray[1].y), Math.max(cornersArray[2].y, cornersArray[3].y));

        int width = (int) (maxX - minX);
        int height = (int) (maxY - minY);

        // Create a mask for the polygon
        Mat mask = Mat.zeros(originalImage.size(), CvType.CV_8UC1);
        org.opencv.core.Point[] polygon = new org.opencv.core.Point[]{
                new org.opencv.core.Point(cornersArray[0].x - minX, cornersArray[0].y - minY),
                new org.opencv.core.Point(cornersArray[1].x - minX, cornersArray[1].y - minY),
                new org.opencv.core.Point(cornersArray[2].x - minX, cornersArray[2].y - minY),
                new org.opencv.core.Point(cornersArray[3].x - minX, cornersArray[3].y - minY)
        };

        MatOfPoint matOfPoint = new MatOfPoint(polygon);
        List<MatOfPoint> listOfPoints = new ArrayList<>();
        listOfPoints.add(matOfPoint);
        Imgproc.fillPoly(mask, listOfPoints, new Scalar(255));

        // Extract the sub-image using the mask
        Mat subImageMat = new Mat();
        originalImage.copyTo(subImageMat, mask);

        // Crop the sub-image to the bounding box
        Rect boundingBox = new Rect((int) minX, (int) minY, width, height);
        Mat croppedSubImage = new Mat(subImageMat, boundingBox);

        return croppedSubImage;
    }

*/
    public double[] extractFeatures(Mat binary) {
        // Load image
       /* Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        if (bitmap == null) {
            Log.e("OpenCV", "Image could not be loaded.");
            return null;
        }

        // Convert Bitmap to Mat
        Mat image = new Mat();
        org.opencv.android.Utils.bitmapToMat(bitmap, image);

        // Convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        // Apply binary thresholding
        Mat binary = new Mat();
        Imgproc.threshold(gray, binary, 160, 255, Imgproc.THRESH_BINARY);
*/
        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Filter contours based on area
        double minAreaThreshold = 15.0;  // Example threshold, adjust as necessary
        List<MatOfPoint> filteredContours = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            if (Imgproc.contourArea(contour) >= minAreaThreshold) {
                filteredContours.add(contour);
            }
        }

        // Sort contours by area and select the largest N contours
        int N = 3;  // Number of contours to use
        Collections.sort(filteredContours, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint o1, MatOfPoint o2) {
                return Double.compare(Imgproc.contourArea(o2), Imgproc.contourArea(o1));
            }
        });

        // Pad with empty contours if necessary
        while (filteredContours.size() < N) {
            filteredContours.add(new MatOfPoint());
        }
        filteredContours = filteredContours.subList(0, N);

        // Calculate Hu Moments
        List<Mat> huMomentsList = new ArrayList<>();
        for (MatOfPoint contour : filteredContours) {
            if (contour.empty()) {
                huMomentsList.add(new Mat());
            } else {
                List<org.opencv.core.Point> points = contour.toList();
                double m00 = 0, m10 = 0, m01 = 0, m20 = 0, m11 = 0, m02 = 0, m30 = 0, m21 = 0, m12 = 0, m03 = 0;
                for (org.opencv.core.Point point : points) {
                    double x = point.x;
                    double y = point.y;
                    m00 += 1;
                    m10 += x;
                    m01 += y;
                    m20 += x * x;
                    m11 += x * y;
                    m02 += y * y;
                    m30 += x * x * x;
                    m21 += x * x * y;
                    m12 += x * y * y;
                    m03 += y * y * y;
                }

                Moments moments = new Moments(m00, m10, m01, m20, m11, m02, m30, m21, m12, m03);

                //Moments moments = new Moments(contour);
                Mat huMoments = new Mat();
                Imgproc.HuMoments(moments, huMoments);
                huMomentsList.add(huMoments);
            }
        }

        // Flatten the list of Hu Moments
        double[] huMomentsArray = new double[huMomentsList.size() * 7];
        int index = 0;
        for (Mat huMoment : huMomentsList) {
            for (int i = 0; i < 7; i++) {
                huMomentsArray[index++] = huMoment.get(0, i)[0];
            }
        }

//        for (Mat huMoment : huMomentsList) {
//            System.arraycopy(huMoment, 0, huMomentsArray, index, huMoment.length);
//            index += huMoment.length;
//        }

        return huMomentsArray;
    }




    public PredictionResult processImage(Mat mainImage, Net net, int destination) {
 //       String[] classNames = {"beaker", "goggle", "hammer", "kapton-tape", "pipette", "screwdriver", "thermometer", "top", "watch", "wrench"};
//        File folder = new File(folderPath);
//        File[] files = folder.listFiles();

        Mat gray = mainImage;
        double[][] intrinsics = api.getNavCamIntrinsics();
        double[] cameraMatrixArray = intrinsics[0];
        double[] distortionCoefficientsArray = intrinsics[1];

        Mat cameraMatrix = new Mat(3, 3, CvType.CV_64F);
        cameraMatrix.put(0, 0, cameraMatrixArray);

        Mat distortionCoefficients = new Mat(1, distortionCoefficientsArray.length, CvType.CV_64F);
        distortionCoefficients.put(0, 0, distortionCoefficientsArray);

        // Load your input image (replace with actual image loading code)


        // Undistort the image
        Mat gray_undistorted = new Mat();
        Calib3d.undistort(gray, gray_undistorted, cameraMatrix, distortionCoefficients);
        // Convert undistorted Mat back to Bitmap
        //Mat gray = new Mat();
        //Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        Dictionary arucoDict = Objdetect.getPredefinedDictionary(Objdetect.DICT_5X5_250);
        ArucoDetector arucoDetector = new ArucoDetector(arucoDict);
        Mat ids = new Mat();
        List<Mat> corners = new ArrayList<>();
        arucoDetector.detectMarkers(gray_undistorted, corners, ids);
        //Aruco.detectMarkers(gray, arucoDict, corners, ids);

        Mat image = new Mat();
        Mat image_markers = new Mat();

        Imgproc.cvtColor(gray_undistorted, image, Imgproc.COLOR_GRAY2BGR);
        image_markers = image.clone();
        //Imgproc.cvtColor(gray, image_markers, Imgproc.COLOR_GRAY2BGR);

        Objdetect.drawDetectedMarkers(image_markers, corners, ids);

        //api.saveMatImage(image_markers, "aruco_marker" + destination + ".png");
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
                cx=centerX-640;
                cz=centerY-480;

            }
        }

        // Retrieve the closest marker corners
        Mat aruco_corners = corners.get(m);
        // Convert aruco_corners to 4x2 matrix
        MatOfPoint2f arucoCorners = new MatOfPoint2f(aruco_corners);
        org.opencv.core.Point[] arucoCornersArray = arucoCorners.toArray();
        double aruco_width = Math.sqrt(Math.pow(arucoCornersArray[1].x - arucoCornersArray[0].x,2) + Math.pow(arucoCornersArray[1].y - arucoCornersArray[0].y, 2));
        double aruco_height = Math.sqrt(Math.pow(arucoCornersArray[2].x - arucoCornersArray[1].x, 2) + Math.pow(arucoCornersArray[2].y - arucoCornersArray[1].y, 2));
        double aruco_area=aruco_width*aruco_height;

        cx = cx*0.07/aruco_width;
        cz=cz*0.07/aruco_height;
        Log.i(TAG, "aruco center(x,y):["+Double.valueOf(cx)+","+Double.valueOf(cz)+"]");
        Log.i(TAG, "aruco width and height: "+ Double.valueOf(aruco_width)+","+Double.valueOf(aruco_height)+"]");

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
        Mat warped = new Mat();
        for (MatOfPoint2f contour : contours) {
            double epsilon = 0.02 * Imgproc.arcLength(contour, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(contour, approx, epsilon, true);
            // find the top right corner
            if (approx.total() == 4) {
                org.opencv.core.Point[] cornersArray = approx.toArray();
                org.opencv.core.Point top_left = cornersArray[0];
                org.opencv.core.Point top_right = cornersArray[1];
                org.opencv.core.Point bottom_right = cornersArray[2];
                org.opencv.core.Point bottom_left = cornersArray[3];




                Log.i(TAG,"top_left"+destination+":"+String.valueOf(top_left.x)+","+String.valueOf(top_left.y));
                Log.i(TAG,"top_right"+destination+":"+String.valueOf(top_right.x)+","+String.valueOf(top_right.y));
                Log.i(TAG,"bottom_left"+destination+":"+String.valueOf(bottom_left.x)+","+String.valueOf(bottom_left.y));
                Log.i(TAG,"bottom_right"+destination+":"+String.valueOf(bottom_right.x)+","+String.valueOf(bottom_right.y));
                // Determine the width and height of the detected rectangle
                double width = Math.sqrt(Math.pow(top_right.x - top_left.x, 2) + Math.pow(top_right.y - top_left.y, 2));
                double height = Math.sqrt(Math.pow(top_right.x - bottom_right.x, 2) + Math.pow(top_right.y - bottom_right.y, 2));
                double contour_area=width*height;
                if (isArucoInsideRectangle(arucoCornersArray, cornersArray) &&  contour_area>2*aruco_area) {
                    //draw contour of paper outline
                    Mat imageContours = image.clone();
                    List<MatOfPoint> contourList1 = new ArrayList<>();
                    contourList1.add(new MatOfPoint(approx.toArray())); // Convert back to MatOfPoint
                    Imgproc.drawContours(imageContours, contourList1, -1, new Scalar(0, 255, 0), 3);
                    //api.saveMatImage(imageContours, "paper_outline"+destination+".png");
                    String paper_width_height="Paper dimension"+destination+"(wxh):"+width+","+height;
                    Log.i(TAG, paper_width_height);

                    // Create destination points for perspective transform
                    MatOfPoint2f dst_pts = new MatOfPoint2f(
                            new org.opencv.core.Point(0, 0),
                            new org.opencv.core.Point(width, 0),
                            new org.opencv.core.Point(width, height),
                            new org.opencv.core.Point(0, height));

                    // Get the perspective transform matrix
                    Mat M = Imgproc.getPerspectiveTransform(approx, dst_pts);
                    Imgproc.warpPerspective(image, warped, M, new org.opencv.core.Size(width, height));
                    if (width < height) {
                        Core.rotate(warped, warped, Core.ROTATE_90_CLOCKWISE);
                            }
                   // warped=extractPaperImage(image,cornersArray);
                    found = true;
                    break;
                }
            }
        }

        if (!found && !arucoCorners.empty()) {


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
            double height1 = Math.sqrt(Math.pow(top_right1.x - bottom_right1.x, 2) + Math.pow(top_right1.y - bottom_right1.y, 2));
            // Create destination points for perspective transform
            MatOfPoint2f dst_pts1 = new MatOfPoint2f(
                    new org.opencv.core.Point(0, 0),
                    new org.opencv.core.Point(width1, 0),
                    new org.opencv.core.Point(width1, height1),
                    new org.opencv.core.Point(0, height1));

            // Get the perspective transform matrix
            Mat M1 = Imgproc.getPerspectiveTransform(new_corners, dst_pts1);
            Imgproc.warpPerspective(image, warped, M1, new org.opencv.core.Size(width1, height1));
            if (width1 < height1) {
                Core.rotate(warped, warped, Core.ROTATE_90_CLOCKWISE);
            }
//            corners1[0]=top_left1;
//            corners1[1]=top_right1;
//            corners1[2]=bottom_right1;
//            corners1[3]=bottom_left1;
//
//            warped=extractPaperImage(image,corners1);
        }

        Log.i(TAG, "image channel:" + warped.channels());
        String warped_image = "warped" + destination + ".png";
        api.saveMatImage(warped, warped_image);


        // Use OpenCV model here
        Log.i(TAG, "TESTING OPENCV MOBILE");

        OpenCVModel cvModel = new OpenCVModel(this);
        Log.i(TAG, "LOADED OPENCV MOBILE, STARTING INFERENCES");
        Imgproc.cvtColor(mainImage, mainImage, Imgproc.COLOR_GRAY2BGR);
        PredictionResult predictionResult = cvModel.inference(mainImage, net);
        //Log.i(TAG, "prediction 0 " + pred);
        Log.i(TAG, "FINISHED INFERENCE");


        return predictionResult;
    }
}
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









