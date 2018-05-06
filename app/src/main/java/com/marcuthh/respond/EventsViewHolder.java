package com.marcuthh.respond;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Calendar;
import java.util.Locale;

public class MessagesViewHolder extends RecyclerView.ViewHolder {

    private View view;

    public MessagesViewHolder(View itemView) {
        super(itemView);

        view = itemView;
    }

    //ALL MESSAGES//
    public void setDisplayName(String displayName) {
        if (displayName != null && !displayName.equals("")) {
            TextView message_user = (TextView) view.findViewById(R.id.message_user);
            message_user.setText(displayName);
        }
    }

    public void setMessageTime(long timestamp) {
        TextView message_time = (TextView) view.findViewById(R.id.message_time);
        message_time.setText(formattedTimestamp(timestamp));
    }

    public void setMessageText(String text) {
        if (text != null && !text.equals("")) {
            TextView message_text = (TextView) view.findViewById(R.id.message_text);
            message_text.setText(text);
        }
    }

    //boolean param indicates whether message was sent by this user or a chat partner
    public void setMessageDisplay(boolean isUserMessage) {
        RelativeLayout message_layout = (RelativeLayout) view.findViewById(R.id.message_layout);
        TextView message_text = (TextView) view.findViewById(R.id.message_text);

        if (!isUserMessage) {
            message_layout.setBackgroundResource(R.drawable.background_border_bottom_green);
            message_text.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            message_text.setPadding(0, 0, 50, 0);
        } else {
            message_layout.setBackgroundResource(R.drawable.background_border_bottom_grey);
            message_text.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            message_text.setPadding(50, 0, 0, 0);
        }
    }
    //ALL MESSAGES//

    //PHOTO MESSAGE//
    public void setPhotoVisible(int visibility) {
        View content_photo_message = view.findViewById(R.id.content_photo_message);

        content_photo_message.setVisibility(visibility);
    }

    public void setMessageImage(Context cntxt, Uri fileUri) {
        ImageView message_image = (ImageView) view.findViewById(R.id.message_image);
        Glide.with(cntxt)
                .load(fileUri)
                .placeholder(R.drawable.no_account_photo)
                .into(message_image);
    }
    //PHOTO MESSAGE//

    //EVENT MESSAGE//
    public void setMessageEventImage(Context cntxt, Uri fileUri) {
        ImageView event_image = (ImageView) view.findViewById(R.id.event_image);
        Glide.with(cntxt)
                .load(fileUri)
                .placeholder(R.drawable.no_account_photo)
                .into(event_image);
    }

    public void setEventVisible(int visibility) {
        View content_event_stub = view.findViewById(R.id.content_event_stub);

        content_event_stub.setVisibility(visibility);
    }

    public void setEventTitle(String title) {
        if (title != null && !title.equals("")) {
            TextView event_title = (TextView) view.findViewById(R.id.event_title);
            event_title.setText(title);
        }
    }

    public void setEventDate(long timestamp) {
        TextView event_date = (TextView) view.findViewById(R.id.event_date);
        event_date.setText(formattedTimestamp(timestamp));
    }

    public void setEventAttending(boolean attending) {
        Switch swch_event_attending = (Switch) view.findViewById(R.id.swch_event_attending);
        TextView txt_event_not_attending = (TextView) view.findViewById(R.id.txt_event_not_attending);
        TextView txt_event_attending = (TextView) view.findViewById(R.id.txt_event_attending);

        if (attending) {
            swch_event_attending.setChecked(true);
            txt_event_not_attending.setVisibility(View.INVISIBLE);
            txt_event_attending.setVisibility(View.VISIBLE);
        } else {
            swch_event_attending.setChecked(false);
            txt_event_not_attending.setVisibility(View.VISIBLE);
            txt_event_attending.setVisibility(View.INVISIBLE);
        }
    }
    //EVENT MESSAGE//

    public View getView() {
        return view;
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
