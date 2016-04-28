package edu.scu.eventshare;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChatActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    List<Event> modifiedOP;
    String eventId;
    String chatString;
    String title;
    AclRule rule = new AclRule();
    AclRule.Scope scope = new AclRule.Scope();
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };
    List<String> result;
    String getChatText;
    Timer timer = new Timer();
    boolean sent = false;
    private ChatAdapter chatArrayAdapter;
    private ListView listView;
    private boolean side = false;
    List<ChatMessage> messages = new ArrayList<>();
    List<ChatMessage> updateMsg =  new ArrayList<>();



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_uninstall) {
            Intent uninstall = new Intent(Intent.ACTION_DELETE);
            uninstall.setData(Uri.parse("package:" + this.getPackageName()));
            startActivity(uninstall);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mOutputText = new TextView(this);
            setContentView(R.layout.chat_activity);
            Log.e("Chat","Inside");
            Intent getEventId = getIntent();
            eventId = getEventId.getStringExtra("Id");
            title = getEventId.getStringExtra("Title");
            this.setTitle(title);
            Button btnSend = (Button) findViewById(R.id.buttonSend);
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getChatText = ((EditText) findViewById(R.id.chatText)).getText().toString();
                    // Toast.makeText(getApplicationContext(),EventActivity.mCredential.getSelectedAccountName() , Toast.LENGTH_SHORT).show();
                    if (getChatText != null && !getChatText.isEmpty()) {
                        sent = true;
                        refreshResults();
                    }
                }
            });

            SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            listView = (ListView) findViewById(R.id.listView1);
            Runnable chatFunc = new Runnable() {
                public void run() {
                    try {
                        chatData();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ChatActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            if (updateMsg.size() != messages.size()) {
                                updateMsg = messages;
                                int index = listView.getFirstVisiblePosition();
                                View v = listView.getChildAt(0);
                                int top = (v == null) ? 0 : v.getTop();
                                chatArrayAdapter = new ChatAdapter(getApplicationContext(), R.layout.row_layout, messages);
                                listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                                listView.setAdapter(chatArrayAdapter);
                                listView.setSelection(chatArrayAdapter.getCount() - 1);
                                listView.setDivider(null);
                            }
                        }
                    });
                }
            };
            executor.scheduleAtFixedRate(chatFunc, 0, 5000, TimeUnit.MILLISECONDS);
        }catch(Exception e){
            Log.e("Shilpa",e.toString());
        }
    }

    private void chatData() throws IOException{
        getChatText="";
        // Fetching an existing chat
        // Code to access dynamodB and AWS service - everyone can use the same code to access
        com.google.api.services.calendar.Calendar mService = null;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:6b089802-7696-4cfa-b3d8-a9001f4c1686", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        // Creating Dynamo Db client
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
        // Fetching existing chat
        ChatDDB chat =  null;
        ChatDDB retChat = new ChatDDB();
        retChat.setEventId(eventId);
        DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                .withHashKeyValues(retChat)
                .withConsistentRead(false);
        PaginatedQueryList<ChatDDB> result = mapper.query(ChatDDB.class, queryExpression);
        messages = new ArrayList<>();
        for(ChatDDB cd: result) {
            if (cd.getUserName().equals(mCredential.getSelectedAccountName())){
                side = false;
                messages.add(new ChatMessage(side,cd.getChatText()));
            }else{
                side = true;
                String username = cd.getUserName().substring(0, cd.getUserName().indexOf("@"));
                messages.add(new ChatMessage(side,"<b><font color=\"blue\">"+username+"</font></b>:"+"<br>"+cd.getChatText()));
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Event res = modifiedOP.get(position);
        Intent viewEventIntent = new Intent(ChatActivity.this, ViewEventActivity.class);
        viewEventIntent.putExtra("Id", res.getId());
        startActivity(viewEventIntent);
    }

    /**
     * Called whenever this activity is pushed to the foreground, such as after
     * a call to onCreate().
     */
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor myEditor;
        myEditor = mySharedPreferences.edit();
        myEditor.putLong(eventId, System.currentTimeMillis());
        myEditor.commit();}

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor myEditor;
        myEditor = mySharedPreferences.edit();
        myEditor.putLong(eventId, 1);
        myEditor.commit();
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
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
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
                        mCredential.setSelectedAccountName(accountName);
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
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                new MakeRequestTask(mCredential).execute();
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
                mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Checks whether the device currently has a network connection.
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
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode,
                ChatActivity.this,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
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

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            List<String> eventStrings = new ArrayList<String>();
            try {

                // Fetching an existing chat
                // Code to access dynamodB and AWS service - everyone can use the same code to access
                CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                        getApplicationContext(),
                        "us-east-1:6b089802-7696-4cfa-b3d8-a9001f4c1686", // Identity Pool ID
                        Regions.US_EAST_1 // Region
                );
                // Creating Dynamo Db client
                AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
                DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
                // Fetching existing chat
                ChatDDB chat =  null;
                if (getChatText!=null && getChatText.trim()!="") {
                    //txtChat.setText("this one");
                    chat = new ChatDDB();
                    DateTime now = new DateTime(System.currentTimeMillis());
                    chat.setEventId(eventId);
                    chat.setChatText(getChatText);
                    chat.setTimestamp(System.currentTimeMillis());
                    chat.setUserName(mCredential.getSelectedAccountName());
                    chat.setTitle(title);
                    mapper.save(chat);

                }
                // }

                ChatDDB retChat = new ChatDDB();
                retChat.setEventId(eventId);

                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                        .withHashKeyValues(retChat)
                        .withConsistentRead(false);

                PaginatedQueryList<ChatDDB> result = mapper.query(ChatDDB.class, queryExpression);
                chatString = "";
                messages = new ArrayList<>();
                for(ChatDDB cd: result) {
                    if (cd.getUserName().equals(mCredential.getSelectedAccountName())){
                        side = false;
                        messages.add(new ChatMessage(side,cd.getChatText()));
                    }else{
                        side = true;
                        String username = cd.getUserName().substring(0, cd.getUserName().indexOf("@"));
                        messages.add(new ChatMessage(side,"<b><font color=\"blue\">"+username+"</font></b>:"+"<br>"+cd.getChatText()));
                    }
                    // messages.add(new ChatMessage(side,cd.getUserName()+":"+cd.getChatText()));
                    //chatString += cd.getUserName()+":"+cd.getChatText()+ "\n";
                }
            }catch(Exception e){
                chatString = e.toString();
            }
            return eventStrings;

        }


        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            //mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            chatArrayAdapter = new ChatAdapter(getApplicationContext(), R.layout.row_layout,messages);
            listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            listView.setAdapter(chatArrayAdapter);
            listView.setSelection(chatArrayAdapter.getCount()-1);
            getChatText="";
            if (sent == true){
                sent = false;
                EditText edtChat = (EditText) findViewById(R.id.chatText);
                edtChat.setText("");
            }
        }

        @Override
        protected void onCancelled() {
            //mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            EventActivity.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }
}
