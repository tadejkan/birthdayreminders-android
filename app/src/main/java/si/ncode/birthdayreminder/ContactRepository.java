package si.ncode.birthdayreminder;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Tadej on 2.10.2015.
 */
public class ContactRepository {
    Context context = null;

    public ContactRepository(Context context)
    {
        this.context = context;
    }

    private Cursor getContactsBirthdays() {
        Uri uri = ContactsContract.Data.CONTENT_URI;

        String[] projection = new String[] {
                Build.VERSION.SDK_INT
                        >= Build.VERSION_CODES.HONEYCOMB ?
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                        ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Event.CONTACT_ID,
                ContactsContract.CommonDataKinds.Event.START_DATE,

        };

        String where =
                ContactsContract.Data.MIMETYPE + "=? AND " +
                ContactsContract.CommonDataKinds.Event.TYPE + "=" +
                        ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY;
        String[] selectionArgs = new String[] {
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
        };
        String sortOrder = null;
        return context.getContentResolver().query(uri, projection, where, selectionArgs, sortOrder);
    }

    public String getContactPhoto(String id)
    {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] {ContactsContract.Contacts.Photo.PHOTO_THUMBNAIL_URI}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } finally {
            cursor.close();
        }
        return null;
        /*
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.Data.CONTACT_ID + "=" + id + " AND "
                        + ContactsContract.Data.MIMETYPE + "='"
                        + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'", null,
                null);
        if (cursor != null) {
            if (!cursor.moveToFirst()) {
                return null;
            }
        } else {
            return null;
        }

        Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));
        return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);*/
    }
    public InputStream getContactPhotoStream(String id)
    {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return new ByteArrayInputStream(data);
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public List<Contact> getContactsWithBirthdays()
    {
        List<Contact> contacts = new ArrayList<Contact>();

        Cursor cursor = getContactsBirthdays();

        int id_idx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.CONTACT_ID);
        int name_idx = cursor.getColumnIndex(Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.HONEYCOMB ?
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                ContactsContract.Contacts.DISPLAY_NAME);
        int birthday_idx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE);

        Pattern pattern_1 = Pattern.compile("(.+)-(.+)-(.+)");
        Calendar today = Calendar.getInstance();

        while (cursor.moveToNext()) {
            String id = cursor.getString(id_idx);
            String name = cursor.getString(name_idx);
            String birthday_raw = cursor.getString(birthday_idx);

            Calendar birthday = null;

            Matcher matcher_1 = pattern_1.matcher(birthday_raw);
            if (matcher_1.matches())
            {
                if (matcher_1.groupCount() == 3)
                {
                    try {
                        birthday = Calendar.getInstance();
                        birthday.set(
                                matcher_1.group(1).equals("-") ? birthday.get(Calendar.YEAR) : Integer.parseInt(matcher_1.group(1)),
                                Integer.parseInt(matcher_1.group(2)) - 1,
                                Integer.parseInt(matcher_1.group(3))
                        );
                    }
                    catch (Exception ex)
                    { }
                }
            }

            if (birthday == null) continue;

            Calendar next_birthday = Calendar.getInstance();
            next_birthday.set(next_birthday.get(Calendar.YEAR), birthday.get(Calendar.MONTH), birthday.get(Calendar.DAY_OF_MONTH));
            if (next_birthday.before(today)) next_birthday.add(Calendar.YEAR, 1);

            contacts.add(new Contact(id, name, birthday, next_birthday));
        }

        Collections.sort(contacts, new Comparator<Contact>() {
            public int compare(Contact item1, Contact item2) {
                return item1.next_birthday.compareTo(item2.next_birthday);
            }
        });

        return contacts;
    }

    public List<Contact> getContactsWithBirthdaysForNextDays(int days)
    {
        List<Contact> contacts = getContactsWithBirthdays();
        List<Contact> selected_contacts = new ArrayList<Contact>();

        Calendar next_days = Calendar.getInstance();
        next_days.add(Calendar.DAY_OF_MONTH, days);

        for (int i=0; i<contacts.size(); i++) {
            if (contacts.get(i).next_birthday.before(next_days)) {
                selected_contacts.add(contacts.get(i));
            }
        }

        return selected_contacts;
    }
}
