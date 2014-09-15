package com.ilovelixin.szjt;

import android.text.Html;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class HttpHelper 
{
    private final static String TAG = "HttpHelper";
    private final static String MainContentStartTag = "<span id=\"MainContent_DATA\">";
    private final static String MainContentEndTag = "</span>";
    private final static String AHrefTag = "<a href=\"";
    private final static String LineGuidTag = "LineGuid=";
    private final static String LineInfoTag = "LineInfo=";
    
    public static String SearchLines(String keyword, boolean is_line)
    {
        String retString = null;
        HttpPost httpRequest = null;
        
        if (is_line)
        {
            httpRequest = new HttpPost("http://www.szjt.gov.cn/apts/APTSLine.aspx");
        }
        else
        {
            httpRequest = new HttpPost("http://www.szjt.gov.cn/apts/default.aspx");
        }
        httpRequest.setHeader("Accept-Encoding", "gzip, deflate"); 

        List <NameValuePair> params = new ArrayList <NameValuePair>();
        if (is_line)
        {
            params.add(new BasicNameValuePair("__VIEWSTATE", "/wEPDwUJNDk3MjU2MjgyD2QWAmYPZBYCAgMPZBYCAgEPZBYCAgYPDxYCHgdWaXNpYmxlaGRkZIRQOyvU8esCyqSqyd1qgFhT2+bZFSmxXzTNIR5o8kew"));
            params.add(new BasicNameValuePair("__VIEWSTATEGENERATOR", "964EC381"));
            params.add(new BasicNameValuePair("__EVENTVALIDATION", "/wEWAwKlwd6yBgL88Oh8AqX89aoKI18g5BVqDt3u2EEcQKTwefOjflsUjJWVUHv9BIcrXWc="));
            params.add(new BasicNameValuePair("ctl00$MainContent$LineName", keyword));
            params.add(new BasicNameValuePair("ctl00$MainContent$SearchLine", "ËÑË÷"));
        }
        else
        {
            params.add(new BasicNameValuePair("__VIEWSTATE", "/wEPDwULLTE5ODM5MjcxNzlkZI6G5BRzUdskhCZlnEaq908K57rffnLIIQaj+SS/lEx3"));
            params.add(new BasicNameValuePair("__VIEWSTATEGENERATOR", "7BCA6D38"));
            params.add(new BasicNameValuePair("__EVENTVALIDATION", "/wEWBQLBpcbHAwLq+uyKCAKkmJj/DwL0+sTIDgLl5vKEDgB1HJbeXqriwNGMfLxbB3/j63P66ss/L27YQwHCyDNt"));
            params.add(new BasicNameValuePair("ctl00$MainContent$StandName", keyword));
            params.add(new BasicNameValuePair("ctl00$MainContent$SearchCode", "ËÑË÷"));
            params.add(new BasicNameValuePair("ctl00$MainContent$StandCode", ""));
        }
        
        try
        {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            httpRequest.setEntity(entity);

            DefaultHttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpRequest);

            int code = httpResponse.getStatusLine().getStatusCode();
            if (code == 200)
            {
                HttpEntity httpEntity = httpResponse.getEntity();
                Header encoding = httpEntity.getContentEncoding();
                
                if (encoding != null && encoding.getValue().indexOf("gzip") != -1)
                {
                    if (httpEntity.getContentLength() > 0)
                    {
                        retString = GZIPInputStreamtoString(new GZIPInputStream(httpEntity.getContent()), HTTP.UTF_8);
                    }
                    else
                    {
                        byte[] retBytes = EntityUtils.toByteArray(httpEntity);
                        
                        ByteArrayInputStream sbis = new ByteArrayInputStream(retBytes);
                        retString = GZIPInputStreamtoString(new GZIPInputStream(sbis), HTTP.UTF_8);
                    }
                }
                else
                {
                    retString = EntityUtils.toString(httpEntity, HTTP.UTF_8);
                }
            }
            else
            {
                Log.v(TAG, "code = " + code);
            }
        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }  

        return retString;
    }
    
    public static String GetStationLineInfo(String keyword, boolean is_line)
    {
        String retString = null;
        HttpGet httpRequest;
        
        if (is_line)
        {
            httpRequest= new HttpGet("http://www.szjt.gov.cn/apts/APTSLine.aspx?LineGuid=" + keyword);
        }
        else
        {
            httpRequest= new HttpGet("http://www.szjt.gov.cn/apts/default.aspx?StandCode=" + keyword);
        }
        httpRequest.setHeader("Accept-Encoding", "gzip, deflate"); 
        
        try
        {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpRequest);

            int code = httpResponse.getStatusLine().getStatusCode();
            if (code == 200)
            {
                HttpEntity httpEntity = httpResponse.getEntity();
                Header encoding = httpEntity.getContentEncoding();
                
                if (encoding != null && encoding.getValue().indexOf("gzip") != -1)
                {
                    if (httpEntity.getContentLength() > 0)
                    {
                        retString = GZIPInputStreamtoString(new GZIPInputStream(httpEntity.getContent()), HTTP.UTF_8);
                    }
                    else
                    {
                        byte[] retBytes = EntityUtils.toByteArray(httpEntity);
 
                        ByteArrayInputStream sbis = new ByteArrayInputStream(retBytes);
                        retString = GZIPInputStreamtoString(new GZIPInputStream(sbis), HTTP.UTF_8);
                    }
                }
                else
                {
                    retString = EntityUtils.toString(httpEntity, HTTP.UTF_8);
                }
            }
            else
            {
                Log.v(TAG, "code = " + code);
            }
        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }  

        return retString;
    }
    
    public static int ParseLineInfo(String source, List<LineSummary> lines)
    {
        int is = source.indexOf(MainContentStartTag);
        if (is < 0)
        {
            return 0;
        }
        int ie = source.indexOf(MainContentEndTag, is);
        if (ie < 0)
        {
            return 0;
        }
        String data = source.substring(is + MainContentStartTag.length(), ie);
        if (data == null || data.length() == 0)
        {
            return 0;
        }
        
        int ia = data.indexOf(AHrefTag);
        if (ia < 0)
        {
            return 0;
        }

        while (ia >= 0)
        {
            ie = data.indexOf('\"', ia + AHrefTag.length());
            if (ie < 0)
            {
                break;
            }
            String info = data.substring(ia + AHrefTag.length(), ie);
            if (info == null || info.length() == 0)
            {
                break;
            }
            int s1 = info.indexOf(LineGuidTag);
            if (s1 < 0)
            {
                break;
            }
            int s2 = info.indexOf('&', s1 + LineGuidTag.length());
            if (s2 < 0)
            {
                break;
            }
            String guid = info.substring(s1 + LineGuidTag.length(), s2);
            
            s1 = info.indexOf(LineInfoTag);
            if (s1 < 0)
            {
                break;
            }
            s2 = info.indexOf('(', s1 + LineInfoTag.length());
            if (s2 < 0)
            {
                break;
            }
            String name = info.substring(s1 + LineInfoTag.length(), s2);
            
            s1 = s2 + 1;
            s2 = info.indexOf(')', s1);
            if (s2 < 0)
            {
                break;
            }
            String summary = info.substring(s1, s2);
            
            LineSummary line = new LineSummary();
            line.Guid = guid;
            line.Info = name;
            line.Summary = HttpCodeDecode(summary);
            lines.add(line);
            
            ia = data.indexOf(AHrefTag, ie);
        }
        
        return lines.size();
    }
    
    public static int ParseStationInfo(String source, List<StationSummary> stations)
    {
        int is = source.indexOf(MainContentStartTag);
        if (is < 0)
        {
            return 0;
        }
        int ie = source.indexOf(MainContentEndTag, is);
        if (ie < 0)
        {
            return 0;
        }
        String data = source.substring(is + MainContentStartTag.length(), ie);
        if (data == null || data.length() == 0)
        {
            return 0;
        }
        
        ByteArrayInputStream inputs = new ByteArrayInputStream(data.getBytes());
        
        StationSummaryParser parser = new StationSummaryParser();
        try 
        {
            parser.parse(inputs, stations);
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        
        return stations.size();
    }
    
    public static int ParseLineStations(String source, List<StationInfo> stations)
    {
        int is = source.indexOf(MainContentStartTag);
        if (is < 0)
        {
            return 0;
        }
        int ie = source.indexOf(MainContentEndTag, is);
        if (ie < 0)
        {
            return 0;
        }
        String data = source.substring(is + MainContentStartTag.length(), ie);
        if (data == null || data.length() == 0)
        {
            return 0;
        }
        
        ByteArrayInputStream inputs = new ByteArrayInputStream(data.getBytes());
        
        LineStationsParser parser = new LineStationsParser();
        try 
        {
            parser.parse(inputs, stations);
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        
        return stations.size();
    }
    
    public static int ParseStationLines(String source, List<StationLineInfo> lines)
    {
        int is = source.indexOf(MainContentStartTag);
        if (is < 0)
        {
            return 0;
        }
        int ie = source.indexOf(MainContentEndTag, is);
        if (ie < 0)
        {
            return 0;
        }
        String data = source.substring(is + MainContentStartTag.length(), ie);
        if (data == null || data.length() == 0)
        {
            return 0;
        }
        
        ByteArrayInputStream inputs = new ByteArrayInputStream(data.getBytes());
        
        StationLinesParser parser = new StationLinesParser();
        try 
        {
            parser.parse(inputs, lines);
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        
        return lines.size();
    }
    
    public static String HttpCodeDecode(String source)
    {
        return Html.fromHtml(source).toString();
    }
    
    public static String GZIPInputStreamtoString(final GZIPInputStream gis, final String defaultCharset)
    {
        if (gis == null) 
        {
            return "";
        }

        StringBuffer buffer = new StringBuffer();
        try 
        {
            Reader reader = new InputStreamReader(gis, defaultCharset);
            char[] tmp = new char[1024];
            int l;
            while((l = reader.read(tmp)) != -1) 
            {
                buffer.append(tmp, 0, l);
            }

            reader.close();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        
        return buffer.toString();
    }
    
    public static long getInputStreamSize(InputStream is)
    {
        byte[] tmp = new byte[1024];
        long l = 0;
        int r;
        try 
        {
            while((r = is.read(tmp)) != -1) 
            {
                l += r;
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        
        return l;
    }
}
