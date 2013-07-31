package cpw.mods.fml.installer;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import argo.jdom.JsonNode;

public class ModInfo {

	public String modName;
	public String modVersion;
	public URL modDownload;
	
	public ModInfo(JsonNode mod) {
		try {
			modName = mod.getStringValue("name");
			modVersion = mod.getStringValue("version");
			modDownload = new URL(mod.getStringValue("url"));
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
	
	public URL getModDownload() {
		return modDownload;
	}
	
	public DownloadFile downloadMod(File dir, ProgressMonitor monitor) {
		File target = new File(dir, getModFileName());
		
		try {
			if (target.exists()) {
				if (!target.delete()) {
					System.out.println("Could not delete file " + target.getAbsolutePath());
					return null;
				}
			}		
			target.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return new DownloadFile(modDownload, target);
	}

	private String getModFileName() {
		String extention = modDownload.getFile().substring(modDownload.getFile().lastIndexOf('.') + 1);
		return modName + "-" + modVersion + "." + extention;
	}
}