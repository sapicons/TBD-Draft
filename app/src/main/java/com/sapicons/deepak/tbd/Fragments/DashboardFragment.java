package com.sapicons.deepak.tbd.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.NumberFormat;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sapicons.deepak.tbd.AddCustomerActivity;
import com.sapicons.deepak.tbd.AddExpenseActivity;
import com.sapicons.deepak.tbd.CollectActivity;
import com.sapicons.deepak.tbd.DisplayCustomerListActivity;
import com.sapicons.deepak.tbd.Objects.AccountItem;
import com.sapicons.deepak.tbd.Objects.CollectItem;
import com.sapicons.deepak.tbd.Objects.CustomerItem;
import com.sapicons.deepak.tbd.Objects.ExpenseItem;
import com.sapicons.deepak.tbd.R;
import com.sapicons.deepak.tbd.TodaysDueActivity;

import java.util.Locale;

import at.markushi.ui.CircleButton;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by Deepak Prasad on 25-07-2018.
 */

public class DashboardFragment extends Fragment {

    ProgressDialog progressDialog;
    TextView profitTv, revenueTv, expensesTv,dueTv;
    LinearLayout profitLL,revenueLL,expensesLL,dueLL;

    FirebaseUser user;
    FirebaseFirestore db;

    static final int PICK_CONTACT=1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_dashboard,container,false);

    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Dashboard");
        Log.d("FRAGMENT","DashboardFragment");


        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        initialiseUI(view);
        updatePER();

    }

    private void initialiseUI(View view){

       FancyButton addCustomerBtn = view.findViewById(R.id.frag_dash_add_customer_btn),
               addAccountBtn = view.findViewById(R.id.frag_dash_add_account_btn),
               collectBtn = view.findViewById(R.id.frag_dash_collect_btn),
               addExpenseBtn = view.findViewById(R.id.frag_dash_add_expense_btn),
                dueTodayBtn = view.findViewById(R.id.frag_dash_due_today_btn);
       profitTv = view.findViewById(R.id.fd_total_profit_tv);
       revenueTv = view.findViewById(R.id.fd_total_revenue_tv);
       expensesTv = view.findViewById(R.id.fd_total_expenses_tv);
       profitLL = view.findViewById(R.id.fd_profit_ll);
       revenueLL= view.findViewById(R.id.fd_revenue_ll);
       expensesLL = view.findViewById(R.id.fd_expenses_ll);
       dueLL = view.findViewById(R.id.fd_due_ll);
       dueTv = view.findViewById(R.id.fd_total_due_tv);


       addCustomerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(getActivity(), AddCustomerActivity.class));
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Open Contacts?");
                builder.setNeutralButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // open Add contacts
                        startActivity(new Intent(getActivity(), AddCustomerActivity.class));
                    }
                }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openOrAddContacts();
                    }
                });
                builder.create().show();
           }
        });

       addAccountBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

               Intent intent = new Intent(getActivity(), DisplayCustomerListActivity.class);
               startActivity(intent);
           }
       });

       collectBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               startActivity(new Intent(getActivity(), CollectActivity.class));
           }
       });

       addExpenseBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               startActivity(new Intent(getActivity(), AddExpenseActivity.class));
           }
       });

       dueTodayBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

               startActivity(new Intent(getActivity(),TodaysDueActivity.class));
           }
       });


    }

    private void updatePER(){

        updateProfit();
        updateRevenue();
        updateExpenses();
        updateTotalDue();


    }

    @Override
    public void onResume() {
        super.onResume();
        displayOrHidePER();
    }

    private  void displayOrHidePER(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean showProfit = sharedPreferences.getBoolean(getResources().getString(R.string.key_show_total_profit),true);
        boolean showRevenue = sharedPreferences.getBoolean(getResources().getString(R.string.key_show_total_revenue),true);
        boolean showExpenses = sharedPreferences.getBoolean(getResources().getString(R.string.key_show_total_expenses),true);
        boolean showDue = sharedPreferences.getBoolean(getResources().getString(R.string.key_show_total_due),true);

        if(!showProfit)  profitLL.setVisibility(View.GONE);
        else profitLL.setVisibility(View.VISIBLE);

        if(!showExpenses) expensesLL.setVisibility(View.GONE);
        else  expensesLL.setVisibility(View.VISIBLE);

        if(!showRevenue) revenueLL.setVisibility(View.GONE);
        else revenueLL.setVisibility(View.VISIBLE);

        if(!showDue) dueLL.setVisibility(View.GONE);
        else dueLL.setVisibility(View.VISIBLE);
    }
    private void updateProfit(){
        final CollectionReference collectionRef = db.collection("users").document(user.getEmail())
                .collection("collections");



        collectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                float profit=0.0f;

                if (e != null) {
                    Log.w("DF", "Listen failed.", e);
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {
                    CollectItem collectItem = doc.toObject(CollectItem.class);
                    profit+=Float.parseFloat(collectItem.getProfitAmount());

                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "in"));
                    profitTv.setText(numberFormat.format(profit));
                } else {

                    java.text.NumberFormat numberFormat = java.text.NumberFormat.getNumberInstance(Locale.US);
                    profitTv.setText(numberFormat.format(profit));
                }

            }
        });
    }
    private void updateRevenue(){
        final CollectionReference collectionRef = db.collection("users").document(user.getEmail())
                .collection("accounts");

        collectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {

                float revenue=0.0f;

                if (e != null) {
                    Log.w("DF", "Listen failed.", e);
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {
                    AccountItem accountItem = doc.toObject(AccountItem.class);
                    revenue+=Float.parseFloat(accountItem.getActualLoanAmt());

                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "in"));
                    revenueTv.setText(numberFormat.format(revenue));
                } else {

                    java.text.NumberFormat numberFormat = java.text.NumberFormat.getNumberInstance(Locale.US);
                    revenueTv.setText(numberFormat.format(revenue));
                }

            }
        });
    }
    private void updateExpenses(){
        final CollectionReference collectionRef = db.collection("users").document(user.getEmail())
                .collection("expenses");

        collectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {


                float expenses =0.0f;
                if (e != null) {
                    Log.w("DF", "Listen failed.", e);
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {
                    ExpenseItem expenseItem = doc.toObject(ExpenseItem.class);
                    expenses+=Float.parseFloat(expenseItem.getAmount());

                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "in"));
                    expensesTv.setText(numberFormat.format(expenses));
                } else {

                    java.text.NumberFormat numberFormat = java.text.NumberFormat.getNumberInstance(Locale.US);
                    expensesTv.setText(numberFormat.format(expenses));
                }

            }
        });
    }

    private void updateTotalDue(){
        final CollectionReference collectionRef = db.collection("users").document(user.getEmail())
                .collection("accounts");

        collectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {


                float due =0.0f;
                if (e != null) {
                    Log.w("DF", "Listen failed.", e);
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {
                    AccountItem accountItem = doc.toObject(AccountItem.class);
                    due+=Float.parseFloat(accountItem.getDueAmt());

                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "in"));
                    dueTv.setText(numberFormat.format(due));
                } else {

                    java.text.NumberFormat numberFormat = java.text.NumberFormat.getNumberInstance(Locale.US);
                    dueTv.setText(numberFormat.format(due));
                }

            }
        });
    }

    public void openOrAddContacts(){

        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CONTACTS},
                    1);
        }

        else{


            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, PICK_CONTACT);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (PICK_CONTACT):

                if (resultCode == Activity.RESULT_OK) {

                    String name="",number="";

                    Uri contactData = data.getData();
                    Cursor c = getActivity().managedQuery(contactData, null, null, null, null);
                    if (c.moveToFirst()) {


                        String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                        String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getActivity().getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                    null, null);
                            phones.moveToFirst();
                            number = phones.getString(phones.getColumnIndex("data1"));
                            //
                        }
                        name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        Log.d("DashboardFragment","Name is:" + name);

                    }

                    number = formatPhoneNumber(number);
                    Log.d("DashboardFragment","number is:" + number);

                    // open Add contacts
                    addCustomerWithContactsDetails(name,number);
                }
                break;
        }
    }

    public String formatPhoneNumber(String number){
        number=number.replace(" ","");
        number=number.replace("-","");

        return number;
    }

    public void addCustomerWithContactsDetails(String name, String number){
        Intent intent = new Intent(getActivity(), AddCustomerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("full_name",name);
        bundle.putString("number",number);

        intent.putExtras(bundle);
        startActivity(intent);
    }

}
