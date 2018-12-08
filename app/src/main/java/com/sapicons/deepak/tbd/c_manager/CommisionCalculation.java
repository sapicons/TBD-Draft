package com.sapicons.deepak.tbd.c_manager;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.sapicons.deepak.tbd.Objects.CGroupItem;
import com.sapicons.deepak.tbd.Objects.CustomerItem;
import com.sapicons.deepak.tbd.R;

public class CommisionCalculation extends AppCompatActivity {

    CustomerItem customerItem;
    CGroupItem groupItem;

    TextView customerNameTv, cValueTv, durationMonthsTv, profitTv, commisionTv;
    FloatingActionButton doneBtn;
    EditText cWonAmtEt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commision_calculation);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        groupItem = (CGroupItem)bundle.getSerializable("c_group");
        customerItem = (CustomerItem)bundle.getSerializable("customer_item");

        initialiseViews();
    }

    public void initialiseViews(){
        customerNameTv = findViewById(R.id.commision_calc_customer_name_tv);
        cValueTv = findViewById(R.id.commision_calc_c_value_tv);
        durationMonthsTv = findViewById(R.id.commision_calc_duration_tv);
        profitTv = findViewById(R.id.commision_calc_profit_tv);
        commisionTv = findViewById(R.id.commision_calc_commision_tv);

        doneBtn = findViewById(R.id.commision_calc_done_fab);
        cWonAmtEt = findViewById(R.id.commision_calc_c_won_et);

        customerNameTv.setText(customerItem.getFirstName()+" "+customerItem.getLastName());
        durationMonthsTv.setText(groupItem.getNoOfMonths());
        cValueTv.setText(groupItem.getAmount());
        profitTv.setText(getProfit(groupItem)+"");


        cWonAmtEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(charSequence.length()>0) {
                    commisionTv.setText(getCommisionAmount(Float.parseFloat(charSequence.toString()), groupItem) + "");
                    doneBtn.setVisibility(View.VISIBLE);
                }else{
                    commisionTv.setText("0");
                    doneBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public float getProfit(CGroupItem groupItem){
        float cValue = Float.parseFloat(groupItem.getAmount());
        float noOfMonths = Float.parseFloat(groupItem.getNoOfMonths());
        return (cValue/noOfMonths);
    }

    public float getCommisionAmount(float cWonAmt, CGroupItem groupItem){
        float cValue = Float.parseFloat(groupItem.getAmount());
        float commision = cValue - cWonAmt - getProfit(groupItem);
        return commision;
    }
}
