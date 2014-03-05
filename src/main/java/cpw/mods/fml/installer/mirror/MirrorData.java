package cpw.mods.fml.installer.mirror;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.IOUtils;

public class MirrorData {

	private List<Mirror> mirrors = new ArrayList<Mirror>();
	private Mirror mirror;

	public MirrorData(String url) throws IOException {
		URL mirrorData = new URL(url);
		InputStream in = mirrorData.openStream();
		List<String> lines = IOUtils.readLines(in);
		
		for (String line : lines) {
			mirrors.add(new Mirror(line));
		}
		
		if (mirrors.size() == 0) {
			throw new IOException("No mirrors found");
		}
		
		Random r = new Random();
		mirror = mirrors.get(r.nextInt(mirrors.size()));
	}
	
	public Mirror getMirror() {
		return mirror;
	}

	public boolean hasMirror() {
		return mirror != null;
	}
}
