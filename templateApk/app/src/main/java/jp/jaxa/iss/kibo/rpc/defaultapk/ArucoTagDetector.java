//package jp.jaxa.iss.kibo.rpc.defaultapk;
//
//import android.util.Log;
//
//import org.opencv.aruco.Aruco;
//import org.opencv.aruco.DetectorParameters;
//import org.opencv.aruco.Dictionary;
//import org.opencv.core.Mat;
//
//import java.util.ArrayList;
//
//public class ArucoTagDetector {
//
//    final String TAG = "ROT";
//    /*
//
//     Returns an arraylist of the corners*/
//    public ArrayList<Mat> detect(Mat img) {
//        Dictionary dictionary= Aruco.getPredefinedDictionary(Aruco.DICT_5X5_250);//DetectorParameters parameters= DetectorParameters.create();//ArucoDetector detector= new ArucoDetector(dictionary,parameters);
//
//        Aruco detector = new Aruco();
//
//        ArrayList<Mat> corners = new ArrayList<Mat>();
//        Mat ids = new Mat();
//        detector.detectMarkers(img, dictionary, corners, ids);
//        return corners;
//    }
//}