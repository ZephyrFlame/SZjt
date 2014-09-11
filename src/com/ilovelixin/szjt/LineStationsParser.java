package com.ilovelixin.szjt;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

public class LineStationsParser 
{
    public int parse(InputStream is, List<StationInfo> stations) throws Exception 
    {
        LineStationsParserHandler handler = new LineStationsParserHandler();
        handler.setStationInfos(stations);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.newSAXParser().parse(is, handler);
        
        return handler.getStationInfos().size();
    }

    private class LineStationsParserHandler extends DefaultHandler {

        private List<StationInfo> stations = null;
        private StationInfo station;
        private StringBuilder builder;
        private boolean isfirst;
        private int index;
        
        public void setStationInfos(List<StationInfo> outsource) 
        {
            stations = outsource;
        }
        
        public List<StationInfo> getStationInfos() 
        {
            return stations;
        }
        
        @Override
        public void startDocument() throws SAXException 
        {
            super.startDocument();
            if (stations == null)
            {
                stations = new ArrayList<StationInfo>();
            }
            builder = new StringBuilder();
            isfirst = true;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException 
        {
            super.startElement(uri, localName, qName, attributes);
            if (localName.equals("tr")) 
            {
                if (isfirst)
                {
                    isfirst = false;
                    station = null;
                }
                else
                {
                    station = new StationInfo();
                    index = 0;
                }
            }
            builder.setLength(0);
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException 
        {
            super.characters(ch, start, length);

            builder.append(ch, start, length);
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException 
        {
            super.endElement(uri, localName, qName);
            
            if (station == null)
            {
                return;
            }

            if (localName.equals("td")) 
            {
                if (index == 1)
                {
                    station.Code = builder.toString();
                }
                else if (index == 2)
                {
                    station.License = builder.toString();
                }
                else if (index == 3)
                {
                    station.Time = builder.toString();
                }
                
                index++;
            } 
            else if (localName.equals("a")) 
            {
                station.Name = builder.toString();
            }
            else if (localName.equals("tr")) 
            {
                stations.add(station);
            }
        }
    }
}
