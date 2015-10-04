package si.ncode.birthdayreminder;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        RecyclerView upcoming_birthdays = (RecyclerView) findViewById(R.id.upcoming_birthdays);
        TextView no_upcoming_birthdays = (TextView) findViewById(R.id.no_events);

        toolbar.setTitle(getString(R.string.list_title));
        setSupportActionBar(toolbar);

        ContactRepository contact_repo = new ContactRepository(this);
        List<Contact> contacts = contact_repo.getContactsWithBirthdaysForNextDays(30);

        upcoming_birthdays.setHasFixedSize(true);
        upcoming_birthdays.setLayoutManager(new LinearLayoutManager(this));
        upcoming_birthdays.setAdapter(new ContactsAdapter(contacts, this));

        if (contacts.size() == 0)
        {
            upcoming_birthdays.setVisibility(View.GONE);
        }
        else
        {
            no_upcoming_birthdays.setVisibility(View.GONE);
        }

        AlarmReceiver alarm = new AlarmReceiver();
        alarm.setAlarm(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        NotificationManager notification_manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification_manager.cancelAll();
    }
}
