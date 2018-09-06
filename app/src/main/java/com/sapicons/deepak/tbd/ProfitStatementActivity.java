package com.sapicons.deepak.tbd;

import android.app.ProgressDialog;
import android.icu.text.NumberFormat;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sapicons.deepak.tbd.Objects.AccountItem;
import com.sapicons.deepak.tbd.Objects.CollectItem;
import com.sapicons.deepak.tbd.Objects.ExpenseItem;

import java.util.Locale;

public class ProfitStatementActivity extends AppCompatActivity {

    TextView profitTv, revenueTv, expensesTv,dueTv;

    ProgressDialog progressDialog;

    FirebaseUser user;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profit_statement);

        setTitle("Profit Statement");

        initialiseUI();
        updatePER();

    }

    private void initialiseUI(){

        profitTv = findViewById(R.id.aps_total_profit_tv);
        revenueTv = findViewById(R.id.aps_total_revenue_tv);
        expensesTv = findViewById(R.id.aps_total_expenses_tv);
        dueTv = findViewById(R.id.aps_total_due_tv);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait ...");
    }




    private void updatePER(){
        progressDialog.show();

        updateProfit();
        updateRevenue();
        updateExpenses();
        updateTotalDue();


    }
    private void updateProfit(){
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

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

                progressDialog.dismiss();

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
}
