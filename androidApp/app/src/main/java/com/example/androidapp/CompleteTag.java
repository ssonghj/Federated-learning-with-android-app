package com.example.androidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class CompleteTag extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complete_tag);
        getSupportActionBar().setTitle("태그 완료 이미지들");
    }
}