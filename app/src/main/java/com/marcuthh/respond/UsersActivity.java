package com.marcuthh.respond;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public class UsersActivity extends AppCompatActivity {

    private static String TAG = "UserActivity";

    private RecyclerView user_list;

    private DatabaseReference mDbRefUsers;
    private final String CHILD_USERS = "users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        user_list = (RecyclerView) findViewById(R.id.user_list);
        user_list.setHasFixedSize(true);
        user_list.setLayoutManager(new LinearLayoutManager(this));

        mDbRefUsers = FirebaseDatabase.getInstance().getReference(CHILD_USERS);
        mDbRefUsers.keepSynced(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //tried to split all this up into separate methods but serious issues
        final FirebaseRecyclerAdapter<User, UsersViewHolder> recyclerAdapter =
                new FirebaseRecyclerAdapter<User, UsersViewHolder>(
                        User.class,
                        R.layout.user_item,
                        UsersViewHolder.class,
                        mDbRefUsers
                ) {
                    @Override
                    protected void populateViewHolder(final UsersViewHolder viewHolder, User model, int position) {
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
                                    .addOnCompleteListener(imageCompleteListener(viewHolder));
                        }

                        viewHolder.getView().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent profileIntent = new Intent(
                                        UsersActivity.this,
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
                        ((LinearLayoutManager) user_list.getLayoutManager())
                                .findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    ((LinearLayoutManager) user_list.getLayoutManager())
                            .scrollToPosition(positionStart);
                }
            }
        });

        user_list.setAdapter(recyclerAdapter);
    }

    private OnCompleteListener<Uri> imageCompleteListener(final UsersViewHolder viewHolder) {
        return new OnCompleteListener<Uri>() {
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
        };
    }
}
