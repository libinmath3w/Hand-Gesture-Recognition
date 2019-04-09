package tech.aksharam.handgesture.RecognitionObjectsTensorFlow;

import android.app.Activity;
import android.os.Bundle;

import tech.aksharam.handgesture.R;
import tech.aksharam.handgesture.RecognitionObjects.Camera2BasicFragment;


public class Camera2Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, Camera2BasicFragment.newInstance())
                    .commit();
        }
    }
}