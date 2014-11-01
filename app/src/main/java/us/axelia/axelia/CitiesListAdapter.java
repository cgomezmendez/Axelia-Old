package us.axelia.axelia;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mac on 1/11/14.
 */
public class CitiesListAdapter extends BaseAdapter implements ListAdapter {
    private List<Location> mLocations;
    private Activity mContext;
    private static final String LOG_TAG = CitiesListAdapter.class.getSimpleName();

    public CitiesListAdapter(List<Location> locations, Activity context) {
        this.mLocations = locations;
        mContext = context;
    }

    public CitiesListAdapter(Activity context) {
        mLocations = new ArrayList<Location>();
        mContext = context;

    }

    @Override
    public int getCount() {
        return mLocations.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView==null) {
            LayoutInflater inflater = mContext.getLayoutInflater();
            rowView = inflater.inflate(R.layout.location_row, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.cityName = (TextView) rowView.findViewById(R.id.location_name_row);
            rowView.setTag(viewHolder);
        }
        final ViewHolder holder = (ViewHolder) rowView.getTag();
        final String locationName = mLocations.get(position).getName();
        holder.cityName.setText(locationName);
        rowView.setClickable(true);
        rowView.setTag(R.id.list_item,mLocations.get(position));
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Location location = (Location) view.getTag(R.id.list_item);
                if (BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, location.getName());
                }
                Intent locationDetailIntent = new Intent(mContext, LocationDetailActivity.class);
                locationDetailIntent.putExtra("id", location.getId());
                locationDetailIntent.putExtra("name", location.getName());
                locationDetailIntent.putExtra("alertMessage", location.getAlertMessage());
                mContext.startActivity(locationDetailIntent);
            }
        });
        return rowView;
    }

    public static class ViewHolder {
        TextView cityName;
        int position;
    }
}
