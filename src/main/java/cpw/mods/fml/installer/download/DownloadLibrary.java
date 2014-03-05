package cpw.mods.fml.installer.download;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

import org.tukaani.xz.XZInputStream;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

import cpw.mods.fml.installer.SimpleInstaller;
import cpw.mods.fml.installer.VersionInfo;

import argo.jdom.JsonNode;

public class DownloadLibrary extends Download {

	private static final File LIBRARY_DIR = new File(SimpleInstaller.installdir, "libraries");
	private static final String PACK_EXTENTION = ".pack.xz";
	
	private String name;
	private URL url;
	private File dest;
	
	public DownloadLibrary(JsonNode library) throws IOException {
		name = library.getStringValue("name");
		
		if (library.isBooleanValue("clientreq") && library.getBooleanValue("clientreq")) {
			String[] nameparts = Iterables.toArray(Splitter.on(':').split(name), String.class);
			name = nameparts[1];
			nameparts[0] = nameparts[0].replace('.', '/');
			String jarName = nameparts[1] + '-' + nameparts[2] + ".jar";
			String pathName = nameparts[0] + '/' + nameparts[1] + '/' + nameparts[2] + '/' + jarName + PACK_EXTENTION;
			File libPath = new File(LIBRARY_DIR, pathName.replace('/', File.separatorChar));
			
			String baseUrl = "https://s3.amazonaws.com/Minecraft.Download/libraries/";
			
			if (VersionInfo.getMirrorData().hasMirror()) {
				baseUrl = VersionInfo.getMirrorData().getMirror().getDownloadBase();
			} else if (library.isStringValue("url")) {
				baseUrl = library.getStringValue("url");
			}
			
			if (!libPath.exists()) {
				libPath.getParentFile().mkdirs();
			}
			
			baseUrl += pathName;

			url = new URL(baseUrl);
			dest = libPath;
		} else {
			active = false;
		}
	}

	@Override
	public URL preDownload(URL url) throws IOException {
		if (!active && url != null) {
			return new URL(url.toString().substring(0, url.toString().length() - PACK_EXTENTION.length()));
		}
		return super.preDownload(url);
	}

	@Override
	public File postDownload(File download) throws IOException {
		File dest = new File(download.getAbsolutePath().substring(0, download.getAbsolutePath().length() - PACK_EXTENTION.length()));
		
		if (dest.exists())
        {
			dest.delete();
        }

        byte[] decompressed = DownloadUtils.readFully(new XZInputStream(new ByteArrayInputStream(Files.toByteArray(download))));
        
        //Snag the checksum signature
        String end = new String(decompressed, decompressed.length - 4, 4);
        if (!end.equals("SIGN"))
        {
        	throw new IOException("Unpacking failed, signature missing " + end);
        }

        int x = decompressed.length;
        int len =
                ((decompressed[x - 8] & 0xFF) ) |
                ((decompressed[x - 7] & 0xFF) << 8 ) |
                ((decompressed[x - 6] & 0xFF) << 16) |
                ((decompressed[x - 5] & 0xFF) << 24);
        byte[] checksums = Arrays.copyOfRange(decompressed, decompressed.length - len - 8, decompressed.length - 8);

        FileOutputStream jarBytes = new FileOutputStream(dest);
        JarOutputStream jos = new JarOutputStream(jarBytes);

        Pack200.newUnpacker().unpack(new ByteArrayInputStream(decompressed), jos);

        jos.putNextEntry(new JarEntry("checksums.sha1"));
        jos.write(checksums);
        jos.closeEntry();

        jos.close();
        jarBytes.close();
        return dest;
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
		return name;
	}

}
