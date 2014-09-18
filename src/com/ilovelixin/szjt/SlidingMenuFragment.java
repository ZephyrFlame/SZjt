package com.ilovelixin.szjt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SlidingMenuFragment extends ListFragment
{
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
        return inflater.inflate(R.layout.list_sliding, null);
    }
    
    public void onActivityCreated(Bundle savedInstanceState) 
    {
        super.onActivityCreated(savedInstanceState);

        SimpleAdapter adapter = new SimpleAdapter(getActivity());
        adapter.add(new SimpleItem(getString(R.string.menu_setting), R.drawable.menu_settings));
        adapter.add(new SimpleItem(getString(R.string.menu_browser), R.drawable.menu_browser));
        adapter.add(new SimpleItem(getString(R.string.menu_help), R.drawable.menu_help));
        adapter.add(new SimpleItem(getString(R.string.menu_update), R.drawable.menu_download));
        adapter.add(new SimpleItem(getString(R.string.menu_exit), R.drawable.menu_exit));

        setListAdapter(adapter);
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) 
    {
        MainActivity activity = (MainActivity)getActivity();
        activity.getSlidingMenu().toggle();
        
        Intent intent;
        switch (position)
        {
            case 0:
                intent = new Intent(activity, MainSettingsActivity.class);  
                startActivity(intent);  
                break;
                
            case 1:
                break;
                
            case 2:
                break;
                
            case 3:
                break;
                
            case 4:
                activity.finish();
                break;
                
            default:
                super.onListItemClick(l, v, position, id);
                break;
        }
    }
    
    private class SimpleItem 
    {
        public String tag;
        public int iconRes;
        public SimpleItem(String tag, int iconRes) 
        {
            this.tag = tag; 
            this.iconRes = iconRes;
        }
    }

    public class SimpleAdapter extends ArrayAdapter<SimpleItem> 
    {
        public SimpleAdapter(Context context) 
        {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent) 
        {
            if (convertView == null) 
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_sliding, null);
            }
            ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
            icon.setImageResource(getItem(position).iconRes);
            TextView title = (TextView) convertView.findViewById(R.id.row_title);
            title.setText(getItem(position).tag);

            return convertView;
        }

    }
}
