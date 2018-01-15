package com.marcuthh.respond;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ContactsFragment extends Fragment {


    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 65636;

    private static final String TAG = "ContactsFragment";

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //...
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) !=
                        PackageManager.PERMISSION_GRANTED) {
            //consent required from user to allow access to contact list
            requestPermissions(
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
            //wait for callback in onRequestPermissionsResult() to determine display
        } else {
            //android version is low enough that permission is granted as default
            displayContactsList(rootView);
        }

        return rootView;
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

    public ArrayList<PhoneContact> getContacts() {
        ArrayList<PhoneContact> phoneContacts = new ArrayList<>();

        //return all contacts from phone
        Cursor cursor = null;
        ContentResolver resolver = getActivity().getContentResolver();
        try {
            cursor = resolver.query(
                    ContactsContract.Contacts.CONTENT_URI, null,
                    null, null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        } catch (Exception ex) {
            Log.e("Error on contact", ex.getMessage());
        }

        //contact record(s) returned
        if (cursor != null && cursor.getCount() > 0) {
            //loop all contacts returned
            while (cursor.moveToNext()) {
                //contact data
                String contactId = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String displayName = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                //lists used to capture all contact details
                //only filtered if account already exists for any one number and/or email
                String[] phoneNumbers = {};
                String[] emailAddresses = {};

                //check contact has at least phone number associated
                int hasPhone = Integer.parseInt(cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                if (hasPhone > 0) {
                    //get one or more numbers associated
                    phoneNumbers = getContactPhoneNumbers(contactId, resolver);
                }
                //get any email addresses associated with contact
                emailAddresses = getContactEmailAddresses(contactId, resolver);

                if (phoneNumbers.length > 0 || emailAddresses.length > 0) {
                    //only display if there is at least one form of contact
                    phoneContacts.add(new PhoneContact(displayName, phoneNumbers, emailAddresses));
                }
            }

            //close access to recordset
            cursor.close();
        }

        return phoneContacts;
    }

    @NonNull
    private String[] getContactPhoneNumbers(String contactId, ContentResolver resolver) {
        ArrayList<String> numbersList = new ArrayList<String>();

        //filter by phone numbers associated to this contact
        Cursor phones = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactId},
                null
        );

        if (phones != null && phones.getCount() > 0) {
            while (phones.moveToNext()) {
                if (phones.getInt(
                        phones.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.TYPE)) ==
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                    numbersList.add(phones.getString(
                            phones.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.DATA)
                    ).replaceAll("\\s", ""));
                }
            }
        }
        phones.close();

        return numbersList.toArray(new String[numbersList.size()]);
    }

    @NonNull
    private String[] getContactEmailAddresses(String contactId, ContentResolver resolver) {
        ArrayList<String> emailsList = new ArrayList<String>();

        //filter by phone numbers associated to this contact
        Cursor emails = resolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                new String[]{contactId},
                null
        );

        if (emails != null && emails.getCount() > 0) {
            while (emails.moveToNext()) {
                String address = emails.getString(
                        emails.getColumnIndex(
                                ContactsContract.CommonDataKinds.Email.DATA));
                if (!address.equals("")) {
                    emailsList.add(address.replaceAll("\\s", ""));
                }
            }
        }
        emails.close();

        return emailsList.toArray(new String[emailsList.size()]);
    }

    public void displayContactsList(View rootView) {
        ListView listviewContacts = (ListView) rootView.findViewById(R.id.listview_Contacts);
        ContactsAdapter adapter =
                new ContactsAdapter(getActivity(), getContacts());
        listviewContacts.setAdapter(adapter);
    }
}
