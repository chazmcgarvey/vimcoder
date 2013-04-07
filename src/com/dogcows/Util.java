
package com.dogcows;

import java.io.*;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Charles McGarvey
 * The TopCoder Arena editor plug-in providing support for Vim.
 * 
 * Distributable under the terms and conditions of the 2-clause BSD license;
 * see the file COPYING for a complete text of the license.
 */
public abstract class Util
{
	/**
	 * Concatenate two arrays into a single array.
	 * @param a First array.
	 * @param b Second array.
	 * @return The combined array.
	 */
	public static <T> T[] concat(T[] a, T[] b)
	{
		T[] result = Arrays.copyOf(a, a.length + b.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	/**
	 * Combined string elements from two arrays into a single array, gluing
	 * together elements of the same index with a delimiter string.
	 * @param a First string array.
	 * @param b Second string array.
	 * @param glue The delimiter string.
	 * @return The combined array.
	 */
	public static String[] combine(String[] a, String[] b, String glue)
	{
		String[] result = new String[Math.min(a.length, b.length)];
		for (int i = 0; i < result.length; ++i)
		{
			result[i] = a[i] + glue + b[i];
		}
		return result;
	}

	/**
	 * Join the elements of a string array with a delimiter.
	 * @param a The array.
	 * @param glue The delimiter string.
	 * @return The joined string.
	 */
	public static String join(String[] a, String glue)
	{
		if (a.length == 0) return "";
		StringBuilder result = new StringBuilder();
		result.append(a[0]);
		for (int i = 1; i < a.length; ++i) result.append(glue).append(a[i]);
		return result.toString();
	}

	/**
	 * Escape a string by replacing prepending backslashes and double
	 * quotation characters with an extra backslash.
	 * @param The string to be escaped.
	 * @return The escaped string.
	 */
	public static String escape(String a)
	{
		a = a.replaceAll("\\\\", "\\\\\\\\");
		a = a.replaceAll("\"",	 "\\\\\\\"");
		return a;
	}

	/**
	 * Simply read a file's contents into a string object.
	 * @param file The file to read.
	 * @return The contents of the file.
	 * @throws IOException If the file is not readable.
	 */
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

		return text.toString().replaceAll("\n", System.getProperty("line.separator"));
	}

	/**
	 * Read a resource file into a string object.
	 * The resources should be placed in the directory `resources'
	 * underneath the parent directory of this class.  Reading resources
	 * packaged in a jar is allowable.
	 * @param path Relative path to the resource.
	 * @return The contents of the resource.
	 * @throws IOException If the resource is not readable.
	 */
	public static String readResource(String path) throws IOException
	{
		StringBuilder text = new StringBuilder();

		InputStream stream = Util.class.getResourceAsStream("resources/" + path);
		if (stream != null)
		{
			try
			{
				byte[]	buffer = new byte[4096];
				int	numBytes = 0;
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

		return text.toString().replaceAll("\n", System.getProperty("line.separator"));
	}

	/**
	 * The poor man's template package.
	 * Provide a template and a map of terms to build the result with the
	 * terms expanded into the template.  Terms in the template should
	 * appear surrounded with dollar signs.	For example, if $MYTERM$
	 * appears in the template, it will be replaced by the value into the
	 * terms map with the key MYTERM (if it exists in the map).
	 * @param template The template string.
	 * @param terms A map of key/value terms.
	 * @return The string expanded from the template and terms.
	 */
	public static String expandTemplate(String template, Map<String,String> terms)
	{
		String text = template;
		for (String key : terms.keySet())
		{
			text = text.replaceAll("\\$" + key + "\\$", Util.escape(terms.get(key)));
		}
		return text;
	}
}

// vim:noet:ts=8
