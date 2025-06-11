package jp.jaxa.iss.kibo.rpc.defaultapk;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
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


import static org.opencv.dnn.Dnn.blobFromImage;
import static org.opencv.dnn.Dnn.readNetFromONNX;
import static org.opencv.imgproc.Imgproc.*;


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

    //    private String[] count_classes = {
//            "1",
//            "2",
//            "3",
//            "4",
//            "5",
//            "6",
//            "7"
//    };
    public OpenCVModel(Context context) {
        this.context = context;
    }

    public PredictionResult inference(Mat mat) {
        // Resize to 224x224
//        Size size = new Size(640, 640);
//
//        //Mat colored = new Mat();
//        //cvtColor(mat, colored, COLOR_GRAY2RGB);
//
//        Mat resized = new Mat();
//        resize(mat, resized, size, 0, 0, INTER_AREA);
        int origW = mat.cols();
        int origH = mat.rows();
        Mat mat_clone = mat.clone();

 //2) Compute scale
//        int targetSize = 640;
//        float scale = Math.min(targetSize / (float)origW, targetSize / (float)origH);
//        int newW = Math.round(origW * scale);
//        int newH = Math.round(origH * scale);
//
//// 3) Resize with INTER_AREA for downsampling (good quality)
//        Mat resized = new Mat();
//        Imgproc.resize(mat, resized, new Size(newW, newH), 0, 0, Imgproc.INTER_AREA);
//
//// 4) Compute padding
//        int padW = targetSize - newW;
//        int padH = targetSize - newH;
//        int padLeft   = padW / 2;
//        int padRight  = padW - padLeft;
//        int padTop    = padH / 2;
//        int padBottom = padH - padTop;
//
//// 5) Create letterbox canvas, filled with gray (114)
//        Mat inputBlob = new Mat(
//                new Size(targetSize, targetSize),
//                mat.type(),
//                new Scalar(114, 114, 114)
//        );
//
//// 6) Copy the resized image into the center
//        resized.copyTo(
//                inputBlob.submat(
//                        padTop, padTop + newH,
//                        padLeft, padLeft + newW
//                )
//        );

        Mat blob = blobFromImage(
                mat,
                1 / 255.0, // scalefactor
                new Size(640,640), // size
                new Scalar(0,0,0),
                //new Scalar(0.7653 * 255.0, 0.7653 * 255.0, 0.7653 * 255.0), // mean
                // TODO: Check this!!!
                false, // swaprb grayscale
                false //crop
                //CvType.CV_32F
        );

        // Divide by standard dev
        //Mat normed = new Mat();
        //Core.divide(blob, Scalar.all(0.3056), normed);
        // Load the ONNX Model
        Log.i("ROT", "Done preprocessing :)");

        Log.i("ROT", "Loading ONNX model");
        try {
            Net net = Dnn.readNetFromONNX(assetFilePath("my_model_50_updated.onnx"));
            Log.i("ROT", "Model loaded, setting input");
            net.setInput(blob);

            Log.i("ROT", "WE FORWARDING");
            List<Mat> output = new ArrayList<>();
            net.forward(output, net.getUnconnectedOutLayersNames());

//            int printRows = Math.min(5, mat.rows());
//            int printCols = Math.min(5, mat.cols());
//            Log.i("ROT", String.valueOf(output.size()));
//
//            for (int i = 0; i < printRows; i++) {
//                for (int j = 0; j < printCols; j++) {
//                    Log.i("ROT",Arrays.toString(mat.get(i, j)) + " ");
//
//                }
//                Log.i("ROT", "\n");
//            }
//            DetectionModel model = new DetectionModel(net);
//            model.setInputParams(1/255.0, new Size(640, 640), new Scalar(0), false);
//
//            MatOfRect boxes    = new MatOfRect();
//            MatOfFloat confidences = new MatOfFloat();
//            MatOfInt classIds  = new MatOfInt();
//
//            model.detect(resized, classIds, confidences, boxes, 0.5f /*confThres*/, 0.4f /*nmsThres*/);
//
//            List<Rect>  boxList = boxes.toList();
//            List<Float> confList = confidences.toList();
//            List<Integer> idList = classIds.toList();
//
//            for (int i = 0; i < boxList.size(); i++) {
//                Log.i("ROT", String.format("box=%s conf=%.2f class=%d",
//                        boxList.get(i).toString(),
//                        confList.get(i),
//                        idList.get(i)));
//            }
            Log.i("ROT", "STARTING POSTPROCESSING");
            Mat detections = output.get(0);  // shape: [1, 15, 8400]
            Log.i("ROT", "Original shape: " + detections.toString());
//
//            // --- STEP 1: TRANSPOSE from [1, 15, 8400] -> [1, 8400, 15] ---
//            List<Mat> channels = new ArrayList<>();
//            Core.split(detections, channels);  // Splits into 15 Mats of shape [1, 8400]
//
//            Mat transposed = new Mat();
//            Core.merge(channels, transposed);  // Now shape [1, 8400, 15]
//            transposed = transposed.reshape(1, 8400);  // Final shape: [8400 x 15]

//            Log.i("ROT", "Transposed & reshaped: " + transposed.toString());
            Mat transposed = detections.reshape(1,8400);

            List<Rect2d> boxes = new ArrayList<>();
            List<Float> confidences = new ArrayList<>();
            List<Integer> classIds = new ArrayList<>();

            // --- STEP 2: LOOP through 8400 detections ---
            // expected shape (8400, 15)
            for (int i = 0; i < transposed.rows(); i++) {
                float[] detection = new float[15];
                transposed.get(i, 0, detection);
                Log.i("ROT",Arrays.toString(detection));
                // Decode objectness confidence
                float objConf = detection[4];
                //Log.i("ROT","object confidence: "+objConf);
                if (objConf < 0.5f) continue;  // early filter

                // Find max class score

                // TODO: Why is this -1, shouldn't this be -infinity
                float maxClassScore = -1f;
                int classId = -1;
                for (int j = 5; j < 15; j++) {
                    float classScore = detection[j];
                    //Log.i("ROT", "classScore: "+classScore+"for class "+j);
                    if (classScore > maxClassScore) {
                        maxClassScore = classScore;
                        classId = j - 5;
                    }
                }
                Log.i("ROT", "class id: "+classId);
                Log.i("ROT", "objConf: "+objConf);
                Log.i("ROT", "max class score: "+maxClassScore);
                float finalConf = objConf * maxClassScore;
                Log.i("ROT", "final confidence: "+finalConf);
                if (finalConf < 0.5f) continue;  // final confidence threshold

                // Decode bounding box from [cx, cy, w, h] to [x, y, w, h]
//                double cx = detection[0] * 640;
//                double cy = detection[1] * 640;
//                double w = detection[2] * 640;
//                double h = detection[3] * 640;
//
//                double x = cx - w / 2.0;
//                double y = cy - h / 2.0;
//
//                boxes.add(new Rect2d(x, y, w, h));
//                float xmin = detection[0];
//                float xmax = detection[1];
//                float ymin = detection[2];
//                float ymax = detection[3];
//                float x_center = ((xmin + xmax) / 2) / 640;
//                float y_center = ((ymin + ymax) / 2) / 640;
//                float bbox_width = (xmax - xmin) / 640;
//                float bbox_height = (ymax - ymin) / 640;
//
//
//
//      // Convert to percentage (Label Studio uses percentages)
//                float x = xmin / 640 * 100;
//                float y = ymin / 640 * 100;
//                float w = (xmax - xmin) / 640 * 100;
//                float h = (ymax - ymin) / 640 * 100;
//                boxes.add(new Rect2d(x, y, w, h));
                float cx = detection[0];
                float cy = detection[1];
                float w = detection[2];
                float h = detection[3];

// Step 1: Convert from center-based to top-left
                float x = cx - w / 2f;
                float y = cy - h / 2f;

// Step 2: Convert from normalized (0–1) to input image size (640x640)
                x *= 640;
                y *= 640;
                w *= 640;
                h *= 640;
                cx *= 640;
                cy *= 640;

// Step 3: Undo padding
//                float x_unpad = (x - padLeft) / scale;
//                float y_unpad = (y - padTop) / scale;
//                float w_unpad = w / scale;
//                float h_unpad = h / scale;

// Step 4: Clamp to original image bounds (optional but recommended)
//                x_unpad = Math.max(0, Math.min(origW - 1, x_unpad));
//                y_unpad = Math.max(0, Math.min(origH - 1, y_unpad));
//                w_unpad = Math.max(0, Math.min(origW - x_unpad, w_unpad));
//                h_unpad = Math.max(0, Math.min(origH - y_unpad, h_unpad));

// Now use this rect for display or saving results
                boxes.add(new Rect2d(x, y, w, h));
                confidences.add(finalConf);
                classIds.add(classId);
            }

            Log.i("ROT", "Starting NMS");
            Log.i("ROT", boxes.toString());
            Log.i("ROT", String.valueOf(boxes.size()));
            Log.i("ROT", String.valueOf(confidences.size()));
            Log.i("ROT", String.valueOf(classIds.size()));
//            for (int k = 0;k<confidences.size();k++) {
//                Log.i("ROT", String.valueOf(confidences.get(k)));
//            }
//            for (int k = 0;k<classIds.size();k++) {
//                Log.i("ROT", String.valueOf(classIds.get(k)));
//            }

            // --- STEP 3: NMS ---
            MatOfRect2d boxesMat = new MatOfRect2d();
            boxesMat.fromList(boxes);
            MatOfFloat confidencesMat = new MatOfFloat(Converters.vector_float_to_Mat(confidences));
            MatOfInt indices = new MatOfInt();

            // TODO: Check if it matches the ultralytics settings
            Dnn.NMSBoxes(boxesMat, confidencesMat, 0.5f, 0.4f, indices);
            //--- STEP 4: Filter Final Results ---
            List<Rect2d> filteredBoxes = new ArrayList<>();
            List<Integer> filteredClassIds = new ArrayList<>();
            List<Float> filteredConfidences = new ArrayList<>();

            for (int i = 0; i < indices.rows(); i++) {
                int idx = (int) indices.get(i, 0)[0];
                filteredBoxes.add(boxes.get(idx));
                filteredClassIds.add(classIds.get(idx));
                filteredConfidences.add(confidences.get(idx));
            }
            Log.i("ROT", "AFTER FINAL FILTER");
            Log.i("ROT", filteredBoxes.toString());
            Log.i("ROT", String.valueOf(filteredBoxes.size()));
            Log.i("ROT", String.valueOf(filteredConfidences.size()));
            Log.i("ROT", String.valueOf(filteredClassIds.size()));
//            for (int k = 0;k<filteredConfidences.size();k++) {
//                Log.i("ROT", String.valueOf(filteredConfidences.get(k)));
//            }
//            for (int k = 0;k<filteredClassIds.size();k++) {
//                Log.i("ROT", String.valueOf(filteredClassIds.get(k)));
//            }
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
                Log.i("YOLOv11", msg);
            }

            Mat visualization = blob;  // or clone() if you need to preserve the original

// 2) Loop over detections
            for (int i = 0; i < filteredBoxes.size(); i++) {
                Rect2d box = filteredBoxes.get(i);
                int classId = filteredClassIds.get(i);
                float conf = filteredConfidences.get(i);

                // 2a) Draw rectangle
                Point topLeft = new Point(box.x, box.y);
                Point bottomRight = new Point(box.x + box.width, box.y + box.height);
                Imgproc.rectangle(visualization, topLeft, bottomRight, new Scalar(0, 255, 0), 2);

                // 2b) Prepare label text
                String label = String.format("%s: %.2f", classes[classId], conf);

                // 2c) Determine text position & draw
                int[] baseLine = new int[1];
                Size  textSize = Imgproc.getTextSize(label, Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, 1, baseLine);
                Point textOrg = new Point(
                        box.x,
                        box.y - 5 < 0 ? box.y + textSize.height + 5 : box.y - 5
                );
                Imgproc.putText(visualization, label, textOrg, Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255,255,255), 1);
            }

