package com.sapicons.deepak.tbd.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.icu.text.NumberFormat;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sapicons.deepak.tbd.Objects.AccountItem;
import com.sapicons.deepak.tbd.Objects.CustomerItem;
import com.sapicons.deepak.tbd.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by Deepak Prasad on 04-08-2018.
 */



public class AccountItemAdapter extends ArrayAdapter<AccountItem> {
    int i ;
    FancyButton collectButton;

    String COLOR_RED ="#d50000";
    String COLOR_GREEN ="#2e7d32";
    String COLOR_YELLOW ="#c56000";

    public AccountItemAdapter(@NonNull Context context, int resource, @NonNull List<AccountItem> objects,int i) {

        super(context, resource, objects);
        this.i = i;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null)
            convertView = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.item_account,parent, false);

        CircleImageView customerPic = convertView.findViewById(R.id.item_account_customer_pic_iv);
        TextView customerNameTv = convertView.findViewById(R.id.item_account_customer_name),
                accTypeTv = convertView.findViewById(R.id.item_account_type_tv),
                startDateTv = convertView.findViewById(R.id.item_account_start_date_tv),
                endDateTv = convertView.findViewById(R.id.item_account_end_date_tv),
                dueAmountTv = convertView.findViewById(R.id.item_account_due_amount_tv),
                accNoTv = convertView.findViewById(R.id.item_account_acc_no_tv);

        collectButton = convertView.findViewById(R.id.item_acc_collect_btn);

        collectButton.setBackgroundColor(Color.parseColor("#2e7d32"));

        AccountItem accountItem = getItem(position);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");
        long s = Long.parseLong(accountItem.getStartDate());
        long e = Long.parseLong(accountItem.getEndDate());
        calendar.setTimeInMillis(s);
        startDateTv.setText(dateFormatter.format(calendar.getTime()));
        calendar.setTimeInMillis(e);
        endDateTv.setText(dateFormatter.format(calendar.getTime()));


        if(accountItem.getCustomerPicUrl().length()>0)
            Glide.with(getContext()).load(accountItem.getCustomerPicUrl()).into(customerPic);
        customerNameTv.setText(accountItem.getFirstName()+" "+accountItem.getLastName());
        accTypeTv.setText(accountItem.getAccoutType());

        Float dA = Float.parseFloat(accountItem.getDueAmt());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en","in"));
            dueAmountTv.setText(numberFormat.format(dA));
        }else {

            java.text.NumberFormat numberFormat = java.text.NumberFormat.getNumberInstance(Locale.US);
            dueAmountTv.setText(numberFormat.format(dA));
        }

        Log.d("TAG","I: "+i);

        if(i==1) {
            accNoTv.setText("Acc/No: "+accountItem.getAccountNumber());
            accNoTv.setVisibility(View.VISIBLE);
        }
        else
            accNoTv.setVisibility(View.GONE);

        setCollectBtnColor(accountItem);


        return convertView;

    }

    public void setCollectBtnColor(AccountItem accountItem){
        Calendar calendar = Calendar.getInstance();
        long currTime = calendar.getTimeInMillis();
        long day = 1000*60*60*24;   // a day
        long yesTime = currTime - day;   //start counting days from yesterday

        long startTime = Long.parseLong(accountItem.getStartDate());  //get the start date of the account
        float loanAmt = Float.parseFloat(accountItem.getLoanAmt());   //get the loan amount
        float totalCollectedAmt=0.0f;
        if(accountItem.getTotalCollectedAmt() !=null)
            totalCollectedAmt = Float.parseFloat(accountItem.getTotalCollectedAmt()); //get total collected amount till now

        float numberOfDays  = (currTime - startTime)/(day);             // no of days (yesterday - start day)
        if(totalCollectedAmt >= (numberOfDays-1)*(0.01*loanAmt) )          // total amount that must be collected for green button
            collectButton.setBackgroundColor(Color.parseColor(COLOR_GREEN));
        else if(totalCollectedAmt < (numberOfDays-4)*(0.01*loanAmt))
            collectButton.setBackgroundColor(Color.parseColor(COLOR_RED));
        else collectButton.setBackgroundColor(Color.parseColor(COLOR_YELLOW));

    }
}
