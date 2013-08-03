package cpw.mods.fml.installer.mods;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import cpw.mods.fml.installer.DownloadFile;

import argo.jdom.JsonNode;

public class ModInfo {

	private String modName;
	private String modVersion;
	private String modArtifactId;
	private URL modDownload;
	private FileType filetype;
	private InstallMethod installMethod;
	
	public ModInfo(JsonNode mod) {
		try {
			modName = mod.getStringValue("name");
			modVersion = mod.getStringValue("version");
			modArtifactId = mod.getStringValue("artifactId");
			modDownload = new URL(mod.getStringValue("url"));
			filetype = FileType.valueOf(mod.getStringValue("filetype"));
			installMethod = InstallMethod.valueOf(mod.getStringValue("installMethod"));
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
	
	public DownloadFile createDownload(File dir, ProgressMonitor monitor) throws ReflectiveOperationException {
		File target = installMethod.getInstaller(this).getDownload(dir);
		
		try {
			if (target.exists()) {
				if (!target.delete()) {
					System.out.println("Could not delete file " + target.getAbsolutePath());
					return null;
				}
			}		
			target.createNewFile();
		} catch (IOException e) {
			System.err.println("Error creating download for " + getModArtifactId());
			e.printStackTrace();
			return null;
		}
		
		return new DownloadFile(modDownload, target);
	}
}
