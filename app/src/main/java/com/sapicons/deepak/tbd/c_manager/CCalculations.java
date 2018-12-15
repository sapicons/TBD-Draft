package com.sapicons.deepak.tbd.c_manager;

import com.sapicons.deepak.tbd.Objects.CGroupItem;

/**
 * Created by Deepak Prasad on 13-12-2018.
 */

public class CCalculations {


    public static float getProfit(CGroupItem groupItem){
        float cValue = Float.parseFloat(groupItem.getAmount());
        float noOfMonths = Float.parseFloat(groupItem.getNoOfMonths());
        return (cValue/noOfMonths);
    }

    public static float getCommisionAmount(float cWonAmt, CGroupItem groupItem){
        float cValue = Float.parseFloat(groupItem.getAmount());
        float commision = cValue - cWonAmt - getProfit(groupItem);
        return commision;
    }
}
