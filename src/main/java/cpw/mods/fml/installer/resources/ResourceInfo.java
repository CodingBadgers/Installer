package cpw.mods.fml.installer.resources;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.JOptionPane;

import cpw.mods.fml.installer.IMonitor;
import cpw.mods.fml.installer.download.DownloadFile;
import cpw.mods.fml.installer.resources.installer.InstallMethod;

import argo.jdom.JsonNode;

public class ResourceInfo {

	private String modName;
	private String modVersion;
	private String modArtifactId;
	private URL modDownload;
	private FileType filetype;
	private InstallMethod installMethod;
	private ConfigInfo[] configs = new ConfigInfo[0];
	
	public ResourceInfo(JsonNode mod) {
		try {
			modName = mod.getStringValue("name");
			modVersion = mod.getStringValue("version");
			modArtifactId = mod.getStringValue("artifactId");
			modDownload = new URL(mod.getStringValue("url"));
			filetype = FileType.valueOf(mod.getStringValue("filetype"));
			installMethod = InstallMethod.valueOf(mod.getStringValue("installMethod"));
			
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
			JOptionPane.showMessageDialog(null, "There was a error reading the mod information", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}
	
	public String getModName() {
		return modName;
	}
	
	public String getModVersion() {
		return modVersion;
	}
	
	public String getModArtifactId() {
		return modArtifactId;
	}
	
	public URL getModDownload() {
		return modDownload;
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
		
		return new DownloadFile(modDownload, target);
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
