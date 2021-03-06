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
import android.widget.ImageButton;
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

import flwr.android_client.FL.FederatedLearning;


public class CompleteTag extends AppCompatActivity {
    SharedPreferences mmPref;
    ListView listView;
    private List str_list;
    ArrayList<ListData> listViewData = new ArrayList<>();
    HashMap<Uri,String> completeTagMap;
    ImageButton notCompleteTagBtn;
    Button start_FL_Btn;
    Button modifyBtn;
    String key;
    Float value;
    Uri imageUri;
    private static final int REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complete_tag);

        //?????? ??????
        notCompleteTagBtn = findViewById(R.id.backBtn);                                                             //////////
        modifyBtn = (Button) findViewById(R.id.modifyBtn);
        start_FL_Btn = (Button) findViewById(R.id.start_FL);

        Intent secondIntent = getIntent();
        completeTagMap = (HashMap<Uri, String>) secondIntent.getSerializableExtra("completeTagMap");



        //????????? ??? ????????????
        listView = findViewById(R.id.listview);
        //?????? ???????????? ????????? ??????
        for(Map.Entry<Uri,String> entry : completeTagMap.entrySet()) {
            ListData listData = new ListData();
            //?????? ?????? ?????? ??????????????? ??????
            listData.mainImage = entry.getKey();
            listViewData.add(listData);
        }

        //????????? ?????????
        str_list = new ArrayList<String>();
        for(Map.Entry<Uri,String> entry : completeTagMap.entrySet()) {
            str_list.add(entry.getValue());
        }

        //????????? ?????????
        CompleteListView oAdapter = new CompleteListView(this, listViewData,str_list, completeTagMap);
        listView.setAdapter(oAdapter);


        //???????????? ????????? ?????? ??????
        notCompleteTagBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //?????? ???????????? ???????????? ???????????? ???????????? ??????
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                //????????? ????????? ?????????
                intent.putExtra("completeTagMap", completeTagMap);
                startActivity(intent);
            }
        });

        //???????????? ????????? ???????????? ??????
        start_FL_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), FederatedLearning.class);
                intent1.putExtra("completeTagMap", completeTagMap);
                startActivity(intent1);
            }
        });

        //???????????? ??????
        modifyBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Context context = getApplicationContext();
