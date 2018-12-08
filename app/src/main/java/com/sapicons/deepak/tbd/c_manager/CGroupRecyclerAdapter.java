package com.sapicons.deepak.tbd.c_manager;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sapicons.deepak.tbd.Objects.CGroupItem;
import com.sapicons.deepak.tbd.R;

import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by Deepak Prasad on 04-12-2018.
 */

public class CGroupRecyclerAdapter extends RecyclerView.Adapter<CGroupRecyclerAdapter.ViewHolder> {

    List<CGroupItem> cGroupList;
    Context context;
    String ACTION=null;


    public static class ViewHolder extends RecyclerView.ViewHolder{
        public FancyButton cGroupBtn;

        public ViewHolder(View view){
            super(view);
            cGroupBtn= view.findViewById(R.id.item_c_group_btn);
        }
    }

    public CGroupRecyclerAdapter(List<CGroupItem> cGroupList){
        this.cGroupList = cGroupList;
    }
    // another constructor to define the action of a click
    public CGroupRecyclerAdapter(List<CGroupItem> cGroupList, String ACTION){
        this.cGroupList = cGroupList;
        this.ACTION = ACTION;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_c_group,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final CGroupItem groupItem = cGroupList.get(position);
        holder.cGroupBtn.setText(groupItem.getGroupName());

        holder.cGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,CCustomers.class);
                intent.putExtra("c_group",groupItem);
                intent.putExtra("action",ACTION);

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cGroupList.size();
    }


}
