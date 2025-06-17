package jp.jaxa.iss.kibo.rpc.defaultapk;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.MatOfRect2d;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.DetectionModel;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//import static org.opencv.dnn.Dnn.blobFromImage;
//import static org.opencv.dnn.Dnn.readNetFromONNX;
import static org.opencv.imgproc.Imgproc.*;
import java.util.Set;
import java.util.HashSet;


public class OpenCVModel {
    private Context context;
    private String[] classes = {
            "coin",
            "compass",
            "coral",
            "crystal",
            "diamond",
            "emerald",
            "fossil",
            "key",
            "letter",
            "shell",
            "treasure_box"
    };
    List<Integer> classIds = new ArrayList<>();
    List<Float> confidences = new ArrayList<>();
    List<Rect2d> boxes = new ArrayList<>();
    float confThreshold = 0.5f; // Keep detections with confidence > 50%
    float nmsThreshold = 0.4f;
    int numClasses = classes.length;

    public OpenCVModel(Context context) {
        this.context = context;
    }

    public PredictionResult inference(Mat mat, Net net) {
        // Resize to 224x224
        Size size = new Size(640, 640);
        Mat blob = Dnn.blobFromImage(
                mat,
                1.0 / 255.0, // scalefactor
                new Size(640,640), // size
                new Scalar(0,0,0),
                //new Scalar(0.7653 * 255.0, 0.7653 * 255.0, 0.7653 * 255.0), // mean
                // TODO: Check this!!!
                true, // RGB->BGR
                false //crop
                //CvType.CV_32F
        );
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble std = new MatOfDouble();
        Core.meanStdDev(blob, mean, std);
        //Log.i("BLOB_STATS", "mean=" + mean.dump() + "   std=" + std.dump());

        //hello my name is jeff
        // Divide by standard dev
        //Mat normed = new Mat();
        //Core.divide(blob, Scalar.all(0.3056), normed);
        // Load the ONNX Model
        //Log.i("ROT", "Done preprocessing :)");

        //Log.i("ROT", "Loading ONNX model");
        try {


            //Log.i("ROT", "Model loaded, setting input");
            net.setInput(blob);

            //Log.i("ROT", "WE FORWARDING");
            Mat output = net.forward();

           // Log.i("ROT", "STARTING POSTPROCESSING");
            //Mat detections = output.get(0);  // shape: [1, 15, 8400]
            Mat detections = output;
            Mat reshaped = detections.reshape(1, 15);  // now shape is [15 rows × 8400 cols]

// 3) transpose into 8400×15
            Mat transposed = new Mat();
            Core.transpose(reshaped, transposed);

            //Log.i("detection shape: ", detections.toString());
            //Mat transposed = detections.reshape(0, detections.size(2));
  //          Log.i("transposed shape toString: ", transposed.toString());
//           Log.i("transposed shape: ", String.valueOf(transposed.size()));
            //Log.i("transposed row: ", String.valueOf(transposed.rows()));
            List<Rect2d> boxes = new ArrayList<>();
            List<Float> confidences = new ArrayList<>();
            List<Integer> classIds = new ArrayList<>();

            // --- STEP 2: LOOP through 8400 detections ---
            // expected shape (8400, 15)
            for (int i = 0; i < transposed.rows(); i++) {
                float[] detection = new float[15];
                transposed.get(i, 0, detection);
                //Log.i("ROT",Arrays.toString(detection));
                // Decode objectness confidence
//                float objConf = detection[4];
//                //Log.i("ROT","object confidence: "+objConf);
//                if (objConf < 0.5f) continue;  // early filter

                // Find max class score

                // TODO: Why is this -1, shouldn't this be -infinity
                float maxClassScore = -1f;
                int classId = -1;
                for (int j = 4; j < 15; j++) {
                    float classScore = detection[j];
                    //Log.i("ROT", "classScore: "+classScore+"for class "+j);
                    if (classScore > maxClassScore) {
                        maxClassScore = classScore;
                        classId = j - 4;
                    }
                }

                //Log.i("ROT", "max class score: "+maxClassScore);

                float finalConf = maxClassScore;
                //Log.i("ROT", "final confidence: "+finalConf);
                if (finalConf < 0.5f) continue;  // final confidence threshold

                float cx = detection[0];
                float cy = detection[1];
                float w = detection[2];
                float h = detection[3];

// Step 1: Convert from center-based to top-left
                float x = cx - w / 2f;
                float y = cy - h / 2f;

// Now use this rect for display or saving results
                boxes.add(new Rect2d(x, y, w, h));
                confidences.add(finalConf);
                classIds.add(classId);
            }

            //Log.i("ROT", "Starting NMS");
            //Log.i("ROT", boxes.toString());
           // Log.i("ROT", String.valueOf(boxes.size()));
           // Log.i("ROT", String.valueOf(confidences.size()));
           // Log.i("ROT", String.valueOf(classIds.size()));
            List<Rect2d> filteredBoxes = new ArrayList<>();
            List<Integer> filteredClassIds = new ArrayList<>();
            List<Float> filteredConfidences = new ArrayList<>();

// Unique set of classes
            Set<Integer> uniqueClassIds = new HashSet<>(classIds);

            for (int classId : uniqueClassIds) {
                List<Rect2d> classBoxes = new ArrayList<>();
                List<Float> classConfidences = new ArrayList<>();
                List<Integer> classIndices = new ArrayList<>();

                for (int i = 0; i < classIds.size(); i++) {
                    if (classIds.get(i) == classId) {
                        classBoxes.add(boxes.get(i));
                        classConfidences.add(confidences.get(i));
                        classIndices.add(i);  // Track original index
                    }
                }

                MatOfRect2d classBoxesMat = new MatOfRect2d();
                classBoxesMat.fromList(classBoxes);
                MatOfFloat classConfidencesMat = new MatOfFloat(Converters.vector_float_to_Mat(classConfidences));
                MatOfInt nmsIndices = new MatOfInt();

                Dnn.NMSBoxes(classBoxesMat, classConfidencesMat, 0.5f, 0.5f, nmsIndices);

                for (int i = 0; i < nmsIndices.rows(); i++) {
                    int localIdx = (int) nmsIndices.get(i, 0)[0];
                    int globalIdx = classIndices.get(localIdx);

                    filteredBoxes.add(boxes.get(globalIdx));
                    filteredConfidences.add(confidences.get(globalIdx));
                    filteredClassIds.add(classId);
                }
            }


            //Log.i("ROT", "AFTER FINAL FILTER");
            //Log.i("ROT", filteredBoxes.toString());
            //Log.i("ROT", String.valueOf(filteredBoxes.size()));
            //Log.i("ROT", String.valueOf(filteredConfidences.size()));
            //Log.i("ROT", String.valueOf(filteredClassIds.size()));
            // --- STEP 5: LOG DETECTIONS ---
            Log.i("ROT", "Detected " + filteredBoxes.size() + " objects:");
            String[] str_predictions = new String[filteredBoxes.size()];
            for (int i = 0; i < filteredBoxes.size(); i++) {
                Rect2d box = filteredBoxes.get(i);
                int classId = filteredClassIds.get(i);
                str_predictions[i] = classes[classId];
                float confidence = filteredConfidences.get(i);
                String msg = String.format("Object %d: Class %d with confidence %f at [x=%.1f, y=%.1f, w=%.1f, h=%.1f]",
                        i + 1, classId, confidence, box.x, box.y, box.width, box.height);
                Log.i("Bounding box", msg);
            }

            Mat visualization = mat.clone();  // or clone() if you need to preserve the original


            int origW = visualization.cols();
            int origH = visualization.rows();

// 3) Compute separate scale factors
            double scaleX = origW / 640.0;
            double scaleY = origH / 640.0;

// 4) Clone the original for drawing

// 5) Loop & draw, mapping each box back:
            for (int i = 0; i < filteredBoxes.size(); i++) {
                Rect2d b = filteredBoxes.get(i);
                float conf = filteredConfidences.get(i);
                String className = classes[filteredClassIds.get(i)];
                if (conf < 0.25f) continue;

                // map from 640×640 space → original space
                double x  = b.x * scaleX;
                double y  = b.y * scaleY;
                double w  = b.width  * scaleX;
                double h  = b.height * scaleY;

                // corners in original
                Point tl = new Point(x,       y);
                Point br = new Point(x + w,   y + h);

                // draw box
                Imgproc.rectangle(visualization, tl, br, new Scalar(0,255,0), 2);

                // draw confidence label (white text on green bg)
                String label = String.format("%s: %.2f", className, conf);;
                int[] baseLine = new int[1];
                Size ts = Imgproc.getTextSize(label, Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, 1, baseLine);
                double ty = y - 5 < ts.height ? y + ts.height + 5 : y - 5;
                Point bgTL = new Point(x, ty + baseLine[0]);
                Point bgBR = new Point(x + ts.width, ty - ts.height);
                Imgproc.rectangle(visualization, bgTL, bgBR, new Scalar(0,255,0), Core.FILLED);
                Imgproc.putText(visualization, label, new Point(x, ty),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.5,
                        new Scalar(0,0,255), 1);
            }



            Log.i("ROT", "POSTPROCESSING FINISHED");

            return new PredictionResult(str_predictions, filteredBoxes.size(), visualization);
        }
        catch (Exception e) {
            Log.i("ROT", e.getMessage());
            return null;
        }

    }




}