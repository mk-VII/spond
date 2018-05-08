package com.marcuthh.respond;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Calendar;
import java.util.Locale;

public class EventsViewHolder extends RecyclerView.ViewHolder {

    private View view;

    public EventsViewHolder(View itemView) {
        super(itemView);

        view = itemView;
    }

    public void setEventImage(Context cntxt, Uri fileUri) {
        ImageView event_image = (ImageView) view.findViewById(R.id.event_image);
        Glide.with(cntxt)
                .load(fileUri)
                .placeholder(R.drawable.event_default)
                .into(event_image);
    }

    public void setEventTitle(String title) {
        if (title != null && !title.equals("")) {
            TextView event_title = (TextView) view.findViewById(R.id.event_title);
            event_title.setText(title);
        }
    }

    public void setInvitedBy(boolean isAdmin, String username) {
        TextView event_invited_by = (TextView) view.findViewById(R.id.event_invited_by);

        if (!isAdmin) {
            if (username != null && !username.equals("")) {
                String display = "Invited by " + username;
                event_invited_by.setText(display);
            } else {
                event_invited_by.setVisibility(View.INVISIBLE);
            }
        } else {
            event_invited_by.setText(R.string.event_admin_self);
        }
    }

    public void setNumAttending(long numInvited, int numAttending) {
        TextView event_num_attnd = (TextView) view.findViewById(R.id.event_num_attnd);

        String display = numAttending + "/" + numInvited + " attending";
        event_num_attnd.setText(display);
    }

    public void setEventAttending(boolean attending) {
        Switch swch_event_attending = (Switch) view.findViewById(R.id.swch_event_attending);
        TextView txt_event_attending = (TextView) view.findViewById(R.id.txt_event_attending);

        if (attending) {
            swch_event_attending.setChecked(true);
            txt_event_attending.setText(R.string.event_attending);
        } else {
            swch_event_attending.setChecked(false);
            txt_event_attending.setText(R.string.event_not_attending);
        }
    }

    public void setAttendingListener(CompoundButton.OnCheckedChangeListener listener) {
        ((Switch) view.findViewById(R.id.swch_event_attending)).setOnCheckedChangeListener(listener);
    }

    public View getView() {
        return view;
    }

    public void setViewOnClickListener(View.OnClickListener listener) {
        view.setOnClickListener(listener);
    }

    public void setView(View view) {
        this.view = view;
    }

    private String formattedTimestamp(long timestamp) {
        Calendar now = Calendar.getInstance();
        Calendar time = Calendar.getInstance(Locale.ENGLISH);
        time.setTimeInMillis(timestamp);

        if (now.get(Calendar.DATE) == time.get(Calendar.DATE))
            return android.text.format.DateFormat.format("hh:mm", time).toString();
        else {
            return android.text.format.DateFormat.format("dd-MM-yyyy hh:mm", time).toString();
        }
    }
}
