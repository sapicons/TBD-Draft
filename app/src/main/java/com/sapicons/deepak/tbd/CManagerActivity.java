package com.sapicons.deepak.tbd;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.sapicons.deepak.tbd.c_manager.CGroups;

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

        FancyButton addCGroup = findViewById(R.id.activity_cmanager_add_cmanager_btn),
                cCustomersBtn = findViewById(R.id.activity_cmanager_c_customers_btn),
                cClosedBtn = findViewById(R.id.activity_cmanager_c_closed_btn),
                cDayBtn = findViewById(R.id.activity_cmanager_c_day_btn);



        addCGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(CManagerActivity.this,AddCGroupActivity.class));
            }
        });

        cCustomersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CManagerActivity.this,CGroups.class);
                intent.putExtra("group_status","open");
                startActivity(intent);
            }
        });

        cClosedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CManagerActivity.this,CGroups.class);
                intent.putExtra("group_status","closed");
                startActivity(intent);
            }
        });
    }
}
