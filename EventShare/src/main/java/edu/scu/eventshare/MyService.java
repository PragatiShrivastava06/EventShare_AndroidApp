package edu.scu.eventshare;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by shilpa on 03/05/16.
 */
public class MyService extends Service  {
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        /*final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            new YourAsyncTask().execute();
                        } catch (Exception e) {

                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 10000);*/
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Runnable chatFunc = new Runnable() {
            public void run() {

                try {
                    final String chatString = chatData();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        executor.scheduleAtFixedRate(chatFunc , 0, 10000, TimeUnit.MILLISECONDS );
    }

    private String chatData() throws IOException{
        String chatString = "";
        Log.e("Shilpa", "hi");
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:6b089802-7696-4cfa-b3d8-a9001f4c1686", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        try {
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Gson gson = new Gson();
            String json = mySharedPreferences.getString("EventList", "");
            Type type = new TypeToken<List<String>>() {
            }.getType();
            List<String> events = gson.fromJson(json, type);
            if (events!=null) {
                for (String item : events) {
                    ChatDDB retChat = new ChatDDB();
                    retChat.setEventId(item);
                    SharedPreferences mySharedPreferences1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    long time = mySharedPreferences1.getLong(item.toString(), 0);
                    if (time != 1) {
                        Condition rangeKeyCondition = new Condition().withComparisonOperator(ComparisonOperator.GT)
                                .withAttributeValueList(new AttributeValue().withN(Long.toString(time)));

                        DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                                .withHashKeyValues(retChat)
                                .withConsistentRead(false).withRangeKeyCondition("timestamp", rangeKeyCondition);

                        PaginatedQueryList<ChatDDB> result = mapper.query(ChatDDB.class, queryExpression);
                        for (ChatDDB cd : result) {
                            chatString += cd.getEventId() + " " + cd.getTimestamp() + " " + cd.getTitle() + "\n";
                            buildNotification(cd.getEventId(), cd.getTitle());
                            Log.e("Shilpa", "notify");
                        }
                    }
                    Log.e("Shilpa", item);
                }
            }
            Log.e("Shilpa",  chatString);
        }catch (Exception e){
            Log.e("Shilpa","Error!");
            Log.e("Shilpa",e.toString());
        }
        return "Success";
    }

   /* public class YourAsyncTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            // your load work
            String chatString = "";
            Log.e("Shilpa", "hi");
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),
                    "us-east-1:6b089802-7696-4cfa-b3d8-a9001f4c1686", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );
            try {
                AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
                DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

                SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                Gson gson = new Gson();
                String json = mySharedPreferences.getString("EventList", "");
                Type type = new TypeToken<List<String>>() {
                }.getType();
                List<String> events = gson.fromJson(json, type);
                for (String item : events) {
                    ChatDDB retChat = new ChatDDB();
                    retChat.setEventId(item);
                    SharedPreferences mySharedPreferences1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    long time = mySharedPreferences1.getLong(item.toString(), 0);
                    if (time!=1) {
                        Condition rangeKeyCondition = new Condition().withComparisonOperator(ComparisonOperator.GT)
                                .withAttributeValueList(new AttributeValue().withN(Long.toString(time)));

                        DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                                .withHashKeyValues(retChat)
                                .withConsistentRead(false).withRangeKeyCondition("timestamp", rangeKeyCondition);

                        PaginatedQueryList<ChatDDB> result = mapper.query(ChatDDB.class, queryExpression);
                        for (ChatDDB cd : result) {
                            chatString += cd.getEventId() + " " + cd.getTimestamp()+" "+cd.getTitle() + "\n";
                            buildNotification(cd.getEventId(),cd.getTitle());
                        }
                    }
                    Log.e("Shilpa", item);
                }
                Log.e("Shilpa",  chatString);
            }catch (Exception e){
                Log.e("Shilpa","Error!");
                Log.e("Shilpa",e.toString());
            }
            return "Success";
        }

        @Override
        protected void onPostExecute(String result) {

        }*/

        public void buildNotification(String itemId, String title){
            Intent resultIntent = new Intent(getApplicationContext(), ChatActivity.class);
            if (itemId!=null&&title!=null) {
                resultIntent.putExtra("Id", itemId);
                resultIntent.putExtra("Title", title);
                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(getApplicationContext(), 1234, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getApplicationContext())
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("EventShare")
                                .setContentText("You have a new message in " + title + "!")
                                .setContentIntent(resultPendingIntent)
                                .setAutoCancel(true);
                NotificationManager mNotificationManager =
                        (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                // mId allows you to update the notification later on.
                mNotificationManager.notify(1111, mBuilder.build());
            }
        }

    }



