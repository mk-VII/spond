package com.marcuthh.respond;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class SpondersViewHolder extends RecyclerView.ViewHolder {

    private View view;

    public SpondersViewHolder(View itemView) {
        super(itemView);

        view = itemView;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public void setDisplayName(String displayName) {
        if (displayName != null && !displayName.equals("")) {
            TextView sponder_displayName = (TextView) view.findViewById(R.id.sponder_displayName);
            sponder_displayName.setText(displayName);
        }
    }

    public void setStatus(String status) {
        if (status != null && !status.equals("")) {
            TextView sponder_status = (TextView) view.findViewById(R.id.sponder_status);
            sponder_status.setText(status);
        }
    }

    public void setProfileImage(Context context, Uri fileUri) {
        ImageView sponder_image = (ImageView) view.findViewById(R.id.sponder_image);
        Glide.with(context)
                .load(fileUri)
                .placeholder(R.drawable.no_account_photo)
                .into(sponder_image);
    }
}
