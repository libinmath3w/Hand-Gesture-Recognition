package tech.aksharam.handgesture.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import tech.aksharam.handgesture.R;

public class HomeActivity extends AppCompatActivity {

    CardView hand, object, objectfirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // hand = (CardView) findViewById(R.id.cardHand);
       // object = (CardView) findViewById(R.id.cardObject);
       // objectfirebase = (CardView)findViewById(R.id.cardObjectFirebase);


        hand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(HomeActivity.this, Camera2Activity.class);
                //startActivity(intent);
            }
        });

        object.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Intent intent = new Intent(HomeActivity.this, CameraActivity.class);
                //startActivity(intent);
            }
        });

        objectfirebase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(HomeActivity.this, RecognitionFirebaseActivity.class);
                //startActivity(intent);
            }
        });
    }
}
