package com.example.androidapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.Toast;

import java.util.ArrayList;

import java.io.File;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private File file;
    ListView listView;
    private List str_list;
    int noComplete = 0;
    //데이터 만들기
//    List<String> items = new ArrayList<>();
    private View header;

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

        for (int i=0; i< list.length; i++) {
            System.out.println(list[i].getName());
        }

        listView = findViewById(R.id.listview);

        ArrayList<ListData> listViewData = new ArrayList<>();
        for (int i=0; i<list.length; ++i)
        {
            ListData listData = new ListData();
            listData.mainImage = Uri.fromFile(list[i]);
            listData.title = "태그 입력 : ";
            listViewData.add(listData);
        }

        str_list = new ArrayList<String>();
        for(int i=0;i<list.length;i++){
            str_list.add("");

        }

        ListAdapter oAdapter = new CustomListView(this, listViewData,str_list);
        listView.setAdapter(oAdapter);


        Button tagBtn = (Button) findViewById(R.id.tagBtn);
        Button strBtn = (Button) findViewById(R.id.strBtn);
        strBtn.setEnabled(false);

        tagBtn.setOnClickListener(new View.OnClickListener(){
            Context context = getApplicationContext();
            @Override
            public void onClick(View v){
                for(int i = 0; i<str_list.size();i++){
                    System.out.println(str_list.get(i));
                    if(str_list.get(i).toString() == ""){
                        noComplete = 1;
                    }
                }
                if(noComplete == 1){
                    Toast.makeText(context, "태그를 모두 입력해주세요", Toast.LENGTH_LONG).show();
                    noComplete = 0;
                }
                else{
                    //태그 입력 완료
                    strBtn.setEnabled(true);
                    //str_list에 입력 완료
                }
            }
        });


        strBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

            }
        });
    }

}