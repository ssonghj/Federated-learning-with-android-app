package flwr.android_client;

import static org.tensorflow.lite.DataType.UINT8;

import android.app.Activity;//
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;//

import org.json.JSONArray;
import org.json.JSONException;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class CompleteTag extends AppCompatActivity {
    int flag = 0;
    SharedPreferences mmPref;
    ListView listView;
    private List str_list;
    ArrayList<ListData> listViewData = new ArrayList<>();
    HashMap<Uri,String> completeTagMap;
    Button modifyBtn;
    Button notCompleteTagBtn;
    Button start_FL_Btn;
    String key;
    Float value;

    Uri imageUri;
    private static final int REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.complete_tag);
        //버튼 선언
        notCompleteTagBtn = (Button) findViewById(R.id.notCompleteTagBtn);
        modifyBtn = (Button) findViewById(R.id.modifyBtn);
        start_FL_Btn = (Button) findViewById(R.id.start_FL);

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

        //연합학습 페이지 넘어가는 버튼
        start_FL_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), FederatedLearning.class);
                intent1.putExtra("completeTagMap", completeTagMap);
                startActivity(intent1);
            }
        });

        //추론하기 버튼
        modifyBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Context context = getApplicationContext();
//                Toast.makeText(context, "태그가 수정되었습니다.", Toast.LENGTH_LONG).show();
                try {
                    //앨범에서 사진 선택하기
                    if (modifyBtn.getText().equals("사진선택")){
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, 2222);
                    }
                    //선택한 사진으로 학습하기
                    else if(modifyBtn.getText().equals("추론하기")){
                        SaveCompleteTagMap(context, completeTagMap);


                        //팝업 띄워서 모델 이름 받기
                        //String tfliteFile = showSelectModelDialog();


                        //테스트 사진 비트맵 변환 ->resize함수 사용
                        Bitmap bitmap = resize(context,imageUri,256);

                        //이미지 전처리
                        ImageProcessor imageProcessor =
                                new ImageProcessor.Builder()
                                        .add(new ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                                        .build();
                        TensorImage tImage = new TensorImage(DataType.UINT8);
                        tImage.load(bitmap);

                        tImage = imageProcessor.process(tImage);

                        //FLOAT32임.... NEURON APP? 확인해보면
                        TensorBuffer probabilityBuffer =
                                TensorBuffer.createFixedSize(new int[]{1,4}, UINT8);

                        // Initialise the model
                        Interpreter tflite = null;

                        //tf파일 받기

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
                                result.put(s[0], Float.parseFloat(s[1])*100);
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


                        //결과 페이지로 넘어가기
                        Intent intent1 = new Intent(getApplicationContext(), Classification.class);
                        intent1.putExtra("testUri",String.valueOf(imageUri));
                        //결괏값 넘기기
                        intent1.putExtra("resultKey", key);
                        intent1.putExtra("resultValue",value);
                        startActivity(intent1);
                    }

                    //식별하기 버튼
                    if (modifyBtn.getText().equals("식별하기")){
                        Intent intent = new Intent(getApplicationContext(), Classification.class);
                        startActivity(intent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


            //이미지 리사이즈
            private Bitmap resize(Context context, Uri uri, int resize){
                Bitmap resizeBitmap=null;

                BitmapFactory.Options options = new BitmapFactory.Options();
                try {
                    BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); // 1번

                    int width = options.outWidth;
                    int height = options.outHeight;
                    int samplesize = 1;

                    while (true) {//2번
                        if (width / 2 < resize || height / 2 < resize)
                            break;
                        width /= 2;
                        height /= 2;
                        samplesize *= 2;
                    }

                    options.inSampleSize = samplesize;
                    Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); //3번
                    resizeBitmap=bitmap;

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return resizeBitmap;
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageUri = data.getData();
        modifyBtn.setText("추론하기");
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
