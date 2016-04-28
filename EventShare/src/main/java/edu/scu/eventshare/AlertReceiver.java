package edu.scu.eventshare;

/**
 * Created by Pragati  on 03/04/16.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.ex.chips.recipientchip.DrawableRecipientChip;

public class AlertReceiver extends BroadcastReceiver {
    private static final String TAG = "AlertReceiver";
    String smsAttendees,msg_for_attendes;
    Intent resultintent;

    // Called when a broadcast is made targeting this class
    @Override
    public void onReceive(Context context, Intent intent) {

        createNotification(context,  "\"Send Email/Sms to attendees of the event!\"");
    }

    public void createNotification(Context context,   String msgAlert){

        PendingIntent pendingIntent;
        if (CreateEventActivity.reminderMode.equals("Email"))
        {
            String emailReceiverList = "";

            for(int i=0;i<CreateEventActivity.attendees.length;i++)
            {

                if(CreateEventActivity.attendees[i] != null)
                {
                    int index_start = CreateEventActivity.attendees[i].getEmail().toString().indexOf('<');
                    Log.d(TAG, "index_start " + index_start);
                    int index_end = CreateEventActivity.attendees[i].getEmail().toString().indexOf('>');
                    Log.d(TAG, "index_end " + index_end);
                    emailReceiverList += CreateEventActivity.attendees[i].getEmail().toString().substring(index_start+1, index_end) + ";";
                }
            }
            Log.d(TAG, "index_end " + emailReceiverList);
            String[] ary = emailReceiverList.split(";");
            //Log.d(TAG, "first " + ary[0]);

            Log.d(TAG, "List of attendes :" + emailReceiverList);

            String emailSubject = CreateEventActivity.event_subject;
            String emailText = CreateEventActivity.event_description;

             resultintent = new Intent(Intent.ACTION_SEND);
            //intent.setType("text/html");
            resultintent.putExtra(Intent.EXTRA_EMAIL,ary);
            resultintent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
            resultintent.putExtra(Intent.EXTRA_TEXT, emailText);
            resultintent.setType("vnd.android.cursor.dir/email");
             pendingIntent =PendingIntent.getActivity(context, 0, resultintent, PendingIntent.FLAG_UPDATE_CURRENT);
            setReminder(context,msgAlert,pendingIntent);
        }
        else
        {
            DrawableRecipientChip[] chip=CreateEventActivity.emailRetv.getSortedRecipients();
            smsAttendees=new String();
            Log.d(TAG, "Chip length : " + chip.length);
            for (int i = 0; i < chip.length; i++) {
                msg_for_attendes = "REMINDER!\n Event Title: "+CreateEventActivity.event_subject+ "\n Event Location: "+CreateEventActivity.event_loc;
                msg_for_attendes = msg_for_attendes+ "\n" +CreateEventActivity.sms_msg;
                Log.d(TAG, "Destination : " + chip[i].getEntry().getDestination());
                smsAttendees += chip[i].getEntry().getDestination() + ',';
                Log.d(TAG, "Sms Attendees string: " + smsAttendees);

            }
            Log.d(TAG, "Sms Attendees " + smsAttendees);
            resultintent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + smsAttendees));

            Log.d(TAG, "SMS Body :" + msg_for_attendes);
            resultintent.putExtra("sms_body",msg_for_attendes);

            pendingIntent =PendingIntent.getActivity(context, 0, resultintent, PendingIntent.FLAG_UPDATE_CURRENT);
            setReminder(context,msgAlert,pendingIntent);

        }

    }
    private void setReminder(Context context, String msgAlert, PendingIntent pendingIntent){



        // Builds a notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setContentTitle("Event Reminder")
                        .setContentText("Send Email/Sms to attendees of the event!")
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setTicker(msgAlert);

        mBuilder.setDefaults(Notification.DEFAULT_SOUND);

        int id=99;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, mBuilder.build());

    }

}


