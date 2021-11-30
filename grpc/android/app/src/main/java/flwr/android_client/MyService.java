package flwr.android_client;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class MyService extends Service {
    private Thread mThread;
    private int mCount = 0;

    public MyService() {

    }

    //startService() -> onStartCommand 호출됨
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mThread == null){
            mThread = MyService.this.
            mThread = new Thread("My Thread"){

                @Override
                public void run() {
                    for (int i = 0; i < 100; i++) {
                        try{
                            mCount++;
                            Thread.sleep(5000);
                        }catch (InterruptedException e){
                            break;
                        }
                        handler.sendEmptyMessage(0);
                        Log.d("service","서비스 동작 중"+mCount);
                        Log.d("service 네트워크 상태 : ",checknetworkStatus(MyService.this));
                        Log.d("service 배터리 상태 : ", String.valueOf(checkbatteryStatus(MyService.this)));
                        Log.d("service 배터리 충전 상태 : ",checkChargeStatus(MyService.this));
                        Log.d("service 핸드폰 현재 온도 상태 : ",String.valueOf(checktemperatureStatus(MyService.this))+ (String)((char) 0x00B0 + "C"));
                    }
                }
            };
            mThread.start();
        }
        return START_STICKY;
        //서비스가 예기치않게 종료되어도 서비스가 자동으로 재시작함
    }

    //onStopService() -> onDestroy()호출됨
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("My Service","onDestroy() 서비스 중지");
        if (mThread != null){
            mThread.interrupt();
            mThread = null;
            mCount = 0;
        }
    }

    private Handler handler = new Handler(){
        @Override public void handleMessage(@NonNull Message msg) {
            Toast.makeText(MyService.this, "동작중",Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static String checknetworkStatus(Context context){
        String networkStatus = "";

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // 활성화된 네트워크의 상태를 표현하는 객체
        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
        if (nc != null) {
            if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                networkStatus = "와이파이 연결";
            } else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                networkStatus = "셀룰러 연결";
            }
        }
        else {
            networkStatus = "인터넷 연결 안됨";
        }
        return networkStatus;
    }

    public static float checkbatteryStatus(Context context){
        String battery = "";

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level * 100 / (float)scale;

        return batteryPct;
    }


    public static String checkChargeStatus(Context context){
        String isCharge = "충전 중";
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        if(isCharging == true){
            isCharge = "충전 중";
        }
        else{
            isCharge = "충전 중이 아님";
        }
        return isCharge;
    }

    public static float checktemperatureStatus(Context context){
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, intentFilter);

        float status = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10;

        return status;
    }
}
