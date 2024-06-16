// package jp.jaxa.iss.kibo.rpc.defaultapk;

// import ai.onnxruntime.NodeInfo;
// import ai.onnxruntime.OnnxTensor;
// import ai.onnxruntime.OrtEnvironment;
// import ai.onnxruntime.OrtException;
// import ai.onnxruntime.OrtSession;
// import ai.onnxruntime.OrtSession.Result;
// import ai.onnxruntime.OrtSession.SessionOptions;
// import ai.onnxruntime.OrtSession.SessionOptions.OptLevel;

// public class ORTModel {
//     public ORTModel(Bitmap bmp) {
//         OrtEnvironment env = OrtEnvironment.getEnvironment();
//         try (OrtSession.SessionOptions opts = new SessionOptions()) {

//             opts.setOptimizationLevel(OptLevel.BASIC_OPT);

//             Bitmap scaledBmp = Bitmap.createScaledBitmap(bmp, 224, 244, false);

//             logger.info("Loading model from " + args[0]);
//             try (OrtSession session = env.createSession(args[0], opts)) {
//                 SparseData data = load(args[1]);

//                 float[][][][] testData = new float[1][1][28][28];
//                 float[][] testDataSKL = new float[1][780];

//                 int correctCount = 0;
//                 int[][] confusionMatrix = new int[10][10];

//                 String inputName = session.getInputNames().iterator().next();

//                 for (int i = 0; i < data.labels.length; i++) {
//                 if (args.length == 3) {
//                     writeDataSKL(testDataSKL, data.indices.get(i), data.values.get(i));
//                 } else {
//                     writeData(testData, data.indices.get(i), data.values.get(i));
//                 }

//                 try (OnnxTensor test =
//                         OnnxTensor.createTensor(env, args.length == 3 ? testDataSKL : testData);
//                     Result output = session.run(Collections.singletonMap(inputName, test))) {

//                     int predLabel;

//                     if (args.length == 3) {
//                         long[] labels = (long[]) output.get(0).getValue();
//                         predLabel = (int) labels[0];
//                     } else {
//                         float[][] outputProbs = (float[][]) output.get(0).getValue();
//                         predLabel = pred(outputProbs[0]);
//                     }
//                     if (predLabel == data.labels[i]) {
//                         correctCount++;
//                     }

//                     confusionMatrix[data.labels[i]][predLabel]++;
//                     }
//                 }
//                 }

//                 logger.info("Final accuracy = " + ((float) correctCount) / data.labels.length);

//                 StringBuilder sb = new StringBuilder();
//                 sb.append("Label");
//                 for (int i = 0; i < confusionMatrix.length; i++) {
//                 sb.append(String.format("%1$5s", "" + i));
//                 }
//                 sb.append("\n");

//                 for (int i = 0; i < confusionMatrix.length; i++) {
//                 sb.append(String.format("%1$5s", "" + i));
//                 for (int j = 0; j < confusionMatrix[i].length; j++) {
//                     sb.append(String.format("%1$5s", "" + confusionMatrix[i][j]));
//                 }
//                 sb.append("\n");
//                 }

//                 System.out.println(sb.toString());
//             }
//             }

//             logger.info("Done!");
//         }
//     }
// }