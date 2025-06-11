package jp.jaxa.iss.kibo.rpc.defaultapk;

import org.opencv.core.Mat;

import java.util.Arrays;

public class PredictionResult {
    public String[] labels;
    public int numObjects;
    public Mat originalBlob;
    public Mat blob;

    public PredictionResult(String[] labels, int numObjects,Mat originalBlob, Mat blob) {
        this.labels = labels;
        this.numObjects = numObjects;
        this.originalBlob = originalBlob;
        this.blob = blob;
    }

    public String[] getLabels() {
        return labels;
    }

    public int getNumObjects() {
        return numObjects;
    }

    public Mat getOriginalBlob() {
        return originalBlob;
    }
    public Mat getBlob() {
        return blob;
    }

    @Override
    public String toString() {
        return "Num Objects: " + numObjects + ", Labels: " + Arrays.toString(labels);
    }
}
