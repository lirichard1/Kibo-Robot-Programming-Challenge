package jp.jaxa.iss.kibo.rpc.defaultapk;

import android.content.res.AssetFileDescriptor;
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
        if (OpenCVLoader.initLocal()) {
            Log.i("OpenCV", "OpenCV loaded successfully");
        } else {
            Log.e("OpenCV", "OpenCV initialization failed!");
            return;
        }
    }
}

