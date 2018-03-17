package org.waoss.oculus.apertor.util;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.waoss.oculus.apertor.Contact;
import org.waoss.oculus.apertor.R;

import java.util.List;

public class ContactListViewAdapter extends ArrayAdapter<Contact> {
    private LayoutInflater layoutInflater;
    private List<Contact> contactList;
    private SparseBooleanArray selectedItemIds;

    public ContactListViewAdapter(final Context context, final int resource, final List<Contact> contactList) {
        super(context, resource);
        this.contactList = contactList;
        layoutInflater = LayoutInflater.from(getContext());
        selectedItemIds = new SparseBooleanArray();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.contact_item, null);
            viewHolder.name = convertView.findViewById()
        }
    }

    private class ViewHolder {
        TextView name;
        TextView number;
    }
}
