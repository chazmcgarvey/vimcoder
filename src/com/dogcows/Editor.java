
package com.dogcows;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.topcoder.client.contestant.ProblemComponentModel;
import com.topcoder.shared.language.Language;
import com.topcoder.shared.problem.DataType;
import com.topcoder.shared.problem.Renderer;
import com.topcoder.shared.problem.TestCase;

/**
 * @author Charles McGarvey
 * The TopCoder Arena editor plug-in providing support for Vim.
 * 
 * Distributable under the terms and conditions of the 2-clause BSD license;
 * see the file COPYING for a complete text of the license.
 */
public class Editor
{
	/**
	 * The problem ID number.
	 */
	private String id;

	/**
	 * The name of the class.
	 */
	private String name;

	/**
	 * The name of the contest.
	 */
	private String contestName;

	/**
	 * The point value.
	 */
	private String points;

	/**
	 * The path of the current source file.
	 */
	private File sourceFile;

	/**
	 * The path of the problem directory.
	 */
	private File directory;


	/**
	 * Map languages names to file extensions.
	 */
	private static final Map<String,String> languageExtension = new HashMap<String,String>();
	static
	{
		languageExtension.put("Java", "java");
		languageExtension.put("C++", "cc");
		languageExtension.put("C#", "cs");
		languageExtension.put("VB", "vb");
		languageExtension.put("Python", "py");
	}


	/**
	 * Construct an editor with the problem objects given us by the Arena.
	 * @param component A container for the particulars of the problem.
	 * @param language The currently selected language.
	 * @param renderer A helper object to help format the problem statement.
	 * @throws Exception If the editor could not set itself up.
	 */
	public Editor(ProblemComponentModel component,
		      Language language, Renderer renderer) throws Exception
	{
		this.id = String.valueOf(component.getProblem().getProblemID());
		this.name = component.getClassName();
		this.contestName = component.getProblem().getRound().getContestName().replaceAll(" ", "-");
		this.points = String.valueOf(component.getPoints().intValue());

		// Make sure the top-level vimcoder directory exists.
		File topDir = VimCoder.getStorageDirectory();
		if (!topDir.isDirectory())
		{
			if (!topDir.mkdirs()) throw new IOException(topDir.getPath());
		}

		// Make sure the problem directory exists.
		File newStyleDirectory = new File(new File(topDir, contestName), points);
		File oldStyleDirectory = new File(topDir, id);
		if (newStyleDirectory.isDirectory())
		{
			this.directory = newStyleDirectory;
		}
		else if (oldStyleDirectory.isDirectory())
		{
			this.directory = oldStyleDirectory;
		}
		else if (VimCoder.isContestDirNames())
		{
			this.directory = newStyleDirectory;
			if (!directory.mkdirs()) throw new IOException(directory.getPath());
		}
		else
		{
			this.directory = oldStyleDirectory;
			if (!directory.mkdirs()) throw new IOException(directory.getPath());
		}

		String lang = language.getName();
		String ext = languageExtension.get(lang);

		// Set up the terms used for the template expansion.
		HashMap<String,String> terms = new HashMap<String,String>();
		terms.put("RETURNTYPE", component.getReturnType().getDescriptor(language));
		terms.put("CLASSNAME", name);
		terms.put("METHODNAME", component.getMethodName());
		terms.put("METHODPARAMS", getMethodParams(component.getParamTypes(),
							  component.getParamNames(), language));
		terms.put("METHODPARAMNAMES", Util.join(component.getParamNames(), ", "));
		terms.put("METHODPARAMSTREAMIN", Util.join(component.getParamNames(), " >> "));
		terms.put("METHODPARAMSTREAMOUT", Util.join(component.getParamNames(), " << \", \" << "));
		terms.put("METHODPARAMDECLARES", getMethodParamDeclarations(component.getParamTypes(),
									    component.getParamNames(), language));

		// Write the problem statement as an HTML file in the problem directory.
		File problemFile = new File(directory, "Problem.html");
		if (!problemFile.canRead())
		{
			FileWriter writer = new FileWriter(problemFile);
			try
			{
				writer.write(renderer.toHTML(language));
			}
			finally
			{
				writer.close();
			}
		}

		// Expand the template for the main class and write it to the current
		// source file.
		this.sourceFile = new File(directory, name + "." + ext);
		if (!sourceFile.canRead())
		{
			String text = Util.expandTemplate(readTemplate(lang + "Template"), terms);
			FileWriter writer = new FileWriter(sourceFile);
			writer.write(text);
			writer.close();
		}

		// Expand the driver template and write it to a source file.
		File driverFile = new File(directory, "driver." + ext);
		if (!driverFile.canRead())
		{
			String text = Util.expandTemplate(readTemplate(lang + "Driver"), terms);
			FileWriter writer = new FileWriter(driverFile);
			writer.write(text);
			writer.close();
		}

		// Write the test cases to a text file.  The driver code can read this
		// file and perform the tests based on what it reads.
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

		// Finally, expand the Makefile template and write it.
		File makeFile = new File(directory, "Makefile");
		if (!makeFile.canRead())
		{
			String text = Util.expandTemplate(readTemplate(lang + "Makefile"), terms);
			FileWriter writer = new FileWriter(makeFile);
			writer.write(text);
			writer.close();
		}
	}

