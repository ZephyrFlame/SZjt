
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
import android.widget.AdapterView.OnItemClickListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SearchStationsActivity extends Activity 
{
    private final int NETWORK_DATA_OK = 0;
    private final int NETWORK_DATA_FAIL = 1;
    
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
        
        forceShowOverflowMenu();
        
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
        getMenuInflater().inflate(R.menu.search_lines, menu);
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
                //Toast.makeText(getApplicationContext(), "arg2:" + arg2 + " arg3: " + arg3, 1000).show();
                Intent intent = new Intent();
                intent.putExtra("code", mStations.get(arg2).Code);
                intent.putExtra("title", mStations.get(arg2).Name);
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
        //private Context mContext;

        public SearchLinesAdapter(Context context)
        {
            //mContext = context;
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
            
            StringBuilder sb = new StringBuilder();
            if (mStations.get(arg0).District != null && mStations.get(arg0).District.length() > 0)
            {
                sb.append(mStations.get(arg0).District);
            }
            if (mStations.get(arg0).Route != null && mStations.get(arg0).Route.length() > 0)
            {
                if (sb.length() > 0)
                {
                    sb.append("£¬");
                }
                sb.append(mStations.get(arg0).Route);
            }
            if (mStations.get(arg0).Side != null && mStations.get(arg0).Side.length() > 0)
            {
                if (sb.length() > 0)
                {
                    if (mStations.get(arg0).Side.length() > 1)
                    {
                        sb.append("£¬");
                    }
                    else
                    {
                        sb.append("£¬Â·");
                    }
                }
                sb.append(mStations.get(arg0).Side);
            }
            holder.title.setText(mStations.get(arg0).Name);
            holder.summary.setText(sb.toString());
            holder.icon.setImageResource(R.drawable.star_off);
            holder.icon.setOnClickListener(
                    new View.OnClickListener() 
                    {
                        public void onClick(View v) 
                        {
                            //Toast.makeText(mContext, "stared: " + arg0, 1000).show();
                        }
                    });

            return arg1;
        }
    }
}
