package com.marcuthh.respond;

import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    //region ////GLOBALS////
    ////CONSTANTS////
    private static final String TAG = "ChatActivity";

    private static final String LOC_CHATS = "chats";
    private static final String LOC_MESSAGES = "messages";
    private static final String LOC_USERS = "users";
    private static final String LOC_EVENTS = "events";
    ////CONSTANTS////

    //Firebase components
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDbRefRoot;
    private DatabaseReference mDbRefChats;
    private DatabaseReference mDbRefMessages;
    private DatabaseReference mDbRefUsers;
    private DatabaseReference mDbRefEvents;
    //file transfer to database
    private StorageReference mStorageRef;
    //interfaces
    private FirebaseAuth.AuthStateListener mAuthListener;
    //Firebase components

    private String mCurrentUserId;
    private String mChatUserId;
    private Conversation conversation;

    FloatingActionButton btnSendMsg;
    EditText input;
    Toolbar toolbar;
    private RecyclerView message_list;

    String defaultResponse;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //get Firebase connections
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //set up reference to Firebase file transfer
        //and check permissions to access files
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //setup auth processes and callback behaviour
        mAuthListener = getAuthListener();

        input = (EditText) findViewById(R.id.input);
        btnSendMsg = (FloatingActionButton) findViewById(R.id.btnSendMsg);
        input.addTextChangedListener(onTextChanged());
        btnSendMsg.setOnClickListener(sendChatMessage());

        message_list = (RecyclerView) findViewById(R.id.message_list);
        message_list.setHasFixedSize(true);
        message_list.setLayoutManager(new LinearLayoutManager(this));

        mChatUserId = getIntent().getStringExtra("CHAT_PARTNER_ID");

        mDbRefRoot = mDatabase.getReference();
        mDbRefRoot.keepSynced(true);
        mDbRefChats = mDatabase.getReference(LOC_CHATS);
        mDbRefChats.keepSynced(true);
        mDbRefChats.addValueEventListener(onChatChangeListener());
        mDbRefMessages = mDatabase.getReference(LOC_MESSAGES);
        mDbRefMessages.keepSynced(true);
        mDbRefMessages.addValueEventListener(onMessagesChangeListener());
        mDbRefUsers = mDatabase.getReference(LOC_USERS);
        mDbRefUsers.keepSynced(true);
        mDbRefEvents = mDatabase.getReference(LOC_EVENTS);
        mDbRefEvents.keepSynced(true);

        getUserDefaultResponse();
    }

    private void getUserDefaultResponse() {
        mDatabase.getReference(LOC_USERS).child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("defaultResponse")) {
                    defaultResponse = dataSnapshot.child("defaultResponse").getValue().toString();
                } else {
                    defaultResponse = getString(R.string.user_default_response);
                }

                //set text as initial hint
                input.setHint("\"" + defaultResponse + "\"");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        if (mChatUserId != null && !mChatUserId.equals("")) {
            displayChatMessages(mDbRefEvents.equalTo(""));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    private TextWatcher onTextChanged() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //...
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() == 0) {
                    input.setHint("\"" + defaultResponse + "\"");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //...
            }
        };
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

    private ValueEventListener onChatChangeListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUserId)) {

                    HashMap<String, Object> chatAddMap = new HashMap<String, Object>();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    HashMap<String, Object> chatUserMap = new HashMap<String, Object>();
                    chatUserMap.put(mCurrentUserId + "/" + mChatUserId, chatAddMap);
                    chatUserMap.put(mChatUserId + "/" + mCurrentUserId, chatAddMap);

                    mDbRefChats.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.d(TAG, "Error creating chat record: " + databaseError.getMessage());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Operation cancelled: " + databaseError.getMessage());
            }
        };
    }

    private ValueEventListener onMessagesChangeListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mChatUserId != null && !mChatUserId.equals("")) {
                    mDbRefMessages.orderByChild("mChatUserId").equalTo(mChatUserId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private ValueEventListener onDataChangeListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "error: " + databaseError.getMessage());
                //...
            }
        };
    }

    private View.OnClickListener sendChatMessage() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText input = (EditText) findViewById(R.id.input);
                String message = input.getText().toString();

                if (message != null && !message.equals("")) {

                    final String currentUserRef = LOC_MESSAGES + "/" + mCurrentUserId + "/" + mChatUserId;
                    final String chatUserRef = LOC_MESSAGES + "/" + mChatUserId + "/" + mCurrentUserId;

                    String pushId = mDbRefRoot.child(currentUserRef).push().getKey();

                    Map<String, Object> messageMap = new HashMap<String, Object>();
                    messageMap.put("message", message);
                    messageMap.put("viewed", false);
                    messageMap.put("type", "text");
                    messageMap.put("time", ServerValue.TIMESTAMP);
                    messageMap.put("sender", mCurrentUserId);

                    Map<String, Object> messageUserMap = new HashMap<String, Object>();
                    messageUserMap.put(currentUserRef + "/" + pushId, messageMap);
                    messageUserMap.put(chatUserRef + "/" + pushId, messageMap);

                    mDbRefMessages.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
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
                                mDbRefChats.child(currentUserRef).child("seen").setValue(true);
                                mDbRefChats.child(currentUserRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                mDbRefChats.child(currentUserRef).child("seen").setValue(true);
                                mDbRefChats.child(currentUserRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                //clear for subsequent input
                                input.setText("");
                            }
                        }
                    });
                } else {
                    //load default response text into field to allow sending
                    input.setText(defaultResponse);
                }
            }
        };
    }

    private void displayChatMessages(Query query) {
        final FirebaseRecyclerAdapter<Message, MessagesViewHolder> recyclerAdapter =
                new FirebaseRecyclerAdapter<Message, MessagesViewHolder>(
                        Message.class,
                        R.layout.message_item,
                        MessagesViewHolder.class,
                        query
                ) {
                    @Override
                    protected void populateViewHolder(final MessagesViewHolder viewHolder, final Message model, int position) {
                        viewHolder.setMessageDisplay((model.getMessageSender().equals(mCurrentUserId)));
                        //all messages have at least a sender, time and text//
                        viewHolder.setMessageTime(model.getMessageTime());
                        viewHolder.setMessageText(model.getMessageText());

                        mDbRefUsers.child(model.getMessageSender()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String senderName = "";
                                String childDisplayName = "displayName";
                                if (dataSnapshot.hasChild(childDisplayName)) {
                                    senderName = dataSnapshot.child(childDisplayName).getValue().toString();
                                } else {
                                    String firstName = dataSnapshot.child("firstName").getValue().toString();
                                    String surname = dataSnapshot.child("surname").getValue().toString();
                                    senderName = firstName + " " + surname;
                                }
                                viewHolder.setDisplayName(senderName);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        //end general

                        //messages can have either an event stub or an image attached
                        if (model.hasEvent()) {
                            mDbRefEvents.child(model.getEventKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot != null) {
                                        //set visible if events found
                                        viewHolder.setEventVisible(View.VISIBLE);

                                        //text fields
                                        String eventTitle = dataSnapshot.child("eventTitle").getValue().toString();
                                        viewHolder.setEventTitle(eventTitle);
                                        long eventTimestamp = Long.parseLong(dataSnapshot.child("eventTimestamp").getValue().toString());
                                        viewHolder.setEventDate(eventTimestamp);

                                        //get image from firebase storage and display
                                        String eventImage = dataSnapshot.child("eventImage").getValue().toString();
                                        if (eventImage != null && !eventImage.equals("")) {
                                            FirebaseStorage.getInstance().getReference(
                                                    "images/events/" + model.getEventKey() + "/" + eventImage
                                            ).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    if (task.isSuccessful()) {
                                                        viewHolder.setMessageEventImage(getApplicationContext(), task.getResult());
                                                    } else {
                                                        Log.d(TAG, "unable to fetch image at location");
                                                    }
                                                }
                                            });
                                        }

                                        //attendance for user viewing
                                        if (dataSnapshot.child("eventInvites").hasChild(mCurrentUserId)) {
                                            long status = Long.parseLong(dataSnapshot
                                                    .child("eventInvites")
                                                    .child(mCurrentUserId)
                                                    .child("status")
                                                    .getValue().toString());

                                            viewHolder.setEventAttending((status == EventInvite.ATTENDING));
                                        }
                                    } else {
                                        //don't display if event not found
                                        viewHolder.setEventVisible(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    viewHolder.setEventVisible(View.GONE);
                                }
                            });

                        } else if (model.hasPhoto()) {
                            FirebaseStorage.getInstance()
                                    .getReference(model.getPhotoLoc()).getDownloadUrl()
                                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (task.isSuccessful()) {
                                                viewHolder.setPhotoVisible(View.VISIBLE);
                                                viewHolder.setMessageImage(
                                                        getApplicationContext(),
                                                        task.getResult()
                                                );
                                            } else {
                                                Log.d(TAG, "unable to fetch photo at this location");
                                                viewHolder.setPhotoVisible(View.GONE);
                                            }
                                        }
                                    });
                        }
                    }
                };
    }
}
