package com.example.poc;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";
    ArrayList<Uri> uriList = new ArrayList<>();     // 이미지의 uri를 담을 ArrayList 객체

    RecyclerView recyclerView;  // 이미지를 보여줄 리사이클러뷰
    MultiImageAdapter adapter;  // 리사이클러뷰에 적용시킬 어댑터
    Button btn_getImage;
    Button btn_start;

    int cnt = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_image);

        // 앨범으로 이동하는 버튼
        btn_getImage = findViewById(R.id.getImage);
        btn_getImage.setOnClickListener(new View.OnClickListener() {//image 버튼 클릭 반응
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 2222);
                cnt = 1;
            }
        });

        //시작 버튼 클릭시 서버 실행할 페이지로 이동해 서버 결과 출력
        btn_start = (Button) findViewById(R.id.start);
        btn_start.setOnClickListener(new Button.OnClickListener(){//start 버튼 클릭 반응
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), start_server.class);
                startActivity(intent);
            }
        });
        recyclerView = findViewById(R.id.recyclerView);
    }

    // 앨범에서 액티비티로 돌아온 후 실행되는 메서드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(cnt==1){
            btn_start.setEnabled(true);
            btn_getImage.setEnabled(false);

        }
        if(data == null){   // 어떤 이미지도 선택하지 않은 경우
            Toast.makeText(getApplicationContext(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show();
        }
        else{   // 이미지를 하나라도 선택한 경우
            int count = 3;
            if(data.getClipData() == null){     // 이미지를 하나만 선택한 경우
                Log.e("single choice: ", String.valueOf(data.getData()));
                Uri imageUri = data.getData();
                uriList.add(imageUri);

                adapter = new MultiImageAdapter(uriList, getApplicationContext());
                recyclerView.setAdapter(adapter);

                recyclerView.setLayoutManager(new GridLayoutManager(this,count));
            }
            else{      // 이미지를 여러장 선택한 경우
                ClipData clipData = data.getClipData();
                Log.e("clipData", String.valueOf(clipData.getItemCount()));

                Log.e(TAG, "multiple choice");

                for (int i = 0; i < clipData.getItemCount(); i++){
                    //System.out.println(clipData.getItemCount());->6출력
                    Uri imageUri = clipData.getItemAt(i).getUri();  // 선택한 이미지들의 uri를 가져온다.
                    try {
                        uriList.add(imageUri);  //uri를 list에 담는다.

                    } catch (Exception e) {
                        Log.e(TAG, "File select error", e);
                    }
                }

                adapter = new MultiImageAdapter(uriList, getApplicationContext());
                recyclerView.setAdapter(adapter);// 리사이클러뷰에 어댑터 세팅
                recyclerView.addItemDecoration(new RecyclerViewDecoration(20));
                recyclerView.setLayoutManager(new GridLayoutManager(this, count));

            }
        }
    }
}

