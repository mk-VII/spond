package com.marcuthh.respond;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private static final String TBL_CHATS = "chats";
    private static final String TBL_CHATMESSAGES = "chatmessages";

    //Firebase components
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDbRefChats;
    private DatabaseReference mDbRefMessages;
    //file transfer to database
    private StorageReference mStorageRef;
    //interfaces
    private FirebaseAuth.AuthStateListener mAuthListener;
    //Firebase components

    private long spondenceId;
    private Spondence spondence;

    FloatingActionButton btnSendMsg;
    EditText input;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //get Firebase connections
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //set up reference to Firebase file transfer
        //and check permissions to access files
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //setup auth processes and callback behaviour
        mAuthListener = getAuthListener();

        spondenceId = getIntent().getLongExtra("SPONDENCE_ID", 0);

        btnSendMsg = (FloatingActionButton) findViewById(R.id.btnSendMsg);
        btnSendMsg.setOnClickListener(sendChatMessage());
        input = (EditText) findViewById(R.id.input);
        input.addTextChangedListener(onTextChanged());

        displayChatMessages();
    }

    private TextWatcher onTextChanged() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    btnSendMsg.setEnabled(true);
                } else {
                    btnSendMsg.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

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

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        //get account details for logged in user
        mDbRefChats = mDatabase.getReference(TBL_CHATS);
        mDbRefChats.addValueEventListener(onChatChangeListener());
        mDbRefMessages = mDatabase.getReference(TBL_CHATMESSAGES);
        mDbRefMessages.addValueEventListener(onMessagesChangeListener());
    }

    private ValueEventListener onChatChangeListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (spondenceId > 0) {
                    DataSnapshot childChat = dataSnapshot.child(Long.toString(spondenceId));
                    if (childChat != null) {
                        Spondence chat = childChat.getValue(Spondence.class);
                        if (chat != null) {
                            String name = chat.getName();
                            Sponder[] sponders = chat.getSponders();
                            Sponse[] sponses = chat.getSponses();

                            spondence = new Spondence(name, sponders, sponses);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private ValueEventListener onMessagesChangeListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (spondenceId > 0) {
                    mDbRefMessages.orderByChild("spondenceId").equalTo(Long.toString(spondenceId));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
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
                EditText input = (EditText) findViewById(R.id.input);

                String sender = mAuth.getCurrentUser().getUid();
                String messageChatRef = TBL_CHATMESSAGES + "/" + spondenceId;
                String pushId = mDbRefMessages
                        .child(Long.toString(spondenceId))
                        .push().getKey();

                final Map<String, String> timeStamp = ServerValue.TIMESTAMP;
                Map<String, Object> messageMap = new HashMap<String, Object>();
                messageMap.put("text", input.getText().toString());
                messageMap.put("sender", sender);
                messageMap.put("viewed", false);
                messageMap.put("time", timeStamp);

                Map<String, Object> messageChatMap = new HashMap<String, Object>();
                messageChatMap.put(messageChatRef + "/" + pushId, messageMap);

                mDbRefMessages.updateChildren(messageChatMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.d(TAG, databaseError.getMessage());
                        } else {
                            mDbRefChats.child(Long.toString(spondenceId))
                                    .child("lastSponded").setValue(timeStamp);
                        }
                    }
                });

                //clear for subsequent input
                input.setText("");
            }
        };
    }

    private void displayChatMessages() {
    }
}
