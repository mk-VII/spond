package com.marcuthh.respond;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Calendar;
import java.util.Locale;

public class ChatsFragment extends Fragment {

    private static final String TAG = "ChatsFragment";
    private static final String LOC_CHATS = "chats";
    private static final String LOC_USERS = "users";
    private static final String LOC_MESSAGES = "messages";

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private String mCurrentUid;

    private DatabaseReference mDbRefChats;
    private DatabaseReference mDbRefUsers;
    private DatabaseReference mDbRefMessages;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mAuth != null && mAuth.getCurrentUser() != null) {
            mAuth = FirebaseAuth.getInstance();
            mCurrentUser = mAuth.getCurrentUser();
            mCurrentUid = mCurrentUser.getUid();

            mDbRefChats = FirebaseDatabase.getInstance().getReference(LOC_CHATS).child(mCurrentUid);
            mDbRefUsers = FirebaseDatabase.getInstance().getReference(LOC_USERS);
            mDbRefMessages = FirebaseDatabase.getInstance().getReference(LOC_MESSAGES).child(mCurrentUid);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chats, container, false);

        return rootView;
    }

    private void displayChatsList() {

        Query userChatsQuery = mDbRefChats.orderByChild("timestamp");

        //<-- this needs to be a query not just a reference
        final FirebaseRecyclerAdapter<Chat, ChatsViewHolder> recyclerAdapter =
                new FirebaseRecyclerAdapter<Chat, ChatsViewHolder>(
                        Chat.class,
                        R.layout.chat_item,
                        ChatsViewHolder.class,
                        userChatsQuery
                ) {
                    @Override
                    protected void populateViewHolder(final ChatsViewHolder viewHolder, Chat model, int position) {
                        final String chatPartnerId = getRef(position).getKey();

                        mDbRefChats.child(chatPartnerId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String currentChatLabel = dataSnapshot.child("chatLabel").getValue().toString();
                                viewHolder.setChatLabel(currentChatLabel);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        Query lastMessageQuery = mDbRefMessages.child(chatPartnerId).limitToLast(1);
                        lastMessageQuery.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                String messageText = dataSnapshot.child("message").getValue().toString();
                                long timestamp = Long.parseLong(dataSnapshot.child("timestamp").getValue().toString());
                                viewHolder.setContactedBy(messageText);
                                viewHolder.setContactedTime(formattedTimestamp(timestamp));
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        mDbRefUsers.child(chatPartnerId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User chatPartner = dataSnapshot.getValue(User.class);
                                if (chatPartner != null) {
                                    String displayName = chatPartner.getDisplayName();
                                    String photoName = chatPartner.getAccountPhotoName();
                                    if (viewHolder.getChatLabel().equals("")) {
                                        viewHolder.setChatLabel(displayName);
                                    }

                                    if (photoName != null && !photoName.equals("")) {
                                        FirebaseStorage.getInstance().getReference(
                                                chatPartner.buildAccountPhotoNodeFilter(
                                                        chatPartnerId,
                                                        true
                                                )
                                        ).getDownloadUrl()
                                                .addOnCompleteListener(imageCompleteListener(viewHolder));
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                };
    }

    private OnCompleteListener<Uri> imageCompleteListener(final ChatsViewHolder viewHolder) {
        return new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    viewHolder.setChatImage(
                            getActivity().getApplicationContext(),
                            task.getResult()
                    );
                } else {
                    Log.d(TAG, "Unable to fetch photo at location");
                }
            }
        };
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
    public void onAttach(Context context) {
        super.onAttach(context);

        //...
    }

    @Override
    public void onDetach() {
        super.onDetach();

        //...
    }
}
