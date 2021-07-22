package com.example.poc;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import java.io.IOException;

import okhttp3.OkHttpClient;
//인터페이스 구현
public interface FederatedModel {
    //모델 만들기
    void buildModel(String modelsip_path);

    // void train(TrainerDataSource trainerDataSource);
    //학습
    void train(int numEpochs) throws InterruptedException;
    //모델 저장
    void saveModel(String modelName);
    //구현모델 저장
    void saveSerializeModel(String modelName);
    //파일 업로드
    void uploadTo(String upload_path, String upload_url, OkHttpClient client) throws IOException;

    DataSetIterator getDataSetIterator(String folderPath, int nSamples) throws IOException;

}