// 3) Save the visualization to disk
//    This will write a PNG or JPG (depending on extension) to the path you choose:


            Log.i("ROT","WE FINISHED BABY");



            //VISUALIZE PREDICTIONS
//            for (int i = 0; i < filteredBoxes.size(); i++) {
//                Rect2d box = filteredBoxes.get(i);
//                int classId = filteredClassIds.get(i);
//                float confidence = confidences.get(i);  // Make sure this list matches filtered indices
//
//                // Convert to int rectangle
//                Rect rect = new Rect(
//                        (int) box.x,
//                        (int) box.y,
//                        (int) box.width,
//                        (int) box.height
//                );
//
//                // Choose color (optional: different color per class)
//                Scalar boxColor = new Scalar(0, 255, 0);  // Green
//
//                // Draw bounding box
//                Imgproc.rectangle(resized, rect, boxColor, 2);
//
//                // Get class label from map
//                String className = (classId >= 0 && classId < classes.length) ? classes[classId] : "Unknown";
//                String label = className + " (" + String.format("%.2f", confidence) + ")";
//
//                // Text parameters
//                int font = Imgproc.FONT_HERSHEY_SIMPLEX;
//                double fontScale = 0.5;
//                int thickness = 1;
//
//                // Measure label size
//                Size labelSize = Imgproc.getTextSize(label, font, fontScale, thickness, null);
//                Point labelOrigin = new Point(rect.x, rect.y - 5);
//
//                // Background for label
//                Imgproc.rectangle(
//                        resized,
//                        new Point(labelOrigin.x, labelOrigin.y - labelSize.height),
//                        new Point(labelOrigin.x + labelSize.width, labelOrigin.y),
//                        boxColor,
//                        Core.FILLED
//                );
//
//                // Draw label text
//                Imgproc.putText(resized, label, labelOrigin, font, fontScale, new Scalar(0, 0, 0), thickness);
//            }
//            //Bitmap annotatedBitmap = Bitmap.createBitmap(resized.cols(), resized.rows(), Bitmap.Config.ARGB_8888);
//            //Utils.matToBitmap(resized, annotatedBitmap);
//            //String filename = "annotated_output.png";
//            //File path = new File(context.getExternalFilesDir(null), filename);  // or use getFilesDir() for internal
//            api.saveMatImage(resized, "annotated.png");
//            try (FileOutputStream out = new FileOutputStream(path)) {
//                annotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//                Log.i("SAVE", "Saved image to: " + path.getAbsolutePath());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

