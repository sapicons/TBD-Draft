package com.sapicons.deepak.tbd.c_manager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sapicons.deepak.tbd.Objects.CGroupItem;
import com.sapicons.deepak.tbd.Objects.CustomerItem;
import com.sapicons.deepak.tbd.R;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class CCustomers extends AppCompatActivity {

    String TAG = "C_CUSTOMERS";

    String GROUPS_STATUS;
    CGroupItem GROUP_ITEM;

    RecyclerView recyclerView;
    List<CustomerItem> customerList = new ArrayList<>();
    CCustomersRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ccustomers);

        Intent intent = getIntent();
        GROUPS_STATUS = intent.getStringExtra("group_status");
        // TODO get group object
        Bundle bundle = intent.getExtras();
        GROUP_ITEM = (CGroupItem)bundle.getSerializable("c_group");
        Log.i(TAG,"STATUS: "+GROUPS_STATUS);
        Log.i(TAG,"GROUP_ID: "+GROUP_ITEM.getGroupID());


        initialiseViews();
        getDataFromFirestore();
    }

    public void initialiseViews(){
        recyclerView = findViewById(R.id.activity_ccustomers_recycler_view);
    }

    public void getDataFromFirestore(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firebaseFirestore.collection("users")
                .document(user.getEmail()).collection("accounts");


        // TODO : Add a Query with status
        //Query query = collectionReference.whereEqualTo("")

        collectionReference
                .whereEqualTo("cId",GROUP_ITEM.getGroupID())
                .whereEqualTo("accoutType","C Account").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                for(QueryDocumentSnapshot doc : value){

                    CustomerItem item = doc.toObject(CustomerItem.class);
                    Log.d(TAG,"Name: "+item.getFirstName());
                    customerList.add(item);
                }
                adapter = new CCustomersRecyclerAdapter(customerList);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setAdapter(adapter);
            }
        });
    }
}
