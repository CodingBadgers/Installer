package cpw.mods.fml.installer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;

import argo.format.PrettyJsonFormatter;
import argo.jdom.JdomParser;
import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;
import argo.saj.InvalidSyntaxException;
import static argo.jdom.JsonNodeFactories.*;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import cpw.mods.fml.installer.download.Download;
import cpw.mods.fml.installer.download.DownloadFile;
import cpw.mods.fml.installer.download.DownloadLibrary;
import cpw.mods.fml.installer.download.DownloadMinecraftJar;
import cpw.mods.fml.installer.download.DownloadUtils;
import cpw.mods.fml.installer.resources.ResourceInfo;

public class ClientInstall implements ActionType {

	@Override
	public boolean run(File launcherdir) {
		File gamedir = new File(launcherdir.getParentFile(), "." + VersionInfo.getProfileName().toLowerCase());
		
		if (!launcherdir.exists()) {
			displayError("There is no minecraft installation at this location! (" + launcherdir.getAbsolutePath() + ")\nHave you run the minecraft installer atleast once?");
			return false;
		}
		
		// load data for installer
		File launcherProfiles = new File(launcherdir, "launcher_profiles.json");
		if (!launcherProfiles.exists()) {
			displayError("There is no minecraft launcher profile at this location, you need to run the launcher first!");
			return false;
		}

		File versionRootDir = new File(launcherdir, "versions");
		File versionTarget = new File(versionRootDir, VersionInfo.getVersionTarget());
		if (!versionTarget.mkdirs() && !versionTarget.isDirectory()) {
			if (!versionTarget.delete()) {
				displayError("There was a problem with the launcher version data. You will need to clear " + versionTarget.getAbsolutePath() + " manually");
			} else {
				versionTarget.mkdirs();
			}
		}

		// Write version data file
		File versionJsonFile = new File(versionTarget, VersionInfo.getVersionTarget() + ".json");
		File clientJarFile = new File(versionTarget, VersionInfo.getVersionTarget() + ".jar");
		File minecraftJarFile = VersionInfo.getMinecraftFile(versionRootDir);
		
		if (!minecraftJarFile.exists()) {
			try {
				DownloadMinecraftJar download = new DownloadMinecraftJar(VersionInfo.getMinecraftVersion(), clientJarFile);
				download.download(DownloadUtils.buildMonitor(), 0);
				download.close();
			} catch (IOException e) {
				displayError("Error downloading minecraft jar from mojang repository (" + e.getMessage() + ")");
				return false;
			}
		} else {
			try {
				Files.copy(minecraftJarFile, clientJarFile);
			} catch (IOException e1) {
				displayError("Error copying minecraft jar to installer directory (" + e1.getMessage() + ")");
				return false;
			}
		}

		JsonRootNode versionJson = object(VersionInfo.getVersionInfo().getFields());

		try {
			BufferedWriter newWriter = Files.newWriter(versionJsonFile, Charsets.UTF_8);
			PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(versionJson, newWriter);
			newWriter.close();
		} catch (Exception e) {
			displayError("There was a problem writing the launcher version data,  is it write protected?");
			return false;
		}
		
		// Copy special files to new gamedir
		File[] dataFiles = new File[] {
			new File(launcherdir, "servers.dat"),
			new File(launcherdir, "options.txt")
		};
		
		for (File dataFile : dataFiles) {
			if (dataFile.exists()) {
				try {
					File newData = new File(gamedir, dataFile.getName());
					FileUtils.copyFile(dataFile, newData);
				} catch (IOException e) {
					displayError("Error copying data file to game dir (" + e.getMessage() + ")");
				}
			}
		}
		
		try {
			//clear mods directory
			List<ResourceInfo> resources = VersionInfo.getResources();
			
			File modsFolder = new File(gamedir, "mods");

			if (!modsFolder.exists()) {
				modsFolder.mkdir();
			} else {
				File[] existingFiles = modsFolder.listFiles(new FileFilter() {
					@Override
					public boolean accept(File file) {
						return file.isFile();
					}
				});

				for (File file : existingFiles) {
					if (!file.delete()) {
						displayError("Error deleting file " + file.getName() + " from your mods folder please delete this file, this may affect your minecraft stability.");
						continue;
					}
				}
			}

			// Download accompanying resources
			List<Download> downloads = new ArrayList<>();

			IMonitor monitor = DownloadUtils.buildMonitor();
			monitor.setMaximum(resources.size() + 1);
			int totalCount = 0;
			int i = 1;

			for (ResourceInfo mod : resources) {
				DownloadFile download = mod.createDownload(gamedir, monitor);
				
				if (download == null) {
					displayError("There was a error setting up the download for " + mod.getModName() + " skipping download.");
					continue;
				}
								
				totalCount += download.getFileSize();
				downloads.add(download);
				
				for (DownloadFile info : mod.createConfigDownloads(gamedir, monitor)) {
					totalCount += info.getFileSize();
					downloads.add(info);
				}
				
				monitor.setProgress(i++);
			}
			
			for (JsonNode library : VersionInfo.getVersionInfo().getArrayNode("libraries")) {
				downloads.add(new DownloadLibrary(library));
			}

			monitor.setMaximum(totalCount);
			int count = 0;
			
			/*
			 * Setup
			 * Download
			 * Close
			 */
			
			for (Download download : downloads) {
				download.setup();
			}

			for (Download download : downloads) {
				count = download.download(monitor, count);
			}
			
			for (Download download : downloads) {
				download.close();
			}

			monitor.setProgress(monitor.getMaximum());

		} catch (Exception e) {
			e.printStackTrace();
			displayError("There was a unexpected exception (" + e.getClass().getSimpleName() + ")");
			return false;
		}

		JdomParser parser = new JdomParser();
		JsonRootNode jsonProfileData;

		try {
			BufferedReader reader = Files.newReader(launcherProfiles, Charsets.UTF_8);
			jsonProfileData = parser.parse(reader);
			reader.close();
		} catch (InvalidSyntaxException e) {
			displayError("The launcher profile file is corrupted. Re-run the minecraft launcher to fix it!");
			return false;
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}

		JsonField[] fields = new JsonField[] {
				field("playerUUID", string("DUMMY-UUID")),
				field("name", string(VersionInfo.getProfileName())), 
				field("lastVersionId", string(VersionInfo.getVersionTarget())), 
				field("launcherVisibilityOnGameClose", string("keep the launcher open")), 
				field("javaArgs", string("-Xmx1G -Dfml.ignoreInvalidMinecraftCertificates=true -Dfml.ignorePatchDiscrepancies=true")),
				field("gameDir", string(gamedir.getAbsolutePath()))
		};

		if (ProfileInfo.getCurrent() != null) {
			fields[0] = field("playerUUID", string(ProfileInfo.getCurrent().getUUID()));
		}

		HashMap<JsonStringNode, JsonNode> profileCopy = Maps.newHashMap(jsonProfileData.getNode("profiles").getFields());
		HashMap<JsonStringNode, JsonNode> rootCopy = Maps.newHashMap(jsonProfileData.getFields());
		profileCopy.put(string(VersionInfo.getProfileName()), object(fields));
		JsonRootNode profileJsonCopy = object(profileCopy);

		rootCopy.put(string("profiles"), profileJsonCopy);
		rootCopy.put(string("selectedProfile"), string(VersionInfo.getProfileName()));

		jsonProfileData = object(rootCopy);

		try {
			BufferedWriter newWriter = Files.newWriter(launcherProfiles, Charsets.UTF_8);
			PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(jsonProfileData, newWriter);
			newWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
			displayError("There was a problem writing the launch profile,  is it write protected?");
			return false;
		}

		return true;
	}

	private void displayError(String error) {
		if (SimpleInstaller.headless) {
			System.err.println(error);
		} else {
			JOptionPane.showMessageDialog(null, error, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	@Override
	public boolean isPathValid(File targetDir) {
		if (targetDir.exists()) {
			File launcherProfiles = new File(targetDir, "launcher_profiles.json");
			return launcherProfiles.exists();
		}
		return false;
	}
	
	@Override
	public String getFileError(File targetDir) {
		if (targetDir.exists()) {
			return "The directory is missing a launcher profile. Please run the minecraft launcher first";
		} else {
			return "There is no minecraft directory set up. Either choose an alternative, or run the minecraft launcher to create one";
		}
	}

	@Override
	public String getSuccessMessage() {
		return String.format("Successfully installed client profile %s for version %s into launcher", VersionInfo.getProfileName(), VersionInfo.getMinecraftVersion());
	}
}
