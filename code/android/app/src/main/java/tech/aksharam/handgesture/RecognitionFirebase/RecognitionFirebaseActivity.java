package tech.aksharam.handgesture.RecognitionFirebase;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import tech.aksharam.handgesture.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabelDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;

import dmax.dialog.SpotsDialog;
public class RecognitionFirebaseActivity extends AppCompatActivity {


    CameraView cameraView;
    Button btnDetect;
    AlertDialog waitingDialog;

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition_firebase);

        cameraView = (CameraView)findViewById(R.id.camera_view);
        btnDetect = (Button)findViewById(R.id.btn_detect);
        waitingDialog = new SpotsDialog.Builder().setContext(this).setMessage("Please wait...").setCancelable(false).build();

        FirebaseVisionLabelDetector detector = FirebaseVision.getInstance().getVisionLabelDetector();

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                waitingDialog.show();
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap,cameraView.getWidth(),cameraView.getHeight(),false);
                cameraView.stop();

                runDetector(bitmap);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.start();
                cameraView.captureImage();
            }
        });

    }

    private void runDetector(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        new InternetCheck(new InternetCheck.Consumer() {
            @Override
            public void accept(boolean internet) {
                if(internet){
                    FirebaseVisionCloudDetectorOptions options = new FirebaseVisionCloudDetectorOptions.Builder().setMaxResults(1).build();
                    FirebaseVisionCloudLabelDetector detector = FirebaseVision.getInstance().getVisionCloudLabelDetector(options);
                    detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLabel>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionCloudLabel> firebaseVisionCloudLabels) {
                            processDataResultCloud(firebaseVisionCloudLabels);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("error", "onFailure: "+e.getMessage());
                        }
                    });
                }else{
                    FirebaseVisionLabelDetectorOptions options = new FirebaseVisionLabelDetectorOptions.Builder().setConfidenceThreshold(0.8f).build();
                    FirebaseVisionLabelDetector detector = FirebaseVision.getInstance().getVisionLabelDetector(options);

                    detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionLabel>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionLabel> firebaseVisionLabels) {
                            processDataResult(firebaseVisionLabels);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("error", "onFailure: "+e.getMessage());
                        }
                    });

                }
            }
        });
    }

    private void processDataResultCloud(List<FirebaseVisionCloudLabel> firebaseVisionCloudLabels) {
        for(FirebaseVisionCloudLabel label : firebaseVisionCloudLabels){
            Toast.makeText(this, "Cloud Result: "+label.getLabel(), Toast.LENGTH_SHORT).show();
        }
        if(waitingDialog.isShowing())
            waitingDialog.dismiss();
    }

    private void processDataResult(List<FirebaseVisionLabel> firebaseVisionLabels) {
        for(FirebaseVisionLabel label : firebaseVisionLabels){
            Toast.makeText(this, "Result of the device: "+label.getLabel(), Toast.LENGTH_SHORT).show();
        }
        if(waitingDialog.isShowing())
            waitingDialog.dismiss();
    }
}

