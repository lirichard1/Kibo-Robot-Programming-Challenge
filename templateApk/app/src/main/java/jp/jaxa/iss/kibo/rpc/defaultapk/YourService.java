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

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;


import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;

import org.opencv.imgproc.Imgproc;
import org.opencv.calib3d.Calib3d;


import java.util.Arrays;

import java.util.List;

import org.opencv.objdetect.Objdetect;


import java.io.File;

import gov.nasa.arc.astrobee.Kinematics;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */


public class YourService extends KiboRpcService {
    final String
            TAG = "ROT",
            SIM = "SIMULATOR";


    double cx = 0;
    double cz = 0;

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
        final int img_MAX = 5;
        int imgRetries = 0;


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

        // optimizing for oasis
        Point pointx = new Point(11.123d, -9.5412d, 4.8427d);
        Point point1x = new Point(11.188d, -8.8894d, 5.3776d);
        Point point2x = new Point(10.681d, -7.9418d, 5.3735d);
        Point point3x = new Point(11.356d, -6.8569d, 4.7129d);

//        Point point = new Point(11.026d, -9.564d, 4.8945);
//        Point point1 = new Point(11.178d, -8.9585d, 5.3901d);
//        Point point2 = new Point(10.81d, -7.9477d, 5.3935d);
//        Point point3 = new Point(11.367d, -6.8833d, 4.8593d);


        Point astronaut = new Point(11.143d, -6.7607d, 4.9654d);


        Quaternion quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
        Quaternion quaternion1 = new Quaternion(0f, 0.707f, 0f, 0.707f);
        Quaternion quaternion2 = new Quaternion(0f, 0f, 1f, 0f);
        Quaternion quaternion3 = new Quaternion(0f, 0f, 0.707f, 0.707f);


        ArrayList<String[]> predictions = new ArrayList<String[]>();


