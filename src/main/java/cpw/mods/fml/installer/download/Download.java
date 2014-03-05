package cpw.mods.fml.installer.download;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;

import cpw.mods.fml.installer.IMonitor;
import cpw.mods.fml.installer.SimpleInstaller;

public abstract class Download implements Closeable {

	private static final int BUFFER = 1024;
	
	private URLConnection connection;
	private InputStream in;
	private OutputStream out;
	private int fileSize;
	
	protected boolean active = true;
	
	public abstract String getName();
	
	public abstract URL getDownloadUrl() throws IOException;
	
	public abstract File getTargetFile() throws IOException;
	
	public URL preDownload(URL url) throws IOException { return url; }
	
	public File postDownload(File dest) throws IOException { return dest; }
	
	public boolean setup() {
		if (!active) {
			return false;
		}
		
		URL url = null;
		File dest = null;
		
    	try {
    		url = getDownloadUrl();
    		dest = getTargetFile();
    		
			connection = url.openConnection();
			fileSize = connection.getContentLength();
			
			in = new BufferedInputStream(connection.getInputStream());
			
			if (!dest.getParentFile().exists() && !dest.getParentFile().mkdirs()) {
				SimpleInstaller.displayMessage("There was a error creating download folder for " + (url == null ? "" : "(" + url + ")") + ", skipping download", "Error");
				active = false;
				return active;
			}
			
			out = new FileOutputStream(dest);
			active = true;
		} catch (IOException e) {
			e.printStackTrace();
			SimpleInstaller.displayMessage("There was a error connecting to the download site " + (url == null ? "" : "(" + url + ")") + ", skipping download", "Error");
			active = false;
		} catch (Exception e) {
			e.printStackTrace();
			SimpleInstaller.displayMessage("A Unknown exception occurred whilst downloading the file " + getName() + ", skipping download", "Error");
			active = false;
		}
    	
		return active;
	}

	public int getFileSize() {
		return fileSize;
	}
	
	public int download(IMonitor monitor, int current) {
		URL url = null;
		File dest = null;
		
		try {
			url = getDownloadUrl();
			dest = getTargetFile();
		} catch (IOException e) {
			SimpleInstaller.displayMessage("There was a error downloading file from " + getName() + ", skipping download", "Error downloading file");
			e.printStackTrace();
			return current + fileSize; // Skip download
		}
		
		try {
			url = this.preDownload(url);
		} catch (IOException e) {
			SimpleInstaller.displayMessage("There was a error setting up download for " + getName() + ", skipping download", "Error downloading file");
			e.printStackTrace();
		}
		
		if (!active) {
			return current;
		}
		
		int totalcount = current;
		monitor.setNote("Downloading " + dest.getName());
    	try {
    		System.out.println("Downloading " + dest.getName());
			byte data[] = new byte[BUFFER];
			int count;
			
			while ((count = in.read(data, 0, data.length)) != -1)
			{
				totalcount += count;
				monitor.setProgress(totalcount);
				out.write(data, 0, count);
			}
			
			System.out.println("Downloaded " + dest.getName());		
    	} catch (IOException e) {
    		SimpleInstaller.displayMessage("There was a error downloading file from " + (url == null ? "" : "(" + url + ")") + ", skipping download", "Error downloading file");
    		e.printStackTrace();
		} finally {
			close();
		}
    	
    	try {
			this.postDownload(dest);
		} catch (IOException e) {
			SimpleInstaller.displayMessage("There was a error decompressing " + getName() + ", skipping download", "Error downloading file");
			e.printStackTrace();
    	}
    	
    	return totalcount;
	}
	
	public void close() {
		IOUtils.closeQuietly(in);
		IOUtils.closeQuietly(out);
	}
}
