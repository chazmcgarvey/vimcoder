
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
    private final static String     version = "VimCoder 0.1";
    private final static String     website = "http://www.dogcows.com/vimcoder";
    
    private JPanel      panel;
    private JTextArea   logArea;
    
    private Editor      editor;
    
    
    private static final Map<String,String> languageExtension = new HashMap<String,String>();
    static
    {
        languageExtension.put("Java",   "java");
        languageExtension.put("C++",    "cc");
        languageExtension.put("C#",     "cs");
        languageExtension.put("VB",     "vb");
        languageExtension.put("Python", "py");
    }
    
    
    private class Editor
    {
        private String  id;
        private String  name;
        
        private File    sourceFile;
        private File    directory;
        
        
        public Editor(ProblemComponentModel component, Language language, Renderer renderer) throws IOException
        {
            this.id     = String.valueOf(component.getProblem().getProblemID());
            this.name   = component.getClassName();
            
            File topDir = new File(System.getProperty("user.home"), ".vimcoder");
            if (!topDir.isDirectory())
            {
                if (!topDir.mkdirs()) throw new IOException(topDir.getPath());
            }
            
            this.directory = new File(topDir, String.valueOf(component.getProblem().getProblemID()));
            if (!directory.isDirectory())
            {
                if (!directory.mkdirs()) throw new IOException(directory.getPath());
            }
            
            String lang = language.getName();
            String ext  = languageExtension.get(lang);
            
            HashMap<String,String> terms = new HashMap<String,String>();
            terms.put("RETURNTYPE", component.getReturnType().getDescriptor(language));
            terms.put("CLASSNAME",  component.getClassName());
            terms.put("METHODNAME", component.getMethodName());
            terms.put("METHODPARAMS", getMethodParams(component.getParamTypes(), component.getParamNames(), language));
            terms.put("METHODPARAMNAMES", join(component.getParamNames(), ", "));
            
            File problemFile = new File(directory, "Problem.html");
            if (!problemFile.canRead())
            {
                FileWriter writer = new FileWriter(problemFile);
                try
                {
                    writer.write(renderer.toHTML(language));
                }
                catch (Exception exception)
                {
                }
                writer.close();
            }
            
            sourceFile = new File(directory, terms.get("CLASSNAME") + "." + ext);
            if (!sourceFile.canRead())
            {
                String text = expandTemplate(readResource(lang + "Template"), terms);
                FileWriter writer = new FileWriter(sourceFile);
                writer.write(text);
                writer.close();
            }

            File driverFile = new File(directory, "driver" + "." + ext);
            if (!driverFile.canRead())
            {
                StringBuilder testCases = new StringBuilder();
                if (component.hasTestCases())
                {
                    HashMap<String,String> testTerms = new HashMap<String,String>();
                    testTerms.putAll(terms);
                    String template = readResource(lang + "Test");
                    for (TestCase testCase : component.getTestCases())
                    {
                        testTerms.put("TESTOUTPUT", "\"" + quote(testCase.getOutput()) + "\"");
                        testTerms.put("TESTINPUTS", join(testCase.getInput(), ", "));
                        testCases.append(expandTemplate(template, testTerms));
                    }
                }
                terms.put("TESTCASES", testCases.toString());
                
                String text = expandTemplate(readResource(lang + "Driver"), terms);
                FileWriter writer = new FileWriter(driverFile);
                writer.write(text);
                writer.close();
            }
            
            File makeFile = new File(directory, "Makefile");
            {
                String text = expandTemplate(readResource(lang + "Makefile"), terms);
                FileWriter writer = new FileWriter(makeFile);
                writer.write(text);
                writer.close();
            }
        }
        
        public void setSource(String source) throws IOException
        {
            String actualSource = readFile(sourceFile);
            if (!actualSource.equals(source))
            {
                File actualFile = new File(directory, name);
                FileWriter writer = new FileWriter(actualFile);
                writer.write(source);
                writer.close();
            }
            doVimCommand("--remote-tab-silent", sourceFile.getPath());
            doVimCommand("--remote-send", "<C-\\><C-N>:if search('\\$CARAT\\\\$') != 0<CR>normal df$<CR>endif<CR>:redraw<CR>");
        }

        public String getSource() throws IOException
        {
            return readFile(sourceFile) + "\n// Edited by " + version + "\n// " + website + "\n\n";
        }
        
        public void setTextEnabled(boolean enable)
        {
            doVimCommand("--remote-send", "<C-\\><C-N>:set readonly<CR>:echo \"The contest is over.\"<CR>");
        }
        
        
        private boolean doVimCommand(String command, String argument)
        {
            String[] arguments = {argument};
            return doVimCommand(command, arguments);
        }
        
        private boolean doVimCommand(String command, String[] arguments)
        {
            try
            {
                String[] exec = {"gvim", "--servername", "VimCoder" + id, command};
                exec = concat(exec, arguments);
                
                Process child = Runtime.getRuntime().exec(exec);
                if (child.waitFor() == 0)
                {
                    return true;
                }
                else
                {
                    logError("vim command failed");
                }
            }
            catch (IOException exception)
            {
                logError("failed to launch external vim process");
                return false;
            }
            catch (InterruptedException exception)
            {
                logWarning("interrupted while waiting on vim process");
            }
            return false;
        }
        
        private String getMethodParams(DataType[] types, String[] names, Language language)
        {
            StringBuilder text = new StringBuilder();
            
            text.append(types[0].getDescriptor(language) + " " + names[0]);
            for (int i = 1; i < names.length; ++i)
            {
                text.append(", " + types[i].getDescriptor(language) + " " + names[i]);
            }
            
            return text.toString();
        }
        
        private String readFile(File file) throws IOException
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
        
        private String readResource(String path) throws IOException
        {
            StringBuilder text = new StringBuilder();
            
            InputStream stream = getClass().getResourceAsStream("resources/" + path);
            if (stream != null)
            {
                try
                {
                    byte[]  buffer = new byte[4096];
                    int     numBytes = 0;
                    while (0 < (numBytes = stream.read(buffer))) text.append(new String(buffer, 0, numBytes));
                }
                finally
                {
                    stream.close();
                }
            }
            
            return text.toString();
        }
        
        private String expandTemplate(String template, Map<String,String> terms)
        {
            String text = template;
            for (String key : terms.keySet())
            {
                text = text.replaceAll("\\$" + key + "\\$", quote(terms.get(key)));
            }
            return text;
        }
    }
    
    
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
        logArea.setText("");
    }
    
    public void stopUsing()
    {
        editor = null;
    }
    
    public JPanel getEditorPanel()
    {
        return panel;
    }
   
    public String getSource()
    {
        try
        {
            String source = editor.getSource();
            logInfo("Source code uploaded to server.");
            return source;
        }
        catch (IOException exception)
        {
            logError("failed to open file source file for reading");
            return "";
        }
    }
   
    public void setSource(String source)
    {
        try
        {
            editor.setSource(source);
            logInfo("source set");
        }
        catch (IOException exception)
        {
            logError("failed setting the source");
            return;
        }
    }
    
    public void setProblemComponent(ProblemComponentModel component, Language language, Renderer renderer)
    {
        try
        {
            editor = new Editor(component, language, renderer);
        }
        catch (IOException exception)
        {
            logError("failed while loading the problem");
        }
    }

    public void setTextEnabled(Boolean enable)
    {
        editor.setTextEnabled(enable);
    }
    
    
    private void log(String what)
    {
        SimpleDateFormat format = new SimpleDateFormat("kk:mm:ss");
        String time = format.format(new Date());
        logArea.append(time + ", " + what);
    }
    
    private void logInfo(String what)
    {
        log(" INFO: " + what + "\n");
    }
    
    private void logWarning(String what)
    {
        log(" WARN: " + what + "\n");
    }
    
    private void logError(String what)
    {
        log("ERROR: " + what + "\n");
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

