package com.ilovelixin.szjt;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

public class StationLinesParser 
{
    public int parse(InputStream is, List<StationLineInfo> lines) throws Exception 
    {
        LineStationsParserHandler handler = new LineStationsParserHandler();
        handler.setLinesInfo(lines);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.newSAXParser().parse(is, handler);
        
        return handler.getLinesInfo().size();
    }

    private class LineStationsParserHandler extends DefaultHandler 
    {
        private final static String LineGuidTag = "LineGuid=";

        private List<StationLineInfo> lines = null;
        private StationLineInfo line;
        private StringBuilder builder;
        private boolean isfirst;
        private int index;
        
        public void setLinesInfo(List<StationLineInfo> outsource) 
        {
            lines = outsource;
        }
        
        public List<StationLineInfo> getLinesInfo() 
        {
            return lines;
        }
        
        @Override
        public void startDocument() throws SAXException 
        {
            super.startDocument();
            if (lines == null)
            {
                lines = new ArrayList<StationLineInfo>();
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
                    line = null;
                }
                else
                {
                    line = new StationLineInfo();
                    index = 0;
                }
            }
            else if (line != null && localName.equals("a")) 
            {
                String href = attributes.getValue("href");
                if (href != null && href.length() > 0)
                {
                    line.Guid = retrieveGuid(href);
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
            
            if (line == null)
            {
                return;
            }

            if (localName.equals("td")) 
            {
                if (index == 1)
                {
                    line.Summary = builder.toString();
                }
                else if (index == 2)
                {
                    line.License = builder.toString();
                }
                else if (index == 3)
                {
                    line.Time = builder.toString();
                }
                else if (index == 4)
                {
                    line.Distance = builder.toString();
                }
                
                index++;
            } 
            else if (localName.equals("a")) 
            {
                line.Name = builder.toString();
            }
            else if (localName.equals("tr")) 
            {
                lines.add(line);
            }
        }
        
        private String retrieveGuid(String source)
        {
            int s1 = source.indexOf(LineGuidTag);
            if (s1 < 0)
            {
                return null;
            }
            int s2 = source.indexOf('&', s1 + LineGuidTag.length());
            if (s2 < 0)
            {
                return null;
            }
            return source.substring(s1 + LineGuidTag.length(), s2);
        }
    }
}
