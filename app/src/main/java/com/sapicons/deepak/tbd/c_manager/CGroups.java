package com.sapicons.deepak.tbd.c_manager;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sapicons.deepak.tbd.Objects.CGroupItem;
import com.sapicons.deepak.tbd.R;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class CGroups extends AppCompatActivity {

    String TAG = "C_GROUPS";

    String GROUPS_STATUS;

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
                adapter = new CGroupRecyclerAdapter(groupList);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setAdapter(adapter);
            }
        });
    }
}
