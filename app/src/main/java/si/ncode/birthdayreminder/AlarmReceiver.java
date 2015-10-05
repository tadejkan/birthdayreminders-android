package si.ncode.birthdayreminder;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Tadej on 2.10.2015.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (MainActivity.is_open) return; //don't show notifications if UI is currently shown

        ContactRepository contact_repo = new ContactRepository(context);
        List<Contact> contacts = contact_repo.getContactsWithBirthdaysForNextDays(7);

        showNotifications(context, contacts, contact_repo);
    }

    private void showNotifications(Context context, List<Contact> contacts, ContactRepository contact_repository)
    {
        Calendar today = Calendar.getInstance();

        if (contacts.size() == 0) return;

        PendingIntent pending_intent = PendingIntent.getActivity(
                context,
                -1,
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_cake_black_48dp)
                .setContentIntent(pending_intent)
                .setColor(context.getResources().getColor(R.color.colorPrimary));

        if (contacts.size() == 1)
        {
            Contact contact = contacts.get(0);

            String title = context.getString(R.string.single_notification_title);
            String text = contact.birthday.get(Calendar.YEAR) == today.get(Calendar.YEAR) ?
                    context.getString(R.string.single_notification_content_without_year, contact.name) :
                    context.getString(R.string.single_notification_content_with_year,
                            contact.name, (today.get(Calendar.YEAR) - contact.birthday.get(Calendar.YEAR)));

            builder.setContentTitle(title)
                .setContentText(text);

            builder.addPerson(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,
                    Long.parseLong(contact.id)).toString());
            InputStream photo_stream = contact_repository.getContactPhotoStream(contact.id);

            Bitmap photo_bmp = (photo_stream == null ?
                    BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_person_white) :
                    BitmapFactory.decodeStream(photo_stream)
            );

            builder.setLargeIcon(getCircleBitmap(photo_bmp));
        }
        else
        {
            NotificationCompat.InboxStyle inbox_style = new NotificationCompat.InboxStyle();
            inbox_style.setBigContentTitle(context.getString(R.string.group_notification_title));

            String content = "";
            int nr_of_lines = Math.min(5, contacts.size());
            for (int i=0; i<nr_of_lines; i++) {
                Contact contact = contacts.get(i);

                String text = contact.birthday.get(Calendar.YEAR) == today.get(Calendar.YEAR) ?
                        context.getString(R.string.group_notification_content_without_year, contact.name) :
                        context.getString(R.string.group_notification_content_with_year,
                                contact.name, (today.get(Calendar.YEAR) - contact.birthday.get(Calendar.YEAR)));
                inbox_style.addLine(text);

                content += contact.name;
                if (i < nr_of_lines - 1)
                {
                    content += ", ";
                }
            }
            if (contacts.size() > 5)
            {
                String text = context.getString(R.string.group_notification_more_content, contacts.size() - 5);

                inbox_style.setSummaryText(text);
                content += ", " + text;
            }

            builder.setContentTitle(context.getString(R.string.group_notification_title))
                .setContentText(content)
                    .setStyle(inbox_style);
        }

        NotificationManager notification_manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notification_manager.notify(1, builder.build());
    }

    public void setAlarm(Context context)
    {
        AlarmManager alarm_manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarm_intent = PendingIntent.getBroadcast(
                context,
                0,
                new Intent(context, AlarmReceiver.class),
                0
        );

        alarm_manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarm_intent);
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }
}
