package com.marcuthh.respond;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class SpondersViewHolder extends RecyclerView.ViewHolder {

    View mView;

    public SpondersViewHolder(View itemView) {
        super(itemView);

        mView = itemView;
    }

    public void setDisplayName(String displayName) {
        TextView sponder_displayName = (TextView) mView.findViewById(R.id.sponder_displayName);
        sponder_displayName.setText(displayName);
    }

    public void setStatus(String status) {
        TextView sponder_status = (TextView) mView.findViewById(R.id.sponder_status);
        sponder_status.setText(status);
    }

    public void setProfileImage(Uri fileUri) {

    }
}
