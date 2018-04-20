package com.marcuthh.respond;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Arrays;

public class ProfileActivity extends AppCompatActivity {

    //region ////CONSTANTS////
    private static String TAG = "ProfileActivity";

    private ImageView profile_image;
    private TextView profile_displayName;
    private TextView profile_status;
    private Button profile_msg_btn;

    private static String CHILD_USERS = "users";
    private static String CHILD_CHATS = "chats";
    private DatabaseReference mDbRefUser;
    private FirebaseUser mCurrentUser;

    //the whole record of the user being viewed by the logged-in user
    private User profileUser;
    //the firebase id of the profile
    private String profileUid;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //find controls
        profile_image = (ImageView) findViewById(R.id.profile_image);
        profile_displayName = (TextView) findViewById(R.id.profile_displayName);
        profile_status = (TextView) findViewById(R.id.profile_status);
        profile_msg_btn = (Button) findViewById(R.id.profile_msg_btn);

        mDbRefUser = FirebaseDatabase.getInstance().getReference(CHILD_USERS);
        mDbRefUser.keepSynced(true);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();

        profileUid = getIntent().getStringExtra("USER_ID");

        profile_msg_btn.setOnClickListener(onChatButtonClick());

        if (profileUid != null && !profileUid.equals("")) {
            mDbRefUser.child(profileUid).addValueEventListener(onUserDataChange());
        } else {
            Log.d(TAG, "error: no user id found");
        }
    }

    private View.OnClickListener onChatButtonClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DatabaseReference dbRefChats =
                        FirebaseDatabase.getInstance().getReference(CHILD_CHATS);
                dbRefChats.keepSynced(true);
                dbRefChats.addValueEventListener(onChatDataChange(dbRefChats));
            }
        };
    }

    private ValueEventListener onChatDataChange(final DatabaseReference ref) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //the keys that will be looked for in chat records
                //ids of the viewer and view-ee
                String chatKey = getChatKeyForUsers(
                        dataSnapshot,
                        new String[]{
                                profileUid,
                                mCurrentUser.getUid()
                        }
                );

                //intent for chat window between users
                Intent chatIntent = new Intent(
                        ProfileActivity.this,
                        ChatActivity.class
                );
                if (!chatKey.equals("")) {
                    //chat found that contains only the two keys
                    chatIntent.putExtra("CHAT_ID", chatKey);
                } else {
                    //no chat found between the two users
                    chatIntent.putExtra("RECIPIENT_ID", profileUid);
                    //push key for new chat - save another execution of the query in chat activity
                    chatIntent.putExtra("NEW_CHAT_ID", ref.push().getKey());
                }
                //open either existing or new chat
                startActivity(chatIntent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "error: " + databaseError.getMessage());
                //...
            }
        };
    }

    private String getChatKeyForUsers(DataSnapshot dataSnapshot, String[] keys) {
        ArrayList<String> searchKeys =
                new ArrayList<String>(Arrays.asList(keys));

        //cleared on each outer loop, holds the keys of all users in each chat
        ArrayList<String> sponderKeys = new ArrayList<String>();

        for (DataSnapshot chat : dataSnapshot.getChildren()) {
            DataSnapshot chatMembersRef = chat.child("sponders");
            if (isIndividualChat(chatMembersRef.getChildrenCount())) {
                //loop through contacts in chat record
                for (DataSnapshot chatSponder : chatMembersRef.getChildren()) {
                    sponderKeys.add(chatSponder.getKey());
                }
            }

            if (sponderKeys.containsAll(searchKeys)) {
                //key is found for chat between logged in user and profile user
                return chat.getKey();
            } else {
                sponderKeys.clear();
            }
        }

        //blank string indicating no chat history exists
        return "";
    }

    private boolean isIndividualChat(long childrenCount) {
        return childrenCount == 2;
    }

    private ValueEventListener onUserDataChange() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    profileUser = dataSnapshot.getValue(User.class);
                    if (profileUser != null) {
                        updateProfileDisplay(profileUser);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "error: " + databaseError.getMessage());
            }
        };
    }

    private void updateProfileDisplay(User accountData) {
        String displayName = accountData.getDisplayName();
        String fullName = buildAccountName(accountData.getFirstName(), accountData.getSurname());

        //if all information components available...
        if (displayName != null && !displayName.equals("")) {
            String formattedNameStr = displayName;
            //show display name first and full name in brackets
            if (fullName != null && !fullName.equals("")) {
                formattedNameStr += " (" + fullName + ")";
            }

            profile_displayName.setText(formattedNameStr);
            //just put username on prompt button
            String btnText = "Send " + displayName + " a message";
            profile_msg_btn.setText(btnText);
        } else {
            if (fullName != null && !fullName.equals("")) {
                profile_displayName.setText(fullName);
            } //otherwise will default to "HeadCount User"
        }

        String status = accountData.getStatus();
        if (status != null && !status.equals("")) {
            profile_status.setText(accountData.getStatus());
        } //otherwise will use default status

        String photoName = accountData.getAccountPhotoName();
        if (photoName != null && !photoName.equals("")) {
            //calculate the path to user's profile picture
            FirebaseStorage.getInstance().getReference(
                    accountData.buildAccountPhotoNodeFilter(
                            profileUid,
                            true
                    )
            ).getDownloadUrl()
                    .addOnCompleteListener(imageLoadedListener());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    private OnCompleteListener<Uri> imageLoadedListener() {
        return new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Glide.with(getApplicationContext())
                            .load(task.getResult())
                            .placeholder(R.drawable.no_account_photo)
                            .into(profile_image);
                } else {
                    Log.d(TAG, "Unable to fetch photo at location");
                }
            }
        };
    }

    private String buildAccountName(String firstName, String surname) {
        if (!(firstName.equals("") && surname.equals(""))) {
            //at least one of the name fields has a value
            String nameStr = "";

            if (!firstName.equals("")) {
                //set first name
                nameStr += firstName;
            }
            if (!surname.equals("")) {
                if (!nameStr.equals("")) {
                    //first name already in string, append space
                    nameStr += " ";
                }
                //append surname to existing string
                nameStr += surname;
            }
            //return concatenated string
            return nameStr;
        } else {
            return "";
        }
    }
}