package com.marcuthh.respond;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class UsersViewHolder extends RecyclerView.ViewHolder {

    private View view;

    public UsersViewHolder(View itemView) {
        super(itemView);

        view = itemView;
    }

    public void setDisplayName(String displayName) {
        if (displayName != null && !displayName.equals("")) {
            TextView user_displayName = (TextView) view.findViewById(R.id.user_displayName);
            user_displayName.setText(displayName);
        }
    }

    public void setStatus(String status) {
        if (status != null && !status.equals("")) {
            TextView user_status = (TextView) view.findViewById(R.id.user_status);
            user_status.setText(status);
        }
    }

    public void setProfileImage(Context context, Uri fileUri) {
        ImageView user_image = (ImageView) view.findViewById(R.id.user_image);
        Glide.with(context)
                .load(fileUri)
                .placeholder(R.drawable.no_account_photo)
                .into(user_image);
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }
}
