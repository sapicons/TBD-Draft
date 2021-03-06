package com.sapicons.deepak.tbd.Fragments;

import android.app.DatePickerDialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sapicons.deepak.tbd.Adapters.ClosedAccountAdapter;
import com.sapicons.deepak.tbd.AddAccountActivity;
import com.sapicons.deepak.tbd.Objects.AccountItem;
import com.sapicons.deepak.tbd.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * Created by Deepak Prasad on 21-08-2018.
 */

public class ClosedAccountsDisplayFragment extends ListFragment implements SearchView.OnQueryTextListener,
        MenuItem.OnActionExpandListener {

    private List<AccountItem> list;
    private ListView listView;
    private ClosedAccountAdapter adapter;
    private Context mContext;

    ProgressDialog progressDialog;
    String TAG = "TAG";

    Calendar startCalendar = Calendar.getInstance(),
            endCalendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener startDate,endDate;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        setHasOptionsMenu(true);
        //populateList();
        Log.d("FRAGMENT","ClosedAccountsDisplayFragment");

    }
    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //getActivity().setTitle("Select Account");

        View view = inflater.inflate(R.layout.fragment_closed_accounts, container, false);
        listView = view.findViewById(R.id.closed_accounts_list_view);
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

        adapter = new ClosedAccountAdapter(mContext,R.layout.item_closed_account,list);

        listView.setAdapter(adapter);
        listView.setEmptyView(view.findViewById(R.id.empty_closed_accounts_tv));

        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Please Wait ...");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
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

                            if(newItem.getAccountStatus().equalsIgnoreCase("closed"))
                                new_list.add(newItem);

                        }
                        Collections.sort(new_list,AccountItem.DateClosedComparator);
                        list = new_list;
                        adapter = new ClosedAccountAdapter(mContext,R.layout.item_closed_account,new_list);
                        adapter.notifyDataSetChanged();
                        listView.setAdapter(adapter);
                        progressDialog.dismiss();

                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu,menu);
        inflater.inflate(R.menu.sort_menu,menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search Accounts");

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if(id==R.id.action_sort){
            askForSortOptionsWindow();
        }
        return super.onOptionsItemSelected(item);
    }

    private void askForSortOptionsWindow(){


        final AlertDialog.Builder alertDialog=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View customView=inflater.inflate(R.layout.custom_sort_options,null);
        final TextView byWeekTv = customView.findViewById(R.id.custom_sort_last_week);
        final TextView byMonthTv = customView.findViewById(R.id.custom_sort_last_month);
        final TextView byDateRangeTv = customView.findViewById(R.id.custom_sort_date_range);
        final TextView byAllTv = customView.findViewById(R.id.custom_sort_all);
        alertDialog.setTitle("Sort By");
        alertDialog.setView(customView);

        final AlertDialog dialog = alertDialog.create();
        dialog.show();

        byWeekTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortByRange(1); //week
                dialog.dismiss();
            }
        });
        byMonthTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortByRange(2); //month
                dialog.dismiss();
            }
        });
        byDateRangeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortByRange(3);
                dialog.dismiss();
            }
        });

        byAllTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetSearch();
                dialog.dismiss();
            }
        });

    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem menuItem) {
        return false;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
        return false;
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
        Collections.sort(filteredValues,AccountItem.DateClosedComparator);
        adapter = new ClosedAccountAdapter(mContext,R.layout.item_closed_account,filteredValues);

        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);

        return false;
    }

    public void resetSearch() {

        adapter = new ClosedAccountAdapter(mContext,R.layout.item_closed_account,list);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
        listenToChanges();
    }

    public void sortByRange(int choice){
        Calendar calendar = Calendar.getInstance();
        long currTime = calendar.getTimeInMillis(); // start from today
        long startTime = 0l; //end at a particular time frame in past

        // if choice is by week
        if(choice == 1){
            startTime= currTime - (1000*60*60*24*7);
            filterAccountsByClosedDate(startTime,currTime);

        }
        else if(choice == 2){
            startTime= currTime - (2592000000l);
            filterAccountsByClosedDate(startTime,currTime);
        }
        else if(choice == 3){



            endDate = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                    endCalendar.set(Calendar.YEAR, year);
                    endCalendar.set(Calendar.MONTH, monthOfYear);
                    endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    //Log.d("DATE RANGE")
                    filterAccountsByClosedDate(startCalendar.getTimeInMillis(),endCalendar.getTimeInMillis());

                }
            };

            startDate = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {

                    startCalendar.set(Calendar.YEAR, year);
                    startCalendar.set(Calendar.MONTH, monthOfYear);
                    startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    Toasty.info(mContext, "Select End Date").show();
                    new DatePickerDialog(getActivity(), endDate, endCalendar
                            .get(Calendar.YEAR), endCalendar.get(Calendar.MONTH),
                            endCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }

            };

            Toasty.info(mContext, "Select Start Date").show();

            new DatePickerDialog(getActivity(), startDate, startCalendar
                    .get(Calendar.YEAR), startCalendar.get(Calendar.MONTH),
                    startCalendar.get(Calendar.DAY_OF_MONTH)).show();

        }




    }

    void  filterAccountsByClosedDate(long startTime,long endTime){
        Log.d("DATE RANGE","ct: "+startTime+"  Et: "+endTime);
        List<AccountItem> filteredValues = new ArrayList<AccountItem>(list);
        for (AccountItem value : list) {

            long endDate = Long.parseLong(value.getEndDate());
            Log.d("DATE RANGE","endDate of acc : "+endDate);

            //remove accounts with closed date other than the range
            if(endDate>=startTime && endDate<= endTime ) {

                Log.d("DATE RANGE"," In range");
            }
            else
                filteredValues.remove(value);
        }

        Collections.sort(filteredValues,AccountItem.DateClosedComparator);
        adapter = new ClosedAccountAdapter(mContext,R.layout.item_closed_account,filteredValues);

        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }
}