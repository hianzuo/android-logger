package com.hianzuo.logger.simple;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.hianzuo.logger.DeleteLogCallback;
import com.hianzuo.logger.Log;
import com.hianzuo.logger.LogServiceHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogServiceHelper.init(getApplication(), "/sdcard/hianzuo", "hianzuo_test_");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onTestClick(View view) {
        Log.d("MainActivity","onTestClick ");
        Log.deleteAll(new DeleteLogCallback() {
            @Override
            public void callback(String result) {
              Log.e("MainActivity", result);
            System.out.println("sssssssssss");
            }
        });
        Log.flush();
    }
}
