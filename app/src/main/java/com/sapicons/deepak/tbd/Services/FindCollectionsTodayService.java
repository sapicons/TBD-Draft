package com.sapicons.deepak.tbd.Services;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sapicons.deepak.tbd.Adapters.AccountItemAdapter;
import com.sapicons.deepak.tbd.Objects.AccountItem;
import com.sapicons.deepak.tbd.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * Created by Deepak Prasad on 24-09-2018.
 */

public class FindCollectionsTodayService extends Service {


    Intent intent ;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        this.intent = intent;

        Log.d("SERVICE","FindCollectionsTodayService");
        //Toast.makeText(this,"Service startedE",Toast.LENGTH_LONG).show();
        listenToChanges();
        return Service.START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


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
                            Log.w("SERVICE", "Listen failed.", e);
                            return;
                        }

                        List<AccountItem> new_list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            AccountItem newItem = doc.toObject(AccountItem.class);
                            Log.d("SERVICE","Name: "+newItem.getFirstName());
                            if(filterAccounts(newItem))
                                new_list.add(newItem);

                        }

                        if(new_list.size() > 0)
                            Toasty.info(getApplicationContext(),"Collections pending!").show();

                    }
                });
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


            Calendar lastCollectionDayCal = Calendar.getInstance();
            lastCollectionDayCal.setTimeInMillis(lastCollectionDate);

            long day=1000*60*60*24;
            /*if(((todaysDate - lastCollectionDate) >= day  &&
                    todaysDate<endDate &&
                    Float.parseFloat(item.getDueAmt())>0 &&
                    item.getAccountStatus().equalsIgnoreCase("open")) )
                return true;*/

            if(item.getAccountStatus().equalsIgnoreCase("open")&&
                    todaysDate<endDate &&
                    Float.parseFloat(item.getDueAmt())>0)
                if(todaysDate > lastCollectionDate){
                    if(calendar.get(Calendar.DAY_OF_YEAR) != lastCollectionDayCal.get(Calendar.DAY_OF_YEAR))
                        return true;
                }



        }

        return false;
    }
}
