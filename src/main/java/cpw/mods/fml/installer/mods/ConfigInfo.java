package cpw.mods.fml.installer.mods;

import java.io.File;
import java.net.URL;

import javax.swing.ProgressMonitor;

import org.apache.commons.io.FileUtils;

import cpw.mods.fml.installer.DownloadFile;

import argo.jdom.JsonNode;

public class ConfigInfo {

	private String filename;
	private String directory;
	private URL url;

	public ConfigInfo(JsonNode json) throws Exception {
		this.filename = json.getStringValue("filename");
		this.directory = json.getStringValue("directory");		
		this.url = new URL(json.getStringValue("url"));
	}
	
	public DownloadFile createDownload(File target, ProgressMonitor monitor) {
		File dest = FileUtils.getFile(target, directory.replace('/', File.separatorChar), filename);
		if (!dest.getParentFile().exists() && dest.getParentFile().isDirectory()) {
			dest.getParentFile().mkdirs();
		}
		return new DownloadFile(url, dest);
	}
}
