package cpw.mods.fml.installer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.OutputSupplier;

import cpw.mods.fml.installer.mods.ModInfo;
import cpw.mods.fml.installer.mods.ResourcePackInfo;

public class VersionInfo {
	public static final int VERSION = 4;
	public static final VersionInfo INSTANCE = new VersionInfo();
	private static String forgeVersion;
	private static String minecraftVersion;
	public final JsonRootNode versionData;

	public VersionInfo() {
		try {
			versionData = parseStream(SimpleInstaller.profileFileLocation.openStream());

			int remoteVersion = Integer.parseInt(versionData.getNumberValue("version"));
			if (remoteVersion < VERSION) {
				JOptionPane.showMessageDialog(null, "The profile file you are using is out of date, please specify a more uptodate file", "Out of Data", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			} else if (remoteVersion > VERSION) {
				JOptionPane.showMessageDialog(null, "The installer you are using is out of date, please update your installer", "Out of Data", JOptionPane.ERROR_MESSAGE);
				System.exit(2);
			}
			
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	private JsonRootNode parseStream(InputStream openStream) throws IOException, InvalidSyntaxException {
		JdomParser parser = new JdomParser();
		
		List<String> lines = IOUtils.readLines(openStream);
		JsonRootNode root = parser.parse(StringUtils.join(lines, ' '));
		forgeVersion = root.getStringValue("install", "forgeVersion");
		minecraftVersion = root.getStringValue("install", "minecraftVersion");
		
		List<String> parsedLines = new ArrayList<String>();
		for (String line : lines) {
			parsedLines.add(replaceMacros(line));
		}
		
		return parser.parse(StringUtils.join(parsedLines, ' '));
	}
	
	private static String replaceMacros(String input) {
		input = input.replace("${minecraft_version}", getMinecraftVersion());
		input = input.replace("${forge_version}", getForgeVersion());
		return input;
	}
	
	public static boolean isHeadless() {
		return INSTANCE.versionData.getBooleanValue("install", "headless");
	}

	public static String getProfileName() {
		return INSTANCE.versionData.getStringValue("install", "profileName");
	}

	public static String getVersionTarget() {
		return INSTANCE.versionData.getStringValue("install", "target");
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

	public static String getContainedFile() {
		return INSTANCE.versionData.getStringValue("install", "filePath");
	}

	public static void extractFile(File path) throws IOException {
		INSTANCE.doFileExtract(path);
	}

	private static String getForgeVersion() {
		return forgeVersion;
	}

	public static String getMinecraftVersion() {
		return minecraftVersion;
	}

	public static File getMinecraftFile(File path) {
		return new File(new File(path, getMinecraftVersion()), getMinecraftVersion() + ".jar");
	}

	public static File getLibraryPath(File root) {
		String path = INSTANCE.versionData.getStringValue("install", "path");
		String[] split = Iterables.toArray(Splitter.on(':').omitEmptyStrings().split(path), String.class);
		File dest = root;
		Iterable<String> subSplit = Splitter.on('.').omitEmptyStrings().split(split[0]);
		for (String part : subSplit) {
			dest = new File(dest, part);
		}
		dest = new File(new File(dest, split[1]), split[2]);
		String fileName = split[1] + "-" + split[2] + ".jar";
		return new File(dest, fileName);
	}

	public static URL getForgeDownloadUrl() {
		try {
			return new URL(replaceMacros(INSTANCE.versionData.getStringValue("install", "forgeUrl")));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void doFileExtract(File path) throws IOException {
		InputStream inputStream = getClass().getResourceAsStream("/" + getContainedFile());
		OutputSupplier<FileOutputStream> outputSupplier = Files.newOutputStreamSupplier(path);
		ByteStreams.copy(inputStream, outputSupplier);
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
