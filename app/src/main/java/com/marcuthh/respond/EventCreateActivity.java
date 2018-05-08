package com.marcuthh.respond;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EventCreateActivity extends AppCompatActivity {

    //region ////CONSTANTS////
    private static final String TAG = "EventCreateActivity";

    private static final String LOC_USERS = "users";
    private static final String LOC_EVENTS = "events";
    private static final String LOC_CHATS = "chats";
    private static final String LOC_MESSAGES = "messages";

    private static final String LOC_INVITES = "eventInvites";

    //Firebase components
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDbRefRoot;
    private DatabaseReference mDbRefUsers;
    private DatabaseReference mDbRefEvents;
    private DatabaseReference mDbRefChats;
    private DatabaseReference mDbRefMessages;
    //file transfer to database
    private StorageReference mStorageRef;
    //interfaces
    private FirebaseAuth.AuthStateListener mAuthListener;
    //Firebase components

    private String mCurrentUserId;
    private String mEventId;
    private String mParentChatId;

    ImageView img_event;
    Button btn_clearPhoto;
    Button btn_editPhoto;
    TextInputEditText etxt_eventTitle;
    TextInputEditText etxt_eventDateTime;
    TextInputEditText etxt_eventLoc;
    TextInputEditText etxt_eventDesc;
    Switch swch_event_attending;
    TextView txt_event_not_attending;
    TextView txt_event_attending;
    Button btn_save;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_create);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //get Firebase connections
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //set up reference to Firebase file transfer
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //setup auth processes and callback behaviour
        mAuthListener = getAuthListener();

        mEventId = getIntent().getStringExtra("EVENT_ID");
        mParentChatId = getIntent().getStringExtra("PARENT_CHAT_ID");

        mDbRefRoot = mDatabase.getReference();
//        mDbRefRoot.keepSynced(true);
        mDbRefUsers = mDatabase.getReference(LOC_USERS);
//        mDbRefUsers.keepSynced(true);
        mDbRefEvents = mDatabase.getReference(LOC_EVENTS);
//        mDbRefEvents.keepSynced(true);
        mDbRefChats = mDatabase.getReference(LOC_CHATS);
//        mDbRefChats.keepSynced(true);
        mDbRefMessages = mDatabase.getReference(LOC_MESSAGES);
