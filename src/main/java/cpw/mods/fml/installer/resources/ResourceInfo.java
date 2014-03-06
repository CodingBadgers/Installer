package cpw.mods.fml.installer.resources;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import cpw.mods.fml.installer.IMonitor;
import cpw.mods.fml.installer.download.DownloadFile;
import cpw.mods.fml.installer.resources.installer.InstallMethod;

import argo.jdom.JsonNode;

public class ResourceInfo {

	private static final Pattern ARTIFACT_PATTERN = Pattern.compile("([\\w\\.]+):([\\w]+):([\\w-\\.]+)");
	
	private String name;
	private String version;
	private String group;
	
	private String artifactId;
	private URL download;
	private FileType filetype;
	private InstallMethod installMethod;
	
	private ConfigInfo[] configs = new ConfigInfo[0];

	
	public ResourceInfo(JsonNode mod) {
		try {
			artifactId = mod.getStringValue("artifactId");
			download = new URL(mod.getStringValue("url"));
			filetype = FileType.valueOf(mod.getStringValue("filetype"));
			installMethod = InstallMethod.valueOf(mod.getStringValue("installMethod"));

			Matcher regex = ARTIFACT_PATTERN.matcher(artifactId);
			
			if (!regex.matches()) {
				throw new Exception("Artifact pattern is not in the correct format");
			}
			
			group = regex.group(1);
			name = regex.group(2);
			version = regex.group(3);
			
			if (mod.isArrayNode("config")) {
				List<JsonNode> json = mod.getArrayNode("config");
				configs = new ConfigInfo[json.size()];
				int i = 0;
				
				for(JsonNode node : json) {
					configs[i] = new ConfigInfo(node);
					i++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "There was a error reading the mod information" + (artifactId != null ? " " + artifactId : ""), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}
	
	public String getModName() {
		return name;
	}
	
	public String getModVersion() {
		return version;
	}
	
	public String getGroupId() {
		return group;
	}
	
	public String getModArtifactId() {
		return artifactId;
	}
	
	public URL getModDownload() {
		return download;
	}

	public String getModFileName() {
		return filetype.createFileName(this);
	}
	
	public DownloadFile createDownload(File dir, IMonitor monitor) throws ReflectiveOperationException {
		File target = InstallMethod.getInstaller(installMethod, this).getDownload(dir);
		
		try {
			if (target.exists()) {
				if (!target.delete()) {
					System.out.println("Could not delete file " + target.getAbsolutePath());
					return null;
				}
			}		
			
			if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
				System.out.println("Could not create parent directory (" + target.getParentFile() + ")");
				return null;
			}
			
			target.createNewFile();
		} catch (IOException e) {
			System.err.println("Error creating download for " + getModArtifactId());
			e.printStackTrace();
			return null;
		}
		
		return new DownloadFile(download, target);
	}
	
	public DownloadFile[] createConfigDownloads(File dir, IMonitor monitor) {
		DownloadFile[] downloads = new DownloadFile[configs.length];
		int i = 0;
		for (ConfigInfo info : configs) {
			downloads[i++] = info.createDownload(dir, monitor);
		}
		return downloads;
	}
}
