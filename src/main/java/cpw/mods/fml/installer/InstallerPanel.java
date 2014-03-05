package cpw.mods.fml.installer;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;

import cpw.mods.fml.installer.mirror.Mirror;

@SuppressWarnings("serial")
public class InstallerPanel extends JPanel {
	
	private JDialog dialog;
	private JButton sponsorButton;
	private JPanel sponsorPanel;
	private JComboBox<ProfileInfo> profileChooser;

	public InstallerPanel(File targetDir) {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		BufferedImage image;
		try {
			image = ImageIO.read(SimpleInstaller.class.getResourceAsStream(VersionInfo.getLogoFileName()));
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}

		JLabel spacer = new JLabel(" ");
		JPanel logoSplash = new JPanel();
		logoSplash.setLayout(new BoxLayout(logoSplash, BoxLayout.Y_AXIS));
		
		ImageIcon icon = new ImageIcon(image);
		JLabel logoLabel = new JLabel(icon);
		logoLabel.setAlignmentX(CENTER_ALIGNMENT);
		logoLabel.setAlignmentY(CENTER_ALIGNMENT);
		logoLabel.setSize(image.getWidth(), image.getHeight());
		logoSplash.add(logoLabel);
		
		String welcomeMessage = VersionInfo.getWelcomeMessage();
		
		for (String line : Splitter.on('\n').omitEmptyStrings().split(welcomeMessage)) {
			JLabel tag = new JLabel(line);
			tag.setAlignmentX(CENTER_ALIGNMENT);
			tag.setAlignmentY(CENTER_ALIGNMENT);
			logoSplash.add(tag);
		}
		
		
		logoSplash.setAlignmentX(CENTER_ALIGNMENT);
		logoSplash.setAlignmentY(TOP_ALIGNMENT);
		this.add(logoSplash);

        sponsorPanel = new JPanel();
        sponsorPanel.setLayout(new BoxLayout(sponsorPanel, BoxLayout.X_AXIS));
        sponsorPanel.setAlignmentX(CENTER_ALIGNMENT);
        sponsorPanel.setAlignmentY(CENTER_ALIGNMENT);

        sponsorButton = new JButton();
        sponsorButton.setAlignmentX(CENTER_ALIGNMENT);
        sponsorButton.setAlignmentY(CENTER_ALIGNMENT);
        sponsorButton.setBorderPainted(false);
        sponsorButton.setOpaque(false);
        sponsorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Desktop.getDesktop().browse(new URI(sponsorButton.getToolTipText()));
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run()
                        {
                            InstallerPanel.this.dialog.toFront();
                            InstallerPanel.this.dialog.requestFocus();
                        }
                    });
                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(InstallerPanel.this, "An error occurred launching the browser", "Error launching browser", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        sponsorPanel.add(sponsorButton);

        this.add(sponsorPanel);
		
		JPanel profilePanel = new JPanel();
		profilePanel.setAlignmentX(CENTER_ALIGNMENT);
		profilePanel.setAlignmentY(CENTER_ALIGNMENT);
        profilePanel.setOpaque(false);
		profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));

		profilePanel.add(spacer);
		
		JLabel profileLabel = new JLabel("Active Profile:");
		profileLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		profileLabel.setAlignmentX(LEFT_ALIGNMENT);
		profileLabel.setAlignmentY(CENTER_ALIGNMENT);
		profilePanel.add(profileLabel);
		
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
		
		this.add(profilePanel);
		
		updateSponsorDetails();
	}
	
	private void updateSponsorDetails() {
		Mirror mirror = VersionInfo.getMirrorData().getMirror();
		String sponsorMessage = mirror.getSponsorMessage();
        if (sponsorMessage != null)
        {
            sponsorButton.setText(sponsorMessage);
            sponsorButton.setToolTipText(mirror.getHomepage().toString());
            if (mirror.getLogo() != null)
            {
                sponsorButton.setIcon(mirror.getLogo());
            }
            else
            {
                sponsorButton.setIcon(null);
            }
            sponsorPanel.setVisible(true);
        }
        else
        {
            sponsorPanel.setVisible(false);
        }
	}


	public void run() {
		JOptionPane optionPane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

		Frame emptyFrame = new Frame("Mod system installer");
		emptyFrame.setUndecorated(true);
		emptyFrame.setVisible(true);
		emptyFrame.setLocationRelativeTo(null);
		dialog = optionPane.createDialog(emptyFrame, VersionInfo.getTitle());
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
		int result = (Integer) (optionPane.getValue() != null ? optionPane.getValue() : -1);
		if (result == JOptionPane.OK_OPTION) {
			SimpleInstaller.runInstaller();
		}
		dialog.dispose();
		emptyFrame.dispose();
	}
}
