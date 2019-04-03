package tech.aksharam.handgesture.RecognitionObjectsTensorFlow;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;
import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

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
}