        try {
            Net net = Dnn.readNetFromONNX(assetFilePath("my_model_150.onnx", this));
            goToPoint(pointx, quaternion);
            goToPoint(point, quaternion);


            api.flashlightControlFront(0.01f);


            Mat image = api.getMatNavCam();
            api.saveMatImage(image, "point.png");
            image.release();
            image = null;
            //Log.i(TAG, "Mat successful");
            api.flashlightControlFront(0.0f);
            if (image == null) {
                while (image == null && imgRetries < img_MAX) {
                    api.flashlightControlFront(0.5f);
                    image = api.getMatNavCam();
                    api.flashlightControlFront(0.0f);
                    imgRetries++;
                }
            }
            int destination = 0;
            Log.i(TAG, "ABOUT TO DO PREDICTIONS");
            PredictionResult predictionResult = processImage(image, net, destination);


            String[] strPreds = predictionResult.getLabels();
            Log.i("ROT", "PREDICTIONS FOR AREA 1: " + Arrays.toString(strPreds));
            int num_Objects = predictionResult.getNumObjects();
            api.saveMatImage(predictionResult.getBlob(), "inference.png");

            String finalPred = "coin";



            if (predictionResult != null) {
                predictions.add(strPreds);
                predictionResult = null;

            } else {
                Log.i(TAG, "Prediction is null for area 1, guessing");
                api.setAreaInfo(1, "coin", 2);
            }
            if (num_Objects > 0) {
                for (int i = 0; i < strPreds.length; i++) {
                    Log.i("ROT", "num_Objects>0");
                    if (strPreds[i].equals("crystal") || strPreds[i].equals("diamond") || strPreds[i].equals("emerald")) {
                        num_Objects--;
                        continue;
                    } else {
                        finalPred = strPreds[i];
                    }

                }
                api.setAreaInfo(1, finalPred, num_Objects);
            } else {
                for (int i = 0; i < strPreds.length; i++) {
                    Log.i("ROT", "num_Objects=0");
                    if (strPreds[i].equals("crystal") || strPreds[i].equals("diamond") || strPreds[i].equals("emerald")) {
                        continue;
                    }
                    api.setAreaInfo(1, strPreds[i], 2);

                }
            }

            strPreds = null;

            goToPoint(point1x, quaternion1);
            goToPoint(point1, quaternion1);
            api.flashlightControlFront(0.5f);

            Mat image1 = api.getMatNavCam();
            api.saveMatImage(image1, "point_1.png");
            image1.release();
            image1 = null;
            api.flashlightControlFront(0.0f);
            if (image1 == null) {
                while (image1 == null && imgRetries < img_MAX) {
                    api.flashlightControlFront(0.05f);

                    image1 = api.getMatNavCam();
                    api.flashlightControlFront(0.0f);
                    imgRetries++;
                }
            }


            destination++;
            PredictionResult predictionResult1 = processImage(image1, net, destination);
            String[] strPreds1 = predictionResult1.getLabels();
            Log.i("ROT", "PREDICTIONS FOR AREA 2: " + Arrays.toString(strPreds1));
            int numObjects1 = predictionResult1.getNumObjects();
            api.saveMatImage(predictionResult1.getBlob(), "inference1.png");
            String finalPred1 = "coin";
            if (predictionResult1 != null) {
                predictions.add(strPreds1);
                predictionResult1 = null;
            } else {
                Log.i(TAG, "Prediction is null for area 2, guessing");
                api.setAreaInfo(2, "coin", 2);
            }
            if (numObjects1 > 0) {
                for (int i = 0; i < strPreds1.length; i++) {
                    Log.i("ROT", "numObjects1>0");
                    if (strPreds1[i].equals("crystal") || strPreds1[i].equals("diamond") || strPreds1[i].equals("emerald")) {
                        numObjects1--;
                        continue;
                    } else {
                        finalPred1 = strPreds1[i];
                    }
                }
                api.setAreaInfo(2, finalPred1, numObjects1);
            } else {
                for (int i = 0; i < strPreds1.length; i++) {
                    Log.i("ROT", "numObjects1=0");
                    if (strPreds1[i].equals("crystal") || strPreds1[i].equals("diamond") || strPreds1[i].equals("emerald")) {
                        continue;
                    }
                    api.setAreaInfo(2, strPreds1[i], 3);
                }

            }

            strPreds1 = null;

            goToPoint(point2x, quaternion1);
            goToPoint(point2, quaternion1);

            api.flashlightControlFront(0.5f);
            Mat image2 = api.getMatNavCam();
            api.saveMatImage(image2, "point_2.png");
            image2.release();
            image2 = null;
            api.flashlightControlFront(0.0f);
            if (image2 == null) {
                while (image2 == null && imgRetries < img_MAX) {
                    api.flashlightControlFront(0.05f);
                    image2 = api.getMatNavCam();
                    api.flashlightControlFront(0.0f);
                    imgRetries++;
                }
            }

            destination++;
            PredictionResult predictionResult2 = processImage(image2, net, destination);
            String[] strPreds2 = predictionResult2.getLabels();
            Log.i("ROT", "PREDICTIONS FOR AREA 3: " + Arrays.toString(strPreds2));
            int numObjects2 = predictionResult2.getNumObjects();
            api.saveMatImage(predictionResult2.getBlob(), "inference2.png");
            String finalPred2 = "coin";
            if (predictionResult2 != null) {
                predictions.add(strPreds2);
                predictionResult2 = null;
            } else {
                Log.i(TAG, "Prediction is null for area 2, guessing");
                api.setAreaInfo(3, "coin", 2);
            }
            if (numObjects2 > 0) {
                for (int i = 0; i < strPreds2.length; i++) {
                    Log.i("ROT", "numObjects2>0");
                    if (strPreds2[i].equals("crystal") || strPreds2[i].equals("diamond") || strPreds2[i].equals("emerald")) {
                        numObjects2--;
                        continue;
                    } else {
                        finalPred2 = strPreds2[i];
                    }
                }
                api.setAreaInfo(3, finalPred2, numObjects2);
            } else {
                for (int i = 0; i < strPreds2.length; i++) {
                    Log.i("ROT", "numObjects2=0");
                    if (strPreds2[i].equals("crystal") || strPreds2[i].equals("diamond") || strPreds2[i].equals("emerald")) {
                        continue;
                    }
                    api.setAreaInfo(3, strPreds2[i], 3);
                }
            }

            strPreds2 = null;

            goToPoint(point3x, quaternion2);
            goToPoint(point3, quaternion2);

            api.flashlightControlFront(0.5f);
            Mat image3 = api.getMatNavCam();
            api.saveMatImage(image3, "point_3.png");
            image3.release();
            image3 = null;
            api.flashlightControlFront(0.0f);
            if (image3 == null) {
                while (image3 == null && imgRetries < img_MAX) {
                    api.flashlightControlFront(0.05f);
                    image3 = api.getMatNavCam();
                    api.flashlightControlFront(0.0f);
                    imgRetries++;
                }
            }


            destination++;
            PredictionResult predictionResult3 = processImage(image3, net, destination);
            String[] strPreds3 = predictionResult3.getLabels();
            int numObjects3 = predictionResult3.getNumObjects();
            api.saveMatImage(predictionResult3.getBlob(), "inference3.png");
            String finalPred3 = "coin";
            //int numObjects1 = countObjects(image1);
            if (predictionResult3 != null) {
                predictions.add(strPreds3);
                predictionResult3 = null;
            } else {
                Log.i(TAG, "Prediction is null for area 2, guessing");
                api.setAreaInfo(4, "coin", 2);
            }
            if (numObjects3 > 0) {
                for (int i = 0; i < strPreds3.length; i++) {
                    Log.i("ROT", "numObjects3>0");
                    if (strPreds3[i].equals("crystal") || strPreds3[i].equals("diamond") || strPreds3[i].equals("emerald")) {
                        numObjects3--;
                        continue;
                    } else {
                        finalPred3 = strPreds3[i];
                    }
                }
                api.setAreaInfo(4, finalPred3, numObjects3);
            } else {
                for (int i = 0; i < strPreds3.length; i++) {

                    if (strPreds3[i].equals("crystal") || strPreds3[i].equals("diamond") || strPreds3[i].equals("emerald")) {
                        continue;
                    }
                    api.setAreaInfo(4, strPreds3[i], 3);
                }
            }
            strPreds3 = null;

            goToPoint(astronaut, quaternion3);
//            Kinematics kinematics = api.getRobotKinematics();
//            Point Pos = kinematics.getPosition();
//            Quaternion Quat = kinematics.getOrientation();
//            Log.i(TAG, "Position - X: " + Pos.getX() + ", Y: " + Pos.getY() + ", Z: " + Pos.getZ());
//            Log.i(TAG, "Orientation - X: " + Quat.getX() + ", Y: " + Quat.getY() + ", Z: " + Quat.getZ() + ", W: " + Quat.getW());
            api.reportRoundingCompletion();

            api.flashlightControlFront(0.5f);
            //Thread.sleep(2000);
            Mat target_item = api.getMatNavCam();
            api.saveMatImage(target_item, "target_item.png");
            target_item.release();
            target_item = null;
            api.flashlightControlFront(0.0f);
            if (target_item == null) {
                Log.i("ROT","target item is null");
                while (target_item == null && imgRetries < img_MAX) {
                    api.flashlightControlFront(0.05f);
                    //Thread.sleep(2000);
                    target_item = api.getMatNavCam();
                    api.flashlightControlFront(0.0f);
                    imgRetries++;
                }
            }
            destination++;
            PredictionResult targetResult = processImage(target_item, net, destination);
            api.saveMatImage(targetResult.getBlob(), "inference_target.png");
            api.notifyRecognitionItem();
            Log.i("ROT", "targetClass:" + Arrays.toString(targetResult.getLabels()));
            Log.i("ROT", "Predictions Array is");
            Log.i("ROT", TextUtils.join(", ", predictions));
            String targetItem = "diamond"; //default value
            for (String item : targetResult.getLabels()) {
                if (item.equals("crystal") || item.equals("diamond") || item.equals("emerald")) {
                    targetItem = item;
                }
            }
            targetResult = null;
            Log.i("TARGET ITEM: ", targetItem);
            int found_target = 0;
            for (int i = 0; i < predictions.size(); i++) {
                for (String prediction : predictions.get(i)) {
                    if (prediction.equals(targetItem)) {
                        found_target = 1;
                        Log.i("ROT", "Going to target at" + String.valueOf(i));
                        if (i == 0) {

                            //goToPoint(point2x, quaternion);
                            //goToPoint(point1x, quaternion);
                            goToPoint(point, quaternion);
                            api.takeTargetItemSnapshot();


                        } else if (i == 1) {

                            //goToPoint(point2x, quaternion1);
                            //goToPoint(point1x, quaternion1);
                            goToPoint(point1, quaternion1);
                            api.takeTargetItemSnapshot();

                        } else if (i == 2) {

                            //goToPoint(point2x, quaternion1);
                            goToPoint(point2, quaternion1);
                            api.takeTargetItemSnapshot();

                        } else if (i == 3) {
                            goToPoint(point3, quaternion2);
                            api.takeTargetItemSnapshot();
                        }
                    }
                }

            }
            if (found_target == 0) {
                goToPoint(point3, quaternion2);
                api.takeTargetItemSnapshot();
            }
            Log.i("ROT", "SAVING FINAL IMAGES");
            api.saveMatImage(api.getMatNavCam(), "final_image.png");











            api.shutdownFactory();


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


    public PredictionResult processImage(Mat gray, Net net, int destination) {
        //       String[] classNames = {"beaker", "goggle", "hammer", "kapton-tape", "pipette", "screwdriver", "thermometer", "top", "watch", "wrench"};
//        File folder = new File(folderPath);
//        File[] files = folder.listFiles();

        //double[][] intrinsics = api.getNavCamIntrinsics();
        double[] cameraMatrixArray = {523.105750, 0.000000, 635.434258,0.000000, 534.765913,
                500.335102,0.000000, 0.000000, 1.000000};
        double[] distortionCoefficientsArray = {-0.164787, 0.020375, -0.001572, -0.000369, 0.000000};

        Mat cameraMatrix = new Mat(3, 3, CvType.CV_64F);
        cameraMatrix.put(0, 0, cameraMatrixArray);

        Mat distortionCoefficients = new Mat(1, distortionCoefficientsArray.length, CvType.CV_64F);
        distortionCoefficients.put(0, 0, distortionCoefficientsArray);

        // Load your input image (replace with actual image loading code)


        // Undistort the image
        Mat gray_undistorted = new Mat();
        Calib3d.undistort(gray, gray_undistorted, cameraMatrix, distortionCoefficients);
        gray.release();
        gray = null;
        cameraMatrix.release();
        cameraMatrix = null;
        distortionCoefficients.release();
        distortionCoefficients = null;
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
                cx = centerX - 640;
                cz = centerY - 480;

            }
        }

