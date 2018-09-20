package com.sapicons.deepak.tbd.Adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.icu.text.NumberFormat;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sapicons.deepak.tbd.Objects.AccountItem;
import com.sapicons.deepak.tbd.Objects.CollectItem;
import com.sapicons.deepak.tbd.Objects.CustomerItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sapicons.deepak.tbd.R;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by Deepak Prasad on 15-08-2018.
 */

public class ClubbedAccountsAdapter  extends ArrayAdapter<CustomerItem> {

    LinearLayout linearLayout;
    Context context;
    List<CustomerItem> custItem = new ArrayList<>();
    List<AccountItem> accItem = new ArrayList<>();
    int layoutResourceId;

    int isCollect = 0;  // 1: collect 0: close

    ProgressDialog progressDialog;
    ViewHolder holder;
    CustomerItem customerItem;


    public ClubbedAccountsAdapter(@NonNull Context context, int resource, @NonNull List<CustomerItem> objects,int isCollect) {
        super(context, resource, objects);
        this.context = context;
        layoutResourceId = resource;
        custItem = objects;
        this.isCollect = isCollect;
    }

    static class ViewHolder{
        ImageView custImage;
        TextView custName;
        LinearLayout accLL;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Log.d("ADAPTER","ClubbedAccountAdapter");

        customerItem = getItem(position);
        if(convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_clubbed_accounts, parent, false);

            holder = new ViewHolder();
            holder.custImage = convertView.findViewById(R.id.item_clubbed_account_customer_pic_iv);
            holder.custName = convertView.findViewById(R.id.item_clubbed_account_customer_name);
            holder.accLL = convertView.findViewById(R.id.item_clubbed_acc_ll);

            convertView.setTag(holder);
            findAccountsOfCustomer(customerItem,holder);


        }else{
            holder=(ViewHolder)convertView.getTag();

        }


        holder.custName.setText(customerItem.getFirstName().toString() + " " + customerItem.getLastName().toString());
        if (customerItem.getPhotoUrl().length() > 0)
            Glide.with(getContext()).load(customerItem.getPhotoUrl()).into(holder.custImage);



