package flwr.android_client.FL;

import android.app.Activity;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;
import java.util.HashMap;

import flwr.android_client.InformationReply;
import flwr.android_client.InformationRequest;
import flwr.android_client.ManagerGrpc;
import flwr.android_client.R;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class FederatedLearning extends AppCompatActivity {
    private EditText ip;
    private EditText port;

    private Button loadDataButton;
    private Button connectButton;
    private Button runfederatedButton;
    private ImageButton backBtn;
    private ImageButton infoBtn;

//    private Button getinformationButton;
//    private Button getmodelButton;
//    private Button pushtrainresultButton;
//    private Button getstatusButton;
//    private Button pushcontrolButton;

    private TextView resultText;
    private EditText device_id;
    private ManagedChannel channel;
    public FlowerClient fc;
    private static String TAG = "Flower";

    private MyDialog Dialog_Listener;

    private int local_epochs = 10; //Hard coded. Need to be set based on server config.

    HashMap<Uri, String> completeTagMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.federated_learning);

        infoBtn = findViewById(R.id.infoBtn);

        resultText = (TextView) findViewById(R.id.grpc_response_text);
        resultText.setMovementMethod(new ScrollingMovementMethod());
        ip = (EditText) findViewById(R.id.serverIP);
        port = (EditText) findViewById(R.id.serverPort);


        loadDataButton = (Button) findViewById(R.id.load_data);
        connectButton = (Button) findViewById(R.id.connect);
        runfederatedButton = (Button) findViewById(R.id.runFederated);
//        getinformationButton = (Button) findViewById(R.id.GetInformation);
//        getmodelButton = (Button) findViewById(R.id.GetModel);
//        pushtrainresultButton = (Button) findViewById(R.id.PushTrainResult);
//        getstatusButton = (Button) findViewById(R.id.GetStatus);
//        pushcontrolButton = (Button) findViewById(R.id.PushControl);


        //completeTag에서 가져온 태그완료된 해시맵
        Intent secondIntent = getIntent();
        completeTagMap = (HashMap<Uri, String>) secondIntent.getSerializableExtra("completeTagMap");

        fc = new FlowerClient(this);
        //완료된 이미지와 태그값 들어옴
        Log.v("hashmap", String.valueOf(completeTagMap.keySet()));



        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> {
            finish();//현재화면 끄기
        });

        //도움말 버튼
        infoBtn.setOnClickListener(v -> {
            Dialog_Listener = new MyDialog(this);
            Dialog_Listener.show();
        });
    }



    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //화면 출력
    public void setResultText(String text) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String time = dateFormat.format(new Date());
        resultText.append("\n" + time + "   " + text);
    }

    //데이터 가져오기
    public void loadData(View view) {
        //load data 버튼 눌렀을 때 내 해시맵의 이미지와 태그값이 들어가야만 함.
        //굳이 editText 필요없음 그냥 버튼만 누르면 되게 하기

        //태그 완료된 이미지와 사진이 없을 때
        if (completeTagMap.isEmpty()) {
            Toast.makeText(this, "No dataSet", Toast.LENGTH_LONG).show();
        }
        //태그 완료된 이미지와 사진 있을 때
        else {
            //키보드 숨기기
            hideKeyboard(this);
            //학습시킬 이미지 개수 출력
            setResultText(String.valueOf("Upload Images : " + completeTagMap.size()));
            loadDataButton.setEnabled(false);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //flowerclient class의 loadData 가져옴
                    //이미지,태그 해시맵 데이터 넣음
                    fc.loadData(completeTagMap, local_epochs);
                    setResultText("Training dataset loaded in memory.");
                    connectButton.setEnabled(true);

                }
            }, 1000);
        }
    }


    //서버와 연결하기 -> 채녈 만들기
    public void connect(View view) {
        String host = ip.getText().toString();
        String portStr = port.getText().toString();
        if (TextUtils.isEmpty(host) || TextUtils.isEmpty(portStr) || !Patterns.IP_ADDRESS.matcher(host).matches()) {
            Toast.makeText(this, "Please enter the correct IP and port of the FL server", Toast.LENGTH_LONG).show();
        } else {
            int port = TextUtils.isEmpty(portStr) ? 0 : Integer.valueOf(portStr);
            channel = ManagedChannelBuilder.forAddress(host, port).maxInboundMetadataSize(512 * 1024 * 1024).maxInboundMessageSize(512 * 1024 * 1024).usePlaintext().build();
            hideKeyboard(this);
            runfederatedButton.setEnabled(true);
            connectButton.setEnabled(false);
            setResultText("Channel established with the FL server.");
        }
    }

    public void runGRCP(View view) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("192.168.1.13", 3306)
                .usePlaintext()
                .build();

        ManagerGrpc.ManagerBlockingStub stub =
                ManagerGrpc.newBlockingStub(channel);


        InformationRequest statusRequest = InformationRequest.newBuilder()
                .setName("SIMPLE-CNN-32-32-3")//7143424
                .build();

        InformationReply features ;
        try {
            features = stub.getInformation(statusRequest);

        } catch (StatusRuntimeException e) {
            Log.i("RPC failed : {0}", e.getStatus().toString());
            return;
        }
