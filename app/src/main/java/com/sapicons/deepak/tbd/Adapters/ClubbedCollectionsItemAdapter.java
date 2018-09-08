package com.sapicons.deepak.tbd.Adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sapicons.deepak.tbd.Objects.AccountItem;
import com.sapicons.deepak.tbd.Objects.CollectItem;
import com.sapicons.deepak.tbd.Objects.CustomerItem;
import com.sapicons.deepak.tbd.R;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by Deepak Prasad on 28-08-2018.
 */

public class ClubbedCollectionsItemAdapter extends ArrayAdapter<AccountItem> {

    LinearLayout linearLayout;
    Context context;
    List<AccountItem> accItemList = new ArrayList<>();
    List<CollectItem> collectItemList = new ArrayList<>();
    int layoutResourceId;

    ProgressDialog progressDialog;
    ViewHolder holder;
    //AccountItem accountItem;


    public ClubbedCollectionsItemAdapter(@NonNull Context context, int resource, @NonNull List<AccountItem> objects) {
        super(context, resource, objects);
        this.context = context;
        layoutResourceId = resource;
        accItemList = objects;
    }

    static class ViewHolder{
        ImageView custImage;
        TextView custName;
        TextView accNo;
        TextView accType;
        LinearLayout accLL;
        AccountItem account;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Log.d("ADAPTER","ClubbedCollectionsItemAdapter");



        //accountItem = getItem(position);
        if(convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_clubbed_collections, parent, false);


            holder = new ViewHolder();
            holder.custImage = convertView.findViewById(R.id.item_clubbed_collections_customer_pic_iv);
            holder.custName = convertView.findViewById(R.id.item_clubbed_collections_customer_name);
            holder.accLL = convertView.findViewById(R.id.item_clubbed_collections_ll);
            holder.accNo = convertView.findViewById(R.id.item_clubbed_collections_acc_no);
            holder.accType = convertView.findViewById(R.id.item_clubbed_collections_acc_type);
            holder.account = getItem(position);

            convertView.setTag(holder);
            findCollectionsForAccount(holder.account,holder);


        }else{
            holder=(ViewHolder)convertView.getTag();
            //accountItem = getItem(position);
            //Log.d("CCIA","AccNumber TOp: "+accountItem.getAccountNumber());
        }


        holder.custName.setText(holder.account.getFirstName().toString() + " " + holder.account.getLastName().toString());
        if (holder.account.getCustomerPicUrl().length() > 0)
            Glide.with(getContext()).load(holder.account.getCustomerPicUrl()).into(holder.custImage);

        holder.accType.setText((holder.account.getAccoutType().contains("D"))?"D":"M");
        holder.accNo.setText("Acc No: "+holder.account.getAccountNumber());



