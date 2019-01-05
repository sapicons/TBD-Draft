package com.sapicons.deepak.tbd.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;

import com.sapicons.deepak.tbd.Objects.AccountItem;
import com.sapicons.deepak.tbd.R;

import es.dmoral.toasty.Toasty;

/**
 * Created by Deepak Prasad on 04-01-2019.
 */

public class SendSmsUtil {

    private static void sendMessageForAccCreation(Context context, AccountItem accountItem){

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean sendSMS = pref.getBoolean(context.getResources().getString(R.string.key_send_sms),false);
        if(sendSMS == false)
            return;
        String msg= accountItem.getAccoutType()+" Created: "+accountItem.getAccountNumber()+" Due Amount: "+accountItem.getDueAmt();
        String phoneNumber = accountItem.getPhoneNumber();

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, msg, null, null);
            Toasty.success(context,"Message Sent").show();
        } catch (Exception ex) {
            //Toast.makeText(getContext(),ex.getMessage().toString(),
            //      Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }
}
