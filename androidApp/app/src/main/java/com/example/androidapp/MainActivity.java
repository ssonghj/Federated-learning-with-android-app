package com.example.androidapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ListView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import java.util.ArrayList;

import java.io.File;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private File file;
    ListView listView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MODE_PRIVATE);

        String rootSD = Environment.getExternalStorageDirectory().toString() + "/Download/ani";

        //String rootSD = Environment.getExternalStorageDirectory().toString();
        file = new File( rootSD) ;
        File list[] = file.listFiles();

        List<String> filesNameList = new ArrayList<>();
        for (int i=0; i< list.length; i++) {
            System.out.println(list[i].getName());

        }

        listView = findViewById(R.id.listview);

        ArrayList<ListData> listViewData = new ArrayList<>();
        for (int i=0; i<list.length; ++i)
        {
//            Bitmap bm = BitmapFactory.decodeStream(list[i]);
            ListData listData = new ListData();
            listData.mainImage = Uri.fromFile(list[i]);
            listData.title = "라벨 입력 : ";

            listViewData.add(listData);
        }


        ListAdapter oAdapter = new CustomListView(listViewData);
        listView.setAdapter(oAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickName = listViewData.get(position).title;
                Log.d("확인","name : "+clickName);
            }
        });

    }
}