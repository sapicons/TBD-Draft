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
import com.sapicons.deepak.tbd.R;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class CDayActivity extends AppCompatActivity {
    String TAG = "C_GROUPS";

    String GROUPS_STATUS;
    String ACTION = "C_DAY";

    RecyclerView recyclerView;
    List<CGroupItem> groupList = new ArrayList<>();
    CGroupRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cgroups);

        Intent intent = getIntent();
        GROUPS_STATUS = intent.getStringExtra("group_status");
        Log.i(TAG,"STATUS: "+GROUPS_STATUS);

        initialiseViews();
        getDataFromFirestore();

    }

    // will show all the active C Cgroups
    public void initialiseViews(){
        recyclerView = findViewById(R.id.activity_cgroups_recycler_view);
    }

    public void getDataFromFirestore(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firebaseFirestore.collection("users")
                .document(user.getEmail()).collection("groups");


        // TODO : Add a Query with status
        //Query query = collectionReference.whereEqualTo("")

        collectionReference
                .whereEqualTo("status",GROUPS_STATUS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        for(QueryDocumentSnapshot doc : value){

                            CGroupItem item = doc.toObject(CGroupItem.class);
                            Log.d(TAG,"Name: "+item.getGroupName());
                            groupList.add(item);
                        }

                        // define with action as on what clicking on a particular customer should result in
                        // Here the action corresponds to collection on C_Won

                        adapter = new CGroupRecyclerAdapter(groupList,ACTION);
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                        recyclerView.setLayoutManager(mLayoutManager);
                        recyclerView.setAdapter(adapter);
                    }
                });
    }
}
