package com.betternpchighlight.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.betternpchighlight.service.ProfileService;
import com.betternpchighlight.service.ProfileService.ImportResult;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

/**
 * Sidebar panel for exporting the plugin settings as a shareable profile and importing one from another player.
 */
public class ProfilePanel extends PluginPanel {
	// Leaves room for the panel border so wrapped labels fit inside PANEL_WIDTH
	private static final int LABEL_WIDTH = 180;

	private final ProfileService profileService;
	private final JTextArea profileText = new JTextArea(14, 0);
	private final JLabel status = new JLabel();

	@Inject
	public ProfilePanel(ProfileService profileService) {
		this.profileService = profileService;
		setLayout(new BorderLayout(0, 6));

		JLabel description = new JLabel(wrap("Export your settings to share them, or paste someone else's profile and import it."));
		description.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		add(description, BorderLayout.NORTH);

		profileText.setLineWrap(true);
		add(new JScrollPane(profileText), BorderLayout.CENTER);

		JButton exportButton = new JButton("Export and copy");
		exportButton.addActionListener(e -> exportProfile());
		JButton pasteButton = new JButton("Paste from clipboard");
		pasteButton.addActionListener(e -> pasteProfile());
		JButton importButton = new JButton("Import");
		importButton.addActionListener(e -> importProfile());

		JPanel controls = new JPanel(new GridLayout(0, 1, 0, 4));
		controls.setOpaque(false);
		controls.add(exportButton);
		controls.add(pasteButton);
		controls.add(importButton);

		JPanel south = new JPanel(new BorderLayout(0, 6));
		south.setOpaque(false);
		south.add(controls, BorderLayout.NORTH);
		south.add(status, BorderLayout.SOUTH);
		add(south, BorderLayout.SOUTH);
	}

	private void exportProfile() {
		String profile = profileService.exportProfile();
		profileText.setText(profile);
		profileText.setCaretPosition(0);
		try
		{
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(profile), null);
			setStatus("Profile copied to clipboard.", true);
		}
		catch (IllegalStateException e)
		{
			setStatus("Clipboard unavailable. Copy the profile from the box above.", false);
		}
	}

	private void pasteProfile() {
		try
		{
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			if (!clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor))
			{
				setStatus("The clipboard does not contain any text.", false);
				return;
			}
			profileText.setText((String) clipboard.getData(DataFlavor.stringFlavor));
			profileText.setCaretPosition(0);
			setStatus("Pasted. Press Import to apply it.", true);
		}
		catch (IllegalStateException | UnsupportedFlavorException | IOException e)
		{
			setStatus("Could not read the clipboard.", false);
		}
	}

	private void importProfile() {
		int choice = JOptionPane.showConfirmDialog(this,
				"This will overwrite your current Better NPC Highlight settings. Continue?",
				"Import profile", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (choice != JOptionPane.YES_OPTION)
		{
			return;
		}

		ImportResult result = profileService.importProfile(profileText.getText());
		setStatus(result.getMessage(), result.isSuccess());
	}

	private void setStatus(String message, boolean success) {
		status.setForeground(success ? ColorScheme.PROGRESS_COMPLETE_COLOR : ColorScheme.PROGRESS_ERROR_COLOR);
		status.setText(wrap(message));
	}

	private static String wrap(String text) {
		return "<html><body style='width: " + LABEL_WIDTH + "px'>" + text + "</body></html>";
	}
}
