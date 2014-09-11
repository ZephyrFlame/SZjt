
package com.ilovelixin.szjt;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
//import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SearchLinesActivity extends ListActivity 
{
    private final int NETWORK_DATA_OK = 0;
    private final int NETWORK_DATA_FAIL = 1;
    private final int TOAST_TIMEOUT = 1000;
    
    private Handler mHandler;
    private List<LineSummary> mLines;
    private TextView mTipTextView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_lines);
        
        Intent intent = getIntent();
        final String keyword = intent.getStringExtra("keyword");  
        
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(String.format(getString(R.string.searching_lines), keyword));
        
        forceShowOverflowMenu();
        
        mLines = new ArrayList<LineSummary>();
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
                        String httpRet = HttpHelper.SearchLines(keyword, true);
                        if (httpRet != null && HttpHelper.ParseLineInfo(httpRet, mLines) > 0)
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
                intent.putExtra("guid", mLines.get(arg2).Guid);
                intent.putExtra("title", mLines.get(arg2).Info);
                intent.setClass(SearchLinesActivity.this, RealtimeLineActivity.class);
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
            return mLines.size();
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
            
            final LineSummary line = mLines.get(arg0);
            
            holder.title.setText(line.Info);
            holder.summary.setText(line.Summary);
            if (DataProvider.getInstance().isInFaverate(line.Guid))
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
                            if (DataProvider.getInstance().isInFaverate(line.Guid))
                            {
                                DataProvider.getInstance().deleteFaverate(line.Guid);
                                Toast.makeText(mContext, getString(R.string.toast_removed_star), TOAST_TIMEOUT).show();
                            }
                            else
                            {
                                DataProvider.getInstance().updateFaverateData(line.Info, FaverateData.LINE, line.Guid, line.Summary);
                                Toast.makeText(mContext, getString(R.string.toast_added_star), TOAST_TIMEOUT).show();
                            }
                            
                            SearchLinesAdapter.this.notifyDataSetChanged();
                        }
                    });

            return arg1;
        }
    }
}
