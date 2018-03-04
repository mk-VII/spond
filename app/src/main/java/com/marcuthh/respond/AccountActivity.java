package com.marcuthh.respond;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
//API imports
//import com.facebook.FacebookSdk;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import id.zelory.compressor.Compressor;

public class AccountActivity extends AppCompatActivity {

    //region //Global Declarations
    ////Globals////
    //CONSTANTS//
    private static final String TAG = "AccountActivity";
    private static final String TBL_USERS = "users";
    private static final int MAX_EMAIL_ATTEMPTS = 3;
    //string appended to filenames that have the same name as an existing file
    private final static String IMAGE_COPY_TAG = "_firebasecopy_";
    private final static String OPTION_CAMERA = "Camera", OPTION_GALLERY = "Gallery", OPTION_CANCEL = "Cancel";
    //PERMISSIONS//
    private static final int PERMISSIONS_REQUEST_ACCESS_STORAGE = 1001;
    private static final int REQUEST_CAMERA = 1002;
    private static final int SELECT_FILE = 1003;
    private static final int REQUEST_CROP = 1004;
    //PERMISSIONS//
    //CONSTANTS//

    //Firebase components
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDbRef;
    //file transfer to database
    private StorageReference mStorageRef;
    //interfaces
    private FirebaseAuth.AuthStateListener mAuthListener;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    //Firebase components

    //activity controls//
    private Button btn_save;
    private TextInputEditText etxt_yourName;
    private TextInputEditText etxt_displayName;
    private TextInputEditText etxt_phone;
    private TextInputEditText etxt_email;
    private ImageView img_account;
    //activity controls//

    //account & updates//
    //full details for current signed in user
    //will be populated in onStart method - stored here to prevent another read before update to DB
    Sponder appUser;
    //indicates whether account update has been successful, allows user to retry
    boolean updateSuccess;
    //tracks number of attempts to send email on change of email address
    int emailAttemptCount = 0;
    //account & updates//

    //Account Image data//
    //flagged as true when new image uploaded
    //instead of comparing the two image objects on save procedure
    boolean appAccountPhotoChanged = false;
    //holds image returned from camera or gallery
    Uri newAccountPhotoUri;
    String newAccountPhotoFileName = "";
    private Drawable DRAWABLE_DEFAULT_PHOTO;
    //Account Image data//
    ////Globals////
    //endregion

    //region ////Overrides////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        //get Firebase connections
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //set up reference to Firebase file transfer
        //and check permissions to access files
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //setup auth processes and callback behaviour
        mAuthListener = getAuthListener();
        mCallBacks = getCallBacks();

