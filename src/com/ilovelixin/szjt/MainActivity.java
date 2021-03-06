
package com.ilovelixin.szjt;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.mobeta.android.dslv.DragSortListView;

public class MainActivity extends SlidingFragmentActivity implements ActionBar.TabListener 
{
    private final int MSG_UPDATE = 0;
    private final int TIMEOUT = 1000;

    private boolean mIsInited = false;
    protected ListFragment mFrag;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setBehindContentView(R.layout.menu_frame);
        if (savedInstanceState == null)
        {
            android.support.v4.app.FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
            mFrag = new SlidingMenuFragment();
            t.replace(R.id.menu_frame, mFrag);
            t.commit();
        } 
        else 
        {
            mFrag = (ListFragment)this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
        }
        
        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.35f);
        sm.setMode(SlidingMenu.RIGHT);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setTitle(R.string.app_title);
        
        // Init data provider for history and faverate
        DataProvider.initInstance(this);
        
        // Set default preference
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);  

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() 
        {
            @Override
            public void onPageSelected(int position) 
            {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) 
        {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String pagePreference = mySharedPreferences.getString(MainSettingsActivity.PRF_DEFAULT_PAGE_KEY, MainSettingsActivity.PRF_DEFAULT_PAGE_VALUE);
        actionBar.setSelectedNavigationItem(Integer.parseInt(pagePreference));
    }
    
    @Override  
    protected void onResume() 
    {  
        super.onResume();  
        
        if (mIsInited)
        {
            FaverateSectionFragment fragment = (FaverateSectionFragment)mSectionsPagerAdapter.getItem(2);
            if (fragment != null)
            {
                fragment.notifyUpdate();
            }
        }
    }
    
    @Override  
    protected void onPause() 
    {  
        super.onPause();  
        
        FaverateSectionFragment fragment = (FaverateSectionFragment)mSectionsPagerAdapter.getItem(2);
        if (fragment != null)
        {
            fragment.preLeavePage();
        }
        
        mIsInited = true;
    }

    @Override  
    protected void onDestroy() 
    {  
        super.onDestroy();  

        DataProvider.getInstance().closeInstance(); 
    } 

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override  
    public boolean onOptionsItemSelected(MenuItem item) 
    {  
        // TODO Auto-generated method stub  
        switch (item.getItemId())
        {
        case R.id.action_more:   
            //Intent intent = new Intent(this, MainSettingsActivity.class);  
            //startActivity(intent);  
            getSlidingMenu().showMenu();
            return true;  
        }
        
        return super.onOptionsItemSelected(item);
    }  

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) 
    {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        if (tab.getPosition() == 2)
        {
            InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE); 
            if (imm.isActive())
            {
                EditText textName = (EditText) this.findViewById(R.id.editLineName);
                imm.hideSoftInputFromWindow(textName.getWindowToken(), 0);
            }
        }
        
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) 
    {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) 
    {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter 
    {
        private Fragment mStationFragment;
        private Fragment mLineFragment;
        private Fragment mFaverateFragment;

        public SectionsPagerAdapter(FragmentManager fm) 
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) 
        {
            if (position == 0)
            {
                if (mStationFragment == null)
                {
                    mStationFragment = new StationSectionFragment();
                }
                return mStationFragment;
            }
            else if (position == 1)
            {
                if (mLineFragment == null)
                {
                    mLineFragment = new LineSectionFragment();
                }
                return mLineFragment;
            }
            else
            {
                if (mFaverateFragment == null)
                {
                    mFaverateFragment = new FaverateSectionFragment();
                }
                return mFaverateFragment;
            }
        }

        @Override
        public int getCount() 
        {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) 
        {
            Locale l = Locale.getDefault();
            switch (position) 
            {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    public class StationSectionFragment extends Fragment 
    {
        private Handler mHandler;
        private ListView mListView;
        private TextView mTextView;
        
        public StationSectionFragment() 
        {
            mHandler = new Handler()
            {
                public void handleMessage(Message msg)
                {
                    super.handleMessage(msg);
                    
                    switch (msg.what)
                    {
                        case MSG_UPDATE:
                            ((HistoryAdapter)mListView.getAdapter()).notifyDataSetChanged();
                            if (DataProvider.getInstance().getStationHistory().size() == 0)
                            {
                                mTextView.setVisibility(View.VISIBLE);
                                mListView.setVisibility(View.GONE);
                            }
                            else
                            {
                                mTextView.setVisibility(View.GONE);
                                mListView.setVisibility(View.VISIBLE);
                            }
                            break;
                    }
                }
            };
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
        {
            View rootView = inflater.inflate(R.layout.fragment_stations, container, false);
            mListView = (ListView) rootView.findViewById(R.id.listStationResult);
            mTextView = (TextView) rootView.findViewById(R.id.textStationResult);
            
            Button buttonGo = (Button) rootView.findViewById(R.id.buttonStationGo);
            buttonGo.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View arg0) 
                {
                    EditText textName = (EditText) getActivity().findViewById(R.id.editStationName);
                    
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 
                    imm.hideSoftInputFromWindow(textName.getWindowToken(), 0);
                    
                    String name = textName.getText().toString();
                    if (name == null || name.length() == 0)
                    {
                        Toast.makeText(getActivity(), R.string.toast_no_input, TIMEOUT).show();
                    }
                    else
                    {
                        startSearching(name);
                        
                        HistoryAdapter adp = (HistoryAdapter)mListView.getAdapter();
                        if (adp.getCount() > 0)
                        {
                            mTextView.setVisibility(View.GONE);
                            mListView.setVisibility(View.VISIBLE);
                        }
                        adp.notifyDataSetChanged();
                    }
                }
            });

            final List<HistoryData> stations = DataProvider.getInstance().getStationHistory();
            if (stations != null)
            {
                
                mListView.setAdapter(new HistoryAdapter(inflater, stations, false, mHandler));
                mListView.setOnItemClickListener(new OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
                    {
                        startSearching(stations.get(arg2).Name);

                        HistoryAdapter adp = (HistoryAdapter)mListView.getAdapter();
                        if (adp.getCount() > 0)
                        {
                            mTextView.setVisibility(View.GONE);
                            mListView.setVisibility(View.VISIBLE);
                        }
                        adp.notifyDataSetChanged();
                    }
                });
                
                if (stations.size() > 0)
                {
                    mTextView.setVisibility(View.GONE);
                }
                else
                {
                    mListView.setVisibility(View.INVISIBLE);
                }
            }
            
            return rootView;
        }
        
        private void startSearching(final String keyword)
        {
            DataProvider.getInstance().updateHistoryData(keyword, HistoryData.STATION, System.currentTimeMillis());
            
            Intent intent = new Intent();
            intent.putExtra("keyword", keyword);
            intent.setClass(MainActivity.this, SearchStationsActivity.class);
            startActivity(intent);
        }
    }
    

    public class LineSectionFragment extends Fragment 
    {
        private Handler mHandler;
        private ListView mListView;
        private TextView mTextView;
        
        public LineSectionFragment() 
        {
            mHandler = new Handler()
            {
                public void handleMessage(Message msg)
                {
                    super.handleMessage(msg);
                    
                    switch (msg.what)
                    {
                        case MSG_UPDATE:
                            ((HistoryAdapter)mListView.getAdapter()).notifyDataSetChanged();
                            if (DataProvider.getInstance().getLineHistory().size() == 0)
                            {
                                mTextView.setVisibility(View.VISIBLE);
                                mListView.setVisibility(View.GONE);
                            }
                            else
                            {
                                mTextView.setVisibility(View.GONE);
                                mListView.setVisibility(View.VISIBLE);
                            }
                            break;
                    }
                }
            };
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
        {
            View rootView = inflater.inflate(R.layout.fragment_lines, container, false);
            mListView = (ListView) rootView.findViewById(R.id.listLineResult);
            mTextView = (TextView) rootView.findViewById(R.id.textLineResult);

            Button buttonGo = (Button) rootView.findViewById(R.id.buttonLineGo);
            buttonGo.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View arg0) 
                {
                    EditText textName = (EditText) getActivity().findViewById(R.id.editLineName);
                    
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 
                    imm.hideSoftInputFromWindow(textName.getWindowToken(), 0);
                    
                    String name = textName.getText().toString();
                    if (name == null || name.length() == 0)
                    {
                        Toast.makeText(getActivity(), R.string.toast_no_input, TIMEOUT).show();
                    }
                    else
                    {
                        startSearching(name);

                        HistoryAdapter adp = (HistoryAdapter)mListView.getAdapter();
                        if (adp.getCount() > 0)
                        {
                            mTextView.setVisibility(View.GONE);
                            mListView.setVisibility(View.VISIBLE);
                        }
                        adp.notifyDataSetChanged();
                    }
                }
            });
            
            final List<HistoryData> lines = DataProvider.getInstance().getLineHistory();
            if (lines != null)
            {
                mListView.setAdapter(new HistoryAdapter(inflater, lines, true, mHandler));
                mListView.setOnItemClickListener(new OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
                    {
                        startSearching(lines.get(arg2).Name);

                        HistoryAdapter adp = (HistoryAdapter)mListView.getAdapter();
                        if (adp.getCount() > 0)
                        {
                            mTextView.setVisibility(View.GONE);
                            mListView.setVisibility(View.VISIBLE);
                        }
                        adp.notifyDataSetChanged();
                    }
                });
                
                if (lines.size() > 0)
                {
                    mTextView.setVisibility(View.GONE);
                }
                else
                {
                    mListView.setVisibility(View.INVISIBLE);
                }
            }
            
            return rootView;
        }
        
        private void startSearching(final String keyword)
        {
            DataProvider.getInstance().updateHistoryData(keyword, HistoryData.LINE, System.currentTimeMillis());
            
            Intent intent = new Intent();
            intent.putExtra("keyword", keyword);
            intent.setClass(MainActivity.this, SearchLinesActivity.class);
            startActivity(intent);
        }
    }
    
    public class FaverateSectionFragment extends Fragment 
    {
        private Handler mHandler;
        private TextView mTextView;
        private DragSortListView mListView;
        private FaverateAdapter mAdapter;
        
        private DragSortListView.DropListener mDropListener = new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) 
            {
                if (from != to)
                {
                    FaverateData item = (FaverateData) mAdapter.getItem(from);
    
                    mAdapter.notifyDataSetChanged();
                    mAdapter.remove(item);
                    mAdapter.insert(item, to);
                }
            }
        };

        private DragSortListView.RemoveListener mRemoveListener = new DragSortListView.RemoveListener() 
        {
            @Override
            public void remove(int which) 
            {
                mAdapter.remove((FaverateData) mAdapter.getItem(which));
            }
        };

        private DragSortListView.DragScrollProfile mDragScrollProfile = new DragSortListView.DragScrollProfile() 
        {
            @Override
            public float getSpeed(float w, long t) 
            {
                if (w > 0.8f) 
                {
                    // Traverse all views in a millisecond
                    return ((float) mAdapter.getCount()) / 0.001f;
                } 
                else 
                {
                    return 10.0f * w;
                }
            }
        };

        public FaverateSectionFragment() 
        {
            mHandler = new Handler()
            {
                public void handleMessage(Message msg)
                {
                    super.handleMessage(msg);
                    
                    switch (msg.what)
                    {
                        case MSG_UPDATE:
                            mAdapter.notifyDataSetChanged();
                            if (DataProvider.getInstance().getFaverate().size() == 0)
                            {
                                mTextView.setVisibility(View.VISIBLE);
                                mListView.setVisibility(View.GONE);
                            }
                            else
                            {
                                mTextView.setVisibility(View.GONE);
                                mListView.setVisibility(View.VISIBLE);
                            }
                            break;
                    }
                }
            };
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
        {
            View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
            
            mTextView = (TextView) rootView.findViewById(R.id.textFaverate);
            mListView = (DragSortListView) rootView.findViewById(R.id.listFaverate);
            mListView.setDropListener(mDropListener);
            mListView.setRemoveListener(mRemoveListener);
            mListView.setDragScrollProfile(mDragScrollProfile);

            final List<FaverateData> faverates = DataProvider.getInstance().getFaverate();
            if (faverates != null)
            {
                mAdapter = new FaverateAdapter(inflater, faverates, mHandler);
                
                mListView.setAdapter(mAdapter);
                mListView.setOnItemClickListener(new OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
                    {
                        FaverateData item = faverates.get(arg2);
                        Intent intent = new Intent();
                        if (item.Type == FaverateData.LINE)
                        {
                            intent.putExtra("guid", item.Keyword);
                            intent.putExtra("title", item.Name);
                            intent.putExtra("summary", item.Info);
                            intent.setClass(getBaseContext(), RealtimeLineActivity.class);
                        }
                        else
                        {
                            intent.putExtra("code", item.Keyword);
                            intent.putExtra("title", item.Name);
                            intent.putExtra("summary", item.Info);
                            intent.setClass(getBaseContext(), RealtimeStationActivity.class);
                        }
                        startActivity(intent);
                    }
                });
                
                if (faverates.size() > 0)
                {
                    mTextView.setVisibility(View.GONE);
                }
                else
                {
                    mListView.setVisibility(View.INVISIBLE);
                }
            }

            return rootView;
        }
        
        public void notifyUpdate()
        {
            mAdapter.notifyDataSetChanged();
            if (DataProvider.getInstance().getFaverate().size() == 0)
            {
                mTextView.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
            }
            else
            {
                mTextView.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
            }
        }
        
        public void preLeavePage()
        {
            if (mAdapter.isChanged())
            {
                DataProvider.getInstance().saveFaverate();
            }
        }
    }
    
    public final class ViewHolder
    {
        public TextView title;
        public TextView summary;
        public ImageButton icon;
    }
    
    public class HistoryAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;
        private List<HistoryData> mHistoryData;
        private boolean mIsLine;
        private Handler mHandler;

        public HistoryAdapter(LayoutInflater inflater, List<HistoryData> list, boolean is_line, Handler handler)
        {
            //mContext = context;
            mInflater = inflater;
            mHistoryData = list;
            mIsLine = is_line;
            mHandler = handler;
        }
        
        @Override
        public int getCount() 
        {
            return mHistoryData.size();
        }

        @Override
        public Object getItem(int arg0) 
        {
            return null;
        }

        @Override
        public long getItemId(int arg0) 
        {
            return arg0;
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
            
            Date date = new Date(mHistoryData.get(arg0).TimeStamp);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = sdf.format(date);
            
            holder.title.setText(mHistoryData.get(arg0).Name);
            holder.summary.setText(dateStr);
            holder.icon.setImageResource(R.drawable.delete);
            holder.icon.setOnClickListener(
                    new View.OnClickListener() 
                    {
                        public void onClick(View v) 
                        {
                            //Toast.makeText(mContext, "stared: " + arg0, 1000).show();
                            if (mIsLine)
                            {
                                HistoryData data = mHistoryData.get(arg0);
                                DataProvider.getInstance().deleteHistory(data.Name, HistoryData.LINE);
                            }
                            else
                            {
                                DataProvider.getInstance().deleteHistory(mHistoryData.get(arg0).Name, HistoryData.STATION);
                            }
                            HistoryAdapter.this.mHandler.sendEmptyMessage(MSG_UPDATE);
                        }
                    });

            return arg1;
        }
    }

    public class FaverateAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;
        private List<FaverateData> mFaverateData;
        private Handler mHandler;
        private boolean mIsChanged;

        public FaverateAdapter(LayoutInflater inflater, List<FaverateData> list, Handler handler)
        {
            //mContext = context;
            mInflater = inflater;
            mFaverateData = list;
            mHandler = handler;
            mIsChanged = false;
        }
        
        public boolean isChanged()
        {
            return mIsChanged;
        }
        
        public void insert(FaverateData item, int to) 
        {
            mFaverateData.add(to, item);
            mIsChanged = true;
        }

        public void remove(FaverateData item) 
        {
            mFaverateData.remove(item);
            mIsChanged = true;
        }

        @Override
        public int getCount() 
        {
            return mFaverateData.size();
        }

        @Override
        public Object getItem(int arg0) 
        {
            return mFaverateData.get(arg0);
        }

        @Override
        public long getItemId(int arg0) 
        {
            return arg0;
        }

        @Override
        public View getView(final int arg0, View arg1, ViewGroup arg2) 
        {
            ViewHolder holder = null;
            if (arg1 == null)
            {
                arg1 = mInflater.inflate(R.layout.two_line_one_icon_drag, null);
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
            
            holder.title.setText(mFaverateData.get(arg0).Name);
            holder.summary.setText(mFaverateData.get(arg0).Info);
            holder.icon.setImageResource(R.drawable.delete);
            holder.icon.setOnClickListener(
                    new View.OnClickListener() 
                    {
                        public void onClick(View v) 
                        {
                            //Toast.makeText(mContext, "stared: " + arg0, 1000).show();
                            DataProvider.getInstance().deleteFaverate(mFaverateData.get(arg0).Keyword);
                            FaverateAdapter.this.mHandler.sendEmptyMessage(MSG_UPDATE);
                        }
                    });

            return arg1;
        }
    }
}
