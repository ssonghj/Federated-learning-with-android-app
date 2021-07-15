package com.example.poc;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class start_server extends AppCompatActivity {
    //버튼 클릭시
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_start);//초기 레이아웃
        Button btn_return = findViewById(R.id.return_upload);

        // 메인으로 이동하는 버튼
        btn_return.setOnClickListener(new View.OnClickListener() {//return 버튼 클릭시 반응
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

}
