package com.sapicons.deepak.tbd;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sapicons.deepak.tbd.Objects.CGroupItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class AddCGroupActivity extends AppCompatActivity {

    Calendar startCalendar,endCalender;
    EditText nameET,noOfMonthsEt,startDateEt,endDateEt;
    DatePickerDialog.OnDateSetListener startDate,endDate;
    FloatingActionButton saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cgroup);

        Log.d("ACTIVITY","AddCGroupActivity");

        setTitle("Add C Group");


        initialiseUI();
        setStartDate();
    }

    private void initialiseUI(){

        startCalendar = Calendar.getInstance();
        endCalender = Calendar.getInstance();

        nameET = findViewById(R.id.add_cgroup_name_et);
        noOfMonthsEt = findViewById(R.id.add_cgroup_no_of_months_et);
        startDateEt = findViewById(R.id.add_cgroup_start_date_et);
        endDateEt = findViewById(R.id.add_cgroup_end_date_et);
        saveBtn = findViewById(R.id.add_cgroup_done_fab);


        nameET.addTextChangedListener(watcher);
        noOfMonthsEt.addTextChangedListener(watcher);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(AddCGroupActivity.this);

                builder.setTitle("Save Group?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //add Group to Firebase
                                addGroupToFirestore();

                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                if(startCalendar.getTimeInMillis() < endCalender.getTimeInMillis())
                    builder.show();
                else
                    Toasty.error(AddCGroupActivity.this,"Start and End Date can't be the same").show();
            }
        });

        // set start date after date picked
        startDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

                startCalendar.set(Calendar.YEAR, year);
                startCalendar.set(Calendar.MONTH, monthOfYear);
                startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateStartDate();

            }

        };

        // set end date after date picked
        endDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                endCalender.set(Calendar.YEAR, year);
                endCalender.set(Calendar.MONTH, monthOfYear);
                endCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                setEndDate(0);
                setNoOfMonths();
            }
        };

        // open start date picker dialog box
        startDateEt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //setEndDate(0);

                new DatePickerDialog(AddCGroupActivity.this, startDate, startCalendar
                        .get(Calendar.YEAR), startCalendar.get(Calendar.MONTH),
                        startCalendar.get(Calendar.DAY_OF_MONTH)).show();

            }
        });

        // open end date picker dialog box
        endDateEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(AddCGroupActivity.this,endDate,
                        endCalender.get(Calendar.YEAR),endCalender.get(Calendar.MONTH),
                        endCalender.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


    }

    private final TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if(nameET.getText().toString().length()==0 ||
                    noOfMonthsEt.getText().toString().length()==0 ){

                saveBtn.setVisibility(View.GONE);


            }else
                saveBtn.setVisibility(View.VISIBLE);

            if(noOfMonthsEt.getText().toString().length() >0){
                float noOfMonths = Float.parseFloat(noOfMonthsEt.getText().toString());


                setEndDate(noOfMonths);
            }

        }
    };

    private void setStartDate(){
        Date now = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM d, yyyy");
        startDateEt.setText(dateFormatter.format(now));
    }

    private void updateStartDate() {
        String myFormat = "MMMM d, yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        startDateEt.setText(sdf.format(startCalendar.getTime()));
    }

    private  void setEndDate(float months){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM d, yyyy");
        long days = 1000*60*60*24;
        long endTimeInMilli = (long)months*days*30 + startCalendar.getTimeInMillis();
        endDateEt.setText(dateFormatter.format(endTimeInMilli));

        if(months>0)
            endCalender.setTimeInMillis(endTimeInMilli);
    }

    private void setNoOfMonths(){
        long startTimeMilli = startCalendar.getTimeInMillis();
        long endTimeMilli = endCalender.getTimeInMillis();
        long days = 1000*60*60*24;
        float noOfMonths = (float)((endTimeMilli - startTimeMilli)/(days*30));
        noOfMonths =(float) (Math.round(noOfMonths*100.0)/100.0);

        if(noOfMonths<0)
            Toasty.error(AddCGroupActivity.this,"End Date can't be less than Start Date").show();
        else
            noOfMonthsEt.setText(noOfMonths+"");
    }

    private void addGroupToFirestore(){
        Log.d("DATE","START DATE: "+startCalendar);
        Log.d("DATE","END DATE: "+endCalender);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        long timestamp = Calendar.getInstance().getTimeInMillis() ;
        final String groupId = timestamp+"";
        String groupName = nameET.getText().toString();
        String noOfmonths= noOfMonthsEt.getText().toString();
        String startDate = startCalendar.getTimeInMillis()+"";
        String endDate= endCalender.getTimeInMillis()+"";

        CGroupItem newGroup = new CGroupItem(groupId,groupName,noOfmonths,startDate,endDate);

        final DocumentReference newGroupRef = db.collection("users").document(user.getEmail())
                .collection("groups").document(groupId);

        newGroupRef.set(newGroup)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG","Group added. ID: "+ groupId);



                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG","Error adding account: "+ e);

            }
        });
        Toasty.success(AddCGroupActivity.this, "Group Added!").show();
        finish();
    }
}
