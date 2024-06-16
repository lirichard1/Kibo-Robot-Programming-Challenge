// package jp.jaxa.iss.kibo.rpc.defaultapk;

// import android.content.Context;
// import android.graphics.Bitmap;
// import android.graphics.BitmapFactory;
// import android.os.Bundle;
// import android.util.Log;
// import android.widget.ImageView;
// import android.widget.TextView;

// import org.pytorch.IValue;
// import org.pytorch.LiteModuleLoader;
// import org.pytorch.MemoryFormat;
// import org.pytorch.Module;
// import org.pytorch.Tensor;
// import org.pytorch.torchvision.TensorImageUtils;

// import java.io.File;
// import java.io.FileOutputStream;
// import java.io.IOException;
// import java.io.InputStream;
// import java.io.OutputStream;
// import java.util.Arrays;

// public class PTMobile {
//     private Context context;
//     private Module module = null;
    // private String[] classes = {
    //     "beaker",
    //     "goggle",
    //     "hammer",
    //     "kapton-tape",
    //     "pipette",
    //     "screwdriver",
    //     "thermometer",
    //     "top",
    //     "watch",
    //     "wrench"
    // };
//     final String TAG = "ROT";
//     public PTMobile(Context context) {
//         this.context = context;
//         try {
//             // loading serialized torchscript module from packaged into app android asset model.pt,
//             // app/src/model/assets/model.pt
//             module = LiteModuleLoader.load(assetFilePath("resnet.ptl"));
//             Log.i(TAG,"MODEL FINISHED LOADING FROM ASSETS");
//             Log.i(TAG, String.valueOf(module));
//         } catch (IOException e) {
//             Log.e("PytorchMobile", "Error reading assets", e);
//         }
//     }

//     public String inference(Bitmap bitmap) {

//         // preparing input tensor
//         try {

//             final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
//                     bitmap,
//                     TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
//                     TensorImageUtils.TORCHVISION_NORM_STD_RGB,
//                     MemoryFormat.CHANNELS_LAST
//             );

//             Log.i("PytorchMobile", "Normalized input image");

//             // running the model
//             final Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();

//             Log.i("PytorchMobile", "Inferenced model");

//             // getting tensor content as java array of floats
//             final float[] scores = outputTensor.getDataAsFloatArray();
//             Log.i(TAG,"CONVERTED TENSOR TO FLOAT ARRAY");
//             // searching for the index with maximum score
//             float maxScore = -Float.MAX_VALUE;
//             int maxScoreIdx = -1;
//             for (int i = 0; i < scores.length; i++) {
//                 if (scores[i] > maxScore) {
//                     maxScore = scores[i];
//                     maxScoreIdx = i;
//                 }
//             }
//             return classes[maxScoreIdx];
//         } catch (Exception e) {
//             Log.e(TAG, e.getMessage());
//             return null;
//         }
//     }
// }