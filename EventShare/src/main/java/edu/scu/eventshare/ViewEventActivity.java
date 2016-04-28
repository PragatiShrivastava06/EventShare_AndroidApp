package edu.scu.eventshare;
/**
 * Created by Anusha and Pragati on 03/06/2016.
 */

import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Rfc822Tokenizer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientEditTextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.api.services.calendar.model.EventReminder;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ViewEventActivity extends AppCompatActivity {

    private TextView mOutputText;
    ProgressDialog mProgress;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR_READONLY};
    String meetingSummary,url_link="";
    Button btnStartDate, btnEndDate,btnAttach;
    TextView txtEventName, txtLocation, txtDescription;
    String startDate, startTime, endDate, endTime;
    static RecipientEditTextView emailRetv;
    TextView spnReminder, spnMode;
    ImageView img;
    LinearLayout linMap;

    SharedPreferences.Editor editor;
    SharedPreferences sharedPrefs;
    Event event1;

    private com.google.api.services.calendar.Calendar mService = null;
    String eventId;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    /**
     * Create the main activity.
     *
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_event);
        this.setTitle(" ");
        /*    ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.ic_launcher);
        actionBar.setDisplayHomeAsUpEnabled(true);//home button
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setHomeButtonEnabled(true);//*/

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Calendar API ...");

        mOutputText = (TextView) findViewById(R.id.txtEventDescription);
        img=(ImageView)findViewById(R.id.imgMap);
        linMap=(LinearLayout)findViewById(R.id.linMap);

        Intent getEventId = getIntent();
        eventId = getEventId.getStringExtra("Id");
        Log.i("Event ID", eventId);
        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        /*EventActivity.mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_event_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_delete) {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    try {
                        mService.events().delete("primary", eventId).execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
            Intent listActivity = new Intent(ViewEventActivity.this, EventActivity.class);
            startActivity(listActivity);

        } else if (id == R.id.action_edit) {
            Intent editEventIntent = new Intent(ViewEventActivity.this, EditEventActivity.class);
            editEventIntent.putExtra("Id", eventId);
            startActivity(editEventIntent);

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
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Attempt to get a set of data from the Google Calendar API to display. If the
     * email address isn't known yet, then call chooseAccount() method so the
     * user can pick an account.
     */
    private void refreshResults() {
        if (EventActivity.mCredential .getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                new MakeRequestTask(CreateEventActivity.mCredential).execute();
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
                EventActivity.mCredential .newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
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
                ViewEventActivity.this,
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

            return event;
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
                txtEventName = (TextView) findViewById(R.id.txtEventName);
                txtLocation = (TextView) findViewById(R.id.txtLocation);
                spnReminder = (TextView) findViewById(R.id.spnReminder);
                spnMode = (TextView) findViewById(R.id.spnMode);

                txtDescription = (TextView) findViewById(R.id.txtEventDescription);
                btnStartDate = (Button) findViewById(R.id.btnStartDate);
                btnEndDate = (Button) findViewById(R.id.btnEndDate);
                DateTime start = output.getStart().getDateTime();
                emailRetv = (RecipientEditTextView) findViewById(R.id.phone_retv);
                btnStartDate.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {     }
                });


                btnEndDate.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                    }
                });


                btnAttach = (Button) findViewById(R.id.btndownload);

                btnAttach.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        if ((url_link.equals("")) || (url_link == null)) {
                            Toast.makeText(getApplicationContext(), "No file attached for this event", Toast.LENGTH_SHORT).show();
                            //btnAttach.setEnabled(false);
                        } else {
                            Uri uri = Uri.parse(url_link);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    }
                });

                DateTime end = output.getEnd().getDateTime();
                Event.Reminders reminders = output.getReminders();

                List<EventReminder> reminderOverrides = reminders.getOverrides();
                if (reminderOverrides != null) {
                    Log.i("Reminder Overrides", reminderOverrides.get(0).getMinutes().toString());
                    spnReminder.setText(reminderOverrides.get(0).getMinutes().toString());
                }
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
                    startTime = startString[1];

                    String[] endString = end.toString().split("T");
                    endDate = endString[0];
                    endTime = endString[1];
                }

                Date startd = null,endd=null;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Log.d("startdate",startDate);
                Log.d("enddate",endDate);
                Log.d("starttime",startTime.substring(0, 5).toString()+":00");
                Log.d("endtime",endTime.substring(0, 5).toString()+":00");
                if (startDate != null) {
                    String sdateInString = startDate+" "+startTime.substring(0, 5)+":00";
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
                    String edateInString=endDate+" "+endTime.substring(0,5)+":00";
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
                if (output.getLocation() != null)
                    txtLocation.setText(output.getLocation());
                // if(output.get("mode").toString().equals("Email")){
                Event.ExtendedProperties ep1 = output.getExtendedProperties();
                if (ep1 != null) {
                    Map<String, String> map = ep1.getShared();
                    String s = map.get("mode");
                    if(s!=null){
                        if (s.equals("Email"))
                            spnMode.setText("Email");
                        else
                            spnMode.setText("SMS");

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

                        }

                    }


                }else{
                    if (output.getAttendees() != null) {
                        List<EventAttendee> attendeeList = output.getAttendees();
                        emailRetv.setText("");
                        emailRetv.setTokenizer(new Rfc822Tokenizer());
                        emailRetv.setAdapter(new BaseRecipientAdapter(getApplicationContext()));
                        for (EventAttendee e : attendeeList) {
                            emailRetv.append(e.getDisplayName() + "<" + e.getEmail() + ">");
                        }
                    }

                }
                emailRetv.setEnabled(false);
                if(output.getLocation()!=null){
                    img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // (you may get multiple results)

                            String thePlace = txtLocation.getText().toString();
                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                    Uri.parse("geo:0,0?q=" + thePlace));
                            startActivity(intent);
                        }
                    });
                }else{
                    linMap.setVisibility(View.INVISIBLE);
                }

            }
        }

        /*private void selectValue(Spinner spinner, String value) {
            for (int i = 0; i < spinner.getCount(); i++) {
                if (spinner.getItemAtPosition(i).equals(value)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }*/

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
                    // mOutputText.setText("The following error occurred:"
                    //  + mLastError.getMessage());
                    mOutputText.setText("");
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }
}
