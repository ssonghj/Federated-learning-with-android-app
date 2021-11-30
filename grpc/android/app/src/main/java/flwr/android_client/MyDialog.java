package flwr.android_client;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class MyDialog extends Dialog {


    Context context;
    private TextView networkStatus;
    private TextView batteryStatus;
    private TextView chargeStatus;
    private TextView temperatureStatus;
    private Button okBtn;
    private Switch autoOnbtn;


    public MyDialog(Context context) {
        super(context);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog);

        networkStatus = findViewById(R.id.networkStatus);
        batteryStatus = findViewById(R.id.batteryStatus);
        chargeStatus = findViewById(R.id.chargeStatus);
        temperatureStatus = findViewById(R.id.temperatureStatus);
        okBtn = findViewById(R.id.okBtn);
        autoOnbtn = findViewById(R.id.autoOnbtn);

        context = getContext().getApplicationContext();

        networkStatus.setText("네트워크 상태 : "+checknetworkStatus(context));
        batteryStatus.setText("배터리 상태 : "+checkbatteryStatus(context));
        chargeStatus.setText("배터리 충전 상태 : "+checkChargeStatus(context));
        temperatureStatus.setText("핸드폰 현재 온도 상태 : "+checktemperatureStatus(context)+ (char) 0x00B0 + "C");


        autoOnbtn.setChecked(loadSwitchState(context));

        //일정시간마다 상태 확인
        autoOnbtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    autoOnbtn.setChecked(true);
                    saveSwitchState(context, true);
                    //서비스 켜기
                    Intent intent = new Intent(context, MyService.class);
                    context.startService(intent);


                }else{
                    autoOnbtn.setChecked(false);
                    saveSwitchState(context, false);
                    //서비스 끄기
                    Intent intent = new Intent(context, MyService.class);
                    context.stopService(intent);

                }
            }
        });

        //확인 버튼
        okBtn.setOnClickListener(v -> {
            dismiss();
        });

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

    public static boolean loadSwitchState(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(
                "SwitchPref", Context.MODE_PRIVATE);
        boolean switchState = sharedPref.getBoolean("switchState",false);
        return switchState;
    }

    public static void saveSwitchState(Context context, boolean state){
        SharedPreferences sharedPreferences = context.getSharedPreferences("SwitchPref",Context.MODE_PRIVATE);
        //값 저장시에는 editor 반드시 불러와줘야한다.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //editText에 입력한 내용을 불러와서 value에 넣어준다.
        boolean switchState = state;
        //value라는 이름에 value값 저장한다.
        editor.putBoolean("switchState",switchState);
        //commit 반드시 해줘야지 최종적으로 기기에 저장된다.
        editor.commit();
    }
}



