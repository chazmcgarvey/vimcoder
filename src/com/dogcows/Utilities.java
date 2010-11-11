
package com.dogcows;

import java.io.*;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Charles McGarvey
 *
 */
public abstract class Utilities
{

    public static <T> T[] concat(T[] a, T[] b)
    {
        T[] result = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    
    public static String join(String[] a, String glue)
    {
        if (a.length == 0) return "";
        StringBuilder result = new StringBuilder();
        result.append(a[0]);
        for (int i = 1; i < a.length; ++i) result.append(glue).append(a[i]);
        return result.toString();
    }
    
    public static String quote(String a)
    {
        a = a.replaceAll("\\\\", "\\\\\\\\");
        a = a.replaceAll("\"",   "\\\\\\\"");
        return a;
    }
    
    public static String readFile(File file) throws IOException
    {
        StringBuilder text = new StringBuilder();
    
        BufferedReader reader = new BufferedReader(new FileReader(file.getPath()));
        try
        {
            String line = null;
            
            while ((line = reader.readLine()) != null)
            {
                text.append(line + System.getProperty("line.separator"));
            }
        }
        finally
        {
            reader.close();
        }
    
        return text.toString();
    }
    
    public static String readResource(String path) throws IOException
    {
        StringBuilder text = new StringBuilder();
        
        InputStream stream = Utilities.class.getResourceAsStream("resources/" + path);
        if (stream != null)
        {
            try
            {
                byte[]  buffer = new byte[4096];
                int     numBytes = 0;
                while (0 < (numBytes = stream.read(buffer)))
                {
                    text.append(new String(buffer, 0, numBytes));
                }
            }
            finally
            {
                stream.close();
            }
        }
        
        return text.toString();
    }
    
    public static String expandTemplate(String template, Map<String,String> terms)
    {
        String text = template;
        for (String key : terms.keySet())
        {
            text = text.replaceAll("\\$" + key + "\\$",
                                   Utilities.quote(terms.get(key)));
        }
        return text;
    }
}
