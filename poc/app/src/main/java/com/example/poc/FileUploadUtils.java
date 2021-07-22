package com.example.poc;

import android.util.Log;

import java.io.File;
import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FileUploadUtils {
    private static String responseString;

    public static void goSend(File file, String upload_url, OkHttpClient client) {
        //api 받아오는 부분
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", file.getName(), RequestBody.create(MultipartBody.FORM, file))
                .build();
        //api 요청
        Request request = new Request.Builder()
                .url(upload_url)
                .post(requestBody)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //응답 성공시
        if (response.isSuccessful()) {
            ResponseBody body = response.body();
            if (body != null) {
                try {
                    responseString = body.string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else
                Log.d("INFO", "Connect Error Occurred");

            if (!responseString.equals("file_success")) {
                Log.d("WARNING", "file not upload to server!!");
            }
        }
        //닫기
        response.body().close();
    }
}
