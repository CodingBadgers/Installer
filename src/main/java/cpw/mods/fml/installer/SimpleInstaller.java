package cpw.mods.fml.installer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;

public class SimpleInstaller {

	public static URL profileFileLocation;

	private static OptionSpecBuilder helpOption;
	private static OptionSpec<String> profileFileOption;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		profileFileLocation = new URL("http://mcbadgercraft.com/adminpack/");

		OptionParser parser = new OptionParser();
		helpOption = parser.acceptsAll(Arrays.asList("h", "help"), "Help with this installer");
		profileFileOption = parser.acceptsAll(Arrays.asList("profile", "p", "file"), "Specifies the profile file location to use").withRequiredArg();
		OptionSet optionSet = parser.parse(args);

		if (optionSet.specs().size() > 0) {
			if (!handleOptions(parser, optionSet)) {
				return;
			}
		}
		
		System.out.println(profileFileLocation);
		launchGui();
	}

	private static boolean handleOptions(OptionParser parser, OptionSet optionSet) throws IOException {
		
		if (optionSet.has(helpOption)) {
			parser.printHelpOn(System.err);
			return false;
		} else if (optionSet.has(profileFileOption)) {
			String file = optionSet.valueOf(profileFileOption);
			URL url = new URL(file);
			profileFileLocation = url;
			return true;
		}
		
		return true;
	}

	private static void launchGui() {
		String userHomeDir = System.getProperty("user.home", ".");
		String osType = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
		File targetDir = null;
		String mcDir = ".minecraft";
		
		if (osType.contains("win") && System.getenv("APPDATA") != null) {
			targetDir = new File(System.getenv("APPDATA"), mcDir);
		} else if (osType.contains("mac")) {
			targetDir = new File(new File(new File(userHomeDir, "Library"), "Application Support"), "minecraft");
		} else {
			targetDir = new File(userHomeDir, mcDir);
		}

		try {
			VersionInfo.getVersionTarget();
		} catch (Throwable e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Corrupt download detected, cannot install", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		InstallerPanel panel = new InstallerPanel(targetDir);
		panel.run();
	}

}
