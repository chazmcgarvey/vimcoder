
package com.dogcows;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import com.topcoder.client.contestant.ProblemComponentModel;
import com.topcoder.shared.language.*;
import com.topcoder.shared.problem.*;
import com.topcoder.shared.problem.Renderer;

/**
 * @author Charles McGarvey
 * The TopCoder Arena editor plug-in providing support for Vim.
 * 
 * Distributable under the terms and conditions of the 2-clause BSD license;
 * see the file COPYING for a complete text of the license.
 */
public class VimCoder
{
    /**
     * 
     */
    public final static String     version = "VimCoder 0.1";
    public final static String     website = "http://www.dogcows.com/vimcoder";
    
    private JPanel      panel;
    private JTextArea   logArea;
    
    private Editor      editor;

   
    public VimCoder()
    {
        logArea = new JTextArea();
        logArea.setForeground(Color.GREEN);
        logArea.setBackground(Color.BLACK);
        logArea.setEditable(false);
        Font font = new Font("Courier", Font.PLAIN, 12);
        if (font != null) logArea.setFont(font);
           
        panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);
    }

    
    public void startUsing()
    {
        System.out.println("startUsing");
        Runnable task = new Runnable()
        {
            public void run()
            {
                logArea.setText("");
            }
        };
        if (SwingUtilities.isEventDispatchThread())
        {
            task.run();
        }
        else
        {
            SwingUtilities.invokeLater(task);
        }
    }
    
    public void stopUsing()
    {
        System.out.println("stopUsing");
        editor = null;
    }
    
    public JPanel getEditorPanel()
    {
        System.out.println("getEditorPanel");
        return panel;
    }
   
    public String getSource() throws Exception
    {
        System.out.println("getSource");
        try
        {
            String source = editor.getSource();
            logInfo("Source code uploaded to server.");
            return source;
        }
        catch (Exception exception)
        {
            logError("Failed to get source code: " + exception.getLocalizedMessage());
            throw exception;
        }
    }
   
    public void setSource(String source)
    {
        System.out.println("setSource: " + source);
        try
        {
            editor.setSource(source);
            logInfo("Source code downloaded from server.");
        }
        catch (Exception exception)
        {
            logError("Failed to save the source given by the server: " + exception.getLocalizedMessage());
            return;
        }
    }
    
    public void setProblemComponent(ProblemComponentModel component, 
                                    Language language,
                                    Renderer renderer)
    {
        System.out.println("setProblemComponent");
        try
        {
            editor = new Editor(component, language, renderer);
        }
        catch (Exception exception)
        {
            logError("An error occured while loading the problem: " + exception.getLocalizedMessage());
        }
    }

    
    private void log(final String what)
    {
        Runnable task = new Runnable()
        {
            public void run()
            {
                SimpleDateFormat format = new SimpleDateFormat("kk:mm:ss");
                logArea.append(format.format(new Date()) + ", " + what);
            }
        };
        if (SwingUtilities.isEventDispatchThread())
        {
            task.run();
        }
        else
        {
            SwingUtilities.invokeLater(task);
        }
    }
    
    private void logInfo(String what)
    {
        log(" INFO: " + what + System.getProperty("line.separator"));
    }
    
    private void logWarning(String what)
    {
        log(" WARN: " + what + System.getProperty("line.separator"));
    }
    
    private void logError(String what)
    {
        log("ERROR: " + what + System.getProperty("line.separator"));
    }
    
    
    public static void main(String args[])
    {
        VimCoder plugin = new VimCoder();
        
        JFrame frame = new JFrame("VimCoder");
        frame.add(plugin.getEditorPanel());
        frame.setSize(640, 480);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        plugin.startUsing();
    }
}

