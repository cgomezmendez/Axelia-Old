package us.axelia.axelia;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Created by Cristian on 10/3/2014.
 */
public class AboutListAdapter extends BaseAdapter {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;
    private static final int TYPE_HTML = 2;
    private static final int TYPE_MAX_COUNT = TYPE_HTML + 1;
    private Context mContext;

    private ArrayList<String> mData = new ArrayList<String>();
    private LayoutInflater mInflater;

    private TreeSet<Integer> mSeparatorsSet = new TreeSet<Integer>();
    private TreeSet<Integer> mHtmlsSet = new TreeSet<Integer>();

    public AboutListAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(final String item) {
        mData.add(item);
        notifyDataSetChanged();
    }

    public void addSeparatorItem(final String item) {
        mData.add(item);
        mSeparatorsSet.add(mData.size() - 1);
        notifyDataSetChanged();
    }

    public void addHtmlItem(final String item) {
        mData.add(item);
        mHtmlsSet.add(mData.size() - 1);
        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        if (mSeparatorsSet.contains(position)) {
            return TYPE_SEPARATOR;
        } else if (mHtmlsSet.contains(position)) {
            return TYPE_HTML;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int type = getItemViewType(position);
        String text = (String) mData.get(position);
        if (convertView == null) {
            holder = new ViewHolder();
            switch (type) {
                case TYPE_ITEM:
                    convertView = mInflater.inflate(R.layout.about_list_row, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.text_about_row);
                    holder.textView.setText(text);
                    if (text.contains("axeliatransito@gmail.com")) {
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, 8, 0, 0);
                        holder.textView.setLayoutParams(lp);
                    }
                    break;
                case TYPE_SEPARATOR:
                    convertView = mInflater.inflate(R.layout.title_about_section, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.title_about);
                    holder.textView.setText(text);
                    break;
                case TYPE_HTML:
                    convertView = mInflater.inflate(R.layout.title_about_section, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.title_about);
                    holder.textView.setText(Html.fromHtml(text));
                    holder.textView.setClickable(true);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(0, 0, 0, 20);
                    holder.textView.setLayoutParams(lp);
                    holder.textView.setMovementMethod(LinkMovementMethod.getInstance());
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        return convertView;
    }

    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        return false;
    }

    public static class ViewHolder {
        public TextView textView;
    }
}
