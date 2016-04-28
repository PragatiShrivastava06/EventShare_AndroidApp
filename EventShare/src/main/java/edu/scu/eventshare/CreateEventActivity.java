package edu.scu.eventshare;

import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.util.Rfc822Tokenizer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.MultiAutoCompleteTextView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anusha and Pragati on 02/17/2016.
 */
public class CreateEventActivity extends EventActivity
{
    private ArrayList<Map<String, String>> mPeopleList;
    private SimpleAdapter mAdapter;
    private MultiAutoCompleteTextView mTxtPhoneNo;
    Button btnStartDate, btnEndDate, btnEndTime, btnStartTime, btnDone;
    static RecipientEditTextView emailRetv = null, phnRetv = null;
    TextView txtEventName, txtLocation, txtDescription, hyperlink;
    Spinner spnReminder, spnMode;
    //TextView link;
    String day1, month1, year1, hourOfDay, minute;
    String linkText, startTime, startDate, endDate, endTime;
    private TextView mOutputText;
    ProgressDialog mProgress;
    static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int RESULT_ATTACH = 1003;
    private static final String PREF_ACCOUNT_NAME = "Calendar";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};
    private static final String TAG = "CreateEvent";
    static String event_description, event_subject, event_loc, reminderMode, sms_msg;
    static EventAttendee[] attendees;
    static DrawableRecipientChip[] chips;


    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event);
        this.setTitle("Create Events");

        btnStartDate = (Button) findViewById(R.id.btnStartDate);
        btnEndDate = (Button) findViewById(R.id.btnEndDate);
        txtEventName = (TextView) findViewById(R.id.txtEventName);
        txtLocation = (TextView) findViewById(R.id.txtLocation);
        spnReminder = (Spinner) findViewById(R.id.spnReminder);
        txtLocation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                try {
                    if (hasFocus) {
                        Intent intent =
                                new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                        .build(CreateEventActivity.this);
                        startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    }

                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }

            }
        });
        btnStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View dialogView = View.inflate(CreateEventActivity.this, R.layout.date_time_picker, null);
                final AlertDialog alertDialog = new AlertDialog.Builder(CreateEventActivity.this).create();

                dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
                        TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);

                        Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                                datePicker.getMonth(),
                                datePicker.getDayOfMonth(),
                                timePicker.getCurrentHour(),
                                timePicker.getCurrentMinute());

                        month1 = validate(datePicker.getMonth() + 1);
                        day1 = validate(datePicker.getDayOfMonth());
                        year1 = String.valueOf(datePicker.getYear());
                        hourOfDay = validate(timePicker.getCurrentHour());
                        minute = validate(timePicker.getCurrentMinute());
                        startDate = year1 + "-" + month1 + "-" + day1;
                        startTime = hourOfDay + ":" + minute;
                        Log.d(TAG, "startDate " + startDate);
//                        btnStartDate.setText(startDate);
//                        btnStartTime.setText(startTime);
                        btnStartDate.setText(String.valueOf(calendar.getTime()).substring(0, 16));
                        Log.i("sdfsd", String.valueOf(calendar.getTime()).substring(0, 16));
                        alertDialog.dismiss();
                    }
                });
                alertDialog.setView(dialogView);
                alertDialog.show();
            }
        });

        btnEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View dialogView = View.inflate(CreateEventActivity.this, R.layout.date_time_picker, null);
                final AlertDialog alertDialog = new AlertDialog.Builder(CreateEventActivity.this).create();

                dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
                        TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);

                        Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                                datePicker.getMonth(),
                                datePicker.getDayOfMonth(),
                                timePicker.getCurrentHour(),
                                timePicker.getCurrentMinute());

                        month1 = validate(datePicker.getMonth() + 1);
                        day1 = validate(datePicker.getDayOfMonth());
                        year1 = String.valueOf(datePicker.getYear());
                        hourOfDay = validate(timePicker.getCurrentHour());
                        minute = validate(timePicker.getCurrentMinute());
                        endDate = year1 + "-" + month1 + "-" + day1;
                        endTime = hourOfDay + ":" + minute;
                        Log.d(TAG, "startDate" + startDate);
                        btnEndDate.setText(String.valueOf(calendar.getTime()).substring(0, 16));
