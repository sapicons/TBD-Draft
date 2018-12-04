package com.sapicons.deepak.tbd.Objects;

import java.io.Serializable;

/**
 * Created by Deepak Prasad on 11-09-2018.
 */

public class CGroupItem implements Serializable {

    String groupID,groupName,noOfMonths,startDate,endDate,amount;
    String status;

    public CGroupItem(){}

    public CGroupItem(String groupID,String groupName,String noOfMonths, String startDate, String endDate,String amount){
        this.groupID = groupID;
        this.groupName= groupName;
        this.noOfMonths= noOfMonths;
        this.startDate = startDate;
        this.endDate = endDate;
        this.amount = amount;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getNoOfMonths() {
        return noOfMonths;
    }

    public void setNoOfMonths(String noOfMonths) {
        this.noOfMonths = noOfMonths;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
