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

import gov.nasa.arc.astrobee.Kinematics;

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

            Net net = Dnn.readNetFromONNX(assetFilePath("my_model_150.onnx", this));

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
            //Log.i(TAG, "FINISHED IMAGE RECOGNITION, START COUNT OBJECTS");
            //Log.i(TAG, "FINISHED COUNT OBJECTS");

            if (predictionResult != null) {
                predictions.add(strPreds);
                api.saveMatImage(predictionResult.getBlob(), "inference.png");
            } else {
                Log.i(TAG, "Prediction is null for area 1, guessing");
                api.setAreaInfo(1, "coin", 1);
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
                api.setAreaInfo(2, "coin", 2);
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
                api.setAreaInfo(3, "coin", 2);
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
                api.setAreaInfo(4, "coin", 2);
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

            goToPoint(astronaut, quaternion3);
            Kinematics kinematics = api.getRobotKinematics();
            Point Pos=kinematics.getPosition();
            Quaternion Quat = kinematics.getOrientation();
            Log.i(TAG, "Position - X: " + Pos.getX() + ", Y: " + Pos.getY() + ", Z: " + Pos.getZ());
            Log.i(TAG, "Orientation - X: " + Quat.getX() + ", Y: " + Quat.getY() + ", Z: " + Quat.getZ() + ", W: " + Quat.getW());
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

        org.opencv.core.Point[] c = arucoCorners.toArray();
        //Point[] c = arucoCorners.toArray();  // Assume: c[0] = top-left, c[1] = top-right, c[2] = bottom-right, c[3] = bottom-left

        double k0 = 2.0;
        double X0 = c[0].x + k0 * (c[1].x - c[0].x);
        double Y0 = c[0].y + k0 * (c[1].y - c[0].y);

        double ktr = 1.0;
        int Xtr = (int)(X0 + ktr * (c[1].x - c[2].x));
        int Ytr = (int)(Y0 + ktr * (c[1].y - c[2].y));

        double ktl = 8.0;
        int Xtl = (int)(Xtr + ktl * (c[0].x - c[1].x));
        int Ytl = (int)(Ytr + ktl * (c[0].y - c[1].y));

        double kbr = 5.0;
        int Xbr = (int)(Xtr + kbr * (c[2].x - c[1].x));
        int Ybr = (int)(Ytr + kbr * (c[2].y - c[1].y));

        double kbl = 8.0;
        int Xbl = (int)(Xbr + kbl * (c[0].x - c[1].x));
        int Ybl = (int)(Ybr + kbl * (c[0].y - c[1].y));

        int xmin = Math.max(0, Math.min(Math.min(Xtr, Xtl), Math.min(Xbl, Xbr)));
        int ymin = Math.max(0, Math.min(Math.min(Ytr, Ytl), Math.min(Ybl, Ybr)));
        int xmax = Math.min(image.cols() - 1, Math.max(Math.max(Xtr, Xtl), Math.max(Xbl, Xbr)));
        int ymax = Math.min(image.rows() - 1, Math.max(Math.max(Ytr, Ytl), Math.max(Ybl, Ybr)));

        Rect roi = new Rect(xmin, ymin, xmax - xmin, ymax - ymin);
        Mat warped = new Mat(image, roi);


        //Log.i(TAG, "image channel:" + warped.channels());
        String warped_image = "warped" + destination + ".png";
        api.saveMatImage(warped, warped_image);


        // Use OpenCV model here
        //.i(TAG, "TESTING OPENCV MOBILE");

        OpenCVModel cvModel = new OpenCVModel(this);
        Log.i(TAG, "LOADED OPENCV MOBILE, STARTING INFERENCES");
        //Imgproc.cvtColor(mainImage, mainImage, Imgproc.COLOR_GRAY2BGR);
        PredictionResult predictionResult = cvModel.inference(warped, net);
        //Log.i(TAG, "prediction 0 " + pred);
        Log.i(TAG, "FINISHED INFERENCE");


        return predictionResult;
    }
}






