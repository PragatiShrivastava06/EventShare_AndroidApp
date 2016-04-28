package edu.scu.eventshare;
/**
 * Created by Anusha and Pragati on 03/04/2016.
 */

import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

public class EditEventActivity extends AppCompatActivity {

    private TextView mOutputText;
    ProgressDialog mProgress;
    static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR_READONLY};
    String meetingSummary;

    String day1, month1, year1, hourOfDay, minute,url_link,place1=null;
    String linkText, startTime, startDate, endDate, endTime;
    Button btnStartDate, btnEndDate, btnEndTime, btnStartTime, btnUpdate;
    TextView txtEventName, txtLocation, txtDescription;
    RecipientEditTextView emailRetv;
    Spinner spnReminder, spnMode;
    Event event1;
    Map<String, String> map = null;
    private com.google.api.services.calendar.Calendar mService = null;
    String eventId;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    /**
     * Create the edit event activity.
     *
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_event);

        mProgress = new ProgressDialog(this);
        txtEventName = (TextView) findViewById(R.id.txtEventName);
        txtLocation = (TextView) findViewById(R.id.txtLocation);
        spnReminder = (Spinner) findViewById(R.id.spnReminder);
        spnMode = (Spinner) findViewById(R.id.spnMode);
        txtDescription = (TextView) findViewById(R.id.txtEventDescription);
        emailRetv = (RecipientEditTextView) findViewById(R.id.phone_retv);
        mProgress.setMessage("Calling Google Calendar API ...");
        mOutputText = (TextView) findViewById(R.id.txtEventDescription);

        btnStartDate = (Button) findViewById(R.id.btnStartDate);
        btnEndDate = (Button) findViewById(R.id.btnEndDate);

        txtLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(EditEventActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
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
                final View dialogView = View.inflate(EditEventActivity.this, R.layout.date_time_picker, null);
                final AlertDialog alertDialog = new AlertDialog.Builder(EditEventActivity.this).create();

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
                       /* btnStartDate.setText(startDate);
                        btnStartTime.setText(startTime);*/
                        btnStartDate.setText(String.valueOf(calendar.getTime()).substring(0, 16));
                        Log.i("sdfsd", String.valueOf(calendar.getTime()).substring(0, 16));
                        //time = calendar.getTimeInMillis();
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
                final View dialogView = View.inflate(EditEventActivity.this, R.layout.date_time_picker, null);
                final AlertDialog alertDialog = new AlertDialog.Builder(EditEventActivity.this).create();

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
                        /*btnEndDate.setText(endDate);
                        btnEndTime.setText(endTime);*/
                        btnEndDate.setText(String.valueOf(calendar.getTime()).substring(0,16));
                        Log.i("sdfsd", String.valueOf(calendar.getTime()).substring(0, 16));
                        //time = calendar.getTimeInMillis();
                        alertDialog.dismiss();
                    }
                });
                alertDialog.setView(dialogView);
                alertDialog.show();
            }
        });

        Intent getEventId = getIntent();
        eventId = getEventId.getStringExtra("Id");
        System.out.println("your id" + eventId);
        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        /*EventActivity.mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));*/
        spnMode = (Spinner) findViewById(R.id.spnMode);
        spnMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                if (spnMode.getSelectedItem().toString().equals("Email")) {
                    emailRetv.setText("");
                    emailRetv.setTokenizer(new Rfc822Tokenizer());
                    emailRetv.setAdapter(new BaseRecipientAdapter(getApplicationContext()));
                } else {
                    emailRetv.setText("");
                    emailRetv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                    emailRetv.setAdapter(new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, getApplicationContext()));

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btnUpdate = (Button) findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
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
                    updateResults();
                    finish();
                }


            }
        });
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


    private void updateResults() {
        Event.ExtendedProperties ep = new Event.ExtendedProperties();
        Map<String, String> tst = new HashMap<String, String>();

        if(PickFileWithOpenerActivity.resId==null)
            PickFileWithOpenerActivity.resId="";
        DrawableRecipientChip[] chips = emailRetv.getSortedRecipients();
        final Event event = new Event()
                .setSummary(txtEventName.getText().toString())
                .setLocation(txtLocation.getText().toString())
                .setDescription(txtDescription.getText().toString() + PickFileWithOpenerActivity.resId);


        DateTime startDateTime = new DateTime(startDate + "T" + startTime + ":00-08:00");
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("America/Los_Angeles");
        event.setStart(start);

        DateTime endDateTime = new DateTime(endDate+ "T" + endTime + ":00-08:00");
        //System.out.print(btnStartDate.getText().toString() + "T" + btnStartTime.getText().toString() + ":00-08:00");
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("America/Los_Angeles");
        event.setEnd(end);
        tst.put("mode", spnMode.getSelectedItem().toString());


        EventAttendee[] attendees = new EventAttendee[10];

        if ((emailRetv != null) && (spnMode.getSelectedItem().toString().equals("Email"))) {

            for (int i = 0; i < chips.length; i++) {
                attendees[i] = new EventAttendee().setEmail(chips[i].toString());
                System.out.println(chips[i].toString());
            }
            event.setAttendees(Arrays.asList(attendees));
            event.isGuestsCanInviteOthers();
            event.setGuestsCanModify(true);
        } else if ((emailRetv != null) && (spnMode.getSelectedItem().toString().equals("SMS"))) {
            for (int i = 0; i < chips.length; i++) {
                String msg ="You are invited!\nTitle: "+ event.getSummary().toString();
                if(!txtDescription.getText().toString().equals("")){
                    System.out.print("sdfsdfsdf");
                    System.out.print(msg);
                    msg+="\nDescription: "+event.getDescription().toString();
                }
                if(!event.getLocation().toString().equals("")){
                    msg+="\nLocation: "+event.getLocation().toString();
                }
                msg+="\nFrom: " + startDate + " " + startTime + "\nTo: " + startDate + " " + startTime;
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
                System.out.println("helrjewlrwklrwelk");
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


        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    mService.events().update("primary", eventId, event).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

        }.execute();
    }

    private String validate(int time) {
        if (time < 10)
            return "0" + String.valueOf(time);
        else
            return String.valueOf(time);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_event_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_delete) {
            try {
                mService.events().delete("primary", eventId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.action_edit) {

        }else if (id == R.id.action_attach) {
            Intent AttachIntent = new Intent(EditEventActivity.this, PickFileWithOpenerActivity.class);
            startActivity(AttachIntent);
        }

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    /**
     * Called whenever this activity is pushed to the foreground, such as after
     * a call to onCreate().
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            refreshResults();
        } else {
            mOutputText.setText("Google Play Services required: " +
                    "after installing, close and relaunch this app.");
        }
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
                    mOutputText.setText("Account unspecified.");
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
                    place1=place.getAddress().toString();
                    //txtLocation.setText(place.getAddress());

                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    // TODO: Handle the error.
                    Log.i("D", status.getStatusMessage());
                    txtLocation.setText("No Location Selected");

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }

                break;
        }

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
                EditEventActivity.this,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, Event> {
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
        protected Event doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         *
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private Event getDataFromApi() throws IOException {

            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            List<String> eventStrings = new ArrayList<String>();
            Event event = mService.events().get("primary", eventId).execute();
            event1 = event;
            return event1;
        }

        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Event output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                mOutputText.setText("No results returned.");
            } else {
                DateTime start = output.getStart().getDateTime();
                DateTime end = output.getEnd().getDateTime();
                if (start == null || end == null) {
                    //Log.d(TAG, "Looks like its an ALL DAY Event");
                    startDate = output.getStart().getDate().toString();
                    startTime = "00:00";
                    //endDate = output.getEnd().getDate().toString(); - this is adding another day so copying value of startDate in EndDate
                    endDate = startDate;
                    endTime = "23:59";
                } else {
                    String[] startString = start.toString().split("T");
                    startDate = startString[0];
                    startTime = startString[1].substring(0, 5);

                    String[] endString = end.toString().split("T");
                    endDate = endString[0];
                    endTime = endString[1].substring(0, 5);
                }
                Date startd = null,endd=null;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Log.d("startdate",startDate);
                Log.d("enddate",endDate);
                Log.d("starttime",startTime.substring(0, 5).toString()+":00");
                Log.d("endtime",endTime.substring(0, 5).toString()+":00");
                if (startDate != null) {
                    String sdateInString = startDate.toString()+" "+startTime.substring(0, 5).toString()+":00";
                    Log.d("sdateInStr",sdateInString);
                    try {
                        startd = sdf.parse(sdateInString);
                        Log.d("startd",startd.toString().substring(0, 16));
                        btnStartDate.setText(startd.toString().substring(0, 16));
                    }catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                if(endDate!=null){
                    String edateInString=endDate.toString()+" "+endTime.substring(0,5).toString()+":00";
                    Log.d("edateInStr",edateInString);
                    try {
                        endd=sdf.parse(edateInString);
                        Log.d("edateInStr",endd.toString());
                        btnEndDate.setText(endd.toString().substring(0, 16));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if (output.getSummary() != null)
                    txtEventName.setText(output.getSummary());
                if (output.getDescription() != null)
                {
                    if(output.getDescription().toLowerCase().contains("https")) {
                        txtDescription.setText(output.getDescription().substring(0, output.getDescription().indexOf("https")));
                        url_link=output.getDescription().substring(output.getDescription().indexOf("https"));
                    }
                    else{
                        txtDescription.setText(output.getDescription());
                    }
                    //Log.d(TAG, "Ev_desc " + output.getDescription());
                    //Log.d(TAG, "Ev_desc " + output.getdescription().indexOf("https"));
                    // Log.d(TAG, "Ev_desc " + output.getdescription().substring(output.getdescription().indexOf("https")));


                }

                    if(place1!=null)
                        txtLocation.setText(place1);
                    else if
                            (output.getLocation() != null)
                        txtLocation.setText(output.getLocation());


                Event.ExtendedProperties ep1 = output.getExtendedProperties();
                if (ep1 != null) {
                    map = ep1.getShared();
                    String s = map.get("mode");
                    if (s.equals("Email"))
                        spnMode.setSelection(0);
                    else
                        spnMode.setSelection(1);

                    if (s.equals("Email")) {
                        if (output.getAttendees() != null) {
                            List<EventAttendee> attendeeList = output.getAttendees();
                            emailRetv.setText("");
                            emailRetv.setTokenizer(new Rfc822Tokenizer());
                            emailRetv.setAdapter(new BaseRecipientAdapter(getApplicationContext()));
                            for (EventAttendee e : attendeeList) {
                                emailRetv.append(e.getDisplayName() + "<" + e.getEmail() + ">");

                            }
                        }
                    } else {
                        if (map.size() > 1) {
                            emailRetv.setText("");
                            emailRetv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                            emailRetv.setAdapter(new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, getApplicationContext()));

                            for (Map.Entry<String, String> m : map.entrySet()) {
                                if (!(m.getKey().equals("mode"))) {
                                    System.out.println(m.getKey() + "<" + m.getValue() + ">");
                                    emailRetv.append(m.getKey() + "<" + m.getValue() + ">");
                                }
                            }
                        }
                        emailRetv.setText(ViewEventActivity.emailRetv.getText());
                    }
                }

            }
            spnMode.setEnabled(false);
            spnReminder.setEnabled(false);

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
                            ViewEventActivity.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("");
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }
}