        DRAWABLE_DEFAULT_PHOTO = getDrawable(R.drawable.no_account_photo);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //load activity controls
        loadControls();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        //get account details for logged in user
        mDbRef = mDatabase.getReference(TBL_USERS);
        mDbRef.addValueEventListener(onDataChangeListener());
    }

    @Override
    public void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_STORAGE) {
            if (!(grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(
                        this,
                        "You cannot edit your account photo without this permission",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                if (data.getExtras() != null) {
                    newAccountPhotoUri = (Uri) data.getExtras().get(MediaStore.EXTRA_OUTPUT);
                    newAccountPhotoFileName = getFileNameFromURI(newAccountPhotoUri);
                    if (!newAccountPhotoFileName.equals("")) {
                        appAccountPhotoChanged = true;
                        //send image to be cropped
                        cropImage(newAccountPhotoUri);
                    }
                }
            } else if (requestCode == SELECT_FILE) {
                newAccountPhotoUri = data.getData();
                newAccountPhotoFileName = getFileNameFromURI(newAccountPhotoUri);
                if (!newAccountPhotoFileName.equals("")) {
                    appAccountPhotoChanged = true;
                    //send image to be cropped
                    cropImage(newAccountPhotoUri);
                }
            } else if (requestCode == REQUEST_CROP) {
                //update URI to store cropped image object
                //flag and file name will already be set from camera or gallery selection
                newAccountPhotoUri = data.getData();
                Glide.with(getApplicationContext())
                        .load(newAccountPhotoUri)
                        .placeholder(R.drawable.no_account_photo)
                        .into(img_account);
            }
        }
    }
    //endregion

    //region ////Methods////
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks getCallBacks() {
        return new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                FirebaseUser regUser = mAuth.getCurrentUser();
                if (regUser != null) {
                    regUser.updatePhoneNumber(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Intent mainIntent = new Intent(
                                            AccountActivity.this,
                                            MainActivity.class
                                    );
                                    startActivity(mainIntent);
                                }
                            });
                }
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Sorry, your credentials were invalid",
                            Toast.LENGTH_SHORT
                    ).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Sorry, you have exceeded the maximum number of SMS verification code requests",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        };
    }

    private void loadControls() {
        //views
        btn_save = (Button) findViewById(R.id.btn_save);
        etxt_yourName = (TextInputEditText) findViewById(R.id.etxt_yourName);
        etxt_displayName = (TextInputEditText) findViewById(R.id.etxt_displayName);
        etxt_phone = (TextInputEditText) findViewById(R.id.etxt_phone);
        etxt_email = (TextInputEditText) findViewById(R.id.etxt_email);
        img_account = (ImageView) findViewById(R.id.img_account);
        Button btn_clearPhoto = (Button) findViewById(R.id.btn_clearPhoto);
        Button btn_editPhoto = (Button) findViewById(R.id.btn_editPhoto);

        //validation listeners
        //listener to implement save and database update
        btn_save.setOnClickListener(updateUserAccount());
        //no special req, just cannot be empty
        etxt_yourName.setOnFocusChangeListener(validateInput());
        //cannot be blank, and resets to one of two options
        etxt_displayName.setOnFocusChangeListener(validateDisplayName());
        //email and phone must be validated together on every change
        //user profile requires one or the other to exist
        etxt_phone.setOnFocusChangeListener(validateEmailAndPhone());
        etxt_email.setOnFocusChangeListener(validateEmailAndPhone());
        //listeners to execute photo actions
        btn_clearPhoto.setOnClickListener(clearAccountPhoto());
        btn_editPhoto.setOnClickListener(selectImageFromCameraOrGallery());
    }

    private void updateDisplay(Sponder accountData) {

        //only enable image if all required information is provided
        btn_save.setEnabled(isValid());

        //get user's name from account fields or registration
        String fullName = buildAccountName(accountData.getFirstName(),
                accountData.getSurname(), accountData.isComplete());

        etxt_yourName.setText(fullName);

        //use full name if no custom display name provided
        if (!accountData.getDisplayName().equals("")) {
            etxt_displayName.setText(accountData.getDisplayName());
        } else {
            etxt_displayName.setText(fullName);
        }

        //contact details
        etxt_phone.setText(accountData.getPhoneNumber());
        etxt_email.setText(accountData.getEmailAddress());

        updateAccountPhotoDisplay(accountData);
    }

    private void updateAccountPhotoDisplay(Sponder accountData) {
        boolean useDefault = false;

        //new image added on page
        if (appAccountPhotoChanged) {
            if (!(newAccountPhotoUri == null)) {
                //use newly-added image that has not yet been committed to storage
                Glide.with(getApplicationContext())
                        .load(newAccountPhotoUri)
                        .placeholder(R.drawable.no_account_photo)
                        .into(img_account);
            } else {
                //previous photo removed
                //use app default image
                useDefault = true;
            }
        } else {
            if (!accountData.usesDefaultPhoto()) {
                //load associated account photo from storage
                mStorageRef.child(
                        accountData.buildAccountPhotoNodeFilter(mAuth.getUid(), true))
                        .getDownloadUrl()
                        .addOnSuccessListener(fileFoundListener())
                        .addOnFailureListener(fileNotFoundListener());
            } else {
                //previous photo removed
                //use app default image
                useDefault = true;
            }
        }

        //flagged during load process to rely on app default image
        if (useDefault) {
            img_account.setImageDrawable(DRAWABLE_DEFAULT_PHOTO);
        }
    }

    private Sponder getUserAccountFromControls(Sponder accountData) {
        //name:
        //all entered into single field
        //last ' ' indicates split between first name and surname
        //if middle name entered, will be attached to first name
        String fullName = etxt_yourName.getText().toString().trim();
        int indexLastSpace = fullName.lastIndexOf(' ');
        if (indexLastSpace >= 0) {
            accountData.setFirstName(fullName.substring(0, indexLastSpace).trim());
            accountData.setSurname(fullName.substring(indexLastSpace + 1).trim());
        } else {
            //no space found
            //assign everything into first name
            accountData.setFirstName(fullName);
            accountData.setSurname("");
        }

        //display name is applied directly from field
        accountData.setDisplayName(etxt_displayName.getText().toString().trim());

        //update phone and email
        accountData.setPhoneNumber(etxt_phone.getText().toString().trim());
        accountData.setEmailAddress(etxt_email.getText().toString().trim());

        //only change image if upload process has flagged this as a different image to previous
        if (appAccountPhotoChanged) {
            //sets file name either to new instance or to empty
            //which will trigger use of default image
            accountData.setAccountPhotoName(newAccountPhotoFileName, false);
        }

        //account flagged as completed once first save has been made
        accountData.setComplete();

        //return updated account object
        return accountData;
    }

    private boolean isValid() {
        //returns false if:
        //full name blank
        //display name blank
        //both phone and email blank
        return !(etxt_yourName.getText().toString().equals("") ||
                etxt_displayName.getText().toString().equals("") ||
                (etxt_phone.getText().toString().equals("") &&
                        etxt_email.getText().toString().equals("")));
    }

    private String buildAccountName(String firstName, String surname, boolean accountComplete) {
        if (!accountComplete || (firstName.equals("") && surname.equals(""))) {
            //no values in either name field
            //use full name from account register
            if (mAuth.getCurrentUser() != null) {
                return mAuth.getCurrentUser().getDisplayName();
            } else {
                //no user data
                //return blank string
                return "";
            }

        } else {
            //at least one of the name fields has a value
            String nameStr = "";

            if (!firstName.equals("")) {
                //set first name
                nameStr += firstName;
            }
            if (!surname.equals("")) {
                if (!nameStr.equals("")) {
                    //first name already in string, append space
                    nameStr += " ";
                }
                //append surname to existing string
                nameStr += surname;
            }
            //return concatenated string
            return nameStr;
        }
    }

    public Uri getImageContentUri(Context inContext, String absPath) {
        Cursor cursor = inContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{absPath}, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(id));
        } else if (absPath.equals("")) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, absPath);
            return inContext.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            return null;
        }
    }

    public boolean storagePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_ACCESS_STORAGE
                );
                return false;
            }
        } else {
            //permission granted automatically
            return true;
        }
    }

    public String getFileNameFromURI(Uri fileUri) {
        String filePath = getRealPathFromURI(fileUri);
        if (!filePath.equals("")) {
            int lastSeparatorIndex = filePath.lastIndexOf("/");
            //increment to ignore leading '/' char
            lastSeparatorIndex++;
            if (lastSeparatorIndex > 0) {
                //return just the file name and extension
                return filePath.substring(lastSeparatorIndex);
            }
        }

        return "";
    }

    public String getRealPathFromURI(Uri fileUri) {
        Cursor cursor = getContentResolver().query(
                fileUri,
                null,
                null,
                null,
                null
        );
        if (cursor != null) {
            cursor.moveToFirst();
            String uriPath = cursor.getString(
                    cursor.getColumnIndex(
                            MediaStore.Images.ImageColumns.DATA));
            cursor.close();

            return uriPath;
        }

        return null;
    }

    private File createFileAtUniquePath(File directory) {
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
        String dateStamp = dateFormat.format(new Date());
        int imageIdNo = 1;
        String imagePath = "IMG_" + dateStamp + getString(R.string.app_name).toUpperCase() + "_1";
        //increment id until a unique file name is found
        while (new File(directory, imagePath).exists()) {
            imageIdNo++;
            imagePath = "IMG_" +
                    dateStamp + "_" +
                    getString(R.string.app_name).toUpperCase() +
                    Integer.toString(imageIdNo);
        }
        //return file with name containing unique id
        return new File(directory, imagePath);
    }

    private void cropImage(Uri imageUri) {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(imageUri, "image/*");

        int imgDimension = 160;
        cropIntent.putExtra("crop", true);
        cropIntent.putExtra("outputX", imgDimension);
        cropIntent.putExtra("outputY", imgDimension);
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("scaleUpIfNeeded", true);
        cropIntent.putExtra("return-data", true);

        cropIntent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION |
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        );
        startActivityForResult(cropIntent, REQUEST_CROP);
    }

    public String getUniqueStoragePath(String fileName) {
        //get all previous account photo file names
        String[] existingFileNames = appUser.getAccountPhotoNames().split(",");
        String copyNumberFileName = "";
        if (existingFileNames.length > 0) {
            copyNumberFileName = buildNewCopyNumberString(existingFileNames, fileName);
        }
        //unique file name created
        //create target path for file storage using user's path and file name
        return appUser.buildAccountPhotoNodeFilter(
                mAuth.getUid(),
                false
        ) + "/" + copyNumberFileName;
    }

    private String buildNewCopyNumberString(String[] existingFiles, String fileName) {
        String fileSuffix = "";
        while (Arrays.asList(existingFiles).contains(fileName)) {
            //remove file type extension from name
            //will be re-appended once version number is added
            fileSuffix = fileName.substring(fileName.lastIndexOf('.'));
            fileName = fileName.replaceAll(fileSuffix, "");

            if (!fileName.contains(IMAGE_COPY_TAG)) {
                //has only one duplicate
                //needs first copy tag assigning
                fileName += IMAGE_COPY_TAG + "1";
            } else {
                //has multiple duplicates
                //loop will continue to increment copy number until unique
                int versionTagIndex = fileName.indexOf(IMAGE_COPY_TAG);
                String oldVersionString = fileName.substring(versionTagIndex);
                int versionNoIndex = (versionTagIndex + (IMAGE_COPY_TAG.length() + 1));
                int versionNo = Integer.parseInt(
                        oldVersionString.substring(versionTagIndex, versionNoIndex));
                //increment isolated copy number and append to tag
                String newVersionString = IMAGE_COPY_TAG + (versionNo + 1);
                //replace previous copy number with new one
                fileName =
                        fileName.replace(oldVersionString, newVersionString);
            }
        }

        //return original filename
        //with appended or updated copy number
        //return updated filename and extension
        return fileName + fileSuffix;
    }

    //methods to update user data in firebase//
    private void writeAccountPhotoToStorage() {

        //update associated account photo first
        //so profile object can be updated with correct filename
        //includes case where filename has to be updated to avoid duplicates

        //holds path to where account photo will be stored
        //factors in possibility of duplicating file names
        //appends or increments a unique copy number if duplicate found
        String targetStoragePath = getUniqueStoragePath(newAccountPhotoFileName);
        //returns full path object and also updates value of parameter ^^

        //TODO: some compression stuff here on image file

        StorageReference uploadRef = mStorageRef.child(targetStoragePath);
        uploadRef.putFile(newAccountPhotoUri)
                .addOnCompleteListener(imageUploadedListener())
                .addOnFailureListener(imageUploadFailedListener());
    }

    private void writeUserAccountToDB() {
        if (mAuth.getUid() != null) {
            mDbRef.child(mAuth.getUid()).setValue(appUser)
                    .addOnFailureListener(accountUpdateFailedListener());
        }
    }

    private void writeRegDataToDB() {
        //all data associated to user registration
        final FirebaseUser regUser = mAuth.getCurrentUser();
        if (regUser != null) {
            UserProfileChangeRequest profileUpdates =
                    new UserProfileChangeRequest.Builder()
                            .setDisplayName(
                                    buildAccountName(
                                            appUser.getFirstName(),
                                            appUser.getSurname(),
                                            appUser.isComplete())
                            ).build();
            //commit updates
            regUser.updateProfile(profileUpdates)
                    .addOnFailureListener(profileUpdateFailedListener());

            if (!appUser.getPhoneNumber().equals("") &&
                    !appUser.getPhoneNumber().equals(regUser.getPhoneNumber())) {
                //request new verification text
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        appUser.getPhoneNumber(),
                        60,
                        TimeUnit.SECONDS,
                        AccountActivity.this,
                        mCallBacks
                );
            }

            if (!appUser.getEmailAddress().equals("") &&
                    !appUser.getEmailAddress().equals(regUser.getEmail()))
                //update email address
                regUser.updateEmail(appUser.getEmailAddress())
                        .addOnCompleteListener(sendRegistrationEmailListener(regUser))
                        .addOnFailureListener(emailUpdateFailedListener());
        }
    }

    private void displayUpdateResult() {
        String message;
        if (updateSuccess) {
            message = "Your details have been updated!";
        } else {
            message = "There was an error updating one or more of your details. Please try again.";
            //display will be updated by onDataChangeListener
            //so any data that hasn't been saved will display previous value
        }

        //display result of update
        Toast.makeText(
                AccountActivity.this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
    //methods to update user data in firebase//
    ////Methods////
    //endregion

    //region ////Event Listeners////
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

    private ValueEventListener onDataChangeListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mAuth.getUid() != null && !mAuth.getUid().equals("")) {
                    DataSnapshot child = dataSnapshot.child(mAuth.getUid());
                    if (child != null) {
                        Sponder acc = child.getValue(Sponder.class);
                        if (acc != null) {
                            String phoneNumber = acc.getPhoneNumber();
                            String emailAddress = acc.getEmailAddress();
                            String firstName = acc.getFirstName();
                            String surname = acc.getSurname();
                            String displayName = acc.getDisplayName();
                            String photoName = acc.getAccountPhotoName();
                            String accountPhotoNames = acc.getAccountPhotoNames();
                            String status = acc.getStatus();
                            long lastOnline = acc.getLastOnline();
                            boolean isComplete = acc.isComplete();
                            String token = acc.getToken();

                            appUser = new Sponder(
                                    phoneNumber, emailAddress,
                                    firstName, surname, displayName,
                                    photoName, accountPhotoNames, status,
                                    lastOnline, isComplete, token);

                            //get current user signed into app
                            //load first screen or redirect to login
                            updateDisplay(appUser);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private View.OnClickListener selectImageFromCameraOrGallery() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(AccountActivity.this);
                builder.setTitle("Add new account photo from:");
                final String[] optionItems = {OPTION_CAMERA, OPTION_GALLERY, OPTION_CANCEL};
                builder.setItems(optionItems, chooseImageLoadMethod(optionItems));
                builder.show();
            }
        };
    }

    private DialogInterface.OnClickListener chooseImageLoadMethod(final String[] optionItems) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (optionItems[i].equals(OPTION_CAMERA)) {
                    if (storagePermissionGranted()) {
                        //intent to open device camera
                        Intent cameraIntent =
                                new Intent(
                                        MediaStore.ACTION_IMAGE_CAPTURE
                                );

                        //target folder for image to be saved
                        File imagesFolder =
                                new File(
                                        Environment.getExternalStoragePublicDirectory(
                                                Environment.DIRECTORY_PICTURES),
                                        getString(R.string.app_name)
                                );

                        //used to track whether path existence is progressing as planned
                        //true and checked on each new create, only set false when a create fails
                        boolean success = true;
                        if (!imagesFolder.exists()) {
                            //create directory if it doesn't already exist
                            success = imagesFolder.mkdirs();
                        }
                        if (success) { //folder exists at this point
                            //create file with unique id, inside folder
                            File image = createFileAtUniquePath(imagesFolder);
                            try {
                                success = image.createNewFile();
                                if (success) {

                                    Uri u = getImageContentUri(getApplicationContext(), image.getAbsolutePath());

//                                    //use authority for AndroidManifest to create permission
//                                    //to get uri from temporary file in app
//                                    String fileProviderAuthority = getApplicationContext().getPackageName() +
//                                            getString(R.string.authorities_fileprovider);
//                                    Uri uriSavedImage = FileProvider.getUriForFile(
//                                            getApplicationContext(),
//                                            fileProviderAuthority,
//                                            image
//                                    );

                                    //pass URI to intent so it will be available in activity result
                                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, u);
                                    //grant read/write permissions with file
                                    cameraIntent.addFlags(
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                    );
                                    startActivityForResult(cameraIntent, REQUEST_CAMERA);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else if (optionItems[i].equals(OPTION_GALLERY)) {
                    Intent pickIntent =
                            new Intent(
                                    Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            );
                    pickIntent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(pickIntent, "Select Image"),
                            SELECT_FILE
                    );
                } else if (optionItems[i].equals(OPTION_CANCEL)) {
                    //hide ui
                    dialogInterface.dismiss();
                }
            }
        };
    }

    private View.OnClickListener clearAccountPhoto() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!appUser.usesDefaultPhoto()) {
                    //user must confirm removal of image
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(AccountActivity.this);
                    builder.setMessage(
                            "Are you sure you want to remove your profile picture?" +
                                    "\nThis cannot be undone.")
                            .setPositiveButton("Yes", confirmDeleteAccountPhoto())
                            .setNegativeButton("No", confirmDeleteAccountPhoto())
                            .show();
                }
            }
        };

    }

    private DialogInterface.OnClickListener confirmDeleteAccountPhoto() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int option) {
                //confirms removal
                if (option == DialogInterface.BUTTON_POSITIVE) {
                    //indicate whether default image was already being used
                    if (!img_account.getDrawable().equals(DRAWABLE_DEFAULT_PHOTO)) {
                        appAccountPhotoChanged = true;
                        newAccountPhotoUri = null;
                        newAccountPhotoFileName = "";
                        //set to default for no account image
                        img_account.setImageDrawable(DRAWABLE_DEFAULT_PHOTO);
                    }
                }

                dialogInterface.dismiss();
            }
        };
    }

    //generic cannot-be-blank listener
    private View.OnFocusChangeListener validateInput() {
        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    //get layout that contains text field
                    TextInputLayout layout =
                            (TextInputLayout) view.getParent().getParent();
                    if (((TextInputEditText) view).getText().toString().trim().equals("")) {
                        //display error message
                        layout.setError(layout.getHint() + " " +
                                getString(R.string.account_input_not_empty_error));
                        //disable button until criteria added
                        btn_save.setEnabled(false);
                        //set focus to blank field
                    } else {
                        //hide error message
                        layout.setErrorEnabled(false);
                        //(re)enable button when criteria met
                        btn_save.setEnabled(true);
                    }
                }
            }
        };
    }

    private View.OnFocusChangeListener validateDisplayName() {
        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                //ensure there is always a value in field as focus leaves
                if (!hasFocus) {
                    if (((TextInputEditText) view).getText().toString().equals("")) {
                        if (!etxt_yourName.getText().toString().equals("")) {
                            ((TextInputEditText) view).setText(etxt_yourName.getText());
                        } else if (mAuth.getCurrentUser() != null) {
                            ((TextInputEditText) view).setText(mAuth.getCurrentUser().getDisplayName());
                        }
                    }
                }
            }
        };
    }

    private View.OnFocusChangeListener validateEmailAndPhone() {
        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (etxt_email.getText().toString().equals("") &&
                        etxt_phone.getText().toString().equals("")) {
                    Toast.makeText(
                            getApplicationContext(),
                            etxt_phone.getHint() + " and " + etxt_email.getHint() + "" +
                                    getString(R.string.account_input_not_both_empty_error),
                            Toast.LENGTH_SHORT
                    ).show();
                    //disable button until criteria added
                    btn_save.setEnabled(false);
                } else {
                    //(re)enable button when criteria met
                    btn_save.setEnabled(true);
                }
            }
        };
    }

    private View.OnClickListener updateUserAccount() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValid()) {
                    appUser = getUserAccountFromControls(appUser);

                    if (mAuth != null && mAuth.getUid() != null) {
                        //flagged as true until an error occurs
                        //any error will set as false
                        updateSuccess = true;
                        //reset following any previous attempts
                        emailAttemptCount = 0;

                        //check if photo has been updated
                        if (appAccountPhotoChanged &&
                                !newAccountPhotoFileName.equals("")) {
                            //save profile picture to firebase storage
                            writeAccountPhotoToStorage();
                        }

                        //update all user profile data
                        writeUserAccountToDB();
                        //update information associated with user registration
                        writeRegDataToDB();

                        //indicate whether or not the update has been successful
                        displayUpdateResult();
                    }

                }
            }
        };
    }

    private OnSuccessListener<Uri> fileFoundListener() {
        return new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri fileUri) {
                Glide.with(getApplicationContext())
                        .load(fileUri)
                        .placeholder(R.drawable.no_account_photo)
                        .into(img_account);
            }
        };
    }

    private OnFailureListener fileNotFoundListener() {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //set to false on error
                //will display error for account update as a whole
                updateSuccess = false;
            }
        };
    }

    private OnFailureListener accountUpdateFailedListener() {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //set to false on error
                //will display error for account update as a whole
                updateSuccess = false;
            }
        };
    }

    private OnFailureListener profileUpdateFailedListener() {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //set to false on error
                //will display error for account update as a whole
                updateSuccess = false;
            }
        };
    }

    private OnFailureListener imageUploadFailedListener() {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //set to false on error
                //will display error for account update as a whole
                updateSuccess = false;
            }
        };
    }

    private OnCompleteListener<UploadTask.TaskSnapshot> imageUploadedListener() {
        return new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                //file successfully uploaded to storage
                //update user account to point to new file
                appUser.setAccountPhotoName(newAccountPhotoFileName, true);
                //also adds file name to list of all used account photos

                //called here so that async method still updates list of photo names
                writeUserAccountToDB();
            }
        };
    }

    private OnCompleteListener<Void> sendRegistrationEmailListener(final FirebaseUser regUser) {
        return new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    emailAttemptCount = 1; //first attempt
                    regUser.sendEmailVerification()
                            .addOnFailureListener(registrationEmailSendFailedListener(regUser));
                } else {
                    updateSuccess = false;
                    emailAttemptCount = 0;
                }
            }
        };
    }

    private OnFailureListener registrationEmailSendFailedListener(final FirebaseUser regUser) {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (emailAttemptCount < MAX_EMAIL_ATTEMPTS) {
                    regUser.sendEmailVerification()
                            .addOnFailureListener(registrationEmailSendFailedListener(regUser));
                    emailAttemptCount++;
                } else {
                    updateSuccess = false;
                    emailAttemptCount = 0;
                }

            }
        };
    }

    private OnFailureListener emailUpdateFailedListener() {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                updateSuccess = false;
            }
        };
    }
    //endregion ////Event Listeners////
}