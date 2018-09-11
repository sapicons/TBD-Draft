package com.sapicons.deepak.tbd;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import mehdi.sakout.fancybuttons.FancyButton;

public class CManagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cmanager);

        setTitle("C Manager");

        initialiseUI();
    }

    private void initialiseUI(){

        FancyButton addCGroup = findViewById(R.id.activity_cmanager_add_cmanager_btn);



        addCGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(CManagerActivity.this,AddCGroupActivity.class));
            }
        });
    }
}
