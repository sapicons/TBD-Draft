package com.sapicons.deepak.tbd.Adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.icu.text.NumberFormat;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sapicons.deepak.tbd.Objects.AccountItem;
import com.sapicons.deepak.tbd.Objects.CollectItem;
import com.sapicons.deepak.tbd.Objects.CustomerItem;
import com.sapicons.deepak.tbd.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by Deepak Prasad on 04-08-2018.
 */



public class AccountItemAdapter extends ArrayAdapter<AccountItem> {
    int i ;
    FancyButton collectButton;

    String COLOR_RED ="#d50000";
    String COLOR_GREEN ="#2e7d32";
    String COLOR_YELLOW ="#c56000";
    String COLOR_BLUE ="#2962ff";

    ProgressDialog progressDialog;

    public AccountItemAdapter(@NonNull Context context, int resource, @NonNull List<AccountItem> objects,int i) {

        super(context, resource, objects);
        this.i = i;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null)
            convertView = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.item_account,parent, false);

        CircleImageView customerPic = convertView.findViewById(R.id.item_account_customer_pic_iv);
        TextView customerNameTv = convertView.findViewById(R.id.item_account_customer_name),
                accTypeTv = convertView.findViewById(R.id.item_account_type_tv),
                startDateTv = convertView.findViewById(R.id.item_account_start_date_tv),
                endDateTv = convertView.findViewById(R.id.item_account_end_date_tv),
                dueAmountTv = convertView.findViewById(R.id.item_account_due_amount_tv),
                accNoTv = convertView.findViewById(R.id.item_account_acc_no_tv);

        collectButton = convertView.findViewById(R.id.item_acc_collect_btn);

        collectButton.setBackgroundColor(Color.parseColor("#2e7d32"));

        final AccountItem accountItem = getItem(position);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");
        long s = Long.parseLong(accountItem.getStartDate());
        long e = Long.parseLong(accountItem.getEndDate());
        calendar.setTimeInMillis(s);
        startDateTv.setText(dateFormatter.format(calendar.getTime()));
        calendar.setTimeInMillis(e);
        endDateTv.setText(dateFormatter.format(calendar.getTime()));


        if(accountItem.getCustomerPicUrl().length()>0)
            Glide.with(getContext()).load(accountItem.getCustomerPicUrl()).into(customerPic);
        customerNameTv.setText(accountItem.getFirstName()+" "+accountItem.getLastName());
        accTypeTv.setText(accountItem.getAccoutType());

        Float dA = Float.parseFloat(accountItem.getDueAmt());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en","in"));
            dueAmountTv.setText(numberFormat.format(dA));
        }else {

            java.text.NumberFormat numberFormat = java.text.NumberFormat.getNumberInstance(Locale.US);
            dueAmountTv.setText(numberFormat.format(dA));
        }

        Log.d("TAG","I: "+i);

        if(i==1) {
            accNoTv.setText("Acc/No: "+accountItem.getAccountNumber());
            accNoTv.setVisibility(View.VISIBLE);
        }
        else
            accNoTv.setVisibility(View.GONE);

        setCollectBtnColor(accountItem);
        collectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUpPopupWindow(accountItem);
            }
        });


        return convertView;

    }

    public void setCollectBtnColor(AccountItem accountItem){
        Calendar calendar = Calendar.getInstance();
        long currTime = calendar.getTimeInMillis();
        long day = 1000 * 60 * 60 * 24;   // a day
        if(accountItem.getAccoutType().contains("D")) {


            long startTime = Long.parseLong(accountItem.getStartDate());  //get the start date of the account
            float loanAmt = Float.parseFloat(accountItem.getLoanAmt());   //get the loan amount
            float totalCollectedAmt = 0.0f;
            if (accountItem.getTotalCollectedAmt() != null)
                totalCollectedAmt = Float.parseFloat(accountItem.getTotalCollectedAmt()); //get total collected amount till now

            float numberOfDays = (currTime - startTime) / (day);             // no of days (yesterday - start day)
            if (totalCollectedAmt >= (numberOfDays - 1) * (0.01 * loanAmt))          // total amount that must be collected for green button
                collectButton.setBackgroundColor(Color.parseColor(COLOR_GREEN));
            else if (totalCollectedAmt < (numberOfDays - 4) * (0.01 * loanAmt))
                collectButton.setBackgroundColor(Color.parseColor(COLOR_RED));
            else collectButton.setBackgroundColor(Color.parseColor(COLOR_YELLOW));
        }
        else if(accountItem.getAccoutType().contains("M")){

            long lastCollectionDate =0l;

            if(accountItem.getLatestCollectionTimestamp() != null)
                lastCollectionDate = Long.parseLong(accountItem.getLatestCollectionTimestamp());
            int noOfDays = (int)(lastCollectionDate/(day));

            if(noOfDays>= 35)
                collectButton.setBackgroundColor(Color.parseColor(COLOR_RED));
            else if(noOfDays>=31 && noOfDays<35)
                collectButton.setBackgroundColor(Color.parseColor(COLOR_YELLOW));
            else
                collectButton.setBackgroundColor(Color.parseColor(COLOR_GREEN));
        }

    }


    public void setUpPopupWindow(final AccountItem item){
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Please Wait ...");

        AlertDialog.Builder alertDialog=new AlertDialog.Builder(getContext());
        LayoutInflater inflater =(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View customView=inflater.inflate(R.layout.custom_collect_popup,null);
        final EditText amtEt = customView.findViewById(R.id.custom_collect_amt_et);

        alertDialog.setTitle("Collect Amount")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setPositiveButton("Collect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Toast.makeText(mContext, "Amt collected!", Toast.LENGTH_SHORT).show();
                float actualAmt = Float.parseFloat(item.getDueAmt());
                String eA = amtEt.getText().toString();
                if(!eA.isEmpty()) {
                    float enteredAmt = Float.parseFloat(eA);
                    if (enteredAmt > actualAmt) {
                        Toast.makeText(getContext(), "Amount collected is more than Due.", Toast.LENGTH_LONG).show();
                    } else {
                        progressDialog.show();

                        deductAmountFromAccount(item, amtEt.getText().toString());
                    }
                }


            }
        });

        alertDialog.setView(customView)
                .create().show();
    }
    private void deductAmountFromAccount(final AccountItem accountItem, String amount){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        String newAmt = (Float.parseFloat(accountItem.getDueAmt()) - Float.parseFloat(amount))+"";

        if(accountItem.getAccoutType().contains("M"))
            newAmt = accountItem.getDueAmt();

        accountItem.setDueAmt(newAmt);


        //update the new info to db
        DocumentReference accRef = db.collection("users").document(user.getEmail())
                .collection("accounts").document(accountItem.getAccountNumber());

        accRef.update("dueAmt",newAmt)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Amount Updated!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG","error updating amt: "+e);
            }
        });


        //close account id dueAmt = zero
        if(Float.parseFloat(newAmt)  < 1.0){
            Toast.makeText(getContext(), "Account is Closed!", Toast.LENGTH_SHORT).show();
            accountItem.setAccountStatus("closed");

            accRef.update("accountStatus","closed")
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //Toast.makeText(context, "Amount Updated!", Toast.LENGTH_SHORT).show();
                            //progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("TAG","error updating account status: "+e);
                }
            });
        }


        addCollectionToFirestore(accountItem,amount);

    }

    private void addCollectionToFirestore(AccountItem item, String collectedAmount){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference collectRef = db.collection("users").document(user.getEmail())
                .collection("collections");

        Date date = new Date();
        String timestamp = date.getTime()+"";
        String profit = "0";

        // profit for D account is zero
        // profit for M account is (loanAmt * interestPct/100)
        if(item.getAccoutType().contains("M")){

            profit= (Float.parseFloat(item.getLoanAmt().trim())*Float.parseFloat(item.getInterestPct().trim())/100)+"";
        }

        CollectItem collectItem = new CollectItem(item.getAccountNumber(),timestamp,collectedAmount,profit,item.getAccoutType());



        // update collection table for the given account
        collectRef.add(collectItem)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG","Failed to write collection amount. "+e);
            }
        });




        updateTotalCollectedAmt(item,collectedAmount);

    }


    private  void updateTotalCollectedAmt(AccountItem item,String collectedAmount){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // update total collected amount
        String totalAmtCollected = item.getTotalCollectedAmt();
        if(totalAmtCollected == null) totalAmtCollected ="0.0";

        totalAmtCollected = (Float.parseFloat(totalAmtCollected) + Float.parseFloat(collectedAmount))+"";
        DocumentReference accRef = db.collection("users").document(user.getEmail())
                .collection("accounts").document(item.getAccountNumber());

        accRef.update("totalCollectedAmt",totalAmtCollected)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.d("COLLECT","Failed to update totalCollectedAmt: "+e);
                    }
                });
        accRef.update("latestCollectionTimestamp",Calendar.getInstance().getTimeInMillis()+"")
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("COLLECT","Failed to update latestCollectionTimestamp: "+e);
                    }
                });


    }
}
