package com.example.androidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CompleteTag extends AppCompatActivity {
    ListView listView;
    private List str_list;
    ArrayList<ListData> listViewData = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complete_tag);
        getSupportActionBar().setTitle("태그 완료 이미지들");

        Intent secondIntent = getIntent();
        HashMap<Uri,String> completeTagMap = (HashMap<Uri, String>) secondIntent.getSerializableExtra("completeTagMap");

//        System.out.println("----------------------!@!@!@!@!@!@!@!------------------");
//        for(Map.Entry<Uri,String> entry : completeTagMap.entrySet()) {
//            System.out.println("키 : "+ entry.getKey()+" 값 : "+ entry.getValue());
//        }
//        System.out.println("----------------------!@!@!@!@!@!@!@!------------------");

        //리스트 뷰 가져오기
        listView = findViewById(R.id.completeListview);
        //태그 미완료된 해쉬만 넣음
        for(Map.Entry<Uri,String> entry : completeTagMap.entrySet()) {
            ListData listData = new ListData();
            //태그 입력 안된 이미지들만 넣기
            listData.mainImage = entry.getKey();
            listData.title = "태그 입력 : ";
            listViewData.add(listData);
        }

        //리스트 초기화
        str_list = new ArrayList<String>();
        for(Map.Entry<Uri,String> entry : completeTagMap.entrySet()) {
            str_list.add(entry.getValue());
        }

        //리스트 어뎁터
        CompleteListView oAdapter = new CompleteListView(this, listViewData,str_list, completeTagMap);
        listView.setAdapter(oAdapter);



        Button notCompleteTagBtn = (Button) findViewById(R.id.notCompleteTagBtn);
        Button modifyBtn = (Button) findViewById(R.id.modifyBtn);
        //미완료된 이미지 보는 버튼
        notCompleteTagBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //분류 미완료된 이미지를 보여주는 페이지로 이동
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                //완료뙨 정보들 넘기기
                intent.putExtra("completeTagMap", completeTagMap);
                startActivity(intent);
            }
        });
        //태그 수정하는 버튼
        modifyBtn.setOnClickListener(new View.OnClickListener(){
            Context context = getApplicationContext();
            @Override
            public void onClick(View v){
                Toast.makeText(context, "태그가 수정되었습니다.", Toast.LENGTH_LONG).show();
                System.out.println("---------------수정 완료 해쉬맵-----------------");
                for(Map.Entry<Uri,String> entry : completeTagMap.entrySet()) {
                    System.out.println("키 : "+ entry.getKey()+" 값 : "+ entry.getValue());
                }
            }
        });
    }

}