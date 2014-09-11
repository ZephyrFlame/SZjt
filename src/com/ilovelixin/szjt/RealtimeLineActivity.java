
package com.ilovelixin.szjt;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
//import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RealtimeLineActivity extends Activity 
{
    private final int NETWORK_DATA_OK = 0;
    private final int NETWORK_DATA_FAIL = 1;
    
    private String mGuid;
    private Handler mHandler;
    private List<StationInfo> mStations;
    private TextView mTipTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_line);
        
        Intent intent = getIntent();
        mGuid = intent.getStringExtra("guid");
        String title = intent.getStringExtra("title");
        
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(title);
        
        forceShowOverflowMenu();
        
        mStations = new ArrayList<StationInfo>();
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
        
        startRefreshLineInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.realtime_line, menu);
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
    
    private void startRefreshLineInfo()
    {
        Thread thread = new Thread(
                new Runnable()
                {
                    public void run()
                    {
                        String httpRet = HttpHelper.GetStationLineInfo(mGuid, true);
                        if (httpRet != null && HttpHelper.ParseLineStations(httpRet, mStations) > 0)
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
    
    private void UpdateListView()
    {
        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(new LineStationsAdapter(this));
        listView.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
            {
                //Toast.makeText(getApplicationContext(), "arg2:" + arg2 + " arg3: " + arg3, 1000).show();
                Intent intent = new Intent();
                intent.putExtra("code", mStations.get(arg2).Code);
                intent.putExtra("title", mStations.get(arg2).Name);
                intent.setClass(RealtimeLineActivity.this, RealtimeStationActivity.class);
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
    }
    
    public class LineStationsAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;

        public LineStationsAdapter(Context context)
        {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() 
        {
            return mStations.size();
        }

        @Override
        public Object getItem(int position) 
        {
            return null;
        }

        @Override
        public long getItemId(int position) 
        {
            return position;
        }

        @Override
        public View getView(final int arg0, View arg1, ViewGroup arg2) 
        {
            ViewHolder holder = null;
            if (arg1 == null)
            {
                arg1 = mInflater.inflate(R.layout.one_line_two_text, null);
                holder = new ViewHolder();
                holder.title = (TextView)arg1.findViewById(R.id.textMain);
                holder.summary = (TextView)arg1.findViewById(R.id.textSub);
                arg1.setTag(holder);
            }
            else
            {
                holder = (ViewHolder)arg1.getTag();
            }
            
            holder.title.setText(mStations.get(arg0).Name);
            if (mStations.get(arg0).Time != null && mStations.get(arg0).Time.length() > 0)
            {
                holder.summary.setText(" " + mStations.get(arg0).Time + " ");
                holder.summary.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.summary.setVisibility(View.GONE);
            }

            return arg1;
        }
    }
}
