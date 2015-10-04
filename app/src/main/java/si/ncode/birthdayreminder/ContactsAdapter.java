package si.ncode.birthdayreminder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Tadej on 2.10.2015.
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder>
{
    private List<Contact> dataset;
    private Context context;
    private ContactRepository contact_repository;

    private Bitmap cached_unknown_photo = null;
    private HashMap<String, Uri> cached_photo_uris = new HashMap<String, Uri>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon;
        public TextView line1;
        public TextView line2;
        public ViewHolder(View parent, ImageView icon, TextView line1, TextView line2) {
            super(parent);

            this.icon = icon;
            this.line1 = line1;
            this.line2 = line2;
        }
    }

    public ContactsAdapter(List<Contact> dataset, Context context) {
        this.dataset = dataset;
        this.context = context;

        this.contact_repository = new ContactRepository(context);
    }

    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_listview_item, parent, false);

        return new ViewHolder(
                view,
                (ImageView) view.findViewById(R.id.icon),
                (TextView) view.findViewById(R.id.title),
                (TextView) view.findViewById(R.id.shortcut)
        );
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat long_date_format = (SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.LONG);

        SimpleDateFormat date_format = new SimpleDateFormat(
                dataset.get(position).birthday.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) ?
                long_date_format.toLocalizedPattern().replace(",y", "").replace(", y", "").replace("y", "") :
                long_date_format.toLocalizedPattern()
        );

        holder.line1.setText(dataset.get(position).name);
        holder.line2.setText(date_format.format(dataset.get(position).birthday.getTime()));

        Uri photo = null;
        if (cached_photo_uris.containsKey(dataset.get(position).id))
        {
            photo = cached_photo_uris.get(dataset.get(position).id);
        }
        else
        {
            String photo_uri = contact_repository.getContactPhoto(dataset.get(position).id);
            photo = (photo_uri == null ? null : Uri.parse(photo_uri));
            cached_photo_uris.put(dataset.get(position).id, photo);
        }

        if (photo != null)
        {
            holder.icon.setImageURI(photo);
        } else
        {
            if (cached_unknown_photo == null)
            {
                cached_unknown_photo = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.ic_person_white);
            }
            holder.icon.setImageBitmap(cached_unknown_photo);
        }
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}