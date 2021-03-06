//package flwr.android_client;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.content.res.AssetFileDescriptor;
//import android.icu.text.SimpleDateFormat;
//import android.net.Uri;
//import android.os.Bundle;
//
//import androidx.appcompat.app.AppCompatActivity;//
//
//import android.os.ConditionVariable;
//import android.os.Handler;
//import android.text.method.ScrollingMovementMethod;
//import android.util.Log;
//import android.util.Pair;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.google.protobuf.ByteString;
//
//import org.tensorflow.lite.Interpreter;
//import org.tensorflow.lite.examples.transfer.api.LiteInitializeModel;
//import org.tensorflow.lite.examples.transfer.api.TransferLearningModel;
//
//import io.grpc.ManagedChannel;
//
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.net.Socket;
//import java.net.UnknownHostException;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.nio.MappedByteBuffer;
//import java.nio.channels.FileChannel;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.TreeMap;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReadWriteLock;
//import java.util.concurrent.locks.ReentrantLock;
//import java.util.concurrent.locks.ReentrantReadWriteLock;
//
//public class fl2 extends AppCompatActivity {
//    private EditText ip;
//    private EditText port;
//    private TextView textview;
//    private Button loadDataButton;
//    private Button connectButton;
//    private Button trainButton;
//    private TextView resultText;
//    private EditText device_id;
//    private ManagedChannel channel;
//
//    private static String TAG = "Flower";
//
//
//    private TransferLearningModelWrapper tlModel;
//    public FlowerClient fc;
//
//    private int local_epochs = 10; //Hard coded. Need to be set based on server config.
//
//    public static final int FLOAT_BYTES = 4;
//
//    Handler handler = new Handler();
//    HashMap<Uri,String> completeTagMap;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.fl2);
//
//        resultText = (TextView) findViewById(R.id.grpc_response_text);
//        resultText.setMovementMethod(new ScrollingMovementMethod());
//        //?????? ??????
//        device_id = (EditText) findViewById(R.id.device_id_edit_text);
//        ip = (EditText) findViewById(R.id.serverIP);
//        port = (EditText) findViewById(R.id.serverPort);
//        loadDataButton = (Button) findViewById(R.id.load_data) ;
//        connectButton = (Button) findViewById(R.id.connect);
//        trainButton = (Button) findViewById(R.id.trainFederated);
//        textview = (TextView) findViewById(R.id.textView);
//        //completeTag?????? ????????? ??????????????? ?????????
//        Intent secondIntent = getIntent();
//        completeTagMap = (HashMap<Uri, String>) secondIntent.getSerializableExtra("completeTagMap");
//
//        //????????? ???????????? ????????? ?????????
//        Log.v("hashmap", String.valueOf(completeTagMap.keySet()));
//
//        //1. ?????? ????????? ???
//
//        //2. ?????? ??????
//        connectButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ClientThread thread = new ClientThread();
//                thread.start();
//            }
//        });
//    }
//
//    //???????????????  ?????????
//    class ClientThread extends Thread{
//        @Override
//        public void run(){
//            String host = "192.168.1.93";
//            int port = 5001;
//
//            try{
//                //?????? ?????? ??????
//                Socket socket = new Socket(host, port);
//
//                //????????? ?????? ?????????
//                ObjectOutputStream outstream = new ObjectOutputStream(socket.getOutputStream());
//                outstream.writeObject("hello");
//                outstream.flush();
//                Log.d("clientstream","sent to server");
//
//                ObjectInputStream instream = new ObjectInputStream(socket.getInputStream());
//                ServerMessage input = (ServerMessage) instream.readObject();
//                Log.d("clientThread","received data "+input);
//
//                Interpreter tflite = getTfliteInterpreter("model/mobileNet_v2.tflite");
//                //???????????? ?????? ?????? ?????????????????? ??????
//                if (input == parameter) {
//
// /////////////////  //1. parameter ??????
//
//                    //???????????? ?????????
////                    int[] parameterSizes = new int[tflite.getInputTensorCount() - 2];
////                    for (int inputIndex = 2; inputIndex < tflite.getInputTensorCount(); inputIndex++) {
////                        parameterSizes[inputIndex - 2] = tflite.getInputTensor(inputIndex).numElements();
////                    }
////
////                    //??? ????????? ?????? modelParameterSizes
////                    int[] modelParameterSizes = parameterSizes;
////
////                    ByteBuffer[] modelParameters = new ByteBuffer[modelParameterSizes.length];
////                    ByteBuffer[] modelGradients = new ByteBuffer[modelParameterSizes.length];
////                    ByteBuffer[] nextModelParameters = new ByteBuffer[modelParameterSizes.length];
////
////                    for (int parameterIndex = 0; parameterIndex < modelParameterSizes.length; parameterIndex++) {
////                        int bufferSize = modelParameterSizes[parameterIndex] * FLOAT_BYTES;
////                        modelParameters[parameterIndex] = allocateBuffer(bufferSize);
////                        modelGradients[parameterIndex] = allocateBuffer(bufferSize);
////                        nextModelParameters[parameterIndex] = allocateBuffer(bufferSize);
////                    }
////
////                    ByteBuffer zero = ByteBuffer.allocateDirect(FLOAT_BYTES);
////                    zero.order(ByteOrder.nativeOrder());
////                    zero.putFloat(0, 0.f);
////
////                    Map<Integer, Object> outputs = new TreeMap<>();
////                    for (int paramIdx = 0; paramIdx < modelParameters.length; paramIdx++) {
////                        outputs.put(paramIdx, modelParameters[paramIdx]);
////                    }
////
////                    tflite.runForMultipleInputsOutputs(new Object[] {zero}, outputs);
////                    for (ByteBuffer buffer : modelParameters) {
////                        buffer.rewind();
////                    }
//
//
//
//
//
//
//
//
//  ///////////////// //2. ????????? parameter??? fit ??????
//
////                  List<ByteString> layers = message.getFitIns().getParameters().getTensorsList();
//
//                    final ReadWriteLock parameterLock = new ReentrantReadWriteLock();
//                    ByteBuffer[] modelParameters;
//
//                    //???????????? ?????? input?????? ???????????? ?????????
//                    List<ByteString> layers = input;
//                    ByteBuffer[] newWeights = new ByteBuffer[2] ;
//                    for (int i = 0; i < 2; i++) {
//                        newWeights[i] = ByteBuffer.wrap(layers.get(i).toByteArray());
//                    }
//                    //fit??????
//                    Pair<ByteBuffer[], Integer> outputs = fc.fit(newWeights);
//
//
//
//
//
//  //////////////////3.fit????????? weight??? ????????? ?????????
//
//
//                }
//                //???????????? ?????? ?????????
//                else{
//                    Toast.makeText(getApplicationContext(), "??????????????? ?????? ???????????????.", Toast.LENGTH_LONG).show();
//                }
//
//
//
//                //?????? ?????? ?????? ???????????? handler
////                handleMessage(input, (fl2) getApplicationContext());
////                handler.post(new Runnable() {
////                    @Override
////                    public void run() {
////                        textview.setText("?????? ?????????: "+input);
////                    }
////                });
//
//            }  catch (IOException | ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
//
//
//
//    //????????? ??????
//    public void loadData(View view) {
//        //load data ?????? ????????? ??? ??? ???????????? ???????????? ???????????? ??????????????? ???.
//        //?????? editText ???????????? ?????? ????????? ????????? ?????? ??????
//
//        //?????? ????????? ???????????? ????????? ?????? ???
//        if (completeTagMap.isEmpty()) {
//            Toast.makeText(this, "No dataSet", Toast.LENGTH_LONG).show();
//        }
//        //?????? ????????? ???????????? ?????? ?????? ???
//        else {
//
//            //???????????? ????????? ?????? ??????
//            setResultText(String.valueOf("Upload Images : " + completeTagMap.size()));
//            loadDataButton.setEnabled(false);
//            final Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    //flowerclient class??? loadData ?????????
//                    //?????????,?????? ????????? ????????? ??????
//                    fc.loadData(completeTagMap, local_epochs);
//                    setResultText("Training dataset loaded in memory.");
//                    connectButton.setEnabled(true);
//
//                }
//            }, 1000);
//        }
//    }
//    //?????? ?????? ??????
//    public void setResultText(String text) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
//        String time = dateFormat.format(new Date());
//        resultText.append("\n" + time + "   " + text);
//    }
//
//
//    // ?????? ?????? ?????????????????? ???????????? ?????? ??????
//    // loadModelFile ????????? ????????? ???????????? ?????? ????????? ????????? try, catch ????????? ????????????.
//    private Interpreter getTfliteInterpreter(String modelPath) {
//        try {
//            return new Interpreter(loadModelFile(this, modelPath));
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
//        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
//        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
//        FileChannel fileChannel = inputStream.getChannel();
//        long startOffset = fileDescriptor.getStartOffset();
//        long declaredLength = fileDescriptor.getDeclaredLength();
//        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
//    }
//
//    //1????????? ??????
//    private static ByteBuffer allocateBuffer(int capacity) {
//        ByteBuffer buffer = ByteBuffer.allocateDirect(capacity);
//        buffer.order(ByteOrder.nativeOrder());
//        return buffer;
//    }
//}
