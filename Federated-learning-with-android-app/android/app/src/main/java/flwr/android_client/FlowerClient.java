package flwr.android_client;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.ConditionVariable;
import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.MutableLiveData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FlowerClient {

    private TransferLearningModelWrapper tlModel;
    private static final int LOWER_BYTE_MASK = 0xFF;
    private MutableLiveData<Float> lastLoss = new MutableLiveData<>();
    private Context context;
    private final ConditionVariable isTraining = new ConditionVariable();
    private int local_epochs = 10;
    private static String TAG = "Flower";


    HashMap<Uri, String> cpMap = new LinkedHashMap<>();

    public FlowerClient(Context context) {

        this.tlModel = new TransferLearningModelWrapper(context);
        this.context = context;
    }

    //가중치 가져오기
    public ByteBuffer[] getWeights() {
        Log.v("weights", Arrays.stream(tlModel.getParameters()).iterator().toString());
        return tlModel.getParameters();
    }

    //학습
    public Pair<ByteBuffer[], Integer> fit(ByteBuffer[] weights) {

        tlModel.updateParameters(weights);
        isTraining.close();
        tlModel.train(this.local_epochs);
        tlModel.enableTraining((epoch, loss) -> setLastLoss(epoch, loss));
        Log.e(TAG ,  "Training enabled");
        isTraining.block();
        return Pair.create(getWeights(), tlModel.getSize_Training());

    }

    //평가
    public Pair<Pair<Float, Float>, Integer> evaluate(ByteBuffer[] weights) {
        tlModel.updateParameters(weights);
        tlModel.disableTraining();
        return Pair.create(tlModel.calculateTestStatistics(), tlModel.getSize_Testing());
    }

    public Pair<Pair<Float, Float>, Integer> getTestStatistics() {
        tlModel.disableTraining();
        return Pair.create(tlModel.calculateTestStatistics(), tlModel.getSize_Testing());
    }


    public void setLastLoss(int epoch, float newLoss) {
        if (epoch == this.local_epochs - 1) {
            Log.e(TAG, "Training finished after epochs = " + epoch);
            lastLoss.postValue(newLoss);
            tlModel.disableTraining();
            isTraining.open();
        }
    }

    //데이터 로드
    public void loadData(HashMap<Uri, String> completeTagMap, int epochs) {
        try {
            //다른 곳에 쓰려고 넣어줌
            cpMap = completeTagMap;
            this.local_epochs = epochs;
            //해시값 돌려보기
            int i = 0;
            for(Map.Entry<Uri, String> entry : completeTagMap.entrySet()) {
                System.out.println("map키 : "+ entry.getKey()+" 값 : "+ entry.getValue());

                //20개는 학습
                if(0 <= i && i <= 20){
                    i++;
                    Log.e(TAG, i + "th training image loaded");
                    //학습할 이미지 추가
                    addSample(String.valueOf(entry.getKey()), true);
                }
                //5개는 테스트
                else{
                    i++;
                    Log.e(TAG, i + "th test image loaded");
                    //테스트 샘플 추가
                    addSample(String.valueOf(entry.getKey()), false);
                }
            }
            //add sample에 이미지 저장해야함

        } catch (IOException ex) {
            ex.printStackTrace();
        }

            //기존 코드
//        try {
//            this.local_epochs = epochs;
//            //data의 device_의 id값의 내용 읽어옴
//            BufferedReader reader = new BufferedReader(new InputStreamReader(this.context.getAssets().open("data/device_" + device_id + "_train.txt")));
//            String line;
//            int i = 0;
//            while ((line = reader.readLine()) != null) {
//                i++;
//                if (i>200) break; //Load only the first 200 images to prevent OOM errors.
//                Log.e(TAG, i + "th training image loaded");
//                //학습할 이미지 추가
//                addSample("data/amazon/" + line, true);
//            }
//            reader.close();
//
//            i = 0;
//            reader = new BufferedReader(new InputStreamReader(this.context.getAssets().open("data/device_" + device_id + "_test.txt")));
//            //device_1test.txt 파일을 한줄씩 읽으면서 데이터 이미지 가져옴
//            while ((line = reader.readLine()) != null) {
//                i++;
//                if (i>200) break; //Load only the first 200 images to prevent OOM errors.
//                Log.e(TAG, i + "th test image loaded");
//                //테스트 샘플 추가
//                addSample("data/amazon/" + line, false);
//            }
//            reader.close();
//
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }

    }

    //이미지 추가
    private void addSample(String photoPath, Boolean isTraining) throws IOException {
        //기존 모델에 변환해서 쓰는 것
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        //photoPath 파라미터로 포토 위치 가져오기
        //data/amazon/projector/frame_0001.jpg 로 가져와 질텐데 -> ㅇㅇ
        Log.v("photoPath",photoPath);

        //uri로 이미지 받아오기
        Bitmap bitmap =  BitmapFactory.decodeStream(this.context.getContentResolver().openInputStream(Uri.parse(photoPath)), null, options);
        //태그 저장
        String sampleClass = get_class(photoPath);

        // get rgb equivalent and class
        float[] rgbImage = prepareImage(bitmap);
        bitmap = null;

        // add to the list.
        try {
            this.tlModel.addSample(rgbImage, sampleClass, isTraining).get();
            rgbImage = null;
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to add sample to model", e.getCause());
        } catch (InterruptedException e) {
            // no-op
        }


//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        //photoPath 파라미터로 포토 위치 가져오기
//        //data/amazon/projector/frame_0001.jpg 로 가져와 질텐데 -> ㅇㅇ
//        Log.v("photoPath",photoPath);
//        Bitmap bitmap =  BitmapFactory.decodeStream(this.context.getAssets().open(photoPath), null, options);
//        //태그 저장
//        String sampleClass = get_class(photoPath);
//
//        // get rgb equivalent and class
//        float[] rgbImage = prepareImage(bitmap);
//        bitmap = null;
//
//        // add to the list.
//        try {
//            this.tlModel.addSample(rgbImage, sampleClass, isTraining).get();
//            rgbImage = null;
//        } catch (ExecutionException e) {
//            throw new RuntimeException("Failed to add sample to model", e.getCause());
//        } catch (InterruptedException e) {
//            // no-op
//        }
    }

    // / 로 구분하고 태그만 저장
    public String get_class(String path) {
        String label = "";
        //이미지 uri에 따른 태그를 찾기
        for(Map.Entry<Uri, String> entry : cpMap.entrySet()) {
            Log.v("path", String.valueOf(entry.getKey()));
            Log.v("path2",path);
            if(entry.getKey().toString().equals(path)){
                label = entry.getValue();
                Log.v("label",label);
            }
        }
        return label;

        //원본 태그
        //String label = path.split("/")[2];
//        return label;
    }

    /**
     * Normalizes a camera image to [0; 1], cropping it
     * to size expected by the model and adjusting for camera rotation.
     */
    private static float[] prepareImage(Bitmap bitmap)  {
        int modelImageSize = TransferLearningModelWrapper.IMAGE_SIZE;

        Bitmap paddedBitmap = padToSquare(bitmap);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                paddedBitmap, modelImageSize, modelImageSize, true);

        float[] normalizedRgb = new float[modelImageSize * modelImageSize * 3];
        int nextIdx = 0;
        for (int y = 0; y < modelImageSize; y++) {
            for (int x = 0; x < modelImageSize; x++) {
                int rgb = scaledBitmap.getPixel(x, y);

                float r = ((rgb >> 16) & LOWER_BYTE_MASK) * (1 / 255.f);
                float g = ((rgb >> 8) & LOWER_BYTE_MASK) * (1 / 255.f);
                float b = (rgb & LOWER_BYTE_MASK) * (1 / 255.f);

                normalizedRgb[nextIdx++] = r;
                normalizedRgb[nextIdx++] = g;
                normalizedRgb[nextIdx++] = b;
            }
        }

        return normalizedRgb;
    }

    private static Bitmap padToSquare(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();

        int paddingX = width < height ? (height - width) / 2 : 0;
        int paddingY = height < width ? (width - height) / 2 : 0;
        Bitmap paddedBitmap = Bitmap.createBitmap(
                width + 2 * paddingX, height + 2 * paddingY, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(paddedBitmap);
        canvas.drawARGB(0xFF, 0xFF, 0xFF, 0xFF);
        canvas.drawBitmap(source, paddingX, paddingY, null);
        return paddedBitmap;
    }


}
