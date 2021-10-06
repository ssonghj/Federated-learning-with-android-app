package com.example.androidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.task.vision.classifier.Classifications;
import org.tensorflow.lite.task.vision.classifier.ImageClassifier;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.tensorflow.lite.Interpreter;

import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;


public class CompleteTag extends AppCompatActivity {
    int flag = 0;
    SharedPreferences mmPref;
    ListView listView;
    private List str_list;
    ArrayList<ListData> listViewData = new ArrayList<>();
    HashMap<Uri,String> completeTagMap;
    Button modifyBtn;
    Button notCompleteTagBtn;

    String key;
    Float value;

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
            @Override
            public void onClick(View v){
                Context context = getApplicationContext();
//                Toast.makeText(context, "태그가 수정되었습니다.", Toast.LENGTH_LONG).show();
                try {
                    if(modifyBtn.getText().equals("학습하기")){
                        SaveCompleteTagMap(context, completeTagMap);

                        //팝업 띄워서 모델 이름 받기
                        //String tfliteFile = showSelectModelDialog();

                        //일단 inception으로 진행
                        //String tfliteFile = "inception_v4.tflite";

                        //inception v3 transfer learning 재학습 시켜서 모델만든 걸로 진행
                        String tfliteFile = "model.tflite";

                        //딥러닝 모델 파일 가져오기
                        AssetFileDescriptor fileDescriptor = getResources().getAssets().openFd(tfliteFile);
                        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
                        FileChannel fileChannel = inputStream.getChannel();
                        long startOffset = fileDescriptor.getStartOffset();
                        long declaredLength = fileDescriptor.getDeclaredLength();
                        MappedByteBuffer tflite_model = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);

                        //input 배열
                        AssetManager am = getAssets();
                        BufferedInputStream buf;
                        //inception v4는 1,299,299,3 , model은 224,224       // 1 1 3 height, width, channel
                        float[][][][] input = new float[1][299][299][3];
                        //inception v4는 1,1001 , model은 1,6
                        float[][] output = new float[1][1001];

                        //테스트 사진
                        String testFile = "acne.jpeg";

                        try {
                            //테스트 사진 bitmap 변환
                            buf = new BufferedInputStream(am.open(testFile));
                            Bitmap bitmap = BitmapFactory.decodeStream(buf);
                            bitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);

                            //이미지 전처리
                            ImageProcessor imageProcessor =
                                    new ImageProcessor.Builder()
                                            .add(new ResizeOp(1, 1, ResizeOp.ResizeMethod.BILINEAR))
                                            .build();
                            TensorImage tImage = new TensorImage(DataType.UINT8);
                            tImage.load(bitmap);
                            tImage = imageProcessor.process(tImage);

                            //FLOAT32임.... NEURON APP? 확인해보면
                            TensorBuffer probabilityBuffer =
                                    TensorBuffer.createFixedSize(new int[]{1,6}, DataType.FLOAT32);

                            // Initialise the model
                            Interpreter tflite = null;
                            try{
                                MappedByteBuffer tfliteModel
                                        = FileUtil.loadMappedFile(getApplicationContext(),
                                        "model.tflite");
                                tflite = new Interpreter(tfliteModel);
                            } catch (IOException e){
                                Log.e("tfliteSupport", "Error reading model", e);
                            }

                            // Running inference
                            if(null != tflite) {
                                System.out.println("학습시작");
                                tflite.run(tImage.getBuffer(), probabilityBuffer.getBuffer());
                                System.out.println("학습 끝");
                            }


                            final String ASSOCIATED_AXIS_LABELS = "output_labels.txt";
                            List associatedAxisLabels = null;

                            try {
                                associatedAxisLabels = FileUtil.loadLabels(context,ASSOCIATED_AXIS_LABELS);
                            } catch (IOException e) {
                                Log.e("tfliteSupport", "Error reading label file", e);
                            }

                            TensorProcessor probabilityProcessor =
                                    new TensorProcessor.Builder().add(new NormalizeOp(0, 255)).build();

                            if (null != associatedAxisLabels) {
                                // Map of labels and their corresponding probability
                                TensorLabel labels = new TensorLabel(associatedAxisLabels,
                                        probabilityProcessor.process(probabilityBuffer));

                                // Create a map to access the result based on label
                                Map floatMap = labels.getMapWithFloatValue();

                                //맵에 들어온 값 한번 확인해 보는 용도
                                for(Object entry : floatMap.entrySet()) {
                                    System.out.println("map키 : "+ entry);
                                }

                                //가장 큰값 찾기
                                HashMap <String, Float> result = new LinkedHashMap<>();
                                for(Object entry : floatMap.entrySet()) {
                                    String[] s = entry.toString().split("=");
                                    result.put(s[0], Float.parseFloat(s[1]));
                                }

                                float max = 0.0F;
                                for(Map.Entry<String, Float> entry : result.entrySet()) {
                                    if(entry.getValue() > max){
                                        max = entry.getValue();
                                    }
                                }

                                //가장 큰 값만 분류 결과 화면에 넘길 수 있도록 key, value 저장
                                for (Map.Entry<String, Float> entry : result.entrySet()) {
                                    if (entry.getValue().equals(max)) {
                                        System.out.println("key : "+entry.getKey()+" value : "+entry.getValue());
                                        key = entry.getKey();
                                        value = entry.getValue();
                                    }
                                }


                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }



                        //결과 페이지로 넘어가기
                        Intent intent = new Intent(getApplicationContext(), Classification.class);
                        //테스트한 이미지 파일 넘기기
                        intent.putExtra("testFile",testFile);
                        //결괏값 넘기기
                        intent.putExtra("resultKey", key);
                        intent.putExtra("resultValue",value);
                        startActivity(intent);
                    }

                    //식별하기 버튼
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
    public String showSelectModelDialog()
    {
        //리스트 아이템 담을 리스트
        final List<String> ListItems = new ArrayList<>();
        ListItems.add("inception_v4");
        ListItems.add("mobilenet_v2");

        final CharSequence[] items =  ListItems.toArray(new String[ ListItems.size()]);
        final List SelectedItems  = new ArrayList();
        //기본 선택값 0번 인거지
        int defaultItem = 0;
        SelectedItems.add(defaultItem);
        String input = "";

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
                        String msg = "";
                        if (!SelectedItems.isEmpty()) {
                            int index = (int) SelectedItems.get(0);
                            msg = ListItems.get(index);
                            //tfliteFile = msg.toString();
                        }
                        System.out.println("msg : "+ msg);
                        Toast.makeText(getApplicationContext(),
                                msg +"가 선택되었습니다." , Toast.LENGTH_LONG)
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
        input = String.format("%s.tflite",ListItems.get((Integer) SelectedItems.get(0)));
        //여기서는 오ㅋㅔ
        System.out.println("tffile : "+input);
        return input;
    }



    // 모델 파일 인터프리터를 생성하는 공통 함수
    // loadModelFile 함수에 예외가 포함되어 있기 때문에 반드시 try, catch 블록이 필요하다.
    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(CompleteTag.this, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

}