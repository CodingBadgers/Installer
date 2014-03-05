package cpw.mods.fml.installer.mirror;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.google.common.base.Splitter;

public class Mirror {

	private static final Splitter split = Splitter.on('!').limit(4).omitEmptyStrings().trimResults();
	
	private String name;
	private Icon logo;
	private URL logoUrl;
	private URL link;
	private String download;
	
	public Mirror(String line) throws IOException {
		Iterator<String> parts = split.split(line).iterator();
		name = parts.next();
		logoUrl = new URL(parts.next());
		link = new URL(parts.next());
		download = parts.next();
		
		logo = new ImageIcon(ImageIO.read(logoUrl));;
	}
	
	public String getName() {
		return name;
	}
	
	public URL getLogoUrl() {
		return logoUrl;
	}
	
	public URL getHomepage() {
		return link;
	}
	
	public String getDownloadBase() {
		return download;
	}

	public Icon getLogo() {
		return logo;
	}
	
	public URL getMirrorUrl(String data) throws IOException {
		return new URL(download.toString() + data);
	}

	public String getSponsorMessage() {
		return String.format("<html><a href=\'%s\'>Data kindly mirrored by %s</a></html>", getHomepage(), getName());
	}
}
