package com.marcuthh.respond;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    //CONSTANTS
    private static final String TAG = "MainActivity";
    private static final String TBL_USERS = "users";
    // PERMISSION CODES //
    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 65636;
    private static final int SIGN_IN_REQUEST_CODE = 1;

    private SectionsPageAdapter mSectionsPageAdapter;
    private ViewPager mViewPager;

    //represents currently logged in user
    //full profile, not just registered user from mAuth
    User appUser;

    //Firebase objects
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDbRef;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseListAdapter<Message> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get Firebase connections
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view,
                        "Replace with your own action",
                        Snackbar.LENGTH_LONG).setAction(
                        "Action",
                        null).show();
            }
        });

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mAuthListener = createAuthListener();

        //test token value
        //Log.d(TAG, "Refreshed token: " + FirebaseInstanceId.getInstance().getToken());
    }

    private FirebaseAuth.AuthStateListener createAuthListener() {
        return new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //user not yet loaded from database
                if (appUser == null) {
                    //get user data
                    mDbRef = mDatabase.getReference(TBL_USERS);
                    mDbRef.keepSynced(true);
                    mDbRef.addValueEventListener(loadUserAccountFromDatabase());
                    //calls initialiseActivity() from inside method once user retrieved
                } else {
                    //get user at state of authchanged call
                    //load first screen or redirect to login
                    initialiseActivity(mAuth.getCurrentUser());
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (mAuth.getCurrentUser() != null) {

                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    mDbRef.child(mAuth.getUid())
                            .child("token")
                            .setValue(deviceToken)
                            //loads welcome screen in callback function
                            //once database request completed
                            .addOnCompleteListener(loginCompleteListener());
                } else {
                    Toast.makeText(this,
                            "We couldn't sign you in. Please try again later.",
                            Toast.LENGTH_LONG)
                            .show();
                    //close app - TODO: go to retry loop
                    finish();
                }
            }
        }
    }

    private OnCompleteListener<Void> loginCompleteListener() {
        return new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mSectionsPageAdapter =
                            new SectionsPageAdapter(getSupportFragmentManager());
                    mViewPager = (ViewPager) findViewById(R.id.container);
                    setupViewPager(mViewPager);

                    TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
                    tabLayout.setupWithViewPager(mViewPager);

                    Toast.makeText(getApplicationContext(),
                            "Successfully signed in. Welcome, " +
                                    mAuth.getCurrentUser().getDisplayName(),
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "We couldn't sign you in. Please try again later.",
                            Toast.LENGTH_LONG)
                            .show();
                    //close app - TODO: go to retry loop
                    finish();
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        if (mDbRef == null) {
            //get account details for logged in user
            mDbRef = mDatabase.getReference(TBL_USERS);
            mDbRef.keepSynced(true);
            mDbRef.addValueEventListener(loadUserAccountFromDatabase());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mItem) {
        if (mItem.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this,
                                    "You have been signed out.",
                                    Toast.LENGTH_LONG)
                                    .show();

                            //close activity on sign out
                            finish();
                        }
                    });
        } else if (mItem.getItemId() == R.id.menu_all_users) {
            Intent spondersIntent = new Intent(
                    MainActivity.this,
                    UsersActivity.class
            );
            startActivity(spondersIntent);
        } else if (mItem.getItemId() == R.id.menu_account) {
            Intent accountIntent = new Intent(
                    MainActivity.this,
                    AccountActivity.class
            );
            startActivity(accountIntent);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        ContactsFragment fragContacts =
                (ContactsFragment) mSectionsPageAdapter.getFragmentByTag("ContactsFragment");

        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission is granted - display contacts
                fragContacts.displayContactsList(fragContacts.getView());
            } else {
                Toast.makeText(
                        this,
                        "Cannot display contacts without permission",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter =
                new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new ChatsFragment(), "Chats");
        adapter.addFragment(new InvitesFragment(), "Invites");
        adapter.addFragment(new ContactsFragment(), "Contacts");
        viewPager.setAdapter(adapter);
    }

    public void initialiseActivity(FirebaseUser currentUser) {
        if (currentUser == null) {
            //start sign in/register process
            startActivityForResult(AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(
                                    Arrays.asList(
                                            new AuthUI.IdpConfig.Builder(
                                                    AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(
                                                    AuthUI.PHONE_VERIFICATION_PROVIDER).build()
                                    )
                            )
                            .build(),
                    SIGN_IN_REQUEST_CODE);
        } else {
            if (appUser.isComplete()) {
                //user is already signed in
                //display Welcome Toast
                Toast.makeText(this,
                        "Welcome back, " + appUser.getDisplayName() + "!",
                        Toast.LENGTH_LONG)
                        .show();
            } else {
                //start new activity for user to complete details
                Intent accountIntent = new Intent(
                        MainActivity.this,
                        AccountActivity.class
                );
                startActivity(accountIntent);
            }
        }
    }

    private ValueEventListener loadUserAccountFromDatabase() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mAuth.getUid() != null && !mAuth.getUid().equals("")) {
                    DataSnapshot child = dataSnapshot.child(mAuth.getUid());
                    if (child != null) {
                        User acc = child.getValue(User.class);
                        if (acc != null) {
                            String phoneNumber = acc.getPhoneNumber();
                            String emailAddress = acc.getEmailAddress();
                            String firstName = acc.getFirstName();
                            String surname = acc.getSurname();
                            String displayName = acc.getDisplayName();
                            String photoName = acc.getAccountPhotoName();
                            String accountPhotoNames = acc.getAccountPhotoNames();
                            boolean isComplete = acc.isComplete();
                            String token = acc.getToken();

                            appUser = new User(
                                    phoneNumber, emailAddress,
                                    firstName, surname, displayName,
                                    photoName, accountPhotoNames, "", 0, isComplete, token);

                            //get current user signed into app
                            //load first screen or redirect to login
                            initialiseActivity(mAuth.getCurrentUser());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }
}
