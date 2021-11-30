package flwr.android_client;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;//
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends AppCompatActivity {
    //앱 시작시 shared preference 첫 실행했는지 세는 cnt
    static int countOfSharedpreference = 0;
    private File file;
    ListView listView;
    private List str_list;
    ArrayList<flwr.android_client.ListData> listViewData = new ArrayList<>();
    //태그 미완료 해쉬맵 - 순서 보장
    ConcurrentHashMap<Uri, String> notTagMap = new ConcurrentHashMap<>();
    //태그 완료 해쉬맵
    HashMap<Uri, String> completeTagMap = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MODE_PRIVATE);

        //처음들어왔을때 한번만 저장된 completeMap 로드하기
        if(countOfSharedpreference == 0){
            try {
                completeTagMap = LoadCompleteTagMapInDevice();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            countOfSharedpreference++;
        }

        //이전 화면에서 값 가져오기
        Intent secondIntent = getIntent();
        //이전화면에서 불러온 값이 null이 아니라면 완료 해쉬맵 불러오기
        if(secondIntent.getSerializableExtra("completeTagMap") != null){
            completeTagMap = (HashMap<Uri, String>) secondIntent.getSerializableExtra("completeTagMap");
        }


        //저장소에서 파일 불러오기
        String rootSD = Environment.getExternalStorageDirectory().toString() + "/Download/data";
        file = new File(rootSD);
        //System.out.println(rootSD);
        //파일 저장 리스트
        File list[] = file.listFiles();
        //데이터 로드 후 파일 이름 출력
        for (int i=0; i< list.length; i++) {
            Uri file = Uri.fromFile(list[i]);
            //불러온 파일의 태그값이 완료맵에 없으면 태그 미완료 해쉬맵에 저장하고 있으면 태그완료 해쉬맵에 저장
            for(Map.Entry<Uri, String> elem : completeTagMap.entrySet()){
                //불러온 키값이 list[i]에 없으면 미완료 해쉬에 넣기
                if( (elem.getKey() != file) ){
                    notTagMap.put(file,"");
                }
            }
            //불러온 키값이 완료맵에 없으면 새로 들어온 이미지 이므로 미완료 해쉬맵에 넣어야함
            if(!completeTagMap.containsKey(file)){
                notTagMap.put(file,"");
            }
            //불러온 키 값이 완료맵에 있었으면 미완료 해쉬맵에서 지우기
            else{
                notTagMap.remove(file);
            }
        }

        //-----------------------------------------------------------------------------------
        System.out.println("---------------미완료 해쉬맵-----------------");
        for(Map.Entry<Uri,String> entry : notTagMap.entrySet()) {
            System.out.println("키 : "+ entry.getKey()+" 값 : "+ entry.getValue());
        }
        System.out.println("---------------완료 해쉬맵-----------------");
        for(Map.Entry<Uri,String> entry : completeTagMap.entrySet()) {
            System.out.println("키 : "+ entry.getKey()+" 값 : "+ entry.getValue());
        }
        //----------------------------------------------------------------------정상적으로 미완료 해쉬에 삽입


        //리스트 뷰 가져오기
        listView = findViewById(R.id.listview);
        //태그 미완료된 해쉬만 넣음
        for(Map.Entry<Uri,String> entry : notTagMap.entrySet()) {
            flwr.android_client.ListData listData = new flwr.android_client.ListData();
            listData.mainImage = entry.getKey();
            listViewData.add(listData);
        }

        //리스트 초기화
        str_list = new ArrayList<String>();
        for(int i=0;i<notTagMap.size();i++){
            str_list.add("");
        }

        //리스트 어뎁터
        flwr.android_client.CustomListView oAdapter = new flwr.android_client.CustomListView(this, listViewData,str_list, notTagMap);
        listView.setAdapter(oAdapter);

        //버튼들 선언
        Button completeBtn = (Button) findViewById(R.id.completeBtn);
        completeBtn.setEnabled(true);
        //태그 입력 완료 페이지로 넘어가서 보여주는 버튼
        completeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                showDialogForGoToCompletetagPage();
            }
        });
    }

    void showDialogForGoToCompletetagPage() {
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(
                MainActivity.this)
                .setTitle("주의")
                .setMessage("분류를 완료 하셨나요?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), "입력이 저장되었습니다.", Toast.LENGTH_SHORT).show();

                        //기존 리스트에서 태그 완료된 이미지 리스트로 옮기고 기존 리스트에서 삭제 후 리스트 뷰 갱신
                        Iterator<Map.Entry<Uri, String>> entries = notTagMap.entrySet().iterator();
                        while(entries.hasNext()){
                            Map.Entry<Uri, String> entry = entries.next();
                            //만약 키의 벨류 값이 비어있지 않다면
                            if(entry.getValue() != ""){
                                //태그 완료된 곳에 넣어주고
                                completeTagMap.put(entry.getKey(),entry.getValue());
                                notTagMap.remove(entry.getKey());
                            }
                        }

                        try {
                            saveCompleteTagMapInDevice(getApplicationContext(), completeTagMap);
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }

                        //새로 입력해서 태그 완료 해쉬맵에 넣어주고 새로운 어댑터에 넣어주기
                        listViewData = new ArrayList<>();
                        for(Map.Entry<Uri,String> entry : notTagMap.entrySet()) {
                            flwr.android_client.ListData listData = new flwr.android_client.ListData();
                            //태그 입력 안된 이미지들만 넣기
                            listData.mainImage = entry.getKey();
                            listViewData.add(listData);
                        }
                        List str_list = new ArrayList<String>();
                        for(int j=0;j<notTagMap.size();j++){
                            str_list.add("");
                        }

                        //새로 어댑터 만들어서 넣어줌
                        flwr.android_client.CustomListView adapter = new flwr.android_client.CustomListView(getApplicationContext(), listViewData,str_list,notTagMap);
                        listView.setAdapter(adapter);

                        //분류 완료된 이미지를 보여주는 페이지로 이동
                        Intent intent = new Intent(getApplicationContext(), flwr.android_client.CompleteTag.class);
                        intent.putExtra("completeTagMap", completeTagMap);
                        startActivity(intent);
                    } })

                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this, "취소", Toast.LENGTH_SHORT).show();
                    } });
        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }


    public void saveCompleteTagMapInDevice(Context context, HashMap<Uri, String> ctMap) throws IOException, JSONException {
        SharedPreferences mmPref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mmPref.edit();
        JSONArray saveJsonKeyArray = new JSONArray();
        JSONArray saveJsonValueArray = new JSONArray();

        //ctMap 돌면서 key와 value 각 array에 저장
        for(Map.Entry<Uri,String> entry : ctMap.entrySet()) {
            saveJsonKeyArray.put(entry.getKey());
            saveJsonValueArray.put(entry.getValue());
        }

        if(!ctMap.isEmpty()){
            //shared preference에 저장하기
            editor.putString("jsonKey", saveJsonKeyArray.toString());
            editor.putString("jsonValue",saveJsonValueArray.toString());
            //적용하기
            editor.apply();
        }else{
            editor.putString("jsonKey", null);
            editor.putString("jsonValue",null);
            editor.apply();
        }
    }

    public HashMap<Uri,String> LoadCompleteTagMapInDevice() throws IOException, ClassNotFoundException {
        SharedPreferences mmPref = getSharedPreferences("pref", Context.MODE_PRIVATE);

        //저장된 key,value json값 가져오기 -> 값 없으면 null
        String json = mmPref.getString("jsonKey", null);
        String json2 = mmPref.getString("jsonValue", null);

        if(json != null){
            try{
                JSONArray keyArray = new JSONArray(json);
                JSONArray valueArray = new JSONArray(json2);

                for(int i = 0; i < keyArray.length(); i++){
                    String key = keyArray.optString(i);
                    String value = valueArray.optString(i);
                    completeTagMap.put(Uri.parse(key),value);
                }
            }catch(JSONException e){
                System.out.println("잘못되었습니다.");
            }
        }
        return completeTagMap;
    }
}
