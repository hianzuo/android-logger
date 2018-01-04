package com.hianzuo.logger.simple;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.hianzuo.logger.Log;
import com.hianzuo.logger.LogServiceHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogServiceHelper.init(getApplication(), "/sdcard/hianzuo", "hianzuo_test_");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "onTestClick ");

        Log.d("MainActivity", "onTestClick ");

        Log.d("MainActivity", "onTestClick ");
    }

    public void onTestClick(View view) {
        for (int i = 0; i < 10000; i++) {
            Log.d("MainActivity", "onTestClick " + i);
        }
        LogServiceHelper.splitTime(5000L);

       /* Log.deleteAll(new DeleteLogCallback() {
            @Override
            public void callback(String result) {
                Log.d("MainActivity", result);
                System.out.println("sssssssssss");
            }
        });
        Log.flush();*/
    }
}
