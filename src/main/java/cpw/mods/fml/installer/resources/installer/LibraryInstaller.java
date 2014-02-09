package cpw.mods.fml.installer.resources.installer;

import java.io.File;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import cpw.mods.fml.installer.resources.ResourceInfo;

public class LibraryInstaller extends ResourceInstallMethod {

	public LibraryInstaller(ResourceInfo info) {
		super(info);
	}

	@Override
	public File getDownload(File targetDir) {
		String path = mod.getModArtifactId();
		String[] split = Iterables.toArray(Splitter.on(':').omitEmptyStrings().split(path), String.class);
		File dest = new File(targetDir, "libraries");
		Iterable<String> subSplit = Splitter.on('.').omitEmptyStrings().split(split[0]);
		for (String part : subSplit) {
			dest = new File(dest, part);
		}
		dest = new File(new File(dest, split[1]), split[2]);
		dest.mkdirs();
		return new File(dest, mod.getModFileName());
	}

}
