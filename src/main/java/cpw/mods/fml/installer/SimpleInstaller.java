package cpw.mods.fml.installer;

import io.github.thefishlive.bootstrap.Bootstrapper;
import io.github.thefishlive.bootstrap.Launcher;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;

public class SimpleInstaller implements Launcher {

	public static URL profileFileLocation;
	public static File installdir = null;
	public static boolean headless = true;

	private static OptionParser parser;
	private static OptionSpecBuilder helpOption;
	private static OptionSpecBuilder headlessOption;
	private static OptionSpec<String> profileFileOption;
	private static OptionSpec<String> installdirOption;

	static {
		parser = new OptionParser();
		helpOption = parser.acceptsAll(Arrays.asList("h", "help"), "Help with this installer");
		headlessOption = parser.acceptsAll(Arrays.asList("headless"), "Launches the installer without a gui");
		installdirOption = parser.acceptsAll(Arrays.asList("dir", "install"), "Sets the installation directory").withRequiredArg();
		profileFileOption = parser.acceptsAll(Arrays.asList("profile", "p", "file"), "Specifies the profile file location to use").withRequiredArg();
	}
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Throwable {
		try {
			Bootstrapper.launch(SimpleInstaller.class, args);
		} catch (InvocationTargetException ex) {
			throw ex.getCause();
		}
	}
	
	public void launch(String[] args) throws IOException {
		headless = false;
		profileFileLocation = new URL("http://mcbadgercraft.com/adminpack/");

		OptionSet optionSet = parser.parse(args);

		if (optionSet.specs().size() > 0) {
			if (!handleOptions(parser, optionSet)) {
				return;
			}
		}
		
		System.out.println(profileFileLocation);
		if (System.getenv("headless") != null || VersionInfo.isHeadless()) {
			System.setProperty("java.awt.headless", "true");
			headless = true;
		}
		
		if (headless) {
			runInstaller();
			return;
		}
		
		launchGui();
	}
	
	public static void displayMessage(String message, String title) {
		if (!SimpleInstaller.headless) {
			try {
				JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
				return;
			} catch (Exception ex) {
			}
		}
		
		System.out.println(title);
		System.out.println(message);
	}

	public static boolean runInstaller() {
		InstallerAction action = InstallerAction.CLIENT;
		
		if (action.run(installdir)) {
			if (headless) {
				System.out.println(action.getSuccessMessage());
			} else {
				JOptionPane.showMessageDialog(null, action.getSuccessMessage(), "Complete", JOptionPane.INFORMATION_MESSAGE);
			}
			
			return true;
		} else {
			return false;
		}
	}

	private static boolean handleOptions(OptionParser parser, OptionSet optionSet) throws IOException {
		
		if (optionSet.has(helpOption)) {
			parser.printHelpOn(System.err);
			return false;
		}
		
		if (optionSet.has(headlessOption)) {
			System.out.println("Running in headless mode");
			headless = true;
		}
		
		if (optionSet.has(profileFileOption)) {
			String file = optionSet.valueOf(profileFileOption);
			
			try {
				URL url = new URL(file);
				profileFileLocation = url;
			} catch (Exception ex) {
				System.out.println("Unkown url provided, exiting");
			}
		}
		
		if (optionSet.has(installdirOption)) {
			installdir = new File(optionSet.valueOf(installdirOption));
		}
		
		return true;
	}

	private static void launchGui() {
		
		if (installdir == null) {
			String userHomeDir = System.getProperty("user.home", ".");
			String osType = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
			String mcDir = ".minecraft";
			
			if (osType.contains("win") && System.getenv("APPDATA") != null) {
				installdir = new File(System.getenv("APPDATA"), mcDir);
			} else if (osType.contains("mac")) {
				installdir = new File(new File(new File(userHomeDir, "Library"), "Application Support"), "minecraft");
			} else {
				installdir = new File(userHomeDir, mcDir);
			}
		}

		try {
			VersionInfo.getVersionTarget();
		} catch (Throwable e) {
			e.printStackTrace();
			if (!headless) {
				JOptionPane.showMessageDialog(null, "Corrupt download detected, cannot install", "Error", JOptionPane.ERROR_MESSAGE);
			}
			return;
		}

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		InstallerPanel panel = new InstallerPanel(installdir);
		panel.run();
	}

}
