package com.hianzuo.logger.simple;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.hianzuo.logger.Log;
import com.hianzuo.logger.LogServiceHelper;

public class MainActivity extends AppCompatActivity {
    PermissionHandler.RequestResultScanner mRequestResultScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRequestResultScanner = PermissionHandler.request(this, new PermissionHandler.Callback() {
            @Override
            public void success() {
                LogServiceHelper.init(getApplication(), "/sdcard/hianzuo", "hianzuo_test_", 100, 1000);
                Toast.makeText(MainActivity.this, "成功", Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure() {
                Toast.makeText(MainActivity.this, "授权失败", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mRequestResultScanner.onResult(requestCode, permissions, grantResults);
    }

    public void onTestClick(View view) {
       /* for (int i = 0; i < 10; i++) {
            Log.d("MainActivity", "onTestClick " + i);
        }
        Log.d("MainActivity", "onTestClick ");

        Log.d("MainActivity", "onTestClick ");

        Log.d("MainActivity", "onTestClick ");*/
        Log.eStackTrace("MainActivity", Thread.currentThread().getStackTrace());
        Log.eThrowable("MainActivity", new RuntimeException("AAAAAAA"));
        Log.eLines("AAAAAAAAAA","aaaa\nbbbbb\nccccc\nddddd\neeeeee");
        Log.e("BBBBBB","msg",new RuntimeException("BBBBBBB"));
        LogServiceHelper.splitTime(5000L);
        LogServiceHelper.flush();

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
