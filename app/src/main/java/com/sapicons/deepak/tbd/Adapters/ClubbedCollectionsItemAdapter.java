package com.sapicons.deepak.tbd.Adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.icu.text.NumberFormat;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sapicons.deepak.tbd.Objects.AccountItem;
import com.sapicons.deepak.tbd.Objects.CollectItem;
import com.sapicons.deepak.tbd.Objects.CustomerItem;
import com.sapicons.deepak.tbd.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
    AccountItem accountItem;


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
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        accountItem = getItem(position);
        if(convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_clubbed_collections, parent, false);

            holder = new ViewHolder();
            holder.custImage = convertView.findViewById(R.id.item_clubbed_collections_customer_pic_iv);
            holder.custName = convertView.findViewById(R.id.item_clubbed_collections_customer_name);
            holder.accLL = convertView.findViewById(R.id.item_clubbed_collections_ll);
            holder.accNo = convertView.findViewById(R.id.item_clubbed_collections_acc_no);
            holder.accType = convertView.findViewById(R.id.item_clubbed_collections_acc_type);

            convertView.setTag(holder);
            findCollectionsForAccount(accountItem,holder);


        }else{
            holder=(ViewHolder)convertView.getTag();

        }


        holder.custName.setText(accountItem.getFirstName().toString() + " " + accountItem.getLastName().toString());
        if (accountItem.getCustomerPicUrl().length() > 0)
            Glide.with(getContext()).load(accountItem.getCustomerPicUrl()).into(holder.custImage);

        holder.accType.setText((accountItem.getAccoutType().contains("D"))?"D":"M");
        holder.accNo.setText("Acc No: "+accountItem.getAccountNumber());



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
                                Log.d("CCIA",item.getAccountNumber());

                                list.add(item);

                            }
                        }else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }

                        //  club the accounts

                        Collections.sort(list,CollectItem.CollectDateComparator);
                        clubCollections(list,holder);
                    }
                });


    }

    public void clubCollections(List<CollectItem> list,ViewHolder holder){

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

            Calendar calendar = Calendar.getInstance();
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

            holder.accLL.addView(view);


        }


    }


}
