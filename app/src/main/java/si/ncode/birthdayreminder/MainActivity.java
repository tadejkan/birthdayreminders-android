package si.ncode.birthdayreminder;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static boolean is_open = false;
    
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    
    RecyclerView upcoming_birthdays;
    TextView no_upcoming_birthdays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        upcoming_birthdays = findViewById(R.id.upcoming_birthdays);
        no_upcoming_birthdays = findViewById(R.id.no_events);

        toolbar.setTitle(getString(R.string.list_title));
        setSupportActionBar(toolbar);
    
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{ Manifest.permission.READ_CONTACTS }, PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        else {
            reloadContacts();
        }

        AlarmReceiver alarm = new AlarmReceiver();
        //alarm.setAlarm(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        is_open = true;

        NotificationManager notification_manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification_manager.cancelAll();
    }

    @Override
    protected void onPause() {
        super.onPause();

        is_open = false;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                reloadContacts();
            } else {
                Toast.makeText(this, "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    protected void reloadContacts() {
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
    }
}
