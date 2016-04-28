package edu.scu.eventshare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.util.List;

/**
 * Created by Pragati on 02/23/2016.
 */
public class CustomAdaptor extends ArrayAdapter<Event> {
    private final List<Event> eventlist;
    SharedPreferences.Editor editor;
    private Context context;
    private static final String TAG = "CUSTOM ADAPTOR";

    public CustomAdaptor(Context context, int resource, List<Event> output) {
        super(context, resource, output);
        eventlist = output;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View row = inflater.inflate(R.layout.custom_row1, null);

        TextView summary = (TextView) row.findViewById(R.id.summary);
        TextView place = (TextView) row.findViewById(R.id.place);
        TextView startDate = (TextView) row.findViewById(R.id.startDate);
        TextView startTime = (TextView) row.findViewById(R.id.startTime);
        summary.setText(eventlist.get(position).getSummary());
        try {
            ImageView imageView = (ImageView) row.findViewById(R.id.imageView);
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.arrow));

        } catch (Exception e) {
            e.printStackTrace();
        }
        ImageButton imgBtn = (ImageButton)row.findViewById(R.id.imgBtn);
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewEventIntent = new Intent(getContext(), ChatActivity.class);
                viewEventIntent.putExtra("Id", eventlist.get(position).getId());
                viewEventIntent.putExtra("Title", eventlist.get(position).getSummary());
                getContext().startActivity(viewEventIntent);

            }
        });
        place.setText(eventlist.get(position).getLocation());

        DateTime start = ((Event)eventlist.get(position)).getStart().getDateTime();
        // split dateTime To date and time

        try{
            if(start != null){
                String[] startString  = start.toString().split("T");
                String sDate = startString[0];
                String sTime = startString[1];
                sTime=sTime.substring(0, 5);
                startDate.setText(sDate);
                startTime.setText(sTime);

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return row;
    }
}

