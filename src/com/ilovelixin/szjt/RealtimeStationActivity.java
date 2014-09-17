
package com.ilovelixin.szjt;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.format.DateUtils;
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
import android.widget.Toast;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RealtimeStationActivity extends Activity 
{
    private final int NETWORK_DATA_OK = 0;
    private final int NETWORK_DATA_FAIL = 1;
    private final int TIMEOUT = 1000;
    
    private String mCode;
    private String mTitle;
    private String mSummary;
    private Handler mHandler;
    private List<StationLineInfo> mLines;
    private PullToRefreshListView mListView;
    private TextView mTipTextView;
    private Dialog mDialog;
    private LineStationsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_station);
        
        Intent intent = getIntent();
        mCode = intent.getStringExtra("code");
        mTitle = intent.getStringExtra("title");
        mSummary = intent.getStringExtra("summary");
        
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(mTitle);
        
        forceShowOverflowMenu();
        
        mLines = new ArrayList<StationLineInfo>();
        mTipTextView = (TextView)findViewById(R.id.textHint);
       
        mHandler = new Handler()
        {
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);
                
                if (mDialog != null) 
                {
                    mDialog.dismiss();
                }
                
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
        getMenuInflater().inflate(R.menu.realtime_station, menu);
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
            
        case R.id.action_favorite:
            if (!DataProvider.getInstance().isInFaverate(mCode))
            {
                if (mSummary == null || mSummary.length() == 0)
                {
                    mSummary = getString(R.string.tip_no_position);
                }
                DataProvider.getInstance().updateFaverateData(mTitle, FaverateData.STATION, mCode, mSummary);
            }
            Toast.makeText(RealtimeStationActivity.this, getString(R.string.toast_added_star), TIMEOUT).show();
            return true;
            
        case R.id.action_refresh:
            startRefreshLineInfo();
            showLoadingDialog();
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
                        String httpRet = HttpHelper.GetStationLineInfo(mCode, false);
                        if (httpRet != null && HttpHelper.ParseStationLines(httpRet, mLines) > 0)
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
        mAdapter = new LineStationsAdapter(this);
        
        mListView = (PullToRefreshListView) findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
            {
                //Toast.makeText(getApplicationContext(), "arg2:" + arg2 + " arg3: " + arg3, 1000).show();
                Intent intent = new Intent();
                intent.putExtra("guid", mLines.get(arg2).Guid);
                intent.putExtra("title", mLines.get(arg2).Name);
                intent.putExtra("summary", mLines.get(arg2).Summary);
                intent.setClass(RealtimeStationActivity.this, RealtimeLineActivity.class);
                startActivity(intent);
            }
        });
        mListView.setOnRefreshListener(new OnRefreshListener<ListView>() 
                {
                    @Override
                    public void onRefresh(PullToRefreshBase<ListView> refreshView) 
                    {
                        String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                        // Update the LastUpdatedLabel
                        refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

                        // Do work to refresh the list here.
                        new GetDataTask().execute();
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
    
    private void showLoadingDialog()
    {
        if (mDialog != null) 
        {
            mDialog.cancel();
        }
        mDialog = LoadingDialog.createLoadingDialog(RealtimeStationActivity.this, getString(R.string.refreshing_wait));
        mDialog.show();
    }
    
    public final class ViewHolder
    {
        public TextView line;
        public TextView title;
        public TextView summary;
        public TextView distance;
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
            return mLines.size();
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
                arg1 = mInflater.inflate(R.layout.two_line_two_state, null);
                holder = new ViewHolder();
                holder.line = (TextView)arg1.findViewById(R.id.textName);
                holder.title = (TextView)arg1.findViewById(R.id.textMain);
                holder.summary = (TextView)arg1.findViewById(R.id.textSub);
                holder.summary.setTextColor(Color.parseColor("#FF4D4DFF"));
                holder.distance = (TextView)arg1.findViewById(R.id.textState);
                arg1.setTag(holder);
            }
            else
            {
                holder = (ViewHolder)arg1.getTag();
            }
            
            holder.line.setText(mLines.get(arg0).Name);
            holder.title.setText(mLines.get(arg0).Summary);
            if (mLines.get(arg0).Time != null && mLines.get(arg0).Time.length() > 0)
            {
                holder.summary.setText(mLines.get(arg0).Time);
                holder.distance.setText(mLines.get(arg0).Distance);
                holder.distance.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.summary.setText(getString(R.string.bus_not_start));
                holder.distance.setVisibility(View.INVISIBLE);
            }

            return arg1;
        }
    }
    
    private class GetDataTask extends AsyncTask<Void, Void, String>
    {
        //后台处理部分
        @Override
        protected String doInBackground(Void... params) 
        {
            String httpRet = HttpHelper.GetStationLineInfo(mCode, false);
            if (httpRet != null)
            {
                mLines.clear();
                HttpHelper.ParseStationLines(httpRet, mLines);
                
                return getString(R.string.tip_refresh_success);
            }
            else
            {
                return getString(R.string.tip_refresh_fail);
            }
        }

        //这里是对刷新的响应，可以利用addFirst（）和addLast()函数将新加的内容加到LISTView中
        //根据AsyncTask的原理，onPostExecute里的result的值就是doInBackground()的返回值
        @Override
        protected void onPostExecute(String result) 
        {
            //通知程序数据集已经改变，如果不做通知，那么将不会刷新mListItems的集合
            mAdapter.notifyDataSetChanged();
            // Call onRefreshComplete when the list has been refreshed.
            mListView.onRefreshComplete();
            
            Toast.makeText(RealtimeStationActivity.this, result, TIMEOUT).show();

            super.onPostExecute(result);
        }
    }
}