        // Retrieve the closest marker corners
        Mat aruco_corners = corners.get(m);
        // Convert aruco_corners to 4x2 matrix
        MatOfPoint2f arucoCorners = new MatOfPoint2f(aruco_corners);
        ids.release();
        image_markers.release();
       aruco_corners.release();
       aruco_corners = null;

       ids = null;
       image_markers = null;

        org.opencv.core.Point[] c = arucoCorners.toArray();
        //Point[] c = arucoCorners.toArray();  // Assume: c[0] = top-left, c[1] = top-right, c[2] = bottom-right, c[3] = bottom-left

        double k0 = 2.0;
        double X0 = c[0].x + k0 * (c[1].x - c[0].x);
        double Y0 = c[0].y + k0 * (c[1].y - c[0].y);

        double ktr = 1.0;
        int Xtr = (int) (X0 + ktr * (c[1].x - c[2].x));
        int Ytr = (int) (Y0 + ktr * (c[1].y - c[2].y));

        double ktl = 8.0;
        int Xtl = (int) (Xtr + ktl * (c[0].x - c[1].x));
        int Ytl = (int) (Ytr + ktl * (c[0].y - c[1].y));

        double kbr = 5.0;
        int Xbr = (int) (Xtr + kbr * (c[2].x - c[1].x));
        int Ybr = (int) (Ytr + kbr * (c[2].y - c[1].y));

