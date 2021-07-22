package com.example.poc;

import android.util.Log;

import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.records.listener.impl.LogRecordListener;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;

public class CNNModel implements FederatedModel {
    private static final String TAG = "CNNModel";
    private static final int BATCH_SIZE = 64;
    private static final int N_EPOCHS = 1;
    private static final int rngSeed = 42;

    private static final int HEIGHT = 224;
    private static final int WIDTH = 224;
    private static final int OUTPUT_NUM = 5;

    private MultiLayerNetwork model;

    private static Logger log = LoggerFactory.getLogger(CNNModel.class);

    private String train_data_path = "/storage/self/primary/Download/data_balance/client1_train/";
    private int N_SAMPLES_CLIENT1_TRAINING = 468;

    private DataSetIterator AcitivityTrain;

    //cnn model
    public CNNModel() throws IOException {
        AcitivityTrain = getDataSetIterator(train_data_path, N_SAMPLES_CLIENT1_TRAINING);
    }

    //빌드 모델
    @Override
    public void buildModel(String modelsip_path) {
        //Load the model
        try {
            File modelzip = new File(modelsip_path + "/MyMultiLayerNetwork.zip");
            //serializer 순차 모델 구현
            //기존거 그대로 사용한 model
            model = ModelSerializer.restoreMultiLayerNetwork(modelzip);
            //모델 입력 순서가 달라서 마지막 두번째 레이어는 직접 넣어주어야한다고 하셨음.
            //그래서 neural_config2로 새롭게 layer 만들어줌
            MultiLayerConfiguration neural_config2 = new NeuralNetConfiguration.Builder()
                    .list()
                    .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                            .nIn(10)
                            .nOut(5)
                            .activation(Activation.SOFTMAX)
                            .build())
                    .build();
            //만든 설정을 model2로 넣어줌
            MultiLayerNetwork model2 = new MultiLayerNetwork(neural_config2);
            model2.init();

            //weight랑 bias 설정
            INDArray para1_W = model.getOutputLayer().getParam("W");
            INDArray para1_b = model.getOutputLayer().getParam("b");

            //layer에 넣어줌
            model2.getLayer(0).setParam("W", para1_W);
            model2.getLayer(0).setParam("b", para1_b);
            //모델합치기
            Layer[] layers = new Layer[model.getnLayers()];
            for(int i = 0; i < model.getnLayers() - 1; i++) {
                layers[i] = model.getLayer(i);
            }
            layers[layers.length-1] = model2.getLayer(0);
            model.setLayers(layers);
            model.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //학습시키기
    @Override
    public void train(int numEpochs) throws InterruptedException {
        Log.d(TAG, " start fit!");
        model.fit(AcitivityTrain, numEpochs);
    }

    //모델저장
    @Override
    public void saveModel(String modelName) {
        try {
            File save_model = new File(modelName);
            model.save(save_model);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //순차모델저장
    @Override
    public void saveSerializeModel(String modelName) {
        try {
            //레이어 갯수 저장
            int layer_length = model.getnLayers();
            //새로운 JSON 객체 생성
            JSONObject para_json = new JSONObject();

            //레이어 길이만큼 도는데
            for(int i = 0; i < layer_length; i++) {
                //모델 Weight가 null이 아니면
                if(model.getLayer(i).getParam("W") != null) {

                    // 1. W param
                    JSONArray data_W = new JSONArray();
                    INDArray param_w = model.getLayer(i).getParam("W");
                    long[] param_shape_w = param_w.shape();

                    int total_size = 1;
                    for(int j = 0; j < param_shape_w.length; j++) {
                        total_size *= param_shape_w[j];
                    }
                    INDArray reshape_param = param_w.reshape(1, total_size);
                    for (int k = 0; k < reshape_param.getRow(0).length(); k++) {
                        data_W.put(reshape_param.getRow(0).getFloat(k));
                    }

                    // 2. b param
                    JSONArray data_b = new JSONArray();
                    INDArray param_b = model.getLayer(i).getParam("b");

                    for (int k = 0; k < param_b.columns(); k++) {
                        data_b.put(param_b.getRow(0).getFloat(k));
                    }
                    //json객체에 w,b 넣어주기
                    para_json.put(Integer.toString(i) + "_W", data_W);
                    para_json.put(Integer.toString(i) + "_b", data_b);
                }
            }

            FileWriter file = new FileWriter("/storage/self/primary/Download/save_weight/" + modelName);
            file.write(para_json.toJSONString());
            file.flush();
            file.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
    //upload
    @Override
    public void uploadTo(String upload_path, String upload_url, OkHttpClient client) throws IOException {
        File tempSelectFile = new File(upload_path);
        FileUploadUtils.goSend(tempSelectFile, upload_url, client);
    }

    @Override
    public DataSetIterator getDataSetIterator(String folderPath, int nSamples) throws IOException {
        File train_data = new File(folderPath);
        FileSplit train = new FileSplit(train_data, NativeImageLoader.ALLOWED_FORMATS, new Random(123));
        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
        ImageRecordReader recordReader = new ImageRecordReader(224, 224, 3, labelMaker);

        recordReader.initialize(train);
        recordReader.setListeners(new LogRecordListener());

        DataSetIterator dataIter = new RecordReaderDataSetIterator(recordReader, 16, 1, 5);

        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
        scaler.fit(dataIter);
        dataIter.setPreProcessor(scaler);

        return dataIter;
    }
}
