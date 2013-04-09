
package com.dogcows;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

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
	public final static String version = "VimCoder 0.3.5";

	/**
	 * The website of the plugin project.
	 */
	public final static String website = "http://github.com/chazmcgarvey/vimcoder";


	/**
	 * The first part of the command used to invoke the Vim server.
	 */
	private static String vimCommand = "gvim";

	/**
	 * The path to the main VimCoder directory.
	 */
	private static File rootDir;
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
	 * Whether or not to use the contest name and point value as problem
	 * directory names.
	 */
	private static boolean contestDirNames = false;


	/**
	 * The panel given to the Arena applet when it is requested.
	 */
	private JPanel panel;

	/**
	 * The text widget where log messages are appended.
	 */
	private JTextArea logArea;

	/**
	 * The current editor object (or null if there is none).
	 */
	private Editor editor;

	/**
	 * The configuration panel.
	 */
	private JDialog configDialog;


	/**
	 * The key for the vim command preference.
	 */
	private final static String VIMCOMMAND = "com.dogcows.VimCoder.config.vimcommand";

	/**
	 * The key for the root directory preference.
	 */
	private final static String ROOTDIR = "com.dogcows.VimCoder.config.rootdir";

	/**
	 * The key for the problem directory name preference.
	 */
	private final static String CONTESTDIRNAMES = "com.dogcows.VimCoder.config.contestdirnames";

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
	 * Get whether or not to save problems in a human-readable directory
	 * structure.
	 * @return The directory name setting.
	 */
	public static boolean isContestDirNames()
	{
		return contestDirNames;
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
			logError("Failed to get source code: " + exception.getLocalizedMessage());
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
					Language language, Renderer renderer)
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
		final int border = 10;
		final int inset = 2;

		loadConfiguration();

		configDialog = new JDialog();
		Container container = configDialog.getContentPane();
		container.setForeground(Common.FG_COLOR);
		container.setBackground(Common.WPB_COLOR);

		JPanel pane = new JPanel();
		container.add(pane);

		BoxLayout boxLayout = new BoxLayout(pane, BoxLayout.Y_AXIS);
		pane.setLayout(boxLayout);
		pane.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));

		JPanel fieldPanel = new JPanel(new GridBagLayout());
		pane.add(fieldPanel);
		pane.add(Box.createRigidArea(new Dimension(0, border)));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(inset, inset, inset, inset);

		JLabel rootDirLabel = new JLabel("Storage Directory:");
		rootDirLabel.setForeground(Common.FG_COLOR);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		fieldPanel.add(rootDirLabel, c);

		final JTextField rootDirField = new JTextField(rootDir.getPath());
		rootDirField.setPreferredSize(new Dimension(0, 24));
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1.0;
		fieldPanel.add(rootDirField, c);

		JButton browseButton = new JButton("Browse");
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.BASELINE_LEADING;
		fieldPanel.add(browseButton, c);

		final JCheckBox contestDirNamesButton = new JCheckBox(
			"Store problems according to contest name and point value.",
			contestDirNames
		);
		contestDirNamesButton.setForeground(Common.FG_COLOR);
		contestDirNamesButton.setBackground(Common.WPB_COLOR);
		contestDirNamesButton.setFont(rootDirLabel.getFont());
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 2;
		fieldPanel.add(contestDirNamesButton, c);

		JLabel vimCommandLabel = new JLabel("Vim Command:");
		vimCommandLabel.setForeground(Common.FG_COLOR);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		fieldPanel.add(vimCommandLabel, c);

		final JTextField vimCommandField = new JTextField(vimCommand);
		vimCommandField.setPreferredSize(new Dimension(0, 24));
		c.gridx = 1;
		c.gridy = 2;
		c.weightx = 1.0;
		c.gridwidth = 2;
		fieldPanel.add(vimCommandField, c);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, inset, inset));
		buttonPanel.setPreferredSize(new Dimension(400, 24 + 2 * inset));
		pane.add(buttonPanel);

		JButton saveButton = new JButton("Save");
		buttonPanel.add(saveButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(1, 0)));

		JButton closeButton = new JButton("Close");
		buttonPanel.add(closeButton);

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
				prefs.setProperty(CONTESTDIRNAMES, String.valueOf(contestDirNamesButton.isSelected()));
				JOptionPane.showMessageDialog(null, "Preferences were saved successfully.");
			}
		});

		configDialog.setTitle("VimCoder Preferences");
		configDialog.pack();
		configDialog.setLocationRelativeTo(null);	// Center dialog in screen.
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

		String cn  = prefs.getProperty(CONTESTDIRNAMES);
		if (cn != null) contestDirNames = Boolean.parseBoolean(cn);
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

// vim:noet:ts=8
