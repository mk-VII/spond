package com.marcuthh.respond;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SpondersActivity extends AppCompatActivity {

    private RecyclerView sponder_list;

    private DatabaseReference mDbRefUsers;
    private final String CHILD_USERS = "users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sponders);

        sponder_list = (RecyclerView) findViewById(R.id.sponder_list);
        sponder_list.setHasFixedSize(true);
        sponder_list.setLayoutManager(new LinearLayoutManager(this));

        mDbRefUsers = FirebaseDatabase.getInstance().getReference(CHILD_USERS);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final FirebaseRecyclerAdapter<Sponder, SpondersViewHolder> recyclerAdapter =
                new FirebaseRecyclerAdapter<Sponder, SpondersViewHolder>(
                        Sponder.class,
                        R.layout.sponder_item,
                        SpondersViewHolder.class,
                        mDbRefUsers
                ) {
                    @Override
                    protected void populateViewHolder(SpondersViewHolder viewHolder, Sponder model, int position) {
                        viewHolder.setDisplayName(model.getDisplayName());
                        viewHolder.setStatus(model.getStatus());
                    }
                };

        recyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = recyclerAdapter.getItemCount();
                int lastVisiblePosition =
                        ((LinearLayoutManager) sponder_list.getLayoutManager())
                                .findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    ((LinearLayoutManager) sponder_list.getLayoutManager())
                            .scrollToPosition(positionStart);
                }
            }
        });

        sponder_list.setAdapter(recyclerAdapter);

    }

}