// Sigmoid function for converting logits to probabilities

//            Log.i("ROT","STARTING POSTPROCESSING");
//            Log.i("ROT",String.valueOf(output.size()));
//            Log.i("ROT",String.valueOf(output.get(0).size()));
//            Log.i("ROT",output.get(0).toString());
//            Mat detections = output.get(0);
//            int numDetections = detections.size(2);
//            Log.i("ROT", "numDetections: "+String.valueOf(numDetections));
//            Log.i("ROT","GOT STUDD FROM OUTPUTS");
//            Log.i("ROT",detections.toString());
//            //Log.i("ROT", String.valueOf(numDetections));
//            List<Rect2d> boxes = new ArrayList<>();
//            List<Float> confidences = new ArrayList<>();
//            List<Integer> classIds = new ArrayList<>();
//            detections = detections.reshape(1, 8400);
//            for (int i = 0; i < numDetections; i++) {
//                Log.i("ROT","in the detections loop!");
//                float[] detection = new float[6];
//                detections.get(i, 0, detection);
//                Log.i("ROT", Arrays.toString(detections.get(i, 0)));
//                float confidence = detection[4];
//                Log.i("ROT", String.valueOf(confidence));
//                if (confidence > 0.5f) {
//                    Log.i("ROT","passed confidence check, extracting values");
//                    double x = detection[0] * resized.cols();  // Use double for Rect2d
//                    double y = detection[1] * resized.rows();
//                    double w = detection[2] * resized.cols();
//                    double h = detection[3] * resized.rows();
//                    Log.i("ROT","adding values to lists");
//                    boxes.add(new Rect2d(x, y, w, h));
//                    confidences.add(confidence);
//                    classIds.add((int)detection[5]);
//                }
//            }
////            Log.i("ROT","processing masks now");
////            // Process masks [1, num_detections, maskH, maskW]
////            Mat masks = output.get(1);
////            Log.i("ROT",masks.toString());
////            Log.i("ROT", String.valueOf(masks.size()));
////            Log.i("ROT",String.valueOf(boxes.size()));
////            int maskH = masks.size(2);
////            int maskW = masks.size(3);
////            List<Mat> maskList = new ArrayList<>();
////
////            for (int i = 0; i < boxes.size(); i++) {  // Use boxes.size() to match filtered detections
////                Log.i("ROT","in for loop at iteration"+String.valueOf(i));
////                float[] maskData = new float[maskH * maskW];
////                masks.get(0, i, maskData);
////                Log.i("ROT","got mask");
////                Mat mask = new Mat(maskH, maskW, CvType.CV_32F);
////                Log.i("ROT","created new mask");
////                mask.put(0, 0, maskData);
////                Log.i("ROT","put new mask");
////                maskList.add(mask);
////                Log.i("ROT","added mask");
////            }
//            Log.i("ROT","Starting NMS");
//            // NMS
//            MatOfRect2d boxesMat = new MatOfRect2d();
//            boxesMat.fromList(boxes);
//            MatOfFloat confidencesMat = new MatOfFloat(Converters.vector_float_to_Mat(confidences));
//            MatOfInt indices = new MatOfInt();
//            Dnn.NMSBoxes(boxesMat, confidencesMat, 0.5f, 0.5f, indices);
//
//            // Filter results
//            Log.i("ROT","FILTER RESULTS");
//            List<Rect2d> filteredBoxes = new ArrayList<>();
//            //List<Mat> filteredMasks = new ArrayList<>();
//            List<Integer> filteredClassIds = new ArrayList<>();
//
//            for (int i = 0; i < indices.rows(); i++) {
//                Log.i("ROT", "in for loop");
//                int idx = (int)indices.get(i, 0)[0];
//                filteredBoxes.add(boxes.get(idx));
//                //filteredMasks.add(maskList.get(idx));
//                filteredClassIds.add(classIds.get(idx));
//            }
//
//            // Resize masks to original image size
////            Log.i("ROT","RESIZE MASKS");
////            for (int i = 0; i < filteredMasks.size(); i++) {
////                Mat resizedMask = new Mat();
////                Imgproc.resize(filteredMasks.get(i), resizedMask, new Size(resized.cols(), resized.rows()));
////                filteredMasks.set(i, resizedMask);
////            }
//
//            // --- FINAL RESULTS EXTRACTION ---
//            Log.i("ROT","Detected " + filteredBoxes.size() + " objects:");
//
//            for (int i = 0; i < filteredBoxes.size(); i++) {
//                Rect2d box = filteredBoxes.get(i);
//                int classId = filteredClassIds.get(i);
//
//                String logMessage = String.format(
//                        "Object %d: Class %d at [x=%.1f, y=%.1f, w=%.1f, h=%.1f]",
//                        i + 1, classId, box.x, box.y, box.width, box.height
//                );
//
//                Log.i("YOLOv11", logMessage);
//
//                // Optional: Get class name if you have a label map
//                // String className = classNames.get(classId);
//            }



