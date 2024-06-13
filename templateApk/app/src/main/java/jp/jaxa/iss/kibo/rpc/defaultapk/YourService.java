package jp.jaxa.iss.kibo.rpc.defaultapk;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import gov.nasa.arc.astrobee.Result;
import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.CvType;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import static java.lang.Thread.sleep;

import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.task.vision.classifier.Classifications;
import org.tensorflow.lite.task.vision.classifier.ImageClassifier;

import java.util.Arrays;
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
//    public Interpreter loadTflite() {
//        try {
//            Interpreter tflite = new Interpreter(loadModelFile());
//            Log.i(TAG, "FINISHED LOADING MODEL FILE NO ERRORS");
//            return tflite;
//        } catch (Exception ex) {
//            Log.i(TAG, "ERROR IN INITIALIZATION OF TFLITE INTERPRETER");
//            ex.printStackTrace();
//
//        }
//        return null;
//    }
    public interface ClassifierListener {
        void onError(String error);

        void onResults(List<Classifications> results, long inferenceTime);
    }
    private ImageClassifier imageClassifier;

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

//        int numPoints = 6;

//        ArrayList<Point> pointList = new ArrayList<Point>();
//        pointList.add(point);
//        pointList.add(point1);
//        pointList.add(point2);
//        pointList.add(point4);
//        pointList.add(point5);
//        pointList.add(point6);

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

        //Interpreter model = loadTflite();
        ImageClassifier model = loadModelFile();



        ArrayList<String> predictions = new ArrayList<String>();
        try {
            result = api.moveTo(point, quaternion, true);
            while (!result.hasSucceeded() && loopCounter < LOOP_MAX) {
                result = api.moveTo(point, quaternion, true);
                ++loopCounter;

            }
            api.flashlightControlFront(0.01f);
            Thread.sleep(2000);


            Bitmap image = api.getBitmapNavCam();
            api.flashlightControlFront(0.0f);
            if (image == null) {
                while (image == null && imgRetries < img_MAX) {
                    api.flashlightControlFront(0.01f);
                    Thread.sleep(2000);
                    image = api.getBitmapNavCam();
                    api.flashlightControlFront(0.0f);
                    imgRetries++;
                }
            } else {

                api.saveBitmapImage(image, "point.png");
            }
            Log.i(TAG, "ABOUT TO DO PREDICTIONS");
            predictions.add(doInference(image, model));

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
            api.flashlightControlFront(0.01f);
            Thread.sleep(2000);
            Mat image1 = api.getMatNavCam();
            api.flashlightControlFront(0.0f);
            if (image1 == null) {
                while (image1 == null && imgRetries < img_MAX) {
                    api.flashlightControlFront(0.01f);
                    Thread.sleep(2000);
                    image1 = api.getMatNavCam();
                    api.flashlightControlFront(0.0f);
                    imgRetries++;
                }
            } else {

                api.saveMatImage(image1, "point_1.png");
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
            api.flashlightControlFront(0.01f);
            Thread.sleep(2000);
            Mat image2 = api.getMatNavCam();
            api.flashlightControlFront(0.0f);
            if (image2 == null) {
                while (image2 == null && imgRetries < img_MAX) {
                    api.flashlightControlFront(0.01f);
                    Thread.sleep(2000);
                    image2 = api.getMatNavCam();
                    api.flashlightControlFront(0.0f);
                    imgRetries++;
                }
            } else {

                api.saveMatImage(image2, "point_2.png");
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
            api.flashlightControlFront(0.01f);
            Thread.sleep(2000);
            Mat image3 = api.getMatNavCam();
            api.flashlightControlFront(0.0f);
            if (image3 == null) {
                while (image3 == null && imgRetries < img_MAX) {
                    api.flashlightControlFront(0.01f);
                    Thread.sleep(2000);
                    image3 = api.getMatNavCam();
                    api.flashlightControlFront(0.0f);
                    imgRetries++;
                }
            } else {

                api.saveMatImage(image3, "point_3.png");
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

            api.flashlightControlFront(0.01f);
            Thread.sleep(2000);
            Mat target_item = api.getMatNavCam();
            api.flashlightControlFront(0.0f);
            if (target_item == null) {
                while (target_item == null && imgRetries < img_MAX) {
                    api.flashlightControlFront(0.01f);
                    Thread.sleep(2000);
                    target_item = api.getMatNavCam();
                    api.flashlightControlFront(0.0f);
                    imgRetries++;
                }
            } else {

                api.saveMatImage(target_item, "target_item.png");
            }

            for (String pred:predictions) {
                Log.i(TAG, pred);
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
    private void moveTo(Point point, Quaternion quaternion) throws InterruptedException {
        final int LOOP_MAX = 10;

        Log.i(TAG, "Moving to: " + point.getX() + ", " + point.getY() + ", " + point.getZ());
        long start = System.currentTimeMillis();

        Result result = api.moveTo(point, quaternion, true);

        api.flashlightControlFront(0.01f);
        Thread.sleep(5000);
        Mat image = api.getMatNavCam();
        api.flashlightControlFront(0.0f);
        api.saveMatImage(image, "ImageData_"+counter+".png");
        counter++;
        long end = System.currentTimeMillis();
        long elapsedTime = end - start;
        Log.i(TAG, "[0] moveTo finished in : " + elapsedTime/1000 + " seconds");
        Log.i(TAG, "[0] hasSucceeded : " + result.hasSucceeded());

        int loopCounter = 1;
        while (!result.hasSucceeded() && loopCounter <= LOOP_MAX) {

            Log.i(TAG, "[" + loopCounter + "] " + "Calling moveTo function");
            start = System.currentTimeMillis();

            result = api.moveTo(point, quaternion, true);

            end = System.currentTimeMillis();
            elapsedTime = end - start;
            Log.i(TAG, "[" + loopCounter + "] " + "moveTo finished in : " + elapsedTime / 1000 +
                    " seconds");
            Log.i(TAG, "[" + loopCounter + "] " + "hasSucceeded : " + result.hasSucceeded());

            loopCounter++;
        }
    }

    private void shiftXLeftRight(Point point, Quaternion quaternion, double increment) throws InterruptedException {
        moveTo(new Point(point.getX()+increment, point.getY(),point.getZ()),quaternion);
        api.moveTo(point, quaternion, true);
        moveTo(new Point(point.getX()-increment, point.getY(),point.getZ()),quaternion);
        api.moveTo(point, quaternion, true);
    }

    public void shiftYInOut(Point point, Quaternion quaternion, double increment) throws InterruptedException {
        moveTo(new Point(point.getX(), point.getY()+increment,point.getZ()),quaternion);
        api.moveTo(point, quaternion, true);
        moveTo(new Point(point.getX(), point.getY()-increment,point.getZ()),quaternion);
        api.moveTo(point, quaternion, true);

    }

    public void shiftZUpDown(Point point, Quaternion quaternion, double increment) throws InterruptedException {
        moveTo(new Point(point.getX(), point.getY(),point.getZ()+increment),quaternion);
        api.moveTo(point, quaternion, true);
        moveTo(new Point(point.getX(), point.getY(),point.getZ()-increment),quaternion);
        api.moveTo(point, quaternion, true);
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



    private ImageClassifier loadModelFile() {
        ImageClassifier imageClassifier;
        String modelName = "resnet.tflite";
        File modelFile = new File("app/src/main/assets/resnet.tflite");
        try {
            imageClassifier =
                    ImageClassifier.createFromFile(modelFile);
            return imageClassifier;
        } catch (IOException e) {
            Log.e(TAG, "TFLite failed to load model with error: "
                    + e.getMessage());
            return null;
        }
    }

    private String doInference(Bitmap image, ImageClassifier tflite) {
        if (tflite == null) {
            loadModelFile();
        }
        long inferenceTime = SystemClock.uptimeMillis();



        Log.i(TAG, "DO INFERENCE");
        String[] classes = {"beaker","google","hammer", "kapton-tape", "pipette", "screwdriver", "thermometer", "top", "watch", "wrench"};
        int index = 0;
        float[] output=new float[10];
        float max = output[0];


        //image.convertTo(image, CvType.CV_32F);
       // Imgproc.resize(image, image, size);
        Size size = new Size(224, 224);
        //Bitmap bitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        //int[] shape = {1, image.rows(), image.cols(), image.channels()};
        //TensorBuffer tensorBuffer = TensorBuffer.createFixedSize(shape, DataType.FLOAT32);
        TensorImage tensorImage;
        //tensorImage.load(bitmap);

        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                        .build();


        tensorImage = imageProcessor.process(TensorImage.fromBitmap(image));
        //int buff[] = new int[(int) (image.total() * image.channels())];
        //image.get(0, 0, buff);
        //tensorImage.load(buff);

        //TensorBuffer inputBuffer = TensorBuffer.createDynamic(DataType.FLOAT32);
        //float[] inputData = inputBuffer.getFloatArray();
        //image.get(0, 0, inputData);
        Log.i(TAG, String.valueOf(tensorImage.getHeight()));
        Log.i(TAG, String.valueOf(tensorImage.getWidth()));
        Log.i(TAG, String.valueOf(tensorImage.getDataType()));
        // Convert the Mat image to Bitmap
        //Utils.matToBitmap(image, bitmap);
        //TensorImage tensorImage = new TensorImage();
        //tensorImage.load(bitmap);
        tflite.classify(tensorImage);
        Log.i(TAG, "DONE INFERENCE");
        List<Classifications> result = tflite.classify(tensorImage);

        inferenceTime = SystemClock.uptimeMillis() - inferenceTime;

        Log.i(TAG, "INFERENCE TIME: "+inferenceTime);
//        tflite.run(tensorImage,output);
        Log.i(TAG, "RUN INFERENCES WITH MODEL");

        for (int i = 1; i<output.length; i++) {
            if (output[i]>max) {
                max = output[i];
                index = i;
            }
        }

        //Log.d(TAG, output[0]);
        return classes[index];
    }



}



