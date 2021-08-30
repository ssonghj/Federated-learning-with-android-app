package com.example.androidapp;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CompleteListView extends BaseAdapter {
    LayoutInflater layoutInflater = null;
    private ArrayList<ListData> listViewData = null;
    private int count = 0;

    private Context context;
    private List list;
    private HashMap map;
    public CompleteListView(Context context, ArrayList<ListData> listData, List str_list, HashMap completeTagMap)
    {
        this.context = context;
        listViewData = listData;
        count = listViewData.size();
        list  = str_list;
        map = completeTagMap;
    }

    //아이템 개수 반환 함수
    @Override
    public int getCount()
    {
        return count;
    }

    //해당 위치 아이템 반환 함수
    @Override
    public Object getItem(int position)
    {
        return null;
    }

    //해당 위치 반환 함수
    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    // Adapter.getView() 해당위치 뷰 반환 함수
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final newViewHolder holder;

        String [] items = { "고양이", "강아지"};

        convertView=null;
        if (convertView == null) {
            holder = new newViewHolder();
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.custom_listview, parent, false);
            holder.textView = convertView.findViewById(R.id.AutoText);

            //자동완성 해쉬맵
            AutoCompleteTextView auto = convertView.findViewById(R.id.AutoText);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, items);
            auto.setAdapter(adapter);

            holder.textView.setTag(position);
            holder.textView.setText(list.get(position).toString());
            convertView.setTag(holder);
            System.out.println("초기화면 등장!");
        }
        else{
            System.out.println("두번째 화면  등장!");
            holder = (newViewHolder) convertView.getTag();
        }

        int tag_position=(Integer) holder.textView.getTag();
        holder.textView.setId(tag_position);

        holder.textView.addTextChangedListener(new TextWatcher() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                System.out.println("태그화면 등장!");
                int position2 = holder.textView.getId();
                TextView Caption = holder.textView;
                if(Caption.getText().toString().length()>0){
//                    System.out.println(position2);
//                    System.out.println(Caption.getText());
                    list.set(position2,Caption.getText().toString());

                    map.put(listViewData.get(position2).mainImage, Caption.getText().toString());
                    System.out.println("!?!??!?!?!?!11111111111111?!?!");
//                    for(Map.Entry<Uri, String> entry : map.entrySet()) {
//                        System.out.println("map키 : "+ entry.getKey()+" 값 : "+ entry.getValue());
//                    }
                    System.out.println("!?!??!?!?!?!?!222222222222?!");
                }else{
                    Toast.makeText(context, "Please enter some value", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });


        // 클릭 비활성화
        convertView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }});

        ImageView mainImage = convertView.findViewById(R.id.mainImage);
        mainImage.setImageURI(listViewData.get(position).mainImage);

        return convertView;
    }
}
class newViewHolder {
    TextView textView;
}