//        StatusReply helloResponse = stub.getStatus(statusRequest);

        String layers = features.getModels(0).getName();
//        String two = helloResponse.getVersion();
        System.out.println(layers);
//        System.out.println(two);
    }



//    run federated 누르면 연결 시작
//    public void runGRCP(View view){
//        new GrpcTask(new ManagerRunnable(), channel, this).execute();
//    }
//
//    private static class GrpcTask extends AsyncTask<Void, Void, String> {
//        private final GrpcRunnable grpcRunnable;
//        private final ManagedChannel channel;
//        private final FederatedLearning activityReference;
//
//        GrpcTask(GrpcRunnable grpcRunnable, ManagedChannel channel, FederatedLearning activity) {
//            this.grpcRunnable = grpcRunnable;
//            this.channel = channel;
//            this.activityReference = activity;
//        }
//
//        @Override
//        protected String doInBackground(Void... nothing) {
//            try {
//                grpcRunnable.run(ManagerGrpc.newBlockingStub(channel), ManagerGrpc.newStub(channel), this.activityReference);
//                return "Connection to the FL server successful \n";
//            } catch (Exception e) {
//                StringWriter sw = new StringWriter();
//                PrintWriter pw = new PrintWriter(sw);
//                e.printStackTrace(pw);
//                pw.flush();
//                return "Failed to connect to the FL server \n" + sw;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            FederatedLearning activity = activityReference;
//            if (activity == null) {
//                return;
//            }
//            activity.setResultText(result);
//            activity.runfederatedButton.setEnabled(false);
//        }
//    }
//
//    //             grpc              //
//    private interface GrpcRunnable {
//        void run(ManagerGrpc.ManagerBlockingStub blockingStub, ManagerGrpc.ManagerStub asyncStub, FederatedLearning activity) throws Exception;
//    }
//
//    private static class ManagerRunnable implements GrpcRunnable {
//        private Throwable failed;
//
//        private InformationRequest informationrequest;//client
//        private ModelRequest modelrequest;
//        private TrainResult trainresult;
//        private StatusRequest statusrequest;
//        private Control control;
//
//
//        @Override
//        public void run(ManagerGrpc.ManagerBlockingStub blockingStub, ManagerGrpc.ManagerStub asyncStub, FederatedLearning activity)
//                throws Exception {
//            //순차적으로 실행해야 할 듯?
//
//            GetInformation(asyncStub, activity);
////            GetModel(asyncStub,activity);
////            PushTrainResult(asyncStub,activity);
////            GetStatus(asyncStub,activity);
//
//        }
//
//        //1
//        private void GetInformation(ManagerGrpc.ManagerStub asyncStub, FederatedLearning activity)
//                throws InterruptedException, RuntimeException {
//
//            informationrequest = InformationRequest.newBuilder() //client
//                    .setName("SIMPLE-CNN-32-32-3")
//                    .build();
//
//            activity.setResultText("GetInformation");
//
//            final CountDownLatch finishLatch = new CountDownLatch(1);
//            asyncStub.getInformation(informationrequest,InformationReply.newBuilder().getModels(0).getName());
//
//            asyncStub.getInformation(informationrequest, new StreamObserver<InformationReply>(){
//
//                @Override
//                public void onNext(InformationReply value) {
//                    //여기에 들어오는 값을 처리
//                    System.out.println( value.getModels(0).getName());
////                    System.out.println(value.get);
////                    handleMessageGetInformation(value,activity);
//                    Log.i("[client]get message in client = {}", String.valueOf(value));
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    finishLatch.countDown();
//                    Log.i("[client]get message in client = {}", String.valueOf(11));
//
//                }
//
//                @Override
//                public void onCompleted() {
//                    finishLatch.countDown();
//                }
//            });
//        }
//
//        //handleMessageGetInformation 진행상태에 따른 메시지 출력
//        private void handleMessageGetInformation(InformationReply message, FederatedLearning activity) {
//
//            try {
//                Log.e(TAG, "Handling GetStatus");
//                activity.setResultText("Handling GetStatus");
//
//                String name = message.getModels(0).getName();
//                System.out.println(name);
//                String version = message.getModels(0).getVersion();
//                System.out.println(version);
////                informationrequest.onNext(InformationRequest.newBuilder().setName("SIMPLE-CNN-32-32-3").build());
//            }
//            catch (Exception e){
//                Log.e(TAG, e.getMessage());
//            }
//        }


    //1
