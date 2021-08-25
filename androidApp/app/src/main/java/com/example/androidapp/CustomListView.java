package com.example.androidapp;

import android.content.Context;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

public class CustomListView extends BaseAdapter {
    LayoutInflater layoutInflater = null;
    private ArrayList<ListData> listViewData = null;
    private int count = 0;

    private Context context;
    private List list;

    public CustomListView(Context context, ArrayList<ListData> listData, List str_list)
    {
        this.context = context;
        listViewData = listData;
        count = listViewData.size();
        list  = str_list;
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
        final ViewHolder holder;

        String [] items = { "고양이", "강아지"};

        convertView=null;
        if (convertView == null) {
            holder = new ViewHolder();
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.custom_listview, parent, false);
            holder.textView = convertView.findViewById(R.id.AutoText);

            AutoCompleteTextView auto = convertView.findViewById(R.id.AutoText);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, items);
            auto.setAdapter(adapter);

            holder.textView.setTag(position);
            holder.textView.setText(list.get(position).toString());
            convertView.setTag(holder);
            System.out.println("초기화면 등장!");
        }
        else{
            System.out.println("두번째 화면  등장!");
            holder = (ViewHolder) convertView.getTag();
        }

        int tag_position=(Integer) holder.textView.getTag();
        holder.textView.setId(tag_position);

        holder.textView.addTextChangedListener(new TextWatcher() {
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
        TextView title = convertView.findViewById(R.id.title);


        mainImage.setImageURI(listViewData.get(position).mainImage);
        title.setText(listViewData.get(position).title);

        return convertView;
    }
}
class ViewHolder {
    TextView textView;
}
class ListData {
    public String title = "";
    public Uri mainImage;
}