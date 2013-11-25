package cpw.mods.fml.installer.download;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DownloadMinecraftJar extends DownloadFile {

	private static final String minecraftDownload = "https://s3.amazonaws.com/Minecraft.Download/versions/";
	
	public DownloadMinecraftJar(String version, File dest) throws IOException {
		super(new URL(minecraftDownload + version + "/" + version + ".jar"), dest);
	}

}