//        private void GetModel(ManagerStub asyncStub, FederatedLearning activity)
//                throws InterruptedException, RuntimeException {
//            modelrequest = ModelRequest.newBuilder() //client
//                    .setName("SIMPLE-CNN-32-32-3")
//                    .setVersion("1.0")
//                    .setLabel(true)
//                    .setCompile(true)
//                    .setArchitecture(true)
//                    .setParameter(true)
//                    .build();
//            activity.setResultText("GetModel");
//
//            asyncStub.getModel(modelrequest, new StreamObserver<ModelReply>(){
//                @Override
//                public void onNext(ModelReply value) {
//                    //여기에 들어오는 값을 처리
//                    handleMessageGetModel(value,activity);
//                    activity.setResultText("model request");
//                    Log.i("[client]get message in client = {}", String.valueOf(value));
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    Log.e("[client]error = {}", t.getMessage());
//                    t.printStackTrace();
//                }
//
//                @Override
//                public void onCompleted() {
//                    Log.i("[client]call onCompleted..","");
//                }
//            });
//        }
//
//        //handleMessageGetModel 진행상태에 따른 메시지 출력
//        private void handleMessageGetModel(ModelReply message, FederatedLearning activity) {
//
//            try {
//                Log.e(TAG, "Handling GetModel");
//                activity.setResultText("Handling GetModel");
//
//                String name = message.getName();
//                String version = message.getVersion();
//                ByteString label = message.getLabel();
//                ByteString compile = message.getCompile();
//                String architecture = message.getArchitecture();
//                ByteString parameter = message.getParameter();
//
//            }
//            catch (Exception e){
//                Log.e(TAG, e.getMessage());
//            }
//        }

    //2
//        private void PushTrainResult(ManagerStub asyncStub, FederatedLearning activity)
//                throws InterruptedException, RuntimeException {
//            trainresult = TrainResult.newBuilder() //client
//                    .setName()
//                    .setVersion()
//                    .setParameter()
//                    .build();
//            activity.setResultText("PushTrainResult");
//
//
//            asyncStub.pushTrainResult(trainresult, new StreamObserver<Note>(){
//                @Override
//                public void onNext(Note value) {
//                    //여기에 들어오는 값을 처리
//                    handleMessagePushTrainResult(value, activity);
//                    Log.i("[client]get message in client = {}", String.valueOf(value));
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    Log.e("[client]error = {}", t.getMessage());
//                    t.printStackTrace();
//                }
//
//                @Override
//                public void onCompleted() {
//                    Log.i("[client]call onCompleted..","");
//                }
//            });
//        }
//
//        //handleMessagePushTrainResult 진행상태에 따른 메시지 출력
//        private void handleMessagePushTrainResult(Note message, FederatedLearning activity) {
//            try {
//                Log.e(TAG, "Handling PushTrainResult");
//                activity.setResultText("Handling PushTrainResult");
//
//                String value = message.getValue();
////                ByteString value2 = message.getValueBytes();
//
//            }
//            catch (Exception e){
//                Log.e(TAG, e.getMessage());
//            }
//        }


    //3
//        private void GetStatus(ManagerStub asyncStub, FederatedLearning activity)
//                throws InterruptedException, RuntimeException {
//            statusrequest = StatusRequest.newBuilder() //client
//                    .setName("SIMPLE-CNN-32-32-3")
//                    .setVersion("v0.0.0")
//                    .build();
//            activity.setResultText("GetStatus");
//
//            asyncStub.getStatus(statusrequest, new StreamObserver<StatusReply>(){
//
//                @Override
//                public void onNext(StatusReply value) {
//                    //여기에 들어오는 값을 처리
//                    handleMessageGetStatus(value,activity);
//                    Log.i("[client]get message in client = {}", String.valueOf(value));
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    Log.e("[client]error = {}", t.getMessage());
//                    t.printStackTrace();
//                }
//
//                @Override
//                public void onCompleted() {
//                    Log.i("[client]call onCompleted..","");
//                }
//            });
//        }
//
//        //handleMessageGetStatus 진행상태에 따른 메시지 출력
//        private void handleMessageGetStatus(StatusReply message, FederatedLearning activity) {
//
//            try {
//                Log.e(TAG, "Handling GetStatus");
//                activity.setResultText("Handling GetStatus");
//
//                String name = message.getName();
//                System.out.println(name);
//                String version = message.getVersion();
//                System.out.println(version);
//                int knowledge = message.getKnowledge();
//                System.out.println(knowledge);
//            }
//            catch (Exception e){
//                Log.e(TAG, e.getMessage());
//            }
//        }


//    }
}

