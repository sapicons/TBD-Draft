package com.sapicons.deepak.tbd.c_manager.model;

/**
 * Created by Deepak Prasad on 13-12-2018.
 */

public class CCommissionItem {
    String commissionId, cGroupId, cGroupName, auctionWonCustomerId, auctionWonCustomerName, commissionDate;
    int noOfCustomers;
    float totalCommission, commissionPerMember;

    public CCommissionItem(){}

    public CCommissionItem(String commissionId,String cGroupId, String cGroupName, String auctionWonCustomerId,
                           String auctionWonCustomerName, int noOfCustomers, float totalCommission,
                           float commissionPerMember){
        this.commissionId = commissionId;
        this.cGroupId= cGroupId;
        this.cGroupName = cGroupName;
        this.auctionWonCustomerName = auctionWonCustomerName;
        this.auctionWonCustomerId = auctionWonCustomerId;
        this.noOfCustomers = noOfCustomers;
        this.totalCommission = totalCommission;
        this.commissionPerMember = commissionPerMember;
        this.commissionDate = commissionId;
    }

    public String getCommissionId() {
        return commissionId;
    }

    public void setCommissionId(String commissionId) {
        this.commissionId = commissionId;
    }

    public String getcGroupId() {
        return cGroupId;
    }

    public void setcGroupId(String cGroupId) {
        this.cGroupId = cGroupId;
    }

    public String getcGroupName() {
        return cGroupName;
    }

    public void setcGroupName(String cGroupName) {
        this.cGroupName = cGroupName;
    }

    public String getAuctionWonCustomerId() {
        return auctionWonCustomerId;
    }

    public void setAuctionWonCustomerId(String auctionWonCustomerId) {
        this.auctionWonCustomerId = auctionWonCustomerId;
    }

    public String getAuctionWonCustomerName() {
        return auctionWonCustomerName;
    }

    public void setAuctionWonCustomerName(String auctionWonCustomerName) {
        this.auctionWonCustomerName = auctionWonCustomerName;
    }

    public String getCommissionDate() {
        return commissionDate;
    }

    public void setCommissionDate(String commissionDate) {
        this.commissionDate = commissionDate;
    }

    public int getNoOfCustomers() {
        return noOfCustomers;
    }

    public void setNoOfCustomers(int noOfCustomers) {
        this.noOfCustomers = noOfCustomers;
    }

    public float getTotalCommission() {
        return totalCommission;
    }

    public void setTotalCommission(float totalCommission) {
        this.totalCommission = totalCommission;
    }

    public float getCommissionPerMember() {
        return commissionPerMember;
    }

    public void setCommissionPerMember(float commissionPerMember) {
        this.commissionPerMember = commissionPerMember;
    }
}
