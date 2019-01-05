package com.sapicons.deepak.tbd.c_manager;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sapicons.deepak.tbd.Objects.AccountItem;
import com.sapicons.deepak.tbd.Objects.CGroupItem;
import com.sapicons.deepak.tbd.Objects.CustomerItem;
import com.sapicons.deepak.tbd.R;
import com.sapicons.deepak.tbd.c_manager.model.CCommissionItem;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import es.dmoral.toasty.Toasty;

public class CommissionCalculationActivity extends AppCompatActivity {

    String TAG = "COMMISSION_CALCULATION";
    CustomerItem customerItem;
    CGroupItem groupItem;
    int noOfCustomers;

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
        noOfCustomers = intent.getIntExtra("no_of_customers",1);

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
        profitTv.setText(CCalculationsUtil.getProfit(groupItem)+"");

        setActions();

    }

    public void setActions(){
        cWonAmtEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(charSequence.length()>0) {
                    commisionTv.setText(CCalculationsUtil.getCommisionAmount(Float.parseFloat(charSequence.toString()), groupItem) + "");
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
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCCommissionTable();
            }
        });
    }


    public void updateCCommissionTable(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String commissionId = System.currentTimeMillis()+"";
        float totalCommission = Float.parseFloat(commisionTv.getText().toString());
        final float commissionPerMember = totalCommission/noOfCustomers;

        CCommissionItem cCommissionItem = new CCommissionItem(commissionId,groupItem.getGroupID(),groupItem.getGroupName(),
                customerItem.getCustomerId(),customerItem.getFirstName()+ " "+customerItem.getLastName(),
                noOfCustomers,totalCommission,commissionPerMember);

        DocumentReference CCommissionRef = FirebaseFirestore.getInstance().collection("users")
                .document(user.getEmail()).collection("ccommission").document(commissionId);

        CCommissionRef.set(cCommissionItem).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toasty.success(CommissionCalculationActivity.this,"Success!").show();
                getCAccountsForThisTransaction(commissionPerMember);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"error uploading commissionItem: "+e);
                Toasty.error(CommissionCalculationActivity.this,"Error updating Commission.").show();
            }
        });
    }

    public void getCAccountsForThisTransaction(final float commissionPerMember){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final List<AccountItem> cAccountsList = new ArrayList<>();

        CollectionReference cAccountsRef = FirebaseFirestore.getInstance().collection("users")
                .document(user.getEmail()).collection("accounts");

        cAccountsRef.whereEqualTo("cId",groupItem.getGroupID()).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if(e!=null){
                    Log.w(TAG,"Fetch failed: E: "+e);
                    return;
                }
                for(QueryDocumentSnapshot doc: queryDocumentSnapshots){
                    AccountItem accountItem = doc.toObject(AccountItem.class);
                    cAccountsList.add(accountItem);
                }

                updateCommissionPerMemberInCAccounts(commissionPerMember,cAccountsList);
            }
        });
    }

    public void updateCommissionPerMemberInCAccounts(float commissionPerMember, List<AccountItem> cAccountsList){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference cAccountRef = FirebaseFirestore.getInstance().collection("users")
                .document(user.getEmail());

        for(final AccountItem accountItem : cAccountsList){
            cAccountRef.collection("accounts").document(accountItem.getAccountNumber())
                    .update("commissionPerMember",commissionPerMember+"")
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.w(TAG,"Commission Set for C Account: "+accountItem.getFirstName());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG,"Failed to update commission per member. E: "+e);
                }
            });
        }
    }
}
