
package com.dogcows;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;

import com.topcoder.client.contestApplet.common.Common;
import com.topcoder.client.contestApplet.common.LocalPreferences;
import com.topcoder.client.contestant.ProblemComponentModel;
import com.topcoder.shared.language.Language;
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
	 * The name and version of this plugin.
	 */
	public final static String	version = "VimCoder 0.3";

	/**
	 * The website of the plugin project.
	 */
	public final static String	website = "http://www.dogcows.com/vimcoder";


	/**
	 * The first part of the command used to invoke the Vim server.
	 */
	private static String		vimCommand = "gvim";

	/**
	 * The path to the main VimCoder directory.
	 */
	private static File			rootDir;
	static
	{
		if (System.getProperty("os.name").toLowerCase().equals("win"))
		{
			vimCommand = "C:\\WINDOWS\\gvim.bat";
		}
		rootDir = new File(System.getProperty("user.home") +
						   System.getProperty("file.separator") + ".vimcoder");
	}


	/**
	 * The panel given to the Arena applet when it is requested.
	 */
	private JPanel		panel;

	/**
	 * The text widget where log messages are appended.
	 */
	private JTextArea	logArea;

	/**
	 * The current editor object (or null if there is none).
	 */
	private Editor		editor;

	/**
	 * The configuration panel.
	 */
	private JDialog		configDialog;


	/**
	 * The key for the vim command preference.
	 */
	private final static String VIMCOMMAND = "com.dogcows.VimCoder.config.vimcommand";

	/**
	 * The key for the root directory preference.
	 */
	private final static String ROOTDIR = "com.dogcows.VimCoder.config.rootdir";

	/**
	 * The preferences object for storing plugin settings.
	 */
	private static LocalPreferences prefs = LocalPreferences.getInstance();


	/**
	 * Get the command for invoking vim.
	 * @return The command.
	 */
	public static String getVimCommand()
	{
		return vimCommand;
	}

	/**
	 * Get the storage directory.
	 * @return The directory.
	 */
	public static File getStorageDirectory()
	{
		return rootDir;
	}


	/**
	 * Instantiate the entry point of the editor plugin.
	 * Sets up the log widget and panel.
	 */
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


	/**
	 * Called by the Arena when the plugin is about to be used.
	 */
	public void startUsing()
	{
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
		loadConfiguration();
	}

	/**
	 * Called by the Arena when the plugin is no longer needed.
	 */
	public void stopUsing()
	{
		editor = null;
	}

	/**
	 * Called by the Arena to obtain the editor panel which we will use to
	 * show log messages.
	 * @return The editor panel.
	 */
	public JPanel getEditorPanel()
	{
		return panel;
	}

	/**
	 * Called by the Arena to obtain the current source.
	 * This happens when the user is saving, compiling, and/or submitting.
	 * @return The current source code.
	 * @throws Exception If the source file edited by Vim couldn't be read.
	 */
	public String getSource() throws Exception
	{
		try
		{
			String source = editor.getSource();
			logInfo("Source code uploaded to server.");
			return source;
		}
		catch (Exception exception)
		{
			logError("Failed to get source code: " +
					 exception.getLocalizedMessage());
			throw exception;
		}
	}

	/**
	 * Called by the Arena to pass the source it has.
	 * @param source The source code.
	 */
	public void setSource(String source)
	{
		try
		{
			editor.setSource(source);
			logInfo("Source code downloaded from server.");
		}
		catch (Exception exception)
		{
			logError("Failed to save the source given by the server: " +
					 exception.getLocalizedMessage());
			return;
		}
	}

	/**
	 * Called by the Arena to pass along information about the current
	 * problem.
	 * @param component A container for the particulars of the problem.
	 * @param language The currently selected language.
	 * @param renderer A helper object to help format the problem
	 * statement.
	 */
	public void setProblemComponent(ProblemComponentModel component,
									Language language,
									Renderer renderer)
	{
		try
		{
			editor = new Editor(component, language, renderer);
		}
		catch (Exception exception)
		{
			logError("An error occured while loading the problem: " +
					 exception.getLocalizedMessage());
		}
	}

	/**
	 * Called by the Arena when it's time to show our configuration panel.
	 */
	public void configure()
	{
		loadConfiguration();

		configDialog = new JDialog();
		Container pane = configDialog.getContentPane();

		pane.setPreferredSize(new Dimension(550, 135));
		pane.setLayout(new GridBagLayout());
		pane.setForeground(Common.FG_COLOR);
		pane.setBackground(Common.WPB_COLOR);
		GridBagConstraints c = new GridBagConstraints();

		JLabel rootDirLabel = new JLabel("Storage Directory:", SwingConstants.RIGHT);
		rootDirLabel.setForeground(Common.FG_COLOR);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 5, 5, 5);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		pane.add(rootDirLabel, c);

		final JTextField rootDirField = new JTextField(rootDir.getPath(), 25);
		c.gridx = 1;
		c.gridy = 0;
		pane.add(rootDirField, c);

		JButton browseButton = new JButton("Browse");
		c.gridx = 2;
		c.gridy = 0;
		c.anchor = GridBagConstraints.BASELINE_LEADING;
		pane.add(browseButton, c);

		JLabel vimCommandLabel = new JLabel("Vim Command:", SwingConstants.RIGHT);
		vimCommandLabel.setForeground(Common.FG_COLOR);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		pane.add(vimCommandLabel, c);

		final JTextField vimCommandField = new JTextField(vimCommand, 25);
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 2;
		pane.add(vimCommandField, c);

		JButton closeButton = new JButton("Cancel");
		c.fill = GridBagConstraints.NONE;
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.EAST;
		pane.add(closeButton, c);

		JButton saveButton = new JButton("Save");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 2;
		c.gridy = 2;
		c.anchor = GridBagConstraints.EAST;
		pane.add(saveButton, c);
		configDialog.getRootPane().setDefaultButton(saveButton);

		browseButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent actionEvent)
			{
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File("."));
				chooser.setDialogTitle("Choose Storage Directory");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);

				if (chooser.showOpenDialog(configDialog) == JFileChooser.APPROVE_OPTION)
				{
					rootDirField.setText(chooser.getSelectedFile().getPath());
				}
			}
		});

		closeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent actionEvent)
			{
				configDialog.dispose();
			}
		});

		saveButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent actionEvent)
			{
				prefs.setProperty(VIMCOMMAND, vimCommandField.getText());
				prefs.setProperty(ROOTDIR, rootDirField.getText());
				configDialog.dispose();
			}
		});

		configDialog.setTitle("VimCoder Preferences");
		configDialog.pack();
		configDialog.setLocationByPlatform(true);
		configDialog.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
		configDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		configDialog.setVisible(true);
	}


	/**
	 * Load the local preferences related to this plugin.
	 */
	private void loadConfiguration()
	{
		String vc  = prefs.getProperty(VIMCOMMAND);
		if (vc != null) vimCommand = vc;

		String dir = prefs.getProperty(ROOTDIR);
		if (dir != null) rootDir = new File(dir);
	}


	/**
	 * A generic logging function, appends text to the text area.  A timestamp
	 * is also prepended to the next text.
	 * @param what The text to append.
	 */
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

	/**
	 * Output non-critical messages to the log.
	 * @param what The text of the message.
	 */
	private void logInfo(String what)
	{
		log(" INFO: " + what + System.getProperty("line.separator"));
	}

	/**
	 * Output critical messages and errors to the log.
	 * @param what The text of the message.
	 */
	private void logError(String what)
	{
		log("ERROR: " + what + System.getProperty("line.separator"));
	}
}

