package com.example.androidapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.Toast;

import java.util.ArrayList;

import java.io.File;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private File file;
    ListView listView;
    private List str_list;
    ArrayList<ListData> listViewData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("태그 미완료 이미지들");
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MODE_PRIVATE);

        String rootSD = Environment.getExternalStorageDirectory().toString() + "/Download/ani";
        file = new File( rootSD) ;
        File list[] = file.listFiles();
        //데이터 로드 후 이름 출력
        for (int i=0; i< list.length; i++) {
//            System.out.println(list[i].getName());
            System.out.println(list[i]);
        }
        //리스트 뷰 가져오기
        listView = findViewById(R.id.listview);


        for (int i=0; i<list.length; ++i)
        {
            ListData listData = new ListData();
            listData.mainImage = Uri.fromFile(list[i]);
            listData.title = "태그 입력 : ";
            listViewData.add(listData);
        }

        //리스트 초기화
        str_list = new ArrayList<String>();
        for(int i=0;i<list.length;i++){
            str_list.add("");
        }

        //리스트 어뎁터
        CustomListView oAdapter = new CustomListView(this, listViewData,str_list);
        listView.setAdapter(oAdapter);

        //버튼들 선언
        Button tagBtn = (Button) findViewById(R.id.tagBtn);
        Button completeBtn = (Button) findViewById(R.id.completeBtn);
        completeBtn.setEnabled(true);

        tagBtn.setOnClickListener(new View.OnClickListener(){
            Context context = getApplicationContext();
            @Override
            public void onClick(View v){
                Toast.makeText(context, "태그 입력이 저장되었습니다.", Toast.LENGTH_LONG).show();
                //기존 리스트에서 태그 완료된 이미지 리스트로 옮기고 기존 리스트에서 삭제 후 리스트 뷰 갱신

                //shared preferences에 저장
                //HashMap에 key : 경로 - value : 태그 저장

                HashMap<File, Object> map = new HashMap<>();
                ArrayList<String> tmp= new ArrayList();

                for(int i = 0; i<list.length; i++){
                    if(str_list.get(i) != ""){ //태그 입력된 것만 출력됌
                        System.out.println("i : " + i + " | 이미지 경로 : "+ list[i]+ " | 태그 : " + str_list.get(i));
                        //경로 및 태그 저장 -> 순서 상관 없음
//                        map.put(list[i],str_list.get(i));
                        // 추가될 경우 리스트뷰에서 삭제
                        listViewData.remove(i);
                        System.out.println("??");

                        List str_list = new ArrayList<String>();
                        for(int j=0;j<listViewData.size();j++){
                            str_list.add("");
                            System.out.println("str_list.size : "+str_list.size());
                        }
                        CustomListView oAdapter = new CustomListView(context, listViewData,str_list);
                        listView.setAdapter(oAdapter);
                        System.out.println("!!");
                    }
                }
                oAdapter.notifyDataSetChanged();
            }
        });

        completeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //분류 완료된 이미지를 보여주는 페이지로 이동
                Intent intent = new Intent(getApplicationContext(), CompleteTag.class);
                startActivity(intent);
            }
        });
    }
}