//                Toast.makeText(context, "????????? ?????????????????????.", Toast.LENGTH_LONG).show();
                try {
                    //???????????? ?????? ????????????
                    if (modifyBtn.getText().equals("?????? ?????? ?????????????")){
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, 2222);
                    }
                    //????????? ???????????? ????????????
                    else if(modifyBtn.getText().equals("?????? ????????????")){
                        SaveCompleteTagMap(context, completeTagMap);


                        //?????? ????????? ?????? ?????? ??????
                        //String tfliteFile = showSelectModelDialog();


                        //????????? ?????? ????????? ?????? ->resize?????? ??????
                        Bitmap bitmap = resize(context,imageUri,256);

                        //????????? ?????????
                        ImageProcessor imageProcessor =
                                new ImageProcessor.Builder()
                                        .add(new ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                                        .build();
                        TensorImage tImage = new TensorImage(DataType.UINT8);
                        tImage.load(bitmap);

                        tImage = imageProcessor.process(tImage);

                        //FLOAT32???.... NEURON APP? ???????????????
                        TensorBuffer probabilityBuffer =
                                TensorBuffer.createFixedSize(new int[]{1,4}, UINT8);

                        // Initialise the model
                        Interpreter tflite = null;

                        //tf?????? ??????
                        tflite = getTfliteInterpreter("model/mobileNet_v2.tflite");

                        // Running inference
                        if(null != tflite) {
                            System.out.println("????????????");
                            tflite.run(tImage.getBuffer(), probabilityBuffer.getBuffer());
                            System.out.println("?????? ???");
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

                            //?????? ????????? ??? ?????? ????????? ?????? ??????
                            for(Object entry : floatMap.entrySet()) {
                                System.out.println("map??? : "+ entry);
                            }

                            //?????? ?????? ??????
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

                            //?????? ??? ?????? ?????? ?????? ????????? ?????? ??? ????????? key, value ??????
                            for (Map.Entry<String, Float> entry : result.entrySet()) {
                                if (entry.getValue().equals(max)) {
                                    System.out.println("key : "+entry.getKey()+" value : "+entry.getValue());
                                    key = entry.getKey();
                                    value = entry.getValue();
                                }
                            }
                        }


                        //?????? ???????????? ????????????
                        Intent intent1 = new Intent(getApplicationContext(), Classification.class);
                        intent1.putExtra("testUri",String.valueOf(imageUri));
                        //????????? ?????????
                        intent1.putExtra("resultKey", key);
                        intent1.putExtra("resultValue",value);
                        startActivity(intent1);
                    }

                    //???????????? ??????
                    if (modifyBtn.getText().equals("????????????")){
                        Intent intent = new Intent(getApplicationContext(), Classification.class);
                        startActivity(intent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


            //????????? ????????????
            private Bitmap resize(Context context, Uri uri, int resize){
                Bitmap resizeBitmap=null;

                BitmapFactory.Options options = new BitmapFactory.Options();
                try {
                    BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); // 1???

                    int width = options.outWidth;
                    int height = options.outHeight;
                    int samplesize = 1;

                    while (true) {//2???
                        if (width / 2 < resize || height / 2 < resize)
                            break;
                        width /= 2;
                        height /= 2;
                        samplesize *= 2;
                    }

                    options.inSampleSize = samplesize;
                    Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); //3???
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
        modifyBtn.setText("?????? ????????????");
    }



    //?????? ????????? sharedPreperence??? ??????
    public void SaveCompleteTagMap(Context context, HashMap<Uri, String> ctMap) throws IOException, JSONException {
        SharedPreferences mmPref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        //arraylist ??? ?????? key??? value ?????? ??????
        SharedPreferences.Editor editor = mmPref.edit();
        //key??? jsonKeyArray ??????
        JSONArray jsonKeyArray = new JSONArray();
        //value??? jsonValueArray ??????
        JSONArray jsonValueArray = new JSONArray();

        //ctMap ????????? key??? value ??? array??? ??????
        for(Map.Entry<Uri,String> entry : ctMap.entrySet()) {
            jsonKeyArray.put(entry.getKey());
            jsonValueArray.put(entry.getValue());
        }

        if(!ctMap.isEmpty()){
            //shared preference??? ????????????
            editor.putString("jsonKey", jsonKeyArray.toString());
            editor.putString("jsonValue",jsonValueArray.toString());
            //????????????
            editor.apply();
        }else{
            editor.putString("jsonKey", null);
            editor.putString("jsonValue",null);
            editor.apply();
        }
    }


    //?????? ?????? ???????????????
    public String showSelectModelDialog()
    {
        //????????? ????????? ?????? ?????????
        final List<String> ListItems = new ArrayList<>();
        ListItems.add("inception_v4");
        ListItems.add("mobilenet_v2");

        final CharSequence[] items =  ListItems.toArray(new String[ ListItems.size()]);
        final List SelectedItems  = new ArrayList();
        //?????? ????????? 0??? ?????????
        int defaultItem = 0;
        SelectedItems.add(defaultItem);
        String input = "";

        //????????? ?????????
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //????????? ?????????
        builder.setTitle("?????? ??????");
        builder.setSingleChoiceItems(items, defaultItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SelectedItems.clear();
                        SelectedItems.add(which);
                    }
                });
        //??????????????? ?????? ok ??????
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
                                msg +"??? ?????????????????????." , Toast.LENGTH_LONG)
                                .show();
                        //?????? text ?????????
                        modifyBtn.setText("????????????");
                        //?????? ??? ?????????
                        //modifyBtn.setBackgroundColor(0xffD1B6E1);
                    }
                });
        //??????????????? ?????? cancel ??????
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //????????????
                    }
                });
        builder.show();
        input = String.format("%s.tflite",ListItems.get((Integer) SelectedItems.get(0)));
        //???????????? ?????????
        System.out.println("tffile : "+input);
        return input;
    }


    // ?????? ?????? ?????????????????? ???????????? ?????? ??????
    // loadModelFile ????????? ????????? ???????????? ?????? ????????? ????????? try, catch ????????? ????????????.
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
