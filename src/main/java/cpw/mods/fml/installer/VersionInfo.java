package cpw.mods.fml.installer;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import argo.jdom.JdomParser;
import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

import cpw.mods.fml.installer.mods.ModInfo;
import cpw.mods.fml.installer.mods.ResourcePackInfo;

public class VersionInfo {
	public static final int VERSION = 4;
	public static final VersionInfo INSTANCE = new VersionInfo();
	private static String forgeVersion;
	private static String minecraftVersion;
	private static String versionTarget;
	
	private JsonRootNode versionData;

	public VersionInfo() {
		try {
			versionData = parseStream(SimpleInstaller.profileFileLocation.openStream());

			int remoteVersion = Integer.parseInt(versionData.getNumberValue("version"));
			
			if (remoteVersion < VERSION) {
				SimpleInstaller.displayMessage("The profile file you are using is out of date\nplease specify a more uptodate file", "Out of Date");
				System.exit(1);
			} else if (remoteVersion > VERSION) {
				SimpleInstaller.displayMessage("The installer you are using is out of date\nplease update your installer.", "Out of Date");
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE)) {
					Desktop.getDesktop().browse(new URI("http://mcbadgercraft.com/adminpack/installer"));
				}
				System.exit(2);
			}
		} catch (Exception e) {
			SimpleInstaller.displayMessage("Error loading version data", "Error loading data");
			System.exit(3);
		}
	}

	private JsonRootNode parseStream(InputStream openStream) throws IOException, InvalidSyntaxException {
		JdomParser parser = new JdomParser();
		
		List<String> lines = IOUtils.readLines(openStream);
		JsonRootNode root = parser.parse(StringUtils.join(lines, ' '));
		minecraftVersion = replaceMacros(root.getStringValue("install", "minecraftVersion"));
		forgeVersion = replaceMacros(root.getStringValue("install", "forgeVersion"));
		versionTarget = replaceMacros(root.getStringValue("install", "target"));
		
		List<String> parsedLines = new ArrayList<String>();
		for (String line : lines) {
			parsedLines.add(replaceMacros(line));
		}
		
		return parser.parse(StringUtils.join(parsedLines, ' '));
	}
	
	private static String replaceMacros(String input) {
		if (getMinecraftVersion() != null) input = input.replace("${minecraft_version}", getMinecraftVersion());
		if (getForgeVersion() != null) input = input.replace("${forge_version}", getForgeVersion());
		if (getVersionTarget() != null) input = input.replace("${target}", getVersionTarget());
		return input;
	}
	
	public static boolean isHeadless() {
		return INSTANCE.versionData.getBooleanValue("install", "headless");
	}

	public static String getProfileName() {
		return INSTANCE.versionData.getStringValue("install", "profileName");
	}

	public static String getTitle() {
		return INSTANCE.versionData.getStringValue("install", "title");
	}

	public static String getWelcomeMessage() {
		return INSTANCE.versionData.getStringValue("install", "welcome");
	}

	public static String getLogoFileName() {
		return INSTANCE.versionData.getStringValue("install", "logo");
	}

	public static JsonNode getVersionInfo() {
		return INSTANCE.versionData.getNode("versionInfo");
	}

	private static String getForgeVersion() {
		return forgeVersion;
	}

	public static String getMinecraftVersion() {
		return minecraftVersion;
	}

	public static String getVersionTarget() {
		return versionTarget;
	}

	public static File getMinecraftFile(File path) {
		return new File(new File(path, getMinecraftVersion()), getMinecraftVersion() + ".jar");
	}

	public static List<ProfileInfo> getAccounts(File target) {
		List<ProfileInfo> profiles = new ArrayList<ProfileInfo>();
		JdomParser parser = new JdomParser();
		JsonRootNode jsonProfileData;

		try {
			jsonProfileData = parser.parse(Files.newReader(new File(target, "launcher_profiles.json"), Charsets.UTF_8));
		} catch (InvalidSyntaxException e) {
			JOptionPane.showMessageDialog(null, "The launcher profile file is corrupted. Re-run the minecraft launcher to fix it!", "Error", JOptionPane.ERROR_MESSAGE);
			return new ArrayList<ProfileInfo>();
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}

		for (JsonField field : jsonProfileData.getNode("authenticationDatabase").getFieldList()) {
			ProfileInfo info = new ProfileInfo(field.getValue());
			
			if (ProfileInfo.getCurrent() == null) {
				ProfileInfo.setCurrent(info);
			}
			profiles.add(info);
		}

		return profiles;
	}
	
	public static List<ModInfo> getModInfo() {
		List<ModInfo> modData = new ArrayList<ModInfo>();
		List<JsonNode> mods = INSTANCE.versionData.getArrayNode("mods");

		for (JsonNode mod : mods) {
			ModInfo modInfo = new ModInfo(mod);
			modData.add(modInfo);
		}

		return modData;
	}
	
	public static List<ResourcePackInfo> getResourcePacks() {
		List<ResourcePackInfo> packData = new ArrayList<ResourcePackInfo>();
		List<JsonNode> packs = INSTANCE.versionData.getArrayNode("resources", "packs");

		for (JsonNode pack : packs) {
			ResourcePackInfo modInfo = new ResourcePackInfo(pack);
			packData.add(modInfo);
		}

		return packData;
	}
}