        double kbl = 8.0;
        int Xbl = (int) (Xbr + kbl * (c[0].x - c[1].x));
        int Ybl = (int) (Ybr + kbl * (c[0].y - c[1].y));

        int xmin = Math.max(0, Math.min(Math.min(Xtr, Xtl), Math.min(Xbl, Xbr)));
        int ymin = Math.max(0, Math.min(Math.min(Ytr, Ytl), Math.min(Ybl, Ybr)));
        int xmax = Math.min(image.cols() - 1, Math.max(Math.max(Xtr, Xtl), Math.max(Xbl, Xbr)));
        int ymax = Math.min(image.rows() - 1, Math.max(Math.max(Ytr, Ytl), Math.max(Ybl, Ybr)));

        Rect roi = new Rect(xmin, ymin, xmax - xmin, ymax - ymin);
        Mat warped = new Mat(image, roi);
        image.release();
        image = null;

        //Log.i(TAG, "image channel:" + warped.channels());
        String warped_image = "warped" + destination + ".png";
        api.saveMatImage(warped, warped_image);


        // Use OpenCV model here
        //.i(TAG, "TESTING OPENCV MOBILE");

        OpenCVModel cvModel = new OpenCVModel(this);
        Log.i(TAG, "LOADED OPENCV MOBILE, STARTING INFERENCES");
        //Imgproc.cvtColor(mainImage, mainImage, Imgproc.COLOR_GRAY2BGR);
        PredictionResult predictionResult = cvModel.inference(warped, net);
        warped.release();
        warped = null;
        //Log.i(TAG, "prediction 0 " + pred);
        Log.i(TAG, "FINISHED INFERENCE");


        return predictionResult;
    }
}