        return  convertView;
    }


    public void findAccountsOfCustomer(CustomerItem item,final ViewHolder holder){

        List<AccountItem> accounts = new ArrayList<>();

        getAccountsFromFirebase(item,accounts,holder);

    }

    public void getAccountsFromFirebase(CustomerItem item, final List<AccountItem> list,final ViewHolder holder){



        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference ref = db.collection("users").document(user.getEmail())
                .collection("accounts");

        //create a query to search for user's accounts
        Query getNoOfAcc = ref.whereEqualTo("customerId",item.getCustomerId())
                .whereEqualTo("accountStatus","open");

        getNoOfAcc.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {


                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                AccountItem item = document.toObject(AccountItem.class);
                                Log.d("CAA",item.getFirstName());

                                list.add(item);

                            }
                            clubAccounts(list,holder);
                        }else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }

                        //  club the accounts

                        //if(accItem.isEmpty())
                            //holder.accLL.setVisibility(View.GONE);

                    }
                });


    }

    public void clubAccounts(List<AccountItem> list,ViewHolder holder){

        Log.d("CAA","No. of acc: "+list.size());

        if(list.size()==0)
            holder.accLL.setVisibility(View.GONE);
        for(final AccountItem item: list) {
            Log.d("CAA", "Name: " + item.getFirstName() + " NO: " + item.getAccountNumber());

            LayoutInflater layoutInflater =  (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view;

            view = layoutInflater.inflate(R.layout.item_single_account_in_club, linearLayout, false);
            final TextView accTypeTv = view.findViewById(R.id.single_account_type_tv),
                    dueAmtTv = view.findViewById(R.id.single_acc_due_amt),
                    startDateTv = view.findViewById(R.id.single_account_start_date_tv),
                    endDateTv =  view.findViewById(R.id.single_account_end_date_tv);
            final FancyButton collectBtn = view.findViewById(R.id.single_acc_collect_btn);

            // if the adapter is used for closing the account
            if(isCollect == 0) {
                //collectBtn.setVisibility(View.INVISIBLE);
                collectBtn.setText("Close");
                collectBtn.setBackgroundColor(Color.parseColor("#c56000"));  //yellow color
            }



            //set the textviews
            accTypeTv.setText(item.getAccoutType());
            //dueAmtTv.setText(item.getDueAmt());


            Float dA = Float.parseFloat(item.getDueAmt());
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en","in"));
                dueAmtTv.setText(numberFormat.format(dA));
            }else {

                java.text.NumberFormat numberFormat = java.text.NumberFormat.getNumberInstance(Locale.US);
                dueAmtTv.setText(numberFormat.format(dA));
            }


            //set the date in formatted way
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");
            long s = Long.parseLong(item.getStartDate());
            long e = Long.parseLong(item.getEndDate());
            calendar.setTimeInMillis(s);
            startDateTv.setText(dateFormatter.format(calendar.getTime()));
            calendar.setTimeInMillis(e);
            endDateTv.setText(dateFormatter.format(calendar.getTime()));


            holder.accLL.addView(view);

            final View singleAccView = view;
            collectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("TAG","Amt: "+item.getDueAmt());

                    if(isCollect ==0) { // if close button is clicked
                        //view.setVisibility(View.GONE);
                        displayPopupToCloseAccount(item,dueAmtTv,singleAccView,collectBtn);
                    }
                    else  // if collect button is clicked
                        setUpPopupWindow(item,dueAmtTv,singleAccView,collectBtn);
                }
            });


        }


    }




    // collect functionality

    public void setUpPopupWindow(final AccountItem item, final TextView dueAmtTv,
                                 final View singleAccView, final FancyButton collectBtn){
        progressDialog = new ProgressDialog(context);
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
                float actualAmt = Float.parseFloat(item.getDueAmt());
                String eA = amtEt.getText().toString();
                if(!eA.isEmpty()) {
                    float enteredAmt = Float.parseFloat(eA);
                    if (enteredAmt > actualAmt) {
                        Toasty.error(context, "Amount collected is more than Due.").show();
                    } else {
                        progressDialog.show();

                        deductAmountFromAccount(item, amtEt.getText().toString(),dueAmtTv,singleAccView,collectBtn);
                    }
                }


            }
        });

        alertDialog.setView(customView)
                .create().show();
    }

    private void deductAmountFromAccount(final AccountItem accountItem,final String amount,TextView dueAmtTv,
                                         final View singleAccView, final FancyButton collectBtn){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        String newAmt = (Float.parseFloat(accountItem.getDueAmt()) - Float.parseFloat(amount))+"";
        float amtZero = Float.parseFloat(newAmt);  //new due amt is zero


        //if(accountItem.getAccoutType().contains("M"))
          //  newAmt = accountItem.getDueAmt();


        if(amtZero <1){ //check if new due amt is zero
            collectBtn.setEnabled(false);
            dueAmtTv.setText("Closed");
            accountItem.setDueAmt("0"); // if account is being closed , set new due amt to zero
            newAmt ="0";

        }else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "in"));
                dueAmtTv.setText(numberFormat.format(Float.parseFloat(newAmt)));
            } else {

                java.text.NumberFormat numberFormat = java.text.NumberFormat.getNumberInstance(Locale.US);
                dueAmtTv.setText(numberFormat.format(Float.parseFloat(newAmt)));
                accountItem.setDueAmt(newAmt);  // if new due amount isn't zero, set the new due amt
            }
        }
        //dueAmtTv.setText(newAmt);

        final String newDueAmt = newAmt;


        //update the new info to db
        DocumentReference accRef = db.collection("users").document(user.getEmail())
                .collection("accounts").document(accountItem.getAccountNumber());

        accRef.update("dueAmt",newAmt)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toasty.success(context, "Amount Updated!").show();
                        progressDialog.dismiss();
                        sendMessage(accountItem,amount,newDueAmt);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG","error updating amt: "+e);
            }
        });


        //close account id dueAmt = zero
        if(Float.parseFloat(newAmt)  < 1.0){
            Toasty.info(context, "Account is Closed!").show();
            accountItem.setAccountStatus("closed");

            accRef.update("accountStatus","closed")
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

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

        Date date = new Date();
        String timestamp = date.getTime()+"";
        String profit = "0";

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference collectRef = db.collection("users").document(user.getEmail())
                .collection("collections").document(timestamp);


        // profit for D account is zero
        // profit for M account is (loanAmt * interestPct/100)
        if(item.getAccoutType().contains("M")){

            if(Float.parseFloat(collectedAmount)>Float.parseFloat(item.getDueAmt()))
                profit = Float.parseFloat(collectedAmount)-Float.parseFloat(item.getDueAmt())+"";
            else
                profit = collectedAmount;
            //profit= (Float.parseFloat(item.getLoanAmt().trim())*Float.parseFloat(item.getInterestPct().trim())/100)+"";
        }

        CollectItem collectItem = new CollectItem(item.getAccountNumber(),timestamp,collectedAmount,profit,item.getAccoutType());



        // update collection table for the given account
        collectRef.set(collectItem).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG","Failed to write collection amount. "+e);
            }
        });


        updateTotalCollectedAmt(item,collectedAmount);

        //reset the user interface
        //resetUI();
    }

    private void resetUI(){

        custItem = new ArrayList<>();
        accItem = new ArrayList<>();
        findAccountsOfCustomer(customerItem,holder);
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

        accRef.update("totalCollectedAmt",totalAmtCollected,
                "latestCollectionTimestamp",Calendar.getInstance().getTimeInMillis()+"")
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.d("COLLECT","Failed to update totalCollectedAmt: "+e);
                    }
                });
        /*accRef.update()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("COLLECT","Failed to update latestCollectionTimestamp: "+e);
                    }
                });*/


    }


    private void displayPopupToCloseAccount(final AccountItem accountItem, final TextView dueAmtTv,
                                            final View singleAccView, final FancyButton collectBtn){

        float dueAmt = Float.parseFloat(accountItem.getDueAmt());
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please Wait ...");



        AlertDialog.Builder alertDialog=new AlertDialog.Builder(getContext());
        LayoutInflater inflater =(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View customView=inflater.inflate(R.layout.custom_close_account_popup,null);
        //final Te amtEt = customView.findViewById(R.id.custom_collect_amt_et);
        final TextView amtTv = customView.findViewById(R.id.custom_close_account_amt_tv);



        if(accountItem.getAccoutType().contains("M")){
            Calendar calendar = Calendar.getInstance();
            long currTime = calendar.getTimeInMillis();
            long day = 1000 * 60 * 60 * 24;
            long lastCollectionDay = Long.parseLong(accountItem.getLatestCollectionTimestamp());
            if (lastCollectionDay==0)
                lastCollectionDay = Long.parseLong(accountItem.getStartDate());
            float months = (float)(currTime-lastCollectionDay)/(day*30);
            float loanAmt= Float.parseFloat(accountItem.getLoanAmt());
            float interestPct = Float.parseFloat(accountItem.getInterestPct());

            dueAmt += loanAmt*(interestPct/100)*months;
        }

        //set dueAmtTv
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en","in"));
            amtTv.setText(numberFormat.format(dueAmt));
        }else {

            java.text.NumberFormat numberFormat = java.text.NumberFormat.getNumberInstance(Locale.US);
            amtTv.setText(numberFormat.format(dueAmt));
        }

        // new  amt to be update to close the account
        final float amtToCloseAccount = dueAmt;

        alertDialog.setTitle("Collect Amount")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setPositiveButton("Collect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                        progressDialog.show();
                        deductAmountFromAccount(accountItem, amtToCloseAccount+"",dueAmtTv,singleAccView,collectBtn);

            }
        });

        alertDialog.setView(customView)
                .create().show();

    }


    private void sendMessage(AccountItem accountItem, String amount, String newDueAmt){
        String msg1= " Ac/No: "+accountItem.getAccountNumber()+" was closed today.";
        String msg2= "Rs. "+amount+" collected for Ac/No: "+accountItem.getAccountNumber()+". Due Amt: "+newDueAmt;
        String phoneNumber = accountItem.getPhoneNumber();
        String msg="";
        if(Float.parseFloat(newDueAmt)>1)
            msg= msg2;
        else msg=msg1;

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, msg, null, null);
            Toasty.success(getContext(),"Message Sent").show();
        } catch (Exception ex) {
            //Toast.makeText(getContext(),ex.getMessage().toString(),
            //      Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

}
