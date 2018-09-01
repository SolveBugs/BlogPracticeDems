package com.test.progressviewwithcircle;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ProgressView commonProgressView1, commonProgressView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        commonProgressView1 = findViewById(R.id.progress1);
        commonProgressView1.setValue("30");
        commonProgressView1.setCurrentPercent(0.3f);


        commonProgressView2 = findViewById(R.id.progress2);
        commonProgressView2.setValue("60");
        commonProgressView2.setCurrentPercent(0.6f);
    }
}
