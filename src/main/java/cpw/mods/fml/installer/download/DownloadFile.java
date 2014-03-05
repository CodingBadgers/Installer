package cpw.mods.fml.installer.download;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DownloadFile extends Download{

	private URL url;
	private File dest;

	public DownloadFile(URL filelocation, File destination) {
		this.url = filelocation;
		this.dest = destination;
	}
	
	@Override
	public URL getDownloadUrl() throws IOException {
		return url;
	}

	@Override
	public File getTargetFile() throws IOException {
		return dest;
	}

	@Override
	public String getName() {
		return dest.getName();
	}
}
