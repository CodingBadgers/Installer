package cpw.mods.fml.installer.mods;

import java.io.File;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import org.apache.commons.io.FileUtils;

import argo.jdom.JsonNode;
import cpw.mods.fml.installer.DownloadFile;
import cpw.mods.fml.installer.IMonitor;

public class ResourcePackInfo {

	private String name;
	private URL url;
	
	public ResourcePackInfo(JsonNode json) {
		try {
			this.name = json.getStringValue("name");
			this.url = new URL(json.getStringValue("url"));
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "There was a error reading the resource pack information", "Error", JOptionPane.ERROR_MESSAGE);
			return;	
		}
	}
	
	public DownloadFile createDownload(File target, IMonitor monitor) {
		File dest = FileUtils.getFile(target, "resourcepacks", name + ".zip");
		if (!dest.getParentFile().exists() && dest.getParentFile().isDirectory()) {
			dest.getParentFile().mkdirs();
		}
		return new DownloadFile(url, dest);
	}
}