	/**
	 * Save the source code provided by the server, and tell the Vim server to
	 * edit the current source file.
	 * @param source The source code.
	 * @throws Exception If the source couldn't be written or the Vim server
	 * had a problem.
	 */
	public void setSource(String source) throws Exception
	{
		FileWriter writer = new FileWriter(new File(directory, name));
		writer.write(source);
		writer.close();
		sendVimCommand("--remote-tab-silent", sourceFile.getPath());
	}

	/**
	 * Read the source code from the current source file.
	 * @return The source code.
	 * @throws IOException If the source file could not be read.
	 */
	public String getSource() throws IOException
	{
		return Util.readFile(sourceFile) + "\n// Edited by " +
		VimCoder.version + "\n// " + VimCoder.website + "\n\n";
	}


	/**
	 * Send a command to the Vim server.
	 * If the server isn't running, it will be started with the name
	 * VIMCODER#### where #### is the problem ID.
	 * @param command The command to send to the server.
	 * @param argument A single argument for the remote command.
	 * @throws Exception If the command could not be sent.
	 */
	private void sendVimCommand(String command, String argument) throws Exception
	{
		String[] arguments = {argument};
		sendVimCommand(command, arguments);
	}

	/**
	 * Send a command to the Vim server.
	 * If the server isn't running, it will be started with the name
	 * VIMCODER#### where #### is the problem ID.
	 * @param command The command to send to the server.
	 * @param argument Arguments for the remote command.
	 * @throws Exception If the command could not be sent.
	 */
	private void sendVimCommand(String command, String[] arguments) throws Exception
	{
		String[] vimCommand = VimCoder.getVimCommand().split("\\s");
		String[] flags = {"--servername", "VimCoder" + id, command};
		vimCommand = Util.concat(vimCommand, flags);
		vimCommand = Util.concat(vimCommand, arguments);
		Process child = Runtime.getRuntime().exec(vimCommand, null, directory);

		/* FIXME: This is a pretty bad hack.  The problem is that the Vim
		 * process doesn't fork to the background on some systems, so we
		 * can't wait on the child.  At the same time, calling this method
		 * before the previous child could finish initializing the server
		 * may result in multiple editor windows popping up.	We'd also
		 * like to be able to get the return code from the child if we can.
		 * The workaround here is to stall the thread for a little while or
		 * until we see that the child exits.  If the child never exits
		 * before the timeout, we will assume it is not backgrounding and
		 * that everything worked.  This works as long as the Vim server is
		 * able to start within the stall period. */
		long expire = System.currentTimeMillis() + 2500;
		while (System.currentTimeMillis() < expire)
		{
			Thread.yield();
			try
			{
				int exitCode = child.exitValue();
				if (exitCode != 0) throw new Exception("Vim process returned exit code " + exitCode + ".");
				break;
			}
			catch (IllegalThreadStateException exception)
			{
				// The child has not exited; intentionally ignoring exception.
			}
		}
	}


	/**
	 * Read a template.
	 * We first look in the storage directory.  If we can't find one, we
	 * look among the resources.
	 * @param tName The name of the template.
	 * @return The contents of the template file, or an empty string.
	 */
	private String readTemplate(String tName)
	{
		File templateFile = new File(VimCoder.getStorageDirectory(), tName);
		try
		{
			if (templateFile.canRead()) return Util.readFile(templateFile);
			return Util.readResource(tName);
		}
		catch (IOException exception)
		{
			return "";
		}
	}


	/**
	 * Convert an array of data types to an array of strings according to a
	 * given language.
	 * @param types The data types.
	 * @param language The language to use in the conversion.
	 * @return The array of string representations of the data types.
	 */
	private String[] getStringTypes(DataType[] types, Language language)
	{
		String[] strings = new String[types.length];
		for (int i = 0; i < types.length; ++i)
		{
			strings[i] = types[i].getDescriptor(language);
		}
		return strings;
	}

	/**
	 * Combine the data types and parameter names into a comma-separated list of
	 * the method parameters.
	 * The result could be used inside the parentheses of a method
	 * declaration.
	 * @param types The data types of the parameters.
	 * @param names The names of the parameters.
	 * @param language The language used for representing the data types.
	 * @return The list of parameters.
	 */
	private String getMethodParams(DataType[] types, String[] names, Language language)
	{
		String[] typeStrings = getStringTypes(types, language);
		return Util.join(Util.combine(typeStrings, names, " "), ", ");
	}

	/**
	 * Combine the data types and parameter names into a group of variable
	 * declarations.
	 * Each declaration is separated by a new line and terminated with a
	 * semicolon.
	 * @param types The data types of the parameters.
	 * @param names The names of the parameters.
	 * @param language The language used for representing the data types.
	 * @return The parameters as a block of declarations.
	 */
	private String getMethodParamDeclarations(DataType[] types, String[] names, Language language)
	{
		final String end = ";" + System.getProperty("line.separator");
		String[] typeStrings = getStringTypes(types, language);
		return Util.join(Util.combine(typeStrings, names, "\t"), end) + end;
	}
}

// vim:noet:ts=8
