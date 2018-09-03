package com.sapicons.deepak.tbd.Adapters;

import android.app.Activity;
import android.content.Context;
import android.icu.text.NumberFormat;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sapicons.deepak.tbd.Objects.ExpenseItem;
import com.sapicons.deepak.tbd.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Deepak Prasad on 19-08-2018.
 */

public class ExpenseItemAdapter extends ArrayAdapter<ExpenseItem> {


    public ExpenseItemAdapter(@NonNull Context context, int resource, @NonNull List<ExpenseItem> objects) {
        super(context, resource, objects);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Log.d("ADAPTER","ExpenseItemAdapter");
        if(convertView == null)
            convertView = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.item_expense,parent, false);

        TextView dateTv = convertView.findViewById(R.id.item_expense_date_tv),
                typeTv = convertView.findViewById(R.id.item_expense_type_tv),
                descriptionTv = convertView.findViewById(R.id.item_expense_description),
                amountTv= convertView.findViewById(R.id.item_expense_amount_tv);


        ExpenseItem item = getItem(position);

        //Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");
        long s = Long.parseLong(item.getDate());
        //calendar.setTimeInMillis(s);

        dateTv.setText(dateFormatter.format(s));
        typeTv.setText(item.getType());
        descriptionTv.setText(item.getDescription());
        //amountTv.setText(item.getAmount());


        Float dA = Float.parseFloat(item.getAmount());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en","in"));
            amountTv.setText(numberFormat.format(dA));
        }else {

            java.text.NumberFormat numberFormat = java.text.NumberFormat.getNumberInstance(Locale.US);
            amountTv.setText(numberFormat.format(dA));
        }


        return  convertView;
    }
}
