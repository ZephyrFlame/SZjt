package com.ilovelixin.szjt;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

public class StationSummaryParser 
{
    public int parse(InputStream is, List<StationSummary> stations) throws Exception 
    {
        LineStationsParserHandler handler = new LineStationsParserHandler();
        handler.setStationSummary(stations);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.newSAXParser().parse(is, handler);
        
        return handler.getStationSummary().size();
    }

    private class LineStationsParserHandler extends DefaultHandler {

        private List<StationSummary> stations = null;
        private StationSummary station;
        private StringBuilder builder;
        private boolean isfirst;
        private int index;
        
        public void setStationSummary(List<StationSummary> outsource) 
        {
            stations = outsource;
        }
        
        public List<StationSummary> getStationSummary() 
        {
            return stations;
        }
        
        @Override
        public void startDocument() throws SAXException 
        {
            super.startDocument();
            if (stations == null)
            {
                stations = new ArrayList<StationSummary>();
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
                    station = new StationSummary();
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
                    station.District = builder.toString();
                }
                else if (index == 3)
                {
                    station.Route = builder.toString();
                }
                else if (index == 4)
                {
                    station.Location = builder.toString();
                }
                else if (index == 5)
                {
                    station.Side = builder.toString();
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
