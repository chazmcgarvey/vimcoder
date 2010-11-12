
package com.dogcows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.topcoder.client.contestant.ProblemComponentModel;
import com.topcoder.shared.language.Language;
import com.topcoder.shared.problem.DataType;
import com.topcoder.shared.problem.Renderer;
import com.topcoder.shared.problem.TestCase;

/**
 * @author Charles McGarvey
 *
 */
public class Editor
{
    private String  id;
    private String  name;
    private File    sourceFile;
    private File    directory;
    
    
    private static final Map<String,String> languageExtension = new HashMap<String,String>();
    static
    {
        languageExtension.put("Java",   "java");
        languageExtension.put("C++",    "cc");
        languageExtension.put("C#",     "cs");
        languageExtension.put("VB",     "vb");
        languageExtension.put("Python", "py");
    }

    
    public Editor(ProblemComponentModel component,
                  Language language,
                  Renderer renderer) throws IOException
    {
        this.id = String.valueOf(component.getProblem().getProblemID());
        this.name = component.getClassName();
        
        File topDir = new File(System.getProperty("user.home"), ".vimcoder");
        if (!topDir.isDirectory())
        {
            if (!topDir.mkdirs()) throw new IOException(topDir.getPath());
        }
        
        this.directory = new File(topDir, id);
        if (!directory.isDirectory())
        {
            if (!directory.mkdirs()) throw new IOException(directory.getPath());
        }
        
        String lang = language.getName();
        String ext  = languageExtension.get(lang);
        
        HashMap<String,String> terms = new HashMap<String,String>();
        terms.put("RETURNTYPE", component.getReturnType().getDescriptor(language).replaceAll("\\s+", ""));
        terms.put("CLASSNAME",  name);
        terms.put("METHODNAME", component.getMethodName());
        terms.put("METHODPARAMS", getMethodParams(component.getParamTypes(),
                                                  component.getParamNames(),
                                                  language));
        terms.put("METHODPARAMNAMES", Utility.join(component.getParamNames(), ", "));
        terms.put("METHODPARAMSTREAMIN", Utility.join(component.getParamNames(), " >> "));
        terms.put("METHODPARAMSTREAMOUT", Utility.join(component.getParamNames(), " << "));
        terms.put("METHODPARAMDECLARES", getMethodParamDeclarations(component.getParamTypes(),
                                                                    component.getParamNames(),
                                                                    language));
        
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
        
        sourceFile = new File(directory, name + "." + ext);
        if (!sourceFile.canRead())
        {
            String text = Utility.expandTemplate(Utility.readResource(lang + "Template"),
                                         terms);
            FileWriter writer = new FileWriter(sourceFile);
            writer.write(text);
            writer.close();
        }

        File testcaseFile = new File(directory, "testcases.txt");
        if (!testcaseFile.canRead())
        {
            StringBuilder text = new StringBuilder();
            if (component.hasTestCases())
            {
                for (TestCase testCase : component.getTestCases())
                {
                    text.append(testCase.getOutput() + System.getProperty("line.separator"));
                    for (String input : testCase.getInput())
                    {
                        text.append(input + System.getProperty("line.separator"));
                    }
                }
            }
            FileWriter writer = new FileWriter(testcaseFile);
            writer.write(text.toString());
            writer.close();
        }
        
        File driverFile = new File(directory, "driver." + ext);
        if (!driverFile.canRead())
        {
            String text = Utility.expandTemplate(Utility.readResource(lang + "Driver"),
                                                 terms);
            FileWriter writer = new FileWriter(driverFile);
            writer.write(text);
            writer.close();
        }
        
        File makeFile = new File(directory, "Makefile");
        {
            String text = Utility.expandTemplate(Utility.readResource(lang + "Makefile"),
                                         terms);
            FileWriter writer = new FileWriter(makeFile);
            writer.write(text);
            writer.close();
        }
    }

    public void setSource(String source) throws Exception
    {
        FileWriter writer = new FileWriter(new File(directory, name));
        writer.write(source);
        writer.close();
        doVimCommand("--remote-tab-silent", sourceFile.getPath());
    }

    public String getSource() throws IOException
    {
        return Utility.readFile(sourceFile) + "\n// Edited by " + VimCoder.version + "\n// " + VimCoder.website + "\n\n";
    }
    
    
    private void doVimCommand(String command, String argument) throws Exception
    {
        String[] arguments = {argument};
        doVimCommand(command, arguments);
    }
    
    private void doVimCommand(String command, String[] arguments) throws Exception
    {
        String[] exec = {"gvim", "--servername", "VimCoder" + id,
                         command};
        exec = Utility.concat(exec, arguments);
        Process child = Runtime.getRuntime().exec(exec, null, directory);
        
        long expire = System.currentTimeMillis() + 500;
        while (System.currentTimeMillis() < expire)
        {
            try
            {
                int exitCode = child.exitValue();
                if (exitCode != 0) throw new Exception("Vim process returned exit code " + exitCode + ".");
                break;
            }
            catch (IllegalThreadStateException exception)
            {
            }
            Thread.yield();
        }
    }
    
    private String getMethodParams(DataType[] types,
                                   String[] names,
                                   Language language)
    {
        StringBuilder text = new StringBuilder();
        
        text.append(types[0].getDescriptor(language).replaceAll("\\s+", "") + " " + names[0]);
        for (int i = 1; i < names.length; ++i)
        {
            text.append(", " + types[i].getDescriptor(language).replaceAll("\\s+", "") + " " + names[i]);
        }
        
        return text.toString();
    }
    
    private String getMethodParamDeclarations (DataType[] types,
                                               String[] names,
                                               Language language)
    {
        StringBuilder text = new StringBuilder();
        
        for (int i = 0; i < names.length; ++i)
        {
            text.append(types[i].getDescriptor(language).replaceAll("\\s+", "") + "\t" + names[i] + ";" + System.getProperty("line.separator"));
        }
        
        return text.toString();
    }
}

