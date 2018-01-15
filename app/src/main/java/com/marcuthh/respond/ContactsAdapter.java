package com.marcuthh.respond;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends BaseAdapter {
    //----------------< Adapter_for_Android_Contacts() >----------------
//< Variables >
    Context mContext;
    List<PhoneContact> mList_Android_Contacts;
//</ Variables >

    //< constructor with ListArray >
    public ContactsAdapter(Context mContext, ArrayList<PhoneContact> mContact) {
        this.mContext = mContext;
        this.mList_Android_Contacts = mContact;
    }
//</ constructor with ListArray >

    @Override
    public int getCount() {
        return mList_Android_Contacts.size();
    }

    @Override
    public Object getItem(int position) {
        return mList_Android_Contacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //----< show items >----
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(mContext, R.layout.contact_items, null);
//< get controls >
        TextView textview_contact_Name = (TextView) view.findViewById(R.id.textview_android_contact_name);
        TextView textview_contact_TelefonNr = (TextView) view.findViewById(R.id.textview_android_contact_phoneNr);
        TextView textview_contact_email = (TextView) view.findViewById(R.id.textview_android_contact_email);
//</ get controls >

//< show values >
        //instance for contact object at current index
        PhoneContact contact = mList_Android_Contacts.get(position);

        textview_contact_Name.setText(contact.getContactName());

        //show primary number and ... to indicate further numbers
        if (contact.hasContactNo()) {
            String numberString = contact.getContactNumbers()[0];
            if (contact.getContactNumbers().length > 1) {
                numberString += "\n...";
            }
            textview_contact_TelefonNr.setText(numberString);
        }

        //show primary email and ... to indicate further addresses
        if (contact.hasContactEmail()) {
            String emailString = contact.getContactEmails()[0];
            if (contact.getContactEmails().length > 1) {
                emailString = "\n...";
            }
            textview_contact_email.setText(emailString);
        }
//</ show values >

        view.setTag(mList_Android_Contacts.get(position).getContactName());
        return view;
    }
}
//----</ show items >----
//----------------</ Adapter_for_Android_Contacts() >----------------