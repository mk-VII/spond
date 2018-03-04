package com.marcuthh.respond;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.net.URI;

public class SpondersActivity extends AppCompatActivity {

    private static String TAG = "SponderActivity";

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

        //tried to split all this up into separate methods but serious issues
        final FirebaseRecyclerAdapter<Sponder, SpondersViewHolder> recyclerAdapter =
                new FirebaseRecyclerAdapter<Sponder, SpondersViewHolder>(
                        Sponder.class,
                        R.layout.sponder_item,
                        SpondersViewHolder.class,
                        mDbRefUsers
                ) {
                    @Override
                    protected void populateViewHolder(final SpondersViewHolder viewHolder, Sponder model, int position) {
                        viewHolder.setDisplayName(model.getDisplayName());
                        viewHolder.setStatus(model.getStatus());

                        //the unique id of this user record
                        final String uid = getRef(position).getKey();

                        String photoName = model.getAccountPhotoName();
                        if (photoName != null && !photoName.equals("")) {
                            //calculate the path to user's profile picture
                            FirebaseStorage.getInstance().getReference(
                                    model.buildAccountPhotoNodeFilter(
                                            uid,
                                            true
                                    )
                            ).getDownloadUrl()
                                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (task.isSuccessful()) {
                                                viewHolder.setProfileImage(
                                                        getApplicationContext(),
                                                        task.getResult()
                                                );
                                            } else {
                                                Log.d(TAG, "Unable to fetch photo at location");
                                            }
                                        }
                                    });
                        }

                        viewHolder.getView().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent profileIntent = new Intent(
                                        SpondersActivity.this,
                                        ProfileActivity.class
                                );
                                profileIntent.putExtra("USER_ID", uid);
                                startActivity(profileIntent);
                            }
                        });
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
