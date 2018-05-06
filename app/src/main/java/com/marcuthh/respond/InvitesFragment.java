package com.marcuthh.respond;

import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class InvitesFragment extends Fragment {

    private static final String TAG = "InvitesFragment";
    private final String LOC_USERS = "users";
    private final String LOC_EVENTS = "events";

    //Firebase components
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;
    private DatabaseReference mDbRefUsers;
    private DatabaseReference mDbRefEvents;
    //file transfer to database
    private StorageReference mStorageRef;
    //interfaces
    private FirebaseAuth.AuthStateListener mAuthListener;
    //Firebase components

    private String mCurrentUserId;

    private RecyclerView invites_list;

    public InvitesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        if (mAuth != null && mAuth.getCurrentUser() != null) {
            mCurrentUserId = mAuth.getCurrentUser().getUid();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_invites, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//
//        invites_list = (RecyclerView) getView().findViewById(R.id.invites_list);
//
//        mDbRefUsers = FirebaseDatabase.getInstance().getReference(LOC_USERS);
//        mDbRefUsers.keepSynced(true);
//        mDbRefEvents = FirebaseDatabase.getInstance().getReference(LOC_EVENTS);
//        mDbRefEvents.keepSynced(true);
//
//        DatabaseReference refUserEvents = mDbRefUsers.child(mCurrentUserId).child("eventInvites");
//
//        displayUserEvents(refUserEvents);

    }

    private void displayUserEvents(DatabaseReference ref) {
        final FirebaseRecyclerAdapter<EventInvite, EventsViewHolder> recyclerAdapter =
                new FirebaseRecyclerAdapter<EventInvite, EventsViewHolder>(
                        EventInvite.class,
                        R.layout.event_item,
                        EventsViewHolder.class,
                        ref
                ) {
                    @Override
                    protected void populateViewHolder(EventsViewHolder viewHolder, EventInvite model, int position) {
                        getRef(position).getKey();
                    }
                };
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }
}
