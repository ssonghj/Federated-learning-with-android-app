package flwr.android_client;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.TextureView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Classification extends AppCompatActivity {
    private TextureView mCameraTextureView;
    private ImageView mImageView;
    //private Preview mPreview;
    private TextView mtextView;
    Activity mainActivity = this;
    private TextView resultTxt;
    private ImageButton backBtn;

    static final int REQUEST_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.classification);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> {
            finish();//현재화면 끄기
        });

        resultTxt = findViewById(R.id.resultTxt);
        resultTxt.bringToFront();
        //카메라 뷰 선언
        //mCameraTextureView = findViewById(R.id.cameraTextureView);

        //이전 화면에서 값 가져오기
        Intent intent = getIntent();
//        String testImage = intent.getExtras().getString("testFile");
        String testImage = intent.getStringExtra("testUri");
        Uri uriImage = Uri.parse(testImage);



        String resultKey = intent.getExtras().getString("resultKey");
        float resultValue = intent.getExtras().getFloat("resultValue");



        //앨범에서 가져온 이미지 뷰 선언 -> 일단 임시 방편
        AssetManager am = getResources().getAssets() ;

        mImageView = findViewById(R.id.imageView);
        mImageView.setImageURI(uriImage);


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
