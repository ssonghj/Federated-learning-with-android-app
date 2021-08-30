package com.example.androidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CompleteTag extends AppCompatActivity {
    SharedPreferences mmPref;
    ListView listView;
    private List str_list;
    ArrayList<ListData> listViewData = new ArrayList<>();
    HashMap<Uri,String> completeTagMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complete_tag);
        getSupportActionBar().setTitle("태그 완료 이미지들");

        Intent secondIntent = getIntent();
        completeTagMap = (HashMap<Uri, String>) secondIntent.getSerializableExtra("completeTagMap");

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

        //버튼 선언
        Button notCompleteTagBtn = (Button) findViewById(R.id.notCompleteTagBtn);
        Button modifyBtn = (Button) findViewById(R.id.modifyBtn);
        //미완료된 이미지 보는 버튼
        notCompleteTagBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //분류 미완료된 이미지를 보여주는 페이지로 이동
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                //완료된 정보들 넘기기
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
                try {
                    SaveCompleteTagMap(context, completeTagMap);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //완료 해쉬맵 sharedPreperence에 저장
    public void SaveCompleteTagMap(Context context, HashMap<Uri, String> ctMap) throws IOException, JSONException {
        SharedPreferences mmPref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        //arraylist 두 개에 key와 value 각각 삽입
        SharedPreferences.Editor editor = mmPref.edit();
        //key에 jsonKeyArray 저장
        JSONArray jsonKeyArray = new JSONArray();
        //value에 jsonValueArray 저장
        JSONArray jsonValueArray = new JSONArray();

        //ctMap 돌면서 key와 value 각 array에 저장
        for(Map.Entry<Uri,String> entry : ctMap.entrySet()) {
            jsonKeyArray.put(entry.getKey());
            jsonValueArray.put(entry.getValue());
        }

        if(!ctMap.isEmpty()){
            //shared preference에 저장하기
            editor.putString("jsonKey", jsonKeyArray.toString());
            editor.putString("jsonValue",jsonValueArray.toString());
            //적용하기
            editor.apply();
        }else{
            editor.putString("jsonKey", null);
            editor.putString("jsonValue",null);
            editor.apply();
        }
    }

}