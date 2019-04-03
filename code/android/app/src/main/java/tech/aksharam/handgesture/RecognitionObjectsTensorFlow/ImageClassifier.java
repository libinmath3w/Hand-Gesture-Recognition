package tech.aksharam.handgesture.RecognitionObjectsTensorFlow;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;
import org.tensorflow.lite.Interpreter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

public class ImageClassifier {

    private static final String TAG = "HandgestureRecognition";

    private static final String MODEL_PATH = "hand_graph.lite";

    private static final String LABEL_PATH = "graph_label_strings.txt";

    private static final int RESULTS_TO_SHOW = 5;

    private static final int DIM_BATCH_SIZE = 1;

    private static final int DIM_PIXEL_SIZE = 3;

    static final int DIM_IMG_SIZE_X = 224;

    static final int DIM_IMG_SIZE_Y = 224;

    private static final int IMAGE_MEAN = 128;

    private static final float IMAGE_STD = 128.0f;

    private int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

    private Interpreter tflite;

    private List<String> labelList;

    private ByteBuffer imgData = null;

    private float[][] labelProbArray = null;

    private float [][] filterLabelProbArray = null;

    private static final int FILTER_STAGES = 3;

    private static final float FILTER_FACTOR = 0.4f;

    private PriorityQueue<Map.Entry<String, Float>> storedLabels = new PriorityQueue<>(RESULTS_TO_SHOW, new Comparator<Map.Entry<String, Float>>() {
        @Override
        public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
            return (o1.getValue()).compareTo(o2.getValue());
        }
    });

    ImageClassifier (Activity activity) throws IOException {
        tflite = new Interpreter(loadModelFile(activity));
        labelList = loadLabelList(activity);
        imgData = ByteBuffer.allocateDirect(4 * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
        imgData.order(ByteOrder.nativeOrder());
        labelProbArray = new float[1][labelList.size()];
        filterLabelProbArray = new float[FILTER_STAGES][labelList.size()];
        Log.d(TAG,"Created a Tensorflow Lite image classifier.");

    }
    String classifyFrame (Bitmap bitmap) {
        long startTime = SystemClock.uptimeMillis();
        tflite.run(imgData,labelProbArray);
        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG,"Time cost to execute model inference: " + Long.toString(endTime - startTime));

        applyFilter();

        String textToShow = printTopKLabels();
        textToShow = Long.toString(endTime - startTime) + "ms" + textToShow;
        return textToShow;
    }

    void applyFilter() {
        int num_labels = labelList.size();

        for (int j=0;j<num_labels;++j){
            filterLabelProbArray[0][j] += FILTER_FACTOR*(labelProbArray[0][j] - filterLabelProbArray[0][j]);
        }
        for (int i=1;i<FILTER_STAGES;++i){
            for(int j=0; j<num_labels; ++j){
                filterLabelProbArray[i][j] += FILTER_FACTOR * (filterLabelProbArray[i-1][j] - filterLabelProbArray[i][j]);
            }
        }
        for (int j=0; j<num_labels; ++j){
            labelProbArray[0][j] = filterLabelProbArray[FILTER_STAGES-1][j];
        }
    }

    void close () {
        tflite.close();
        tflite = null;
    }

    private List<String> loadLabelList (Activity activity) throws IOException {
        List<String> labelList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(activity.getAssets().open(LABEL_PATH)));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }
private MappedByteBuffer loadModelFile (Activity activity) throws  IOException {
    AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_PATH);
    FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
    FileChannel fileChannel = inputStream.getChannel();
    long startoffset = fileDescriptor.getDeclaredLength();
    long declaredLength = fileDescriptor.getDeclaredLength();
    return fileChannel.map(FileChannel.MapMode.READ_ONLY,startoffset,declaredLength);

}
private void convertBitmapToByteBuffer (Bitmap bitmap){
        if(imgData == null)
            return;
        imgData.rewind();
        bitmap.getPixels(intValues,0, bitmap.getWidth(),0,0, bitmap.getWidth(),bitmap.getHeight());
        int pixel = 0;
        long startTime = SystemClock.uptimeMillis();
        for (int i = 0;i <DIM_IMG_SIZE_X; ++i) {
            for (int j = 0; j <DIM_IMG_SIZE_Y; ++j) {
                final int val = intValues[pixel++];
                imgData.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                imgData.putFloat((((val >> 8 )& 0xFF) - IMAGE_MEAN)/IMAGE_STD);
                imgData.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
            }
        }
long endTime = SystemClock.uptimeMillis();
        Log.d(TAG,"Time cost to put values in ByteBuffer" + Long.toString(endTime - startTime));

}


}
