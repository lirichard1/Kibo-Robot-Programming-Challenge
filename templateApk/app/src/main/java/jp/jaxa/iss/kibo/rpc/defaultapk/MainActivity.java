package jp.jaxa.iss.kibo.rpc.defaultapk;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity{

    private static final int REQUEST_PERMISSION = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Unable to load OpenCV!");
        } else {
            Log.i("OpenCV", "OpenCV loaded Successfully!");
        }
        checkPermissions();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("Permission", "Storage permission granted");
            } else {
                Log.e("Permission", "Storage permission denied");
            }
        }
    }

////added by wende
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        // Load images
//        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.beaker_template);
//        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.image2);
//
//        // Convert Bitmaps to Mat
//        Mat mat1 = new Mat();
//        org.opencv.android.Utils.bitmapToMat(bitmap1, mat1);
//        Mat mat2 = new Mat();
//        org.opencv.android.Utils.bitmapToMat(bitmap2, mat2);
//
//        // Process each image
//        processImage(mat1);
//        processImage(mat2);
//    }
}