        return  convertView;
    }


    public void findCollectionsForAccount(AccountItem item,final ViewHolder holder){

        List<CollectItem> collections = new ArrayList<>();

        getAccountsFromFirebase(item,collections,holder);

    }

    public void getAccountsFromFirebase(AccountItem item, final List<CollectItem> list,final ViewHolder holder){



        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference ref = db.collection("users").document(user.getEmail())
                .collection("collections");

        //create a query to search for user's accounts
        Query getNoOfAcc = ref.whereEqualTo("accountNumber",item.getAccountNumber());

        getNoOfAcc.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {


                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                CollectItem item = document.toObject(CollectItem.class);

                                //remove first collection for D accounts
                                float profitAmt = Float.parseFloat(item.getProfitAmount());
                                float collectedAmt = Float.parseFloat(item.getAmountCollected());
                                if(profitAmt >= 0.0 && collectedAmt > 0.0)
                                    list.add(item);

                            }
                            Collections.sort(list,CollectItem.CollectDateComparator);
                            clubCollections(list,holder);

                        }else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }

                        //  club the accounts


                    }
                });


    }

    public void clubCollections(List<CollectItem> list, final ViewHolder holder){

        Log.d("CAA","No. of collections: "+list.size());

        if(list.size()==0)
            holder.accLL.setVisibility(View.GONE);
        for(final CollectItem item: list) {
            Log.d("CAA", "AC NO: " + item.getAccountNumber());

            LayoutInflater layoutInflater =  (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view;

            view = layoutInflater.inflate(R.layout.item_single_collection_in_club, linearLayout, false);
            final TextView collectionDateTV = view.findViewById(R.id.single_collection_date_tv),
                    collectionAmtTv = view.findViewById(R.id.single_collection_amt_tv);
            final FancyButton editBtn  = view.findViewById(R.id.single_collection_edit_btn);

            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");
            long collectionDate = Long.parseLong(item.getTimestamp());
            //set the textviews
            collectionDateTV.setText("Collected On: "+dateFormatter.format(collectionDate));

            Float dA = Float.parseFloat(item.getAmountCollected());
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en","in"));
                collectionAmtTv.setText(numberFormat.format(dA));
            }else {

                java.text.NumberFormat numberFormat = java.text.NumberFormat.getNumberInstance(Locale.US);
                collectionAmtTv.setText(numberFormat.format(dA));
            }


            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    Log.d("CCIA","CollecItem: "+item.getTimestamp());
                    Log.d("CCIA","AccItemHolder: "+holder.account.getAccountNumber());
                    setupPopupWindow(item,collectionAmtTv,holder);
                }
            });
            holder.accLL.addView(view);
        }



    }


    public void setupPopupWindow(final CollectItem collectItem, final TextView collectionAmtTv,final ViewHolder holder){
        progressDialog  = new ProgressDialog(context);
        progressDialog.setMessage("Please Wait ...");

        AlertDialog.Builder alertDialog=new AlertDialog.Builder(getContext());
        LayoutInflater inflater =(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View customView=inflater.inflate(R.layout.custom_collect_popup,null);
        final EditText amtEt = customView.findViewById(R.id.custom_collect_amt_et);

        alertDialog.setTitle("Edit Amount")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                float originalCollectionAmt = Float.parseFloat(collectItem.getAmountCollected());
                String eA = amtEt.getText().toString();
                if(!eA.isEmpty()) {

                    float enteredAmt = Float.parseFloat(eA);
                    float difference = enteredAmt - originalCollectionAmt;
                    float originalDueAmt  = Float.parseFloat(holder.account.getDueAmt());
                    float newDueAmt = originalDueAmt-difference;

                    if(newDueAmt<0)
                        Toasty.error(context,"Entered Amount is more than Due Amount").show();

                    else {

                        progressDialog.show();
                        editCollectionAmount(collectItem, amtEt.getText().toString(), collectionAmtTv,holder);
                    }

                }


            }
        });
        alertDialog.setView(customView)
                .create().show();
    }


    public void editCollectionAmount(CollectItem collectItem,final String updatedAmount, TextView collectionAmtTv,final ViewHolder holder){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //update local info
        final String originalCollectedAmt= collectItem.getAmountCollected();
        collectItem.setAmountCollected(updatedAmount);
        //collectionAmtTv.setText(updatedAmount);
        float dA=Float.parseFloat(updatedAmount);




        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en","in"));
            collectionAmtTv.setText(numberFormat.format(dA));
        }else {

            java.text.NumberFormat numberFormat = java.text.NumberFormat.getNumberInstance(Locale.US);
            collectionAmtTv.setText(numberFormat.format(dA));
        }


        Log.d("CCIA","CollectionID: "+collectItem.getTimestamp());


        //update the new info to db
        DocumentReference collectRef = db.collection("users").document(user.getEmail())
                .collection("collections").document(collectItem.getTimestamp());

        collectRef.update("amountCollected",updatedAmount,"profitAmount",updatedAmount)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toasty.success(context,"Collection Updated").show();
                        progressDialog.dismiss();
                        updateAccountInfo(originalCollectedAmt,updatedAmount,holder);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG","error updating amt: "+e);
            }
        });


    }
    public void updateAccountInfo(String originalCollectedAmt, String updatedAmount,final ViewHolder holder){

        //Log.d("CCIA","updateAccountInfo");
        Log.d("CCIA","HolderAccId: "+holder.account.getAccountNumber());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        float difference = Float.parseFloat(updatedAmount)-Float.parseFloat(originalCollectedAmt);

        String newDueAmt = (Float.parseFloat(holder.account.getDueAmt())-difference)+"";

        String newTotalCollectedAmt = (Float.parseFloat(holder.account.getTotalCollectedAmt())+difference)+"";

        if(holder.account.getAccoutType().contains("D"))
            holder.account.setDueAmt(newDueAmt);
        else if(holder.account.getAccoutType().contains("M")) {
            holder.account.setDueAmt(holder.account.getLoanAmt());
            newDueAmt = holder.account.getLoanAmt();
        }
        holder.account.setTotalCollectedAmt(newTotalCollectedAmt);


        DocumentReference accRef = db.collection("users").document(user.getEmail())
                .collection("accounts").document(holder.account.getAccountNumber());

        if(holder.account.getAccoutType().contains("D"))
            accRef.update("dueAmt",newDueAmt,
                        "totalCollectedAmt",newTotalCollectedAmt)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG","Failed to write collection amount. "+e);
                    }
                });

        else if(holder.account.getAccoutType().contains("M"))
            accRef.update("dueAmt",newDueAmt,"totalCollectedAmt",newTotalCollectedAmt)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("TAG","Failed to write collection amount. "+e);
                        }
                    });



        //close account id dueAmt = zero
        if(Float.parseFloat(newDueAmt)  < 1.0){
            Toasty.info(context,"Account Closed").show();
            holder.account.setAccountStatus("closed");

            accRef.update("accountStatus","closed")
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            //progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("TAG","error updating account status: "+e);
                }
            });
        }

        //open account id newDueAmt > zero and account is closed
        if(Float.parseFloat(newDueAmt)  >=1.0 && holder.account.getAccountStatus().contains("closed")){
            Toasty.info(context,"Account Opened").show();
            holder.account.setAccountStatus("open");

            accRef.update("accountStatus","open")
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            //progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("TAG","error updating account status: "+e);
                }
            });
        }
    }
}
