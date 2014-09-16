
package com.ilovelixin.szjt;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.ilovelixin.szjt.SearchLinesActivity.SearchLinesAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SearchStationsActivity extends Activity 
{
    private final int NETWORK_DATA_OK = 0;
    private final int NETWORK_DATA_FAIL = 1;
    private final int TOAST_TIMEOUT = 1000;
    
    private Handler mHandler;
    private List<StationSummary> mStations;
    private TextView mTipTextView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_stations);
        
        Intent intent = getIntent();
        final String keyword = intent.getStringExtra("keyword");  
        
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(String.format(getString(R.string.searching_stations), keyword));
        
        //forceShowOverflowMenu();
        
        mStations = new ArrayList<StationSummary>();
        mTipTextView = (TextView)findViewById(R.id.textHint);
       
        mHandler = new Handler()
        {
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);
                
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                switch (msg.what)
                {
                    case NETWORK_DATA_OK:
                        mTipTextView.setVisibility(View.INVISIBLE);
                        UpdateListView();
                        break;
                        
                    case NETWORK_DATA_FAIL:
                        mTipTextView.setText(R.string.tip_no_result);
                        break;
                }
            }
        };
        
        Thread thread = new Thread(
                new Runnable()
                {
                    public void run()
                    {
                        String httpRet = HttpHelper.SearchLines(keyword, false);
                        if (httpRet != null && HttpHelper.ParseStationInfo(httpRet, mStations) > 0)
                        {
                            mHandler.sendEmptyMessageDelayed(NETWORK_DATA_OK, 100);
                        }
                        else
                        {
                            mHandler.sendEmptyMessageDelayed(NETWORK_DATA_FAIL, 100);
                        }
                    }
                }
            );
        thread.start();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.search_lines, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        // Respond to the action bar's Up/Home button
        case android.R.id.home:
            //NavUtils.navigateUpFromSameTask(this);        // if use this, ADD android:parentActivityName=".XXXActivity" IN Manifest
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void UpdateListView()
    {
        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(new SearchLinesAdapter(this));
        listView.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
            {
                StationSummary station = mStations.get(arg2);
                
                StringBuilder sb = new StringBuilder();
                if (station.District != null && station.District.length() > 0)
                {
                    sb.append(station.District);
                }
                if (station.Route != null && station.Route.length() > 0)
                {
                    if (sb.length() > 0)
                    {
                        sb.append("£¬");
                    }
                    sb.append(station.Route);
                }
                if (station.Side != null && station.Side.length() > 0)
                {
                    if (sb.length() > 0)
                    {
                        if (station.Side.length() > 1)
                        {
                            sb.append("£¬");
                        }
                        else
                        {
                            sb.append("£¬Â·");
                        }
                    }
                    sb.append(station.Side);
                }
                
                //Toast.makeText(getApplicationContext(), "arg2:" + arg2 + " arg3: " + arg3, 1000).show();
                Intent intent = new Intent();
                intent.putExtra("code", station.Code);
                intent.putExtra("title", station.Name);
                intent.putExtra("summary", sb.toString());
                intent.setClass(SearchStationsActivity.this, RealtimeStationActivity.class);
                startActivity(intent);
            }
        });
    }
    
    private void forceShowOverflowMenu() 
    {
        try 
        {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) 
            {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    public final class ViewHolder
    {
        public TextView title;
        public TextView summary;
        public ImageButton icon;
    }
    
    public class SearchLinesAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;
        private Context mContext;

        public SearchLinesAdapter(Context context)
        {
            mContext = context;
            mInflater = LayoutInflater.from(context);
        }
        
        @Override
        public int getCount() 
        {
            return mStations.size();
        }

        @Override
        public Object getItem(int arg0) 
        {
            return null;
        }

        @Override
        public long getItemId(int arg0) 
        {
            return 0;
        }

        @Override
        public View getView(final int arg0, View arg1, ViewGroup arg2) 
        {
            ViewHolder holder = null;
            if (arg1 == null)
            {
                arg1 = mInflater.inflate(R.layout.two_line_one_icon, null);
                holder = new ViewHolder();
                holder.title = (TextView)arg1.findViewById(R.id.textMain);
                holder.summary = (TextView)arg1.findViewById(R.id.textSub);
                holder.summary.setTextColor(Color.parseColor("#FF3F3F3F"));
                holder.icon = (ImageButton)arg1.findViewById(R.id.imageAction);
                arg1.setTag(holder);
            }
            else
            {
                holder = (ViewHolder)arg1.getTag();
            }
            
            final StationSummary station = mStations.get(arg0);
            
            StringBuilder sb = new StringBuilder();
            if (station.District != null && station.District.length() > 0)
            {
                sb.append(station.District);
            }
            if (station.Route != null && station.Route.length() > 0)
            {
                if (sb.length() > 0)
                {
                    sb.append("£¬");
                }
                sb.append(station.Route);
            }
            if (station.Side != null && station.Side.length() > 0)
            {
                if (sb.length() > 0)
                {
                    if (station.Side.length() > 1)
                    {
                        sb.append("£¬");
                    }
                    else
                    {
                        sb.append("£¬Â·");
                    }
                }
                sb.append(station.Side);
            }
            final String info = sb.toString();

            holder.title.setText(station.Name);
            holder.summary.setText(info);
            if (DataProvider.getInstance().isInFaverate(station.Code))
            {
                holder.icon.setImageResource(R.drawable.star_on);
            }
            else
            {
                holder.icon.setImageResource(R.drawable.star_off);
            }
            holder.icon.setOnClickListener(
                    new View.OnClickListener() 
                    {
                        public void onClick(View v) 
                        {
                            //Toast.makeText(mContext, "stared: " + arg0, 1000).show();
                            if (DataProvider.getInstance().isInFaverate(station.Code))
                            {
                                DataProvider.getInstance().deleteFaverate(station.Code);
                                Toast.makeText(mContext, getString(R.string.toast_removed_star), TOAST_TIMEOUT).show();
                            }
                            else
                            {
                                DataProvider.getInstance().updateFaverateData(station.Name, FaverateData.STATION, station.Code, info);
                                Toast.makeText(mContext, getString(R.string.toast_added_star), TOAST_TIMEOUT).show();
                            }
                            
                            SearchLinesAdapter.this.notifyDataSetChanged();
                        }
                    });

            return arg1;
        }
    }
}
