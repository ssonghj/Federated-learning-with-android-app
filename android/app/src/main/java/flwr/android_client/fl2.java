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
//        //지울 버튼
//        device_id = (EditText) findViewById(R.id.device_id_edit_text);
//        ip = (EditText) findViewById(R.id.serverIP);
//        port = (EditText) findViewById(R.id.serverPort);
//        loadDataButton = (Button) findViewById(R.id.load_data) ;
//        connectButton = (Button) findViewById(R.id.connect);
//        trainButton = (Button) findViewById(R.id.trainFederated);
//        textview = (TextView) findViewById(R.id.textView);
//        //completeTag에서 가져온 태그완료된 해시맵
//        Intent secondIntent = getIntent();
//        completeTagMap = (HashMap<Uri, String>) secondIntent.getSerializableExtra("completeTagMap");
//
//        //완료된 이미지와 태그값 들어옴
//        Log.v("hashmap", String.valueOf(completeTagMap.keySet()));
//
//        //1. 로드 데이터 셋
//
//        //2. 서버 연결
//        connectButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ClientThread thread = new ClientThread();
//                thread.start();
//            }
//        });
//    }
//
//    //클라이언트  쓰레드
//    class ClientThread extends Thread{
//        @Override
//        public void run(){
//            String host = "192.168.1.93";
//            int port = 5001;
//
//            try{
//                //소켓 통신 시작
//                Socket socket = new Socket(host, port);
//
//                //서버로 신호 보내기
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
//                //서버에서 받은 값이 파라미터라고 하면
//                if (input == parameter) {
//
// /////////////////  //1. parameter 받기
//
//                    //파라미터 사이즈
////                    int[] parameterSizes = new int[tflite.getInputTensorCount() - 2];
////                    for (int inputIndex = 2; inputIndex < tflite.getInputTensorCount(); inputIndex++) {
////                        parameterSizes[inputIndex - 2] = tflite.getInputTensor(inputIndex).numElements();
////                    }
////
////                    //위 반환값 저장 modelParameterSizes
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
//  ///////////////// //2. 들어온 parameter로 fit 하기
//
////                  List<ByteString> layers = message.getFitIns().getParameters().getTensorsList();
//
//                    final ReadWriteLock parameterLock = new ReentrantReadWriteLock();
//                    ByteBuffer[] modelParameters;
//
//                    //서버에서 받은 input값이 파라미터 텐서값
//                    List<ByteString> layers = input;
//                    ByteBuffer[] newWeights = new ByteBuffer[2] ;
//                    for (int i = 0; i < 2; i++) {
//                        newWeights[i] = ByteBuffer.wrap(layers.get(i).toByteArray());
//                    }
//                    //fit하기
//                    Pair<ByteBuffer[], Integer> outputs = fc.fit(newWeights);
//
//
//
//
//
//  //////////////////3.fit결과의 weight를 서버로 보내기
//
//
//                }
//                //파라미터 값이 아니면
//                else{
//                    Toast.makeText(getApplicationContext(), "파라미터를 받지 못했습니다.", Toast.LENGTH_LONG).show();
//                }
//
//
//
//                //화면 변화 바로 보여주는 handler
////                handleMessage(input, (fl2) getApplicationContext());
////                handler.post(new Runnable() {
////                    @Override
////                    public void run() {
////                        textview.setText("받은 데이터: "+input);
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
//    //데이터 로드
//    public void loadData(View view) {
//        //load data 버튼 눌렀을 때 내 해시맵의 이미지와 태그값이 들어가야만 함.
//        //굳이 editText 필요없음 그냥 버튼만 누르면 되게 하기
//
//        //태그 완료된 이미지와 사진이 없을 때
//        if (completeTagMap.isEmpty()) {
//            Toast.makeText(this, "No dataSet", Toast.LENGTH_LONG).show();
//        }
//        //태그 완료된 이미지와 사진 있을 때
//        else {
//
//            //학습시킬 이미지 개수 출력
//            setResultText(String.valueOf("Upload Images : " + completeTagMap.size()));
//            loadDataButton.setEnabled(false);
//            final Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    //flowerclient class의 loadData 가져옴
//                    //이미지,태그 해시맵 데이터 넣음
//                    fc.loadData(completeTagMap, local_epochs);
//                    setResultText("Training dataset loaded in memory.");
//                    connectButton.setEnabled(true);
//
//                }
//            }, 1000);
//        }
//    }
//    //결과 화면 출력
//    public void setResultText(String text) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
//        String time = dateFormat.format(new Date());
//        resultText.append("\n" + time + "   " + text);
//    }
//
//
//    // 모델 파일 인터프리터를 생성하는 공통 함수
//    // loadModelFile 함수에 예외가 포함되어 있기 때문에 반드시 try, catch 블록이 필요하다.
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
//    //1번에서 사용
//    private static ByteBuffer allocateBuffer(int capacity) {
//        ByteBuffer buffer = ByteBuffer.allocateDirect(capacity);
//        buffer.order(ByteOrder.nativeOrder());
//        return buffer;
//    }
//}
