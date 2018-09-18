package com.sapicons.deepak.tbd;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sapicons.deepak.tbd.Adapters.CustomerItemAdapter;
import com.sapicons.deepak.tbd.Objects.AccountItem;
import com.sapicons.deepak.tbd.Objects.CGroupItem;
import com.sapicons.deepak.tbd.Objects.CollectItem;
import com.sapicons.deepak.tbd.Objects.CustomerItem;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Date;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

public class AddAccountActivity extends AppCompatActivity  {
    CustomerItem selectedCustomer;
    Calendar myCalendar,endCalender;
    EditText amtET,interestPctEt,startDateEt,endDateEt;
    DatePickerDialog.OnDateSetListener date,endDate;
    LinearLayout interestLL, cGroupLL;
    FloatingActionButton saveBtn;

    TextView customerNameTV;
    Spinner selectAccTypeSpinner,selectCGroupSpinner;

    String selectedAccType;

    PopupWindow popupWindow;



    List<CGroupItem> groupItems;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);
        setTitle("Add Account");

        Log.d("ACTIVITY","AddAccountActivity");

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();

        selectedCustomer = (CustomerItem)bundle.getSerializable("selected_customer");

        updateUI();
        setDate();
    }



    private void updateUI(){
        myCalendar = Calendar.getInstance();
        endCalender = Calendar.getInstance();

        startDateEt= (EditText) findViewById(R.id.add_acc_start_date_et);
        customerNameTV = findViewById(R.id.add_acc_customer_name_et);
        selectAccTypeSpinner = findViewById(R.id.add_acc_choose_ac_type_spinner);
        saveBtn = findViewById(R.id.add_acc_save_btn);
        amtET = findViewById(R.id.add_acc_amount_et);
        interestPctEt = findViewById(R.id.add_acc_interest_pct_et);
        endDateEt = findViewById(R.id.add_acc_end_date_et);

        interestLL = findViewById(R.id.add_acc_interest_ll);
        cGroupLL = findViewById(R.id.add_acc_cgroup_ll);
        selectCGroupSpinner = findViewById(R.id.add_acc_choose_cgroup_spinner);



        //add text watcher
        interestPctEt.addTextChangedListener(watcher);
        amtET.addTextChangedListener(watcher);

        List<String> accTypes = new ArrayList<String>();
        accTypes.add("D Account");
        accTypes.add("M Account");
        accTypes.add("C Account");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, accTypes);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        selectAccTypeSpinner.setAdapter(dataAdapter);

        // Spinner click listener
        selectAccTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                // On selecting a spinner item
                selectedAccType = parent.getItemAtPosition(position).toString();
                Log.d("TAG","ACC TYPE :"+selectedAccType);

                if(selectedAccType.contains("D") || selectedAccType.contains("M")) {
                    Log.d("TAG","D or M");
                    interestPctEt.setText("");
                    cGroupLL.setVisibility(View.GONE);
                    interestLL.setVisibility(View.VISIBLE);
                    setEndDate(0); //automatically set end date and interest rates

                }
                else if(selectedAccType.equals("C Account")) {
                    //add a C account
                    Toasty.info(AddAccountActivity.this, "Inflate C groups").show();
                    cGroupLL.setVisibility(View.VISIBLE);
                    interestLL.setVisibility(View.GONE);
                    interestPctEt.setText("0");


                    getCGroupsFromFirestore();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        customerNameTV.setText(selectedCustomer.getFirstName()+" "+selectedCustomer.getLastName());

        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateStartDate();
                setEndDate(0);
            }

        };

        endDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                endCalender.set(Calendar.YEAR, year);
                endCalender.set(Calendar.MONTH, monthOfYear);
                endCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                setEndDate(1);
            }
        };



        startDateEt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setEndDate(0);

                new DatePickerDialog(AddAccountActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();

            }
        });

        endDateEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(AddAccountActivity.this,endDate,
                        endCalender.get(Calendar.YEAR),endCalender.get(Calendar.MONTH),
                        endCalender.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(AddAccountActivity.this);

                builder.setTitle("Save Account?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //add account
                                addAccountToFirestore();


                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
            }
        });

        interestPctEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                PopupWindow popUp = popupWindowsort();
                popUp.showAsDropDown(view, 0, 0); // show popup like dropdown list
            }
        });
        interestPctEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupWindow popUp = popupWindowsort();
                popUp.showAsDropDown(view, 0, 0); // show popup like dropdown list
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
            if(amtET.getText().toString().length()==0 ||
                    interestPctEt.getText().toString().length()==0 ){

                saveBtn.setVisibility(View.GONE);


            }
            else {
                if (Float.parseFloat(amtET.getText().toString()) > 0)
                    saveBtn.setVisibility(View.VISIBLE);
                else
                    saveBtn.setVisibility(View.GONE);
            }

        }
    };


    //update date when the activity starts for the first time
    private void setDate(){
        Date now = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM d, yyyy");
        startDateEt.setText(dateFormatter.format(now));
    }


    private void updateStartDate() {
        String myFormat = "MMMM d, yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        startDateEt.setText(sdf.format(myCalendar.getTime()));
    }

    private void setEndDate(int manual){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM d, yyyy");



        // if date is entered manually
        if(manual==1){
            endDateEt.setText(dateFormatter.format(endCalender.getTime()));
            Log.d("DATE","START DATE: "+myCalendar);
            Log.d("DATE","END DATE: "+endCalender);
            return;
        }
        //automatic date set
        if(selectedAccType.contains("D")) {
            long time = myCalendar.getTimeInMillis();
            time = time+100*24*60*60*1000l;

            endCalender.setTimeInMillis(time);

            Date endDate = new Date(time);
            endDateEt.setText(dateFormatter.format(endDate));
            interestPctEt.setText("3.0");
        }else if(selectedAccType.contains("M")){


            Date endDate =  new Date(4102338600000l);
            endCalender.setTimeInMillis(4102338600000l);
            endDateEt.setText(dateFormatter.format(endDate));

            //interestPctEt.getText().clear();
        }
        else if(selectedAccType.contains("C")){
            interestPctEt.getText().clear();
        }


        Log.d("DATE","START DATE: "+myCalendar);
        Log.d("DATE","END DATE: "+endCalender);


    }

    private void addAccountToFirestore(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // create firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Calendar cal = Calendar.getInstance();

        long timestamp = cal.getTimeInMillis() ;


        final String accNumber = timestamp+"";
        //String startDate = startDateEt.getText().toString();
        String startDate = myCalendar.getTimeInMillis()+"";
        String endDate = endCalender.getTimeInMillis()+"";
        String phoneNumber = selectedCustomer.getPhone();
        String firstName = selectedCustomer.getFirstName();
        String lastName = selectedCustomer.getLastName();
        String accountType = selectedAccType;
        String interestPct = interestPctEt.getText().toString();
        String amount = amtET.getText().toString();
        String actualAmt = amount;
        String dueAmt = amount;
        String accoutStatus = "open";
        String customerPicUrl = selectedCustomer.getPhotoUrl();

        String loanAmt = dueAmt;
        String actualLoanAmt;
        if(accountType.contains("D")){
            actualLoanAmt = (Float.parseFloat(loanAmt)- (0.1* Float.parseFloat(loanAmt)))+"";
        }else
            actualLoanAmt = dueAmt;

        final DocumentReference newAccRef = db.collection("users").document(user.getEmail())
                .collection("accounts").document(accNumber);

        final AccountItem accountItem = new AccountItem(accNumber,startDate,endDate,accountType,firstName,
                lastName,phoneNumber,amount,actualAmt,dueAmt,interestPct, accoutStatus,
                customerPicUrl,loanAmt,actualLoanAmt,"0","0",selectedCustomer.getCustomerId());

        newAccRef.set(accountItem)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG","Account added. AccNo: "+ accNumber);
                        addProfitCollectionForDAcc(accountItem);
                        sendMessageForAccCreation(accountItem);


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG","Error adding account: "+ e);

            }
        });
        Toasty.success(AddAccountActivity.this, "Account Added!").show();
        finish();

    }


    /**
     * show popup window method reuturn PopupWindow
     */
    private PopupWindow popupWindowsort() {

        // initialize a pop up window type
        popupWindow = new PopupWindow(this);

        ArrayList<String> sortList = new ArrayList<String>();
        for(double i=0.5;i<=9.0;i+=0.5){
            sortList.add(i+"");
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                sortList);
        // the drop down list is a list view

        ListView listViewSort = new ListView(this);

        // set our adapter and pass our pop up window contents
        listViewSort.setAdapter(adapter);
        listViewSort.setBackgroundColor(Color.parseColor("#ffffff"));

        // set on item selected
        listViewSort.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                interestPctEt.setText((String)adapter.getItem(i)+"");
                popupWindow.dismiss();
            }
        });

        // some other visual settings for popup window
        popupWindow.setFocusable(true);
        popupWindow.setWidth(250);
        // popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.white));
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        // set the listview as popup content
        popupWindow.setContentView(listViewSort);


        return popupWindow;
    }

    private void addProfitCollectionForDAcc(AccountItem accountItem){

        if(accountItem.getAccoutType().contains("D")){

            Date date = new Date();
            String timestamp = date.getTime()+"";
            String profit = (0.1*Float.parseFloat(accountItem.getLoanAmt()))+"";
            String accNo = accountItem.getAccountNumber();
            String accType = accountItem.getAccoutType();

            CollectItem collectItem = new CollectItem(accNo,timestamp,"0",profit,accType);


            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            DocumentReference collectRef = db.collection("users").document(user.getEmail())
                    .collection("collections").document(timestamp);

            collectRef.set(collectItem).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("TAG","Failed to write collection amount. "+e);
                }
            });
        }

    }

    private void getCGroupsFromFirestore(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //final CollectionReference docRef = db.collection("users").document(user.getEmail()).collection("");

        groupItems = new ArrayList<CGroupItem>();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Getting C Groups. Please Wait ...");
        progressDialog.show();

        db.collection("users").document(user.getEmail()).collection("groups")

                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("TAG", "Listen failed.", e);
                            return;
                        }


                        for (QueryDocumentSnapshot doc : value) {
                            CGroupItem newItem = doc.toObject(CGroupItem.class);
                            Log.d("TAG","GId: "+newItem.getGroupID());
                            Log.d("TAG","GName: "+newItem.getGroupName());
                            groupItems.add(newItem);

                        }

                        progressDialog.dismiss();
                        setCGroupDropDown(groupItems);

                    }
                });
    }


    private void setCGroupDropDown(final List<CGroupItem> groupItems){

        List<String> gName = new ArrayList<String>();
        for(CGroupItem item : groupItems)
            gName.add(item.getGroupName());

        // Creating adapter for spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, gName);

        // Drop down layout style - list view with radio button
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        selectCGroupSpinner.setAdapter(adapter);

        // Spinner click listener
        selectCGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setStartAndEndDateForCAccount(groupItems.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setStartAndEndDateForCAccount(CGroupItem item){
        long startDate = Long.parseLong(item.getStartDate());
        long endDate = Long.parseLong(item.getEndDate());

        myCalendar.setTimeInMillis(startDate);
        endCalender.setTimeInMillis(endDate);

        Log.d("TAG","START DATE: "+myCalendar);
        Log.d("TAG","END DATE: "+endCalender);
        updateStartDate();
        setEndDate(1);
    }


    private  void sendMessageForAccCreation(AccountItem accountItem){
        String msg= accountItem.getAccoutType()+" Created: "+accountItem.getAccountNumber()+" Due Amount: "+accountItem.getDueAmt();
        String phoneNumber = accountItem.getPhoneNumber();

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, msg, null, null);
            Toasty.success(this,"Message Sent").show();
        } catch (Exception ex) {
            //Toast.makeText(getContext(),ex.getMessage().toString(),
            //      Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }
}
