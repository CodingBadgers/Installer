package cpw.mods.fml.installer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.ProgressMonitor;

import org.apache.commons.io.IOUtils;

public class DownloadFile {

	private URL url;
	private File dest;
	
	private URLConnection connection;
	private InputStream in;
	private OutputStream out;
	private int fileSize;

	public DownloadFile(URL filelocation, File destination) {
		this.url = filelocation;
		this.dest = destination;
		this.setup();
	}
	
	public void setup() {
    	try {
			connection = url.openConnection();
			fileSize = connection.getContentLength();
			
			in = new BufferedInputStream(connection.getInputStream());
			out = new FileOutputStream(dest);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getFileSize() {
		return fileSize;
	}
	
	public int run(ProgressMonitor monitor, int current) {
		int totalcount = current;
		monitor.setNote("Downloading " + dest.getName());
    	try {
    		System.out.println("Downloading " + dest.getName());
			byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, data.length)) != -1)
			{
				totalcount += count;
				monitor.setProgress(totalcount);
				out.write(data, 0, count);
			}    	
			System.out.println("Downloaded " + dest.getName());		
    	} catch (IOException e) {
    		e.printStackTrace();
		} finally {
			close();
		}
    	return totalcount;
	}
	
	public void close() {
		IOUtils.closeQuietly(in);
		IOUtils.closeQuietly(out);
	}
}
