package com.marcuthh.respond;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class ChatsViewHolder extends RecyclerView.ViewHolder {

    private View view;

    public ChatsViewHolder(View itemView) {
        super(itemView);

        view = itemView;
    }

    public String getChatLabel() {
        TextView chat_label = (TextView) view.findViewById(R.id.chat_label);
        if (chat_label.getText() == null) {
            chat_label.setText("");
        }

        return chat_label.getText().toString();
    }

    public void setChatLabel(String label) {
        if (label != null && !label.equals("")) {
            TextView chat_label = (TextView) view.findViewById(R.id.chat_label);
            chat_label.setText(label);
        }
    }

    public void setContactedBy(String contactedBy) {
        if (contactedBy != null && !contactedBy.equals("")) {
            TextView chat_last_contacted_by = (TextView) view.findViewById(R.id.chat_last_contacted_by);
            chat_last_contacted_by.setText(contactedBy);
        }
    }

    public void setContactedTime(String contactedTime) {
        if (contactedTime != null && !contactedTime.equals("")) {
            TextView chat_last_contacted_time = (TextView) view.findViewById(R.id.chat_last_contacted_time);
            chat_last_contacted_time.setText(contactedTime);
        }
    }

    public void setChatImage(Context context, Uri fileUri) {
        ImageView chat_image = (ImageView) view.findViewById(R.id.chat_image);
        Glide.with(context)
                .load(fileUri)
                .placeholder(R.drawable.no_account_photo)
                .into(chat_image);
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }
}
