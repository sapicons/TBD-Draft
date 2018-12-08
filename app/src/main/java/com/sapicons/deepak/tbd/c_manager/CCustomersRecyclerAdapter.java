package com.sapicons.deepak.tbd.c_manager;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sapicons.deepak.tbd.Objects.CGroupItem;
import com.sapicons.deepak.tbd.Objects.CustomerItem;
import com.sapicons.deepak.tbd.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by Deepak Prasad on 04-12-2018.
 */

public class CCustomersRecyclerAdapter extends RecyclerView.Adapter<CCustomersRecyclerAdapter.ViewHolder> {
    List<CustomerItem> customerList;
    Context context;
    String ACTION=null;
    CGroupItem groupItem;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView customerPic;
        public TextView customerName;
        public RelativeLayout relativeLayout;

        public ViewHolder(View view){
            super(view);
            customerName = view.findViewById(R.id.item_customer_name);
            customerPic = view.findViewById(R.id.item_customer_image);
            relativeLayout = view.findViewById(R.id.item_customer_relative_layout);
        }
    }

    public CCustomersRecyclerAdapter(List<CustomerItem> customerList,CGroupItem groupItem){
        this.customerList = customerList;
        this.groupItem = groupItem;
    }

    // another constructor to define action for what a click on customer item does
    public CCustomersRecyclerAdapter(List<CustomerItem> customerList,CGroupItem groupItem, String ACTION){
        this.customerList = customerList;
        this.groupItem = groupItem;
        this.ACTION = ACTION;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_customer,parent,false);

        return new CCustomersRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final CustomerItem customerItem = customerList.get(position);
        holder.customerName.setText(customerItem.getFirstName()+" "+customerItem.getLastName());
        if(customerItem.getPhotoUrl() != null){
            Glide.with(context).load(customerItem.getPhotoUrl()).into(holder.customerPic);
        }else Glide.with(context).load(R.mipmap.ic_customers_black).into(holder.customerPic);

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseAction(customerItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    public void chooseAction(CustomerItem customerItem){
        if(ACTION!=null) {
            if (ACTION.equals("C_DAY")) {
                Toasty.info(context, "C_DAY").show();
                Intent intent = new Intent(context,CommisionCalculation.class);
                intent.putExtra("c_group",groupItem);
                intent.putExtra("customer_item",customerItem);

                context.startActivity(intent);
            }
        }
    }


}
