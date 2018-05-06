package com.marcuthh.respond;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class UsersTestActivity extends AppCompatActivity {

    private static String TAG = "UserActivity";

    private final String LOC_USERS = "users";
    private final String LOC_EVENTS = "events";
    private final String LOC_INVITES = "eventInvites";

    FirebaseAuth mAuth;
    String mCurrentUserId;

    private DatabaseReference mDbRefUsers;
    private DatabaseReference mDbRefEvents;

    private StorageReference mStorageRef;

    private RecyclerView invites_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_invites);

        invites_list = (RecyclerView) findViewById(R.id.invites_list);
        invites_list.setHasFixedSize(true);
        invites_list.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mDbRefUsers = FirebaseDatabase.getInstance().getReference(LOC_USERS);
        mDbRefUsers.keepSynced(true);
        mDbRefEvents = FirebaseDatabase.getInstance().getReference(LOC_EVENTS);
        mDbRefEvents.keepSynced(true);

        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        Query qUserEvents = mDbRefUsers.child(mCurrentUserId).child(LOC_INVITES).orderByKey();
        displayUsersList(qUserEvents);
    }

    private void displayUsersList(Query query) {
        //tried to split all this up into separate methods but serious issues
        final FirebaseRecyclerAdapter<EventInvite, EventsViewHolder> recyclerAdapter =
                new FirebaseRecyclerAdapter<EventInvite, EventsViewHolder>(
                        EventInvite.class,
                        R.layout.event_item,
                        EventsViewHolder.class,
                        query
                ) {
                    @Override
                    protected void populateViewHolder(final EventsViewHolder viewHolder, EventInvite model, int position) {
                        final String eventKey = getRef(position).getKey();

                        viewHolder.setEventAttending((model.getStatus() == EventInvite.ATTENDING));
                        //write select event for switch - manipulates database to edit user status
                        viewHolder.getAttendingSwitchControl()
                                .setOnCheckedChangeListener(attendSwitchListener(eventKey, model.getSentTimestamp()));

                        mDbRefEvents.child(eventKey).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String eventTitle = "";
                                String eventAdmin = "";
                                String eventImage = "";

                                if (dataSnapshot.child("eventTitle").getValue() != null) {
                                    eventTitle = dataSnapshot.child("eventTitle").getValue().toString();
                                    viewHolder.setEventTitle(eventTitle);
                                }
                                if (dataSnapshot.child("eventAdmin").getValue() != null) {
                                    eventAdmin = dataSnapshot.child("eventAdmin").getValue().toString();
                                    mDbRefUsers.child(eventAdmin)
                                            .child("displayName")
                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dsDisplayName) {
                                                    if (dsDisplayName != null && dsDisplayName.getValue() != null) {
                                                        String displayName = dsDisplayName.getValue().toString();
                                                        viewHolder.setInvitedBy(displayName);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    Log.d(TAG, "unable to fetch event admin data");
                                                }
                                            });
                                }
                                if (dataSnapshot.child("eventImage").getValue() != null) {
                                    eventImage = dataSnapshot.child("eventImage").getValue().toString();

                                    mStorageRef.child("images/events/" + eventKey + "/" + eventImage)
                                            .getDownloadUrl().addOnCompleteListener(imageCompleteListener(viewHolder));
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(TAG, "error fetching events: " + databaseError.getMessage());
                            }
                        });

                        viewHolder.getView().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int viewId = view.getId();

                                if (viewId == R.id.swch_event_attending) {

                                } else {
                                    Intent evIntent = new Intent(
                                            UsersTestActivity.this,
                                            ProfileActivity.class
                                    );
                                    evIntent.putExtra("EVENT_ID", eventKey);
                                    startActivity(evIntent);
                                }
                            }
                        });
                    }
                };

        invites_list.setAdapter(recyclerAdapter);
    }

    private CompoundButton.OnCheckedChangeListener attendSwitchListener(final String eventKey, final long sentTimestamp) {
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
                String inviteLocEvent = LOC_EVENTS + "/" + eventKey + "/" + LOC_INVITES + "/" + mCurrentUserId;
                String inviteLocUser = LOC_USERS + "/" + mCurrentUserId + "/" + LOC_INVITES + "/" + eventKey;
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

    private OnCompleteListener<Uri> imageCompleteListener(final EventsViewHolder viewHolder) {
        return new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    viewHolder.setEventImage(
                            getApplicationContext(),
                            task.getResult()
                    );
                } else {
                    Log.d(TAG, "unable to fetch event photo");
                }
            }
        };
    }
}