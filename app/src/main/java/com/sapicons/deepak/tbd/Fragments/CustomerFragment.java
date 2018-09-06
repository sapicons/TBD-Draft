package com.sapicons.deepak.tbd.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sapicons.deepak.tbd.Adapters.CustomerItemAdapter;
import com.sapicons.deepak.tbd.AddCustomerActivity;
import com.sapicons.deepak.tbd.CustomerDetailsActivity;
import com.sapicons.deepak.tbd.Objects.CustomerItem;
import com.sapicons.deepak.tbd.R;

import java.util.ArrayList;
import java.util.List;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

/**
 * Created by Deepak Prasad on 30-07-2018.
 */

public class CustomerFragment extends ListFragment implements SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {

    private List<CustomerItem> list;
    private ListView listView;
    private CustomerItemAdapter adapter;
    private Context mContext;

    ProgressDialog progressDialog;
    String TAG = "TAG";
    static final int PICK_CONTACT=1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        setHasOptionsMenu(true);
        //populateList();
        Log.d("FRAGMENT","CustomerFragment");

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("Customers");
        View view = inflater.inflate(R.layout.fragment_customer, container, false);
        listView = view.findViewById(R.id.customer_list_view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        initialiseViews(view);
        progressDialog.show();
        //getDataFromFirestore();

        //get realtime updates
        listenToChanges();
    }

    private void initialiseViews(View view){
        list = new ArrayList<>();

        adapter = new CustomerItemAdapter(mContext,R.layout.item_customer,list);

        listView.setAdapter(adapter);
        listView.setEmptyView(view.findViewById(R.id.empty_customers_tv));

        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Please Wait ...");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CustomerItem item = (CustomerItem)adapterView.getItemAtPosition(i);
                Bundle bundle = new Bundle();
                bundle.putSerializable("selected_customer",item);
                Intent intent = new Intent(getActivity(), CustomerDetailsActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);

                onDetach();
            }
        });

        FloatingActionButton addCustomerBtn = view.findViewById(R.id.frag_customer_add_fab);
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
    }

    //get all data at once , not realtime
    private  void getDataFromFirestore(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getEmail())
                .collection("customers")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot doc : task.getResult()){
                                Log.d("CustomerActivity",doc.getId() + " => " + doc.getData());
                                CustomerItem newItem = doc.toObject(CustomerItem.class);
                                list.add(newItem);
                                adapter.notifyDataSetChanged();
                            }
                            progressDialog.dismiss();
                        }else {
                            Log.d("CustomerActivity", "Error getting documents: ", task.getException());
                        }
                    }
                });


    }

    //get realtime updates
    private void listenToChanges(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //final CollectionReference docRef = db.collection("users").document(user.getEmail()).collection("");

        db.collection("users").document(user.getEmail()).collection("customers")

                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        List<CustomerItem> new_list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            CustomerItem newItem = doc.toObject(CustomerItem.class);
                            Log.d(TAG,"Name: "+newItem.getFirstName());
                            new_list.add(newItem);

                        }
                        list = new_list;
                        adapter = new CustomerItemAdapter(mContext,R.layout.item_customer,new_list);
                        adapter.notifyDataSetChanged();
                        listView.setAdapter(adapter);
                        progressDialog.dismiss();

                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu,menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search Customers");

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem menuItem) {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText == null || newText.trim().isEmpty()) {
            resetSearch();
            return false;
        }
        List<CustomerItem> filteredValues = new ArrayList<CustomerItem>(list);
        for (CustomerItem value : list) {

            if (!value.getFirstName().contains(newText)) {

                filteredValues.remove(value);
            }
        }
        adapter = new CustomerItemAdapter(mContext,R.layout.item_customer,filteredValues);
        //setListAdapter(adapter);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);

        return false;
    }
    public void resetSearch() {
        adapter = new CustomerItemAdapter(mContext,R.layout.item_customer,list);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
        //listenToChanges();
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
                        Log.d(TAG,"Name is:" + name);

                    }

                    number = formatPhoneNumber(number);
                    Log.d(TAG,"number is:" + number);

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
