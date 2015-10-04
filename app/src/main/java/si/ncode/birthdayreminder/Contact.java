package si.ncode.birthdayreminder;

import java.util.Calendar;

/**
 * Created by Tadej on 2.10.2015.
 */
public class Contact {
    public String id;
    public String name;
    public Calendar birthday;
    public Calendar next_birthday;

    public Contact()
    {
    }

    public Contact(String id, String name, Calendar birthday, Calendar next_birthday)
    {
        this.id = id;
        this.name = name;
        this.birthday = birthday;
        this.next_birthday = next_birthday;
    }
}