//        mDbRefMessages.keepSynced(true);

        img_event = (ImageView) findViewById(R.id.img_event);
        btn_clearPhoto = (Button) findViewById(R.id.btn_clearPhoto);
        btn_editPhoto = (Button) findViewById(R.id.btn_editPhoto);
        etxt_eventTitle = (TextInputEditText) findViewById(R.id.etxt_eventTitle);
        etxt_eventDateTime = (TextInputEditText) findViewById(R.id.etxt_eventDateTime);
        etxt_eventLoc = (TextInputEditText) findViewById(R.id.etxt_eventLoc);
        etxt_eventDesc = (TextInputEditText) findViewById(R.id.etxt_eventDesc);
        swch_event_attending = (Switch) findViewById(R.id.swch_event_attending);
        btn_save = (Button) findViewById(R.id.btn_save);

        btn_clearPhoto.setOnClickListener(removeImageListener());
        btn_editPhoto.setOnClickListener(selectImageFromCameraOrGallery());
        boolean isExistingEvent = (mEventId != null && !mEventId.equals(""));
        btn_save.setOnClickListener(eventSaveListener(!isExistingEvent));

        if (isExistingEvent) {
            btn_save.setText(R.string.event_save);
            loadEventData(mEventId);
        }
    }

    private CompoundButton.OnCheckedChangeListener attendanceListener(final long sentTimestamp) {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int response;
                if (b) {
                    response = EventInvite.ATTENDING;
                } else {
                    response = EventInvite.NOT_ATTENDING;
                }

                //data to be stored on event response
                HashMap<String, Object> mapResponse = new HashMap<String, Object>();
                mapResponse.put("status", response);
                mapResponse.put("sentTimestamp", sentTimestamp);
                mapResponse.put("responseTimestamp", ServerValue.TIMESTAMP);

                //locations at which data is stored, relating to user and event
                HashMap<String, Object> mapInvite = new HashMap<String, Object>();
                String inviteLocEvent = LOC_EVENTS + "/" + mEventId + "/" + LOC_INVITES + "/" + mCurrentUserId;
                String inviteLocUser = LOC_USERS + "/" + mCurrentUserId + "/" + LOC_INVITES + "/" + mEventId;
                mapInvite.put(inviteLocEvent, mapResponse);
                mapInvite.put(inviteLocUser, mapResponse);

                //must use database root to reach both event and user nodes
                DatabaseReference refRoot = FirebaseDatabase.getInstance().getReference();

                //write changes to database
                refRoot.updateChildren(mapInvite, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.d(TAG, "error responding to event: " + databaseError.getMessage());
                        } else {
                            Snackbar.make(
                                    findViewById(R.id.event_layout),
                                    "Your status for this event has been updated",
                                    Snackbar.LENGTH_LONG
                            ).show();
                        }
                    }
                });
            }
        };
    }

    private View.OnClickListener selectImageFromCameraOrGallery() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        };
    }

    private View.OnClickListener removeImageListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        };
    }

    private View.OnClickListener eventSaveListener(final boolean isNewEvent) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String eventTitle = etxt_eventTitle.getText().toString();
                String eventLoc = etxt_eventLoc.getText().toString();
                String eventDesc = etxt_eventDesc.getText().toString();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                Calendar cal = Calendar.getInstance();
                long eventTimestamp = 0;
                try {
                    cal.setTime(sdf.parse(etxt_eventDateTime.getText().toString()));
                    eventTimestamp = cal.getTimeInMillis();
                } catch (ParseException e) {
                    Log.d(TAG, "invalid date format: " + e.getLocalizedMessage());
                }
                final Map<String, String> eventCreated = ServerValue.TIMESTAMP;

                HashMap<String, Object> mapEvent = new HashMap<String, Object>();
                mapEvent.put("eventTitle", etxt_eventTitle.getText().toString());
                mapEvent.put("eventLoc", etxt_eventLoc.getText().toString());
                mapEvent.put("eventDesc", etxt_eventDesc.getText().toString());
                mapEvent.put("eventTimestamp", eventTimestamp);
                if (isNewEvent) {
                    //won't ever change after initial save
                    mapEvent.put("eventAdmin", mCurrentUserId);
                    mapEvent.put("eventCreated", eventCreated);

                    mEventId = mDbRefEvents.push().getKey();
                }

                mDbRefEvents.child(mEventId).updateChildren(mapEvent, onEventCreatedListener(eventCreated));
            }
        };
    }

    private DatabaseReference.CompletionListener onEventCreatedListener(final Map<String, String> eventCreated) {
        return new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.d(TAG, "unable to create event: " + databaseError.getMessage());
                } else {
                    //event successfully created
                    //create invite records
                    mDbRefChats.child(mParentChatId).addValueEventListener(createEventInvites(eventCreated));
                }
            }
        };
    }

    private ValueEventListener createEventInvites(final Map<String, String> eventCreated) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot chatSnapshot) {
                if (chatSnapshot.child("members").hasChildren()) {

                    //default invite values that will be stored on event and user collections
                    final HashMap<String, Object> mapInviteDefault = new HashMap<String, Object>();
                    mapInviteDefault.put("sentTimestamp", eventCreated);

                    for (DataSnapshot member : chatSnapshot.child("members").getChildren()) {
                        String userKey = member.getKey();

                        if (userKey.equals(mCurrentUserId)) {
                            //admin user has already selected their response
                            if (swch_event_attending.isChecked()) {
                                mapInviteDefault.put("status", 1);
                            } else {
                                mapInviteDefault.put("status", -1);
                            }
                        } else {
                            //defaults to 0 for all other users
                            mapInviteDefault.put("status", 0);
                        }

                        String inviteLocEvent = LOC_EVENTS + "/" + mEventId + "/" + LOC_INVITES + "/" + userKey;
                        String inviteLocUser = LOC_USERS + "/" + userKey + "/" + LOC_INVITES + "/" + mEventId;

                        HashMap<String, Object> mapInvite = new HashMap<String, Object>();
                        mapInvite.put(inviteLocEvent, mapInviteDefault);
                        mapInvite.put(inviteLocUser, mapInviteDefault);

                        mDbRefRoot.updateChildren(mapInvite, invitesCreatedListener());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "unable to send event invites");
            }
        };
    }

    private DatabaseReference.CompletionListener invitesCreatedListener() {
        return new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.d(TAG, "unable to create invites: " + databaseError.getMessage());

                } else {
                    String messageKey = mDbRefMessages.push().getKey();

                    HashMap<String, Object> messageMap = new HashMap<String, Object>();
                    messageMap.put("messageText", "");
                    messageMap.put("messageTimestamp", ServerValue.TIMESTAMP);
                    messageMap.put("messageSender", mCurrentUserId);
                    messageMap.put("messageChat", mParentChatId);
                    messageMap.put("eventKey", mEventId);

                    HashMap<String, Object> messageChatMap = new HashMap<String, Object>();
                    messageChatMap.put(messageKey, messageMap);

                    mDbRefMessages.updateChildren(messageChatMap, messagesSentListener());
                }
            }
        };
    }

    private DatabaseReference.CompletionListener messagesSentListener() {
        return new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.d(TAG, databaseError.getMessage());
                    Toast.makeText(
                            getApplicationContext(),
                            "Error sending message!",
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    mDbRefChats.child(mParentChatId).addListenerForSingleValueEvent(updateChatStatus());
                }
            }
        };
    }

    private ValueEventListener updateChatStatus() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot chatSnapshot) {
                if (chatSnapshot.child("members").hasChildren()) {

                    HashMap<String, Object> statusMap = new HashMap<String, Object>();
                    statusMap.put("timestamp", ServerValue.TIMESTAMP);

                    //loop through all users in the chat
                    for (DataSnapshot member : chatSnapshot.child("members").getChildren()) {
                        //all will be flagged as having not seen the chat
                        //apart from the sender
                        if (member.getKey().equals(mCurrentUserId)) {
                            statusMap.put("seen", true);
                        } else {
                            statusMap.put("seen", false);
                        }

                        String chatUserRef = LOC_CHATS + "/" + mParentChatId + "/members/" + member.getKey();
                        String userChatRef = LOC_USERS + "/" + member.getKey() + "/member/" + mParentChatId;

                        HashMap<String, Object> chatStatusMap = new HashMap<String, Object>();
                        chatStatusMap.put(chatUserRef, statusMap);
                        chatStatusMap.put(userChatRef, statusMap);

                        mDbRefRoot.updateChildren(chatStatusMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Log.d(TAG, "error updating chat: " + databaseError.getMessage());
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error updating chat record: " + databaseError.getMessage());
            }
        };
    }

    private void loadEventData(String eventKey) {
        mDbRefEvents.child(eventKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    String eventTitle = "";
                    String eventLoc = "";
                    String eventDesc = "";
                    long eventTimestamp = 0;
                    String eventImage = "";

                    if (dataSnapshot.child("eventTitle").getValue() != null) {
                        eventTitle = dataSnapshot.child("eventTitle").getValue().toString();
                    }
                    if (dataSnapshot.child("eventLocation").getValue() != null) {
                        eventLoc = dataSnapshot.child("eventLocation").getValue().toString();
                    }
                    if (dataSnapshot.child("eventDesc").getValue() != null) {
                        eventDesc = dataSnapshot.child("eventDesc").getValue().toString();
                    }
                    if (dataSnapshot.child("eventTimestamp").getValue() != null) {
                        eventTimestamp = Long.parseLong(dataSnapshot.child("eventTimestamp").getValue().toString());
                    }
                    if (dataSnapshot.child("eventDesc").getValue() != null) {
                        eventDesc = dataSnapshot.child("eventDesc").getValue().toString();
                    }
                    if (dataSnapshot.child("eventImage").getValue() != null) {
                        eventImage = dataSnapshot.child("eventImage").getValue().toString();
                    }

                    //set text fields
                    etxt_eventTitle.setText(eventTitle);
                    etxt_eventDateTime.setText(formattedTimestamp(eventTimestamp));
                    etxt_eventLoc.setText(eventLoc);
                    etxt_eventDesc.setText(eventDesc);

                    //load event image from file storage
                    mStorageRef.child("images/events/" + mEventId + "/" + eventImage)
                            .getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Glide.with(getApplicationContext())
                                        .load(task.getResult())
                                        .placeholder(R.drawable.event_default)
                                        .into(img_event);
                            } else {
                                Log.d(TAG, "unable to fetch event photo");
                            }
                        }
                    });

                    if (dataSnapshot.child(LOC_INVITES).hasChild(mCurrentUserId)) {
                        int status = Integer.parseInt(dataSnapshot
                                .child(LOC_INVITES)
                                .child(mCurrentUserId)
                                .child("status")
                                .getValue().toString()
                        );

                        long sentTimestamp = Long.parseLong(dataSnapshot
                                .child(LOC_INVITES)
                                .child(mCurrentUserId)
                                .child("sentTimestamp")
                                .getValue().toString()
                        );

                        if (status == EventInvite.ATTENDING) {
                            swch_event_attending.setChecked(true);
                            txt_event_not_attending.setVisibility(View.INVISIBLE);
                            txt_event_attending.setVisibility(View.VISIBLE);
                        } else {
                            swch_event_attending.setChecked(false);
                            txt_event_not_attending.setVisibility(View.VISIBLE);
                            txt_event_attending.setVisibility(View.INVISIBLE);
                        }
                        //apply listener for any subsequent clicks

                        swch_event_attending.setOnCheckedChangeListener(attendanceListener(sentTimestamp));
                    } else { //precautionary measure - should never happen
                        //no invite to this user
                        swch_event_attending.setChecked(false);
                        txt_event_not_attending.setVisibility(View.VISIBLE);
                        txt_event_attending.setVisibility(View.INVISIBLE);

                        //default to now as invite time
                        swch_event_attending.setOnCheckedChangeListener(attendanceListener(Calendar.getInstance().getTimeInMillis()));
                    }
                }
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

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "action cancelled: " + databaseError.getMessage());
            }
        });

    }

    private FirebaseAuth.AuthStateListener getAuthListener() {
        return null;
    }
}
