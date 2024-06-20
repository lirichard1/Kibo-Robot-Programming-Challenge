package jp.jaxa.iss.kibo.rpc.defaultapk;

import android.content.Context;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Dnn.*;
import org.opencv.core.Scalar;
import org.opencv.core.CvType.*;
import org.opencv.dnn.Net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.opencv.dnn.Dnn.blobFromImage;
import static org.opencv.dnn.Dnn.readNetFromONNX;
import static org.opencv.imgproc.Imgproc.*;


public class OpenCVModel {
    private Context context;
    private String[] classes = {
            "beaker",
            "goggle",
            "hammer",
            "kapton-tape",
            "pipette",
            "screwdriver",
            "thermometer",
            "top",
            "watch",
            "wrench"
    };
    public OpenCVModel(Context context) {
        this.context = context;
    }

    public String inference(Mat mat) {
        // Resize to 224x224
        Size size = new Size(224, 224);

        //Mat colored = new Mat();
        //cvtColor(mat, colored, COLOR_GRAY2RGB);

        Mat resized = new Mat();
        resize(mat, resized, size, 0, 0, INTER_AREA);

        Mat blob = blobFromImage(
                resized,
                1 / 255.0, // scalefactor
                size, // size
                //new Scalar(0.7653 * 255.0, 0.7653 * 255.0, 0.7653 * 255.0), // mean
                new Scalar(0.7726 * 255.0, 0.7726 * 255.0, 0.7726 * 255.0),
                false, // swaprb
                false, //crop
                CvType.CV_32F
        );

        // Divide by standard dev
        Mat normed = new Mat();
        Core.divide(blob, Scalar.all(0.3092), normed);

        // Load the ONNX Model
        Log.i("ROT", "Done preprocessing :)");

        Log.i("ROT", "Loading ONNX model");
        try {
            Net net = Dnn.readNetFromONNX(assetFilePath("resnet.onnx"));
            Log.i("ROT", "Model loaded, setting input");
            net.setInput(normed);

            Log.i("ROT", "WE FORWARDING");
            Mat output = net.forward();
            // Do argmax
            int argmax = 0;
            double maxval = -100;
            for (int i = 0; i < output.rows(); i++) {
                for (int j = 0; j < output.cols(); j++) {
                    double val = output.get(i, j)[0];
                    if (val > maxval) {
                        argmax = j;
                        maxval = val;
                    }
                }
            }
            Log.i("ROT", "WE ARGMAXING");
            return classes[argmax];
        }
        catch (Exception e) {
            Log.i("ROT", e.getMessage());

            return null;
        }

    }

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