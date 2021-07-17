package com.example.httpprac;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private HttpConnection httpConn = HttpConnection.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendData();
    }

    //웹 서버로 데이터 전송
    private void sendData(){
        //네트워크 통신 작업은 무조건 작업 스레드 생성해 호출
        new Thread(){
            public void run(){
                //파라미터 2개와 미리 정의해논 콜백함수를 매개변수로 전달해 호출
                httpConn.requestWebServer("데이터1","데이터2",callback);
            }
        }.start();
    }

    private  final Callback callback = new Callback(){
        @Override
        public void onFailure(Call call, IOException e){
            Log.d(TAG, "콜백오류:"+e.getMessage());
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException{
            String body = response.body().string();
            Log.d(TAG, "서버에서 응답한 Body"+body);
        }
    };
}