package cpw.mods.fml.installer;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import com.google.common.base.Throwables;

@SuppressWarnings("serial")
public class InstallerPanel extends JPanel {
	private File targetDir;
	private JTextField selectedDirText;
	private JLabel infoLabel;
	private JDialog dialog;
	private JPanel fileEntryPanel;
	private JComboBox<ProfileInfo> profileChooser;

	private class FileSelectAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser dirChooser = new JFileChooser();
			dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			dirChooser.setFileHidingEnabled(false);
			dirChooser.ensureFileIsVisible(targetDir);
			dirChooser.setSelectedFile(targetDir);
			int response = dirChooser.showOpenDialog(InstallerPanel.this);
			switch (response) {
			case JFileChooser.APPROVE_OPTION:
				targetDir = dirChooser.getSelectedFile();
				updateFilePath();
				break;
			default:
				break;
			}
		}
	}

	public InstallerPanel(File targetDir) {
		this.targetDir = targetDir;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		BufferedImage image;
		try {
			image = ImageIO.read(SimpleInstaller.class.getResourceAsStream(VersionInfo.getLogoFileName()));
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}

		JPanel logoSplash = new JPanel();
		logoSplash.setLayout(new BoxLayout(logoSplash, BoxLayout.Y_AXIS));
		
		ImageIcon icon = new ImageIcon(image);
		JLabel logoLabel = new JLabel(icon);
		logoLabel.setAlignmentX(CENTER_ALIGNMENT);
		logoLabel.setAlignmentY(CENTER_ALIGNMENT);
		logoLabel.setSize(image.getWidth(), image.getHeight());
		logoSplash.add(logoLabel);
		
		JLabel tag = new JLabel(VersionInfo.getWelcomeMessage());
		tag.setAlignmentX(CENTER_ALIGNMENT);
		tag.setAlignmentY(CENTER_ALIGNMENT);
		logoSplash.add(tag);

		logoSplash.setAlignmentX(CENTER_ALIGNMENT);
		logoSplash.setAlignmentY(TOP_ALIGNMENT);
		this.add(logoSplash);
		
		JPanel profilePanel = new JPanel();
		profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
		profileChooser = new JComboBox<ProfileInfo>();
		for (ProfileInfo info : VersionInfo.getAccounts(targetDir)) {
			profileChooser.addItem(info);
		}
		profileChooser.setToolTipText("Choose your desired minecraft auth profile");
		profileChooser.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					ProfileInfo.setCurrent((ProfileInfo)e.getItem());
				}
			}		
		});
		profilePanel.add(profileChooser);
		profilePanel.setAlignmentX(CENTER_ALIGNMENT);
		profilePanel.setAlignmentY(TOP_ALIGNMENT);
		this.add(profilePanel);
		
		JPanel entryPanel = new JPanel();
		entryPanel.setLayout(new BoxLayout(entryPanel, BoxLayout.X_AXIS));
		
		selectedDirText = new JTextField();
		selectedDirText.setEditable(false);
		selectedDirText.setToolTipText("Path to minecraft");
		selectedDirText.setColumns(30);
		entryPanel.add(selectedDirText);
		JButton dirSelect = new JButton();
		dirSelect.setAction(new FileSelectAction());
		dirSelect.setText("...");
		dirSelect.setToolTipText("Select an alternative minecraft directory");
		
		entryPanel.add(dirSelect);
		entryPanel.setAlignmentX(LEFT_ALIGNMENT);
		entryPanel.setAlignmentY(TOP_ALIGNMENT);
		
		infoLabel = new JLabel();
		infoLabel.setHorizontalTextPosition(JLabel.LEFT);
		infoLabel.setVerticalTextPosition(JLabel.TOP);
		infoLabel.setAlignmentX(LEFT_ALIGNMENT);
		infoLabel.setAlignmentY(TOP_ALIGNMENT);
		infoLabel.setForeground(Color.RED);
		infoLabel.setVisible(false);

		fileEntryPanel = new JPanel();
		fileEntryPanel.setLayout(new BoxLayout(fileEntryPanel, BoxLayout.Y_AXIS));
		fileEntryPanel.add(infoLabel);
		fileEntryPanel.add(Box.createVerticalGlue());
		fileEntryPanel.add(entryPanel);
		fileEntryPanel.setAlignmentX(CENTER_ALIGNMENT);
		fileEntryPanel.setAlignmentY(TOP_ALIGNMENT);
		this.add(fileEntryPanel);
		updateFilePath();
	}

	private void updateFilePath() {
		try {
			targetDir = targetDir.getCanonicalFile();
			selectedDirText.setText(targetDir.getPath());
		} catch (IOException e) {

		}

		InstallerAction action = InstallerAction.CLIENT;
		boolean valid = action.isPathValid(targetDir);

		if (valid) {
			selectedDirText.setForeground(Color.BLACK);
			infoLabel.setVisible(false);
			fileEntryPanel.setBorder(null);
			if (dialog != null) {
				dialog.invalidate();
				dialog.pack();
			}
		} else {
			selectedDirText.setForeground(Color.RED);
			fileEntryPanel.setBorder(new LineBorder(Color.RED));
			infoLabel.setText("<html>" + action.getFileError(targetDir) + "</html>");
			infoLabel.setVisible(true);
			if (dialog != null) {
				dialog.invalidate();
				dialog.pack();
			}
		}
	}

	public void run() {
		JOptionPane optionPane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

		Frame emptyFrame = new Frame("Mod system installer");
		emptyFrame.setUndecorated(true);
		emptyFrame.setVisible(true);
		emptyFrame.setLocationRelativeTo(null);
		dialog = optionPane.createDialog(emptyFrame, "Mod system installer");
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
		int result = (Integer) (optionPane.getValue() != null ? optionPane.getValue() : -1);
		if (result == JOptionPane.OK_OPTION) {
			InstallerAction action = InstallerAction.CLIENT;;
			if (action.run(targetDir)) {
				JOptionPane.showMessageDialog(null, action.getSuccessMessage(), "Complete", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		dialog.dispose();
		emptyFrame.dispose();
	}
}