//                        btnEndDate.setText(endDate);
//                        btnEndTime.setText(endTime);
                        //time = calendar.getTimeInMillis();
                        alertDialog.dismiss();
                    }
                });
                alertDialog.setView(dialogView);
                alertDialog.show();
            }
        });
        btnDone = (Button) findViewById(R.id.btnDone);
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        emailRetv = (RecipientEditTextView) findViewById(R.id.phone_retv);
        /*EventActivity.mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());*/
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Calendar API ...");
        spnMode = (Spinner) findViewById(R.id.spnMode);
        spnMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                if (spnMode.getSelectedItem().toString().equals("Email")) {
                    reminderMode = "Email";
                    emailRetv.setText("");
                    emailRetv.setTokenizer(new Rfc822Tokenizer());
                    emailRetv.setAdapter(new BaseRecipientAdapter(getApplicationContext()));
                } else {
                    reminderMode = "SMS";
                    emailRetv.setText("");
                    emailRetv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                    emailRetv.setAdapter(new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, getApplicationContext()));

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
        //Saving the created event
        btnDone.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           if ((btnStartDate.getText().toString().toLowerCase().equals("start date")) || (btnEndDate.getText().toString().toLowerCase().equals("end date"))) {
                                               Toast.makeText(v.getContext(), "Enter the Date Range", Toast.LENGTH_LONG).show();
                                           } else if (validateDates(startDate + "T" + startTime + ":00", endDate + "T" + endTime + ":00")) {
                                               Toast.makeText(v.getContext(), "Invalid Start and End Dates", Toast.LENGTH_LONG).show();
                                           } else if (emailRetv.getText().equals("")) {
                                               Toast.makeText(v.getContext(), "Enter the Attendees", Toast.LENGTH_LONG).show();
                                           } else if (txtEventName.getText().toString().equals("")) {
                                               Toast.makeText(v.getContext(), "Enter the Event Title", Toast.LENGTH_LONG).show();
                                           } else {
                                               setAlarm(v);
                                               refreshResults();
                                               finish();
                                           }
                                       }
                                   }

        );

        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private Boolean validateDates(String start, String end) {
        Date sDate = null, eDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            sDate = sdf.parse(start);
            eDate = sdf.parse(end);
            Log.i("StartD", sDate.toString());
            Log.i("EndD", eDate.toString());
            Log.i("Compareto", String.valueOf(sDate.compareTo(eDate) < 0));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (sDate.compareTo(eDate) < 0)
            return false;
        else
            return true;
    }

    //Checked with DateTime parameters are double Digits

    private String validate(int time) {
        if (time < 10)
            return "0" + String.valueOf(time);
        else
            return String.valueOf(time);
    }

    //Alarm
    public void setAlarm(View view) {

        //Long alertTime = new GregorianCalendar().getTimeInMillis()*5000;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.parseInt(startDate.substring(0, 4)),
                Integer.parseInt(startDate.substring(5, 7)) - 1,
                Integer.parseInt(startDate.substring(8, 10)),
                Integer.parseInt(startTime.substring(0, 2)),
                Integer.parseInt(startTime.substring(3, 5)));
        long startUTCTime = (calendar.getTimeInMillis() - (Integer.parseInt(spnReminder.getSelectedItem().toString()) * 60000));
        Log.d(TAG, "startUTCTime " + startUTCTime);


        long alertTime = startUTCTime;

        // Define our intention of executing AlertReceiver

        Intent alertIntent = new Intent(this, AlertReceiver.class);

        alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Allows you to schedule for your application to do something at a later date
        // even if it is in he background or isn't active
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // set() schedules an alarm to trigger
        // Trigger for alertIntent to fire few minutes before the event
        // FLAG_UPDATE_CURRENT : Update the Intent if active
        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
                PendingIntent.getBroadcast(this, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_event_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_attach) {
            Intent AttachIntent = new Intent(CreateEventActivity.this, PickFileWithOpenerActivity.class);
            startActivity(AttachIntent);
        }
      /*  if (id == R.id.action_create) {

        }*/

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {


            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        EventActivity.mCredential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    //   mOutputText.setText("Account unspecified.");
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK) {
                    chooseAccount();
                }
                break;
            case PLACE_AUTOCOMPLETE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    Log.i("d", "Place: " + place.getAddress());

                    txtLocation.setText(place.getAddress());

                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    // TODO: Handle the error.
                    Log.i("D", status.getStatusMessage());
                    txtLocation.setText("No Location Selected");

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }

                break;
            /*case RESULT_ATTACH:
                if (resultCode != RESULT_OK) {
                    TextView link = (TextView) findViewById(R.id.hyperlink);

                    linkText = DownloadRandomPicture.shareAddress;
                    link.setText(Html.fromHtml(linkText));
                    link.setMovementMethod(LinkMovementMethod.getInstance());
                }
                break;C*/
        }
        // sendMail();

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Attempt to get a set of data from the Google Calendar API to display. If the
     * email address isn't known yet, then call chooseAccount() method so the
     * user can pick an account.
     */
    private void refreshResults() {
        if (EventActivity.mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                // sendMail();
                new MakeRequestTask(EventActivity.mCredential).execute();
            } else {
                mOutputText.setText("No network connection available.");
            }
        }
    }

    /**
     * Starts an activity in Google Play Services so the user can pick an
     * account.
     */
    private void chooseAccount() {
        Log.d("Pragati", "chooseAccount");
        startActivityForResult(
                EventActivity.mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode,
                CreateEventActivity.this,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
/*
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "CreateEventActivity Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://edu.scu.eventshare/http/host/path")
        );
       // AppIndex.AppIndexApi.start(client, viewAction);*/
    }

    @Override
    public void onStop() {
        super.onStop();

    }


    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }


        private List<String> getDataFromApi() throws IOException {
            Event.ExtendedProperties ep = new Event.ExtendedProperties();
            Map<String, String> tst = new HashMap<String, String>();

            chips = emailRetv.getSortedRecipients();
            Log.d(TAG, "Sms chips " + chips[0].toString());

            for (int i = 0; i < chips.length; i++) {
                String msg = "Reminder:" + CreateEventActivity.event_subject + ": " + CreateEventActivity.event_description;
                Log.d(TAG, "Sms Attendees string: " + chips[i].getEntry().getDestination() + ",");

            }
            txtDescription = (TextView) findViewById(R.id.txtEventDescription);

            if (PickFileWithOpenerActivity.resId == null)
                PickFileWithOpenerActivity.resId = "";
            Event event = new Event()
                    .setSummary(txtEventName.getText().toString())
                    .setLocation(txtLocation.getText().toString())
                    .setDescription(txtDescription.getText().toString() + PickFileWithOpenerActivity.resId);
            event_description = txtDescription.getText().toString();
            event_subject = event.getSummary();
            event_loc = event.getLocation();

            DateTime startDateTime = new DateTime(startDate + "T" + startTime + ":00-08:00");
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone("America/Los_Angeles");
            event.setStart(start);

            DateTime endDateTime = new DateTime(endDate + "T" + endTime + ":00-08:00");
            System.out.print(endDate + "T" + endTime + ":00-08:00");
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone("America/Los_Angeles");
            event.setEnd(end);
            tst.put("mode", spnMode.getSelectedItem().toString());

            attendees = new EventAttendee[10];

            if ((emailRetv != null) && (spnMode.getSelectedItem().toString().equals("Email"))) {

                for (int i = 0; i < chips.length; i++) {
                    attendees[i] = new EventAttendee().setEmail(chips[i].toString());
                    System.out.println(chips[i].toString());
                }
                event.setAttendees(Arrays.asList(attendees));
                event.isGuestsCanInviteOthers();
                event.setGuestsCanModify(false);
            } else if ((emailRetv != null) && (spnMode.getSelectedItem().toString().equals("SMS"))) {
                for (int i = 0; i < chips.length; i++) {
                    String msg = "You are invited!\nTitle: " + event.getSummary().toString();
                    if (!txtDescription.getText().toString().equals("")) {
                        System.out.print("sdfsdfsdf");
                        System.out.print(msg);
                        msg += "\nDescription: " + event.getDescription().toString();
                    }
                    if (!event.getLocation().toString().equals("")) {
                        msg += "\nLocation: " + event.getLocation().toString();
                    }
                    msg += "\nFrom: " + startDate + " " + startTime + "\nTo: " + startDate + " " + startTime;
                    sms_msg = msg;
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(chips[i].toString(), null, msg, null, null);
                    System.out.println("contactid" + chips[i].getContactId());
                    System.out.println("display" + chips[i].getDisplay());
                    System.out.println("loogkup key" + chips[i].getLookupKey());
                    System.out.println(chips[i].toString());
                    System.out.println("destination" + chips[i].getEntry().getDestination());
                    String key = chips[i].getDisplay().toString();
                    String value = chips[i].getEntry().getDestination();

                    System.out.println(key + ":" + value);
                    tst.put(key, value);
                }
                //send sms
            }

            ep.setShared(tst);
            event.setExtendedProperties(ep);


            EventReminder[] reminderOverrides = new EventReminder[]{
                    new EventReminder().setMethod("email").setMinutes(Integer.parseInt(spnReminder.getSelectedItem().toString())),
                    new EventReminder().setMethod("popup").setMinutes(Integer.parseInt(spnReminder.getSelectedItem().toString())),
            };
            Event.Reminders reminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(Arrays.asList(reminderOverrides));
            event.setReminders(reminders);


            event = mService.events().insert(mService.events().list("primary").getCalendarId(), event).setSendNotifications(true).execute();
            System.out.printf("Event created: %s\n", event.getHtmlLink());
            Log.d(TAG, "Event created : " + event.getHtmlLink());


            List<String> eventStrings = new ArrayList<String>();

            Log.d(TAG, "Picked Date " + btnStartDate.getText());
            Log.d(TAG, "Picked Time " + btnStartTime.getText());

            return eventStrings;

        }


        @Override
        protected void onPreExecute() {
            // mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            finish();
            mProgress.hide();
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            CreateEventActivity.REQUEST_AUTHORIZATION);
                } else {
                    //mOutputText.setText("The following error occurred: "
                    // + mLastError.getMessage());
                }
            } else {
                // mOutputText.setText("Request cancelled.");
            }
        }
    }
}

