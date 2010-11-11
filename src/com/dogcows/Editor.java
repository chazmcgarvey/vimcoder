
package com.dogcows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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
        this.id     = String.valueOf(component.getProblem().getProblemID());
        this.name   = component.getClassName();
        
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
        terms.put("RETURNTYPE", component.getReturnType().getDescriptor(language));
        terms.put("CLASSNAME",  component.getClassName());
        terms.put("METHODNAME", component.getMethodName());
        terms.put("METHODPARAMS", getMethodParams(component.getParamTypes(),
                                                  component.getParamNames(),
                                                  language));
        terms.put("METHODPARAMNAMES", Utilities.join(component.getParamNames(), ", "));
        
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
            String text = Utilities.expandTemplate(Utilities.readResource(lang + "Template"),
                                         terms);
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
                String template = Utilities.readResource(lang + "Test");
                for (TestCase testCase : component.getTestCases())
                {
                    testTerms.put("TESTOUTPUT", "\"" + Utilities.quote(testCase.getOutput()) + "\"");
                    testTerms.put("TESTINPUTS", Utilities.join(testCase.getInput(), ", "));
                    testCases.append(Utilities.expandTemplate(template, testTerms));
                }
            }
            terms.put("TESTCASES", testCases.toString());
            
            String text = Utilities.expandTemplate(Utilities.readResource(lang + "Driver"),
                                         terms);
            FileWriter writer = new FileWriter(driverFile);
            writer.write(text);
            writer.close();
        }
        
        File makeFile = new File(directory, "Makefile");
        {
            String text = Utilities.expandTemplate(Utilities.readResource(lang + "Makefile"),
                                         terms);
            FileWriter writer = new FileWriter(makeFile);
            writer.write(text);
            writer.close();
        }
    }
    
    public void setSource(String source) throws IOException
    {
        FileWriter writer = new FileWriter(new File(directory, name));
        writer.write(source);
        writer.close();
        doVimCommand("--remote-tab-silent", sourceFile.getPath());
    }

    public String getSource() throws IOException
    {
        return Utilities.readFile(sourceFile) + "\n// Edited by " + VimCoder.version + "\n// " + VimCoder.website + "\n\n";
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
            String[] exec = {"gvim", "--servername", "VimCoder" + id,
                             command};
            exec = Utilities.concat(exec, arguments);
            Runtime.getRuntime().exec(exec, null, directory);
        }
        catch (IOException exception)
        {
            System.out.println("Failed to launch external vim process.  :-(");
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
}

