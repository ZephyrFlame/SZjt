package com.ilovelixin.szjt;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DataProvider 
{
    public final static int ACT_ADDED = 0;
    public final static int ACT_UPDATED = 1;
    
    public static DataProvider mSelf = null;
    
    private List<HistoryData> mLineHistorys = null;
    private List<HistoryData> mStationHistorys = null;
    private List<FaverateData> mFaverates = null;
    private DatabaseManager mManager = null;
    private Context mContext;
    private ComparatorHistory mHComparator;
    
    private DataProvider(Context context)
    {
        mContext = context;
        mManager = new DatabaseManager(mContext);
        mHComparator = new ComparatorHistory();
        
        initHistory();
        initFaverate();
    }
    
    public static void initInstance(Context context)
    {
        mSelf = new DataProvider(context);
    }
    
    public static DataProvider getInstance()
    {
        return mSelf;
    }
    
    public void closeInstance() 
    {  
        mManager.closeDatabase();  
    } 
    
    public List<HistoryData> getLineHistory()
    {
        return mLineHistorys;
    }
    
    public List<HistoryData> getStationHistory()
    {
        return mStationHistorys;
    }
    
    public List<FaverateData> getFaverate()
    {
        return mFaverates;
    }
    
    public int updateHistoryData(String name, int type, long timestamp)
    {
        if (type == HistoryData.LINE)
        {
            for (HistoryData item : mLineHistorys)
            {
                if (item.Name.equals(name))
                {
                    item.TimeStamp = timestamp;
                    mManager.updateHistory(item);
                    
                    Collections.sort(mLineHistorys, mHComparator);

                    return ACT_UPDATED;
                }
            }
            
            HistoryData data = new HistoryData();
            data.Name = name;
            data.Type = type;
            data.TimeStamp = timestamp;
            mManager.addHistory(data);
            
            mLineHistorys.add(data);
            Collections.sort(mLineHistorys, mHComparator);
            
            return ACT_ADDED;
        }
        else
        {
            for (HistoryData item : mStationHistorys)
            {
                if (item.Name.equals(name))
                {
                    item.TimeStamp = timestamp;
                    mManager.updateHistory(item);
                    
                    Collections.sort(mStationHistorys, mHComparator);

                    return ACT_UPDATED;
                }
            }
            
            HistoryData data = new HistoryData();
            data.Name = name;
            data.Type = type;
            data.TimeStamp = timestamp;
            mManager.addHistory(data);
            
            mStationHistorys.add(data);
            Collections.sort(mStationHistorys, mHComparator);
            
            return ACT_ADDED;
        }
    }
    
    public void deleteHistory(String name, int type)
    {
        if (type == HistoryData.LINE)
        {
            for (HistoryData item : mLineHistorys)
            {
                if (item.Name.equals(name))
                {
                    mManager.deleteHistory(item);
                    mLineHistorys.remove(item);
                    
                    return;
                }
            }
        }
        else
        {
            for (HistoryData item : mStationHistorys)
            {
                if (item.Name.equals(name))
                {
                    mManager.deleteHistory(item);
                    mStationHistorys.remove(item);
                    
                    return;
                }
            }
        }
    }
    
    public boolean isInHistory(String name, int type)
    {
        if (type == HistoryData.LINE)
        {
            for (HistoryData item : mLineHistorys)
            {
                if (item.Name.equals(name))
                {
                    return true;
                }
            }
        }
        else
        {
            for (HistoryData item : mStationHistorys)
            {
                if (item.Name.equals(name))
                {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public int updateFaverateData(String name, int type, String keyword, String info)
    {
        FaverateData data = new FaverateData();
        data.Name = name;
        data.Type = type;
        data.Keyword = keyword;
        data.Info = info;
        mManager.addFaverate(data);

        mFaverates.add(data);
        
        return ACT_ADDED;
    }
    
    public void deleteFaverate(String keyword)
    {
        for (FaverateData item : mFaverates)
        {
            if (item.Keyword.equals(keyword))
            {
                mManager.deleteFaverate(item);
                mFaverates.remove(item);
                
                return;
            }
        }
    }
    
    public boolean isInFaverate(String keyword)
    {
        for (FaverateData item : mFaverates)
        {
            if (item.Keyword.equals(keyword))
            {
                return true;
            }
        }
        
        return false;
    }
    
    private void initHistory()
    {
        mLineHistorys = new ArrayList<HistoryData>();
        mStationHistorys = new ArrayList<HistoryData>();

        List<HistoryData> all = mManager.queryHistorys();
        for (HistoryData item : all)
        {
            if (item.Type == HistoryData.LINE)
            {
                mLineHistorys.add(item);
            }
            else
            {
                mStationHistorys.add(item);
            }
        }
        
        if (mLineHistorys.size() > 0)
        {
            Collections.sort(mLineHistorys, mHComparator);
        }
        if (mStationHistorys.size() > 0)
        {
            Collections.sort(mStationHistorys, mHComparator);
        }
    }
    
    private void initFaverate()
    {
        mFaverates = mManager.queryFaverates();
    }
    
    public class ComparatorHistory implements Comparator<Object>
    {
        public int compare(Object arg0, Object arg1) 
        {
            HistoryData i0 = (HistoryData)arg0;
            HistoryData i1 = (HistoryData)arg1;
            
            if (i0.TimeStamp < i1.TimeStamp)
            {
                return 1;
            }
            else if (i0.TimeStamp > i1.TimeStamp)
            {
                return -1;
            }
            return 0;
        }
    }
}
