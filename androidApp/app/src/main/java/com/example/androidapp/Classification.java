package com.example.androidapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Classification extends AppCompatActivity {
    private TextureView mCameraTextureView;
    private ImageView mImageView;
    private Preview mPreview;
    private TextView mtextView;
    Activity mainActivity = this;

    static final int REQUEST_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.classification);

        //카메라 뷰 선언
        //mCameraTextureView = findViewById(R.id.cameraTextureView);

                                                                    /////////////////////////////
        //이전 화면에서 값 가져오기
        Intent intent = getIntent();
        String testImage = intent.getExtras().getString("testFile");
        String resultKey = intent.getExtras().getString("resultKey");
        float resultValue = intent.getExtras().getFloat("resultValue");

        //이미지 뷰 선언 -> 일단 임시 방편
        AssetManager am = getResources().getAssets() ;
        try {
            InputStream is = am.open(testImage) ;
            Bitmap bm = BitmapFactory.decodeStream(is) ;

            mImageView = findViewById(R.id.imageView);
            mImageView.setImageBitmap(bm) ;
        } catch (IOException e) {
            e.printStackTrace();
        }

        mtextView = findViewById(R.id.classificationTag);
        mtextView.setText(String.valueOf(resultKey)+" = "+String.valueOf(resultValue));


        //preview 객체 생성
        //mPreview = new Preview(this, mCameraTextureView);
    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case REQUEST_CAMERA:
//                for (int i = 0; i < permissions.length; i++) {
//                    String permission = permissions[i];
//                    int grantResult = grantResults[i];
//                    if (permission.equals(Manifest.permission.CAMERA)) {
//                        if(grantResult == PackageManager.PERMISSION_GRANTED) {
//                           // mCameraTextureView = findViewById(R.id.cameraTextureView);
//                            mPreview = new Preview(mainActivity, mCameraTextureView);
//                        } else {
//                            Toast.makeText(this,"Should have camera permission to run", Toast.LENGTH_LONG).show();
//                            finish();
//                        }
//                    }
//                }
//                break;
//        }
//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        mPreview.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        mPreview.onPause();
//    }
}