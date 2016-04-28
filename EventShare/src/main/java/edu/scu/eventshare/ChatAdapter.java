package edu.scu.eventshare;

import android.content.Context;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by shilpa on 03/07/16.
 */
public class ChatAdapter extends ArrayAdapter<ChatMessage> {
    private final List<ChatMessage> eventlist;
    private Context context;

    public ChatAdapter(Context context, int resource, List<ChatMessage> output) {
        super(context, resource, output);
        eventlist = output;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.row_layout, null);
        TextView msg = (TextView) row.findViewById(R.id.singleMessage);
        LinearLayout singleMessageContainer = (LinearLayout) row.findViewById(R.id.singleMessageContainer);

        msg.setText(Html.fromHtml(eventlist.get(position).getMessage()));
        ChatMessage chatMessageObj = getItem(position);
        msg.setBackgroundResource(chatMessageObj.left ? R.drawable.bubble_a : R.drawable.bubble_b);
        singleMessageContainer.setGravity(chatMessageObj.left ? Gravity.LEFT : Gravity.RIGHT);
        return row;

}
}