//            Log.i("ROT", "TIME FOR POST PROCESSING");
//            Map<Integer, Integer> classCounts = getAccurateClassCounts((List<Mat>) output, confThreshold, nmsThreshold, numClasses);
//            Log.i("ROT", "WE POST PROCESSED");

            // Do argmax
//            int argmax = 0;
//            double maxval = -100;
//            for (int i = 0; i < output.rows(); i++) {
//                for (int j = 0; j < output.cols(); j++) {
//                    double val = output.get(i, j)[0];
//                    if (val > maxval) {
//                        argmax = j;
//                        maxval = val;
//                    }
//                }
//            }
//            Log.i("ROT", "WE ARGMAXING");
//            return classes[argmax];
            return new PredictionResult(str_predictions, filteredBoxes.size(), blob, visualization);
        }
        catch (Exception e) {
            Log.i("ROT", e.getMessage());
            return null;
        }

    }

    private float sigmoid(float x) {
        return (float)(1.0 / (1.0 + Math.exp(-x)));
    }


    public static Map<Integer, Integer> getAccurateClassCounts(
        List <Mat> outs,
        float confThreshold,
        float nmsThreshold, // Optional: set to 0 to disable NMS
        int numClasses) {

            // Temporary storage for NMS processing
        List<Integer> classIds = new ArrayList<>();
        List<Float> confidences = new ArrayList<>();
        List<Rect2d> boxes = new ArrayList<>(); // Still needed for NMS
        Log.i("ROT","WE IN TE FUNC");
        // Process outputs
        for (Mat out : outs) {
            float[] data = new float[(int)out.total()];
            out.get(0, 0, data);
            int numPredictions = out.rows();

            for (int i = 0; i < numPredictions; i++) {
                int offset = i * (5 + numClasses);
                float objectness = data[offset + 4];

                if (objectness >= confThreshold) {
                    // Find best class
                    int classId = -1;
                    float maxClassProb = 0;
                    for (int j = 0; j < numClasses; j++) {
                        float classProb = data[offset + 5 + j];
                        if (classProb > maxClassProb) {
                            maxClassProb = classProb;
                            classId = j;
                        }
                    }

                    float confidence = objectness * maxClassProb;
                    if (confidence >= confThreshold) {
                        classIds.add(classId);
                        confidences.add(confidence);

                        // Store dummy boxes (NMS needs these even if we don't use them)
                        boxes.add(new Rect2d(
                                data[offset], data[offset+1], // cx,cy
                                data[offset+2], data[offset+3] // w,h
                        ));
                    }
                }
            }
        }

        // Apply NMS if threshold > 0
        if (nmsThreshold > 0 && !boxes.isEmpty()) {
            MatOfRect2d boxesMat = new MatOfRect2d(boxes.toArray(new Rect2d[0]));
            MatOfFloat confidencesMat = new MatOfFloat(toFloatArray(confidences));
            MatOfInt indices = new MatOfInt();

            Dnn.NMSBoxes(boxesMat, confidencesMat, confThreshold, nmsThreshold, indices);

            // Filter results using NMS indices
            List<Integer> filteredClassIds = new ArrayList<>();
            for (int i = 0; i < indices.total(); i++) {
                int idx = (int)indices.get(i, 0)[0];
                filteredClassIds.add(classIds.get(idx));
            }
            classIds = filteredClassIds;
        }

        // Generate final counts
        Map<Integer, Integer> classCounts = new HashMap<>();
        for (int classId : classIds) {
            classCounts.put(classId, classCounts.getOrDefault(classId, 0) + 1);
        }
        for (Map.Entry<Integer, Integer> entry : classCounts.entrySet()) {
            Log.i("ROT","Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }

        return classCounts;
    }


    public static float[] toFloatArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

//    public String countObjects(Mat mat) {
//        // Resize to 224x224
//        Size size = new Size(224, 224);
//
//        //Mat colored = new Mat();
//        //cvtColor(mat, colored, COLOR_GRAY2RGB);
//
//        Mat resized = new Mat();
//        resize(mat, resized, size, 0, 0, INTER_AREA);
//
//        Mat blob = blobFromImage(
//                resized,
//                1 / 255.0, // scalefactor
//                size, // size
//                new Scalar(0.7794 * 255.0, 0.7794 * 255.0, 0.7794 * 255.0), // mean
//                false, // swaprb
//                false, //crop
//                CvType.CV_32F
//        );
//
//        // Divide by standard dev
//        Mat normed = new Mat();
//        Core.divide(blob, Scalar.all(0.3461), normed);
//
//        // Load the ONNX Model
//        Log.i("ROT", "Done preprocessing :)");
//
//        Log.i("ROT", "Loading ONNX model");
//        try {
//            Net net = Dnn.readNetFromONNX(assetFilePath("resnet_count.onnx"));
//            Log.i("ROT", "Model loaded, setting input");
//            net.setInput(normed);
//
//            Log.i("ROT", "WE FORWARDING");
//            Mat output = net.forward();
//            // Do argmax
//            int argmax = 0;
//            double maxval = -100;
//            for (int i = 0; i < output.rows(); i++) {
//                for (int j = 0; j < output.cols(); j++) {
//                    double val = output.get(i, j)[0];
//                    if (val > maxval) {
//                        argmax = j;
//                        maxval = val;
//                    }
//                }
//            }
//            Log.i("ROT", "WE ARGMAXING");
//            return count_classes[argmax];
//        }
//        catch (Exception e) {
//            Log.i("ROT", e.getMessage());
//
//            return null;
//        }
//
//    }

    public String assetFilePath(String assetName) throws IOException {
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
}