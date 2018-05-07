package com.marcuthh.respond;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class EventDetailsActivity extends AppCompatActivity {

    //region ////CONSTANTS////
    private static final String TAG = "EventDetailsActivity";

    private static final String LOC_USERS = "users";
    private static final String LOC_EVENTS = "events";
    private static final String LOC_INVITES = "eventInvites";

    //Firebase components
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDbRefRoot;
    private DatabaseReference mDbRefUsers;
    private DatabaseReference mDbRefEvents;
    //file transfer to database
    private StorageReference mStorageRef;
    //interfaces
    private FirebaseAuth.AuthStateListener mAuthListener;
    //Firebase components

    private String mCurrentUserId;
    private String mEventId;

    ImageView event_image;
    TextView event_title;
    TextView event_loc;
    TextView event_date;
    View event_attnd_layout;
    Switch swch_event_attending;
    TextView txt_event_not_attending;
    TextView txt_event_attending;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
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

        mDbRefRoot = mDatabase.getReference();
        mDbRefRoot.keepSynced(true);
        mDbRefUsers = mDatabase.getReference(LOC_USERS);
        mDbRefUsers.keepSynced(true);
        mDbRefEvents = mDatabase.getReference(LOC_EVENTS);
        mDbRefEvents.keepSynced(true);

        event_image = (ImageView) findViewById(R.id.event_image);
        event_title = (TextView) findViewById(R.id.event_title);
        event_loc = (TextView) findViewById(R.id.event_loc);
        event_date = (TextView) findViewById(R.id.event_date);
        event_attnd_layout = findViewById(R.id.event_attnd_layout);
        swch_event_attending = (Switch) findViewById(R.id.swch_event_attending);
        txt_event_not_attending = (TextView) findViewById(R.id.txt_event_not_attending);
        txt_event_attending = (TextView) findViewById(R.id.txt_event_attending);

        updateDisplay(mEventId);
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

    private void updateDisplay(String eventId) {
        mDbRefEvents.child(eventId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String eventTitle = "";
                String eventLoc = "";
                long eventTimestamp = 0;
                String eventDesc = "";
                String eventImage = "";

                if (dataSnapshot.child("eventTitle").getValue() != null) {
                    eventTitle = dataSnapshot.child("eventTitle").getValue().toString();
                }
                if (dataSnapshot.child("eventLocation").getValue() != null) {
                    eventLoc = dataSnapshot.child("eventLocation").getValue().toString();
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

                event_title.setText(eventTitle);
                event_loc.setText(eventLoc);
                event_date.setText(formattedTimestamp(eventTimestamp));

                mStorageRef.child("images/events/" + mEventId + "/" + eventImage)
                        .getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Glide.with(getApplicationContext())
                                    .load(task.getResult())
                                    .placeholder(R.drawable.event_default)
                                    .into(event_image);
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
                } else {
                    //do not allow response from someone with no invite
                    event_attnd_layout.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "unable to fetch event data");
            }
        });
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

    private FirebaseAuth.AuthStateListener getAuthListener() {
        return new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //...
                } else {
                    //...
                }
            }
        };
    }
}
