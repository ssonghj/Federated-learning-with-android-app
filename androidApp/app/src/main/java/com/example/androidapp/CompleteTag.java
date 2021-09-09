package com.example.androidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
    int flag = 0;
    SharedPreferences mmPref;
    ListView listView;
    private List str_list;
    ArrayList<ListData> listViewData = new ArrayList<>();
    HashMap<Uri,String> completeTagMap;
    Button modifyBtn;
    Button notCompleteTagBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.complete_tag);
        //버튼 선언
        notCompleteTagBtn = (Button) findViewById(R.id.notCompleteTagBtn);
        modifyBtn = (Button) findViewById(R.id.modifyBtn);

        Intent secondIntent = getIntent();
        completeTagMap = (HashMap<Uri, String>) secondIntent.getSerializableExtra("completeTagMap");

        //리스트 뷰 가져오기
        listView = findViewById(R.id.listview);
        //태그 미완료된 해쉬만 넣음
        for(Map.Entry<Uri,String> entry : completeTagMap.entrySet()) {
            ListData listData = new ListData();
            //태그 입력 안된 이미지들만 넣기
            listData.mainImage = entry.getKey();
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


        //학습하기버튼
        modifyBtn.setOnClickListener(new View.OnClickListener(){
            Context context = getApplicationContext();
            @Override
            public void onClick(View v){
//                Toast.makeText(context, "태그가 수정되었습니다.", Toast.LENGTH_LONG).show();
                try {
                    if(modifyBtn.getText().equals("학습하기")){
                        SaveCompleteTagMap(context, completeTagMap);
                        showSelectModelDialog();
                    }
                    else if(modifyBtn.getText().equals("식별하기")){
                        Intent intent = new Intent(getApplicationContext(), Classification.class);
                        startActivity(intent);
                    }
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


    //모델 선택 다이얼로그
    void showSelectModelDialog()
    {
        //리스트 아이템 담을 리스트
        final List<String> ListItems = new ArrayList<>();
        ListItems.add("Inception v4");
        ListItems.add("ResNet");
        final CharSequence[] items =  ListItems.toArray(new String[ ListItems.size()]);

        final List SelectedItems  = new ArrayList();
        int defaultItem = 0;
        SelectedItems.add(defaultItem);

        //경고창 만들기
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //타이틀 만들기
        builder.setTitle("모델 선택");
        builder.setSingleChoiceItems(items, defaultItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SelectedItems.clear();
                        SelectedItems.add(which);
                    }
                });
        //다이얼로그 내의 ok 버튼
        builder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String msg="";

                        if (!SelectedItems.isEmpty()) {
                            int index = (int) SelectedItems.get(0);
                            msg = ListItems.get(index);
                        }
                        Toast.makeText(getApplicationContext(),
                                msg+"가 선택되었습니다." , Toast.LENGTH_LONG)
                                .show();
                        //버튼 text 바꾸기
                        modifyBtn.setText("식별하기");
                        //버튼 색 바꾸기
                        //modifyBtn.setBackgroundColor(0xffD1B6E1);
                    }
                });
        //다이얼로그 내의 cancel 버튼
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //뒤로가기
                    }
                });
        builder.show();
    }

}