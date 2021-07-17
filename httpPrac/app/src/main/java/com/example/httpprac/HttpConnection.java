package com.example.httpprac;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpConnection {
    private OkHttpClient client;
    private static HttpConnection instance = new HttpConnection();
    public static HttpConnection getInstance(){
        return instance;
    }

    //생성자는 private로 설정하고 getInstance()를 통해서 내부에서 단 한번만 객체 생성토록 하는
    //싱글톤 패턴 적용
    private HttpConnection(){this.client = new OkHttpClient();}

    /**웹 서버로 요청을 함.*/
    public void requestWebServer(String parameter, String parameter2, Callback callback){
        RequestBody body = new FormBody.Builder()
                .add("parameter",parameter)
                .add("parameter2",parameter2)
                .build();
        Request request = new Request.Builder()
                .url("https://www.naver.com")
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }
}
