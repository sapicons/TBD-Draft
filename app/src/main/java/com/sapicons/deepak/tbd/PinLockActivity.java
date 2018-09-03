package com.sapicons.deepak.tbd;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class PinLockActivity extends AppCompatActivity {

    public static final String TAG = "PinLockView";

    private PinLockView mPinLockView;
    private IndicatorDots mIndicatorDots;
    private TextView instructionTV;
    private TextView forgotPinLockTv;

    SharedPreferences sharedPreferences ;
    SharedPreferences.Editor editor;
    //String iPin="";


    String savedPin, initialPin="";
    int step=1;

    private PinLockListener mPinLockListener = new PinLockListener() {
        @Override
        public void onComplete(String pin) {

            Log.d(TAG, "Pin complete: " + pin);
            Log.d("TAG","Step: "+step);
            Log.d("TAG", "IP: "+initialPin+"  P: "+pin);

            //if pin exists
            if(savedPin.length()>0){
                if(savedPin.equalsIgnoreCase(pin)){

                    //pin match. direct user to main activity
                    Log.d("TAG","Success PIN match");
                    //Toast.makeText(this,"success pin match",Toast.LENGTH_SHORT).show();
                    Toast.makeText(PinLockActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                    directUser();
                }
                else{
                    //pin mismatch. display error and reset
                    instructionTV.setText("Wrong PIN. Try Again!");
                    mPinLockView.resetPinLockView();
                }
            }
            //if pin doesn't exits. New pin to be created
            else if(savedPin.length()==0){
                //step 1: save the pin and ask for confirmation
                if(step==1){
                    initialPin = pin;
                    mPinLockView.resetPinLockView();
                    instructionTV.setText("Confirm PIN");


                }
                //step 2: match the confirmed pin
                if(step ==2){

                    //if both pin matches, save the pin to sharedPref and direct user to main activity
                    if(initialPin.equalsIgnoreCase(pin)){
                        editor.putString("existing_pin",pin);
                        editor.apply();
                        editor.commit();
                        instructionTV.setText("Pin Created");
                        directUser();
                    }
                    //if pins mismatch, reset pinLockView, delete initialPin, revert to step 1;
                    else{
                        step = 1;
                        mPinLockView.resetPinLockView();
                        initialPin="";
                        instructionTV.setText("PIN mismatch. Enter New PIN");
                    }
                }
            }


        }

        @Override
        public void onEmpty() {
            Log.d(TAG, "Pin empty");

        }

        @Override
        public void onPinChange(int pinLength, String intermediatePin) {

            // change to step 2 if step 1 is completed
            if(!initialPin.equalsIgnoreCase("") && step==1)
                step =2;

            Log.d(TAG, "Pin changed, new length " + pinLength + " with intermediate pin " + intermediatePin);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_pin_lock);

        Log.d("ACTIVITY","PinLockActivity");

        //Shared preferences to store,validate and create pin
        sharedPreferences = this.getSharedPreferences("pin",0);
        editor = sharedPreferences.edit();
        savedPin = sharedPreferences.getString("existing_pin","");
        step =1;

        Log.d("TAG","savedPin: "+savedPin);
        Log.d("TAG","step : "+step);


        mPinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
        mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);
        instructionTV = findViewById(R.id.pin_lock_instruction_tv);
        forgotPinLockTv = findViewById(R.id.activity_pin_lock_forgot_pin_tv);

        // display according to new pin to be entered or log in with existing pin
        if(savedPin.length()>0)
            instructionTV.setText("Enter PIN");
        else
            instructionTV.setText("Enter new PIN");

        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mPinLockView.setPinLockListener(mPinLockListener);
        //mPinLockView.setCustomKeySet(new int[]{2, 3, 1, 5, 9, 6, 7, 0, 8, 4});
        //mPinLockView.enableLayoutShuffling();


        mPinLockView.setPinLength(4);
        mPinLockView.setTextColor(ContextCompat.getColor(this, R.color.white));



        mIndicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL_WITH_ANIMATION);

        // forgot pin
        forgotPinLockTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertBuilder= new AlertDialog.Builder(PinLockActivity.this);
                alertBuilder.setTitle("Reset PIN?");
                alertBuilder.setMessage("You will be logged Out!");
                alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        signOut();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                alertBuilder.create().show();
            }

        });
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private  void directUser(){
        startActivity(new Intent(PinLockActivity.this, Main2Activity.class));
        finish();
    }

    public void signOut(){
        AuthUI.getInstance()
                .signOut(getApplicationContext())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                        SharedPreferences preferences = getSharedPreferences("pin",0);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("existing_pin","");
                        editor.apply();
                        editor.commit();
                        preferences = getSharedPreferences("remember_me",0);
                        editor = preferences.edit();
                        editor.putBoolean("is_checked",false);
                        startActivity(new Intent(PinLockActivity.this,SignInActivity.class));
                        finish();
                    }
                });
    }

}
