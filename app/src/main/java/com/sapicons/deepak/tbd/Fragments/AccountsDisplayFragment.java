package com.sapicons.deepak.tbd.Fragments;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sapicons.deepak.tbd.AccountsDetailsActivity;
import com.sapicons.deepak.tbd.Adapters.AccountItemAdapter;
import com.sapicons.deepak.tbd.Adapters.CustomerItemAdapter;
import com.sapicons.deepak.tbd.AddAccountActivity;
import com.sapicons.deepak.tbd.Objects.AccountItem;
import com.sapicons.deepak.tbd.R;

import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;


/**
 * Created by Deepak Prasad on 04-08-2018.
 */

public class AccountsDisplayFragment extends ListFragment implements SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {

    private List<AccountItem> list;
    private ListView listView;
    private AccountItemAdapter adapter;
    private Context mContext;

    ProgressDialog progressDialog;
    String TAG = "TAG";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        setHasOptionsMenu(true);
        //populateList();

        Log.d("FRAGMENT","AccountsDisplayFragment");

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("Select Account");

        View view = inflater.inflate(R.layout.fragment_accounts_display, container, false);
        listView = view.findViewById(R.id.accounts_display_list_view);
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

        adapter = new AccountItemAdapter(mContext,R.layout.item_account,list,1);

        listView.setAdapter(adapter);
        listView.setEmptyView(view.findViewById(R.id.empty_accounts_tv));

        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Please Wait ...");


    }



    //get realtime updates
    private void listenToChanges(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //final CollectionReference docRef = db.collection("users").document(user.getEmail()).collection("");

        db.collection("users").document(user.getEmail()).collection("accounts")

                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        List<AccountItem> new_list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            AccountItem newItem = doc.toObject(AccountItem.class);
                            Log.d(TAG,"Name: "+newItem.getFirstName());
                            if(filterAccounts(newItem))
                                new_list.add(newItem);

                        }
                        list = new_list;
                        adapter = new AccountItemAdapter(mContext,R.layout.item_account,new_list,1);
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
        searchView.setQueryHint("Search Accounts");

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
        List<AccountItem> filteredValues = new ArrayList<AccountItem>(list);
        for (AccountItem value : list) {

            String name = value.getFirstName()+" "+value.getLastName()+" "+value.getAccountNumber();
            name = name.toLowerCase();

            if ( !(name.contains(newText.toLowerCase())) ) {

                filteredValues.remove(value);
            }
        }
        adapter = new AccountItemAdapter(mContext,R.layout.item_account,filteredValues,2);
        //setListAdapter(adapter);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);

        return false;
    }
    public void resetSearch() {
        adapter = new AccountItemAdapter(mContext,R.layout.item_account,list,1);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
        //listenToChanges();
    }


    public float getAmountToBeCollected(AccountItem accountItem){
        Calendar calendar = Calendar.getInstance();
        long currTime = calendar.getTimeInMillis();
        long day = 1000 * 60 * 60 * 24;

        float amountToBeCollected=0.0f;
        float loanAmt= Float.parseFloat(accountItem.getLoanAmt());
        float totalCollectedAmt = 0.0f;
        if (accountItem.getTotalCollectedAmt() != null)
            totalCollectedAmt = Float.parseFloat(accountItem.getTotalCollectedAmt()); //get total collected amount till now

        long lastCollectionDay = Long.parseLong(accountItem.getLatestCollectionTimestamp());
        if (lastCollectionDay ==0)
            lastCollectionDay= Long.parseLong(accountItem.getStartDate());

        if(accountItem.getAccoutType().contains("D")) {

            int daysUnpaid =(int) ((currTime-lastCollectionDay)/day );
            amountToBeCollected =(float) (daysUnpaid*0.01*loanAmt - totalCollectedAmt);

        }
        else if(accountItem.getAccoutType().contains("M")){

            long month = day*30;
            long startDate = Long.parseLong(accountItem.getStartDate());
            int monthsFromStart = (int)((currTime-startDate)/month);

            float interestPct = Float.parseFloat(accountItem.getInterestPct());
            amountToBeCollected = (loanAmt*(interestPct/100)*monthsFromStart - totalCollectedAmt);
        }

        if(amountToBeCollected<0)
            amountToBeCollected=0.0f;
        return amountToBeCollected;
    }
    boolean filterAccounts(AccountItem item){

        Calendar calendar = Calendar.getInstance();

        //for M account
        if(item.getAccoutType().contains("M")){


            long currTime = calendar.getTimeInMillis();
            long day = 1000 * 60 * 60 * 24;   // a day
            long lastCollectionDate ;

            if( item.getLatestCollectionTimestamp() ==null || Long.parseLong(item.getLatestCollectionTimestamp()) == 0)
                lastCollectionDate = Long.parseLong(item.getStartDate());
            else
                lastCollectionDate = Long.parseLong(item.getLatestCollectionTimestamp());
            int noOfDays = (int)((currTime-lastCollectionDate)/(day));

            Log.d("ADF","NO of days: "+noOfDays);
            Log.d("ADF","last collection date: "+lastCollectionDate);
            // if started on the same day of the previous months and account is open return true

            Log.d("ADF","amt to be collected: "+getAmountToBeCollected(item));

            if(getAmountToBeCollected(item)>0)
                return true;
            if((noOfDays >=30 &&
                    item.getAccountStatus().equalsIgnoreCase("open")) ) {
                return true;
            }

        }

        else if(item.getAccoutType().contains("D")){

            long startDate = Long.parseLong(item.getStartDate());
            long endDate = Long.parseLong(item.getEndDate());
            long todaysDate = calendar.getTimeInMillis();
            //set new calendar equal to start date of the account

            Calendar newCal = Calendar.getInstance();
            newCal.setTimeInMillis(startDate);

            long lastCollectionDate ;

            if( item.getLatestCollectionTimestamp() ==null || Long.parseLong(item.getLatestCollectionTimestamp()) == 0)
                lastCollectionDate = Long.parseLong(item.getStartDate());
            else
                lastCollectionDate = Long.parseLong(item.getLatestCollectionTimestamp());


            long day=1000*60*60*24;
            if(((todaysDate - lastCollectionDate) >= day  &&
                    todaysDate<endDate &&
                    Float.parseFloat(item.getDueAmt())>0 &&
                    item.getAccountStatus().equalsIgnoreCase("open")) )
                return true;


        }

        return false;
    }
}
