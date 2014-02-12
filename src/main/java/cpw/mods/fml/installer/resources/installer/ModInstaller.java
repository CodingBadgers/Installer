package cpw.mods.fml.installer.resources.installer;

import java.io.File;

import org.apache.commons.io.FileUtils;

import cpw.mods.fml.installer.resources.ResourceInfo;


public class ModInstaller extends ResourceInstallMethod {

	public ModInstaller(ResourceInfo info) {
		super(info);
	}

	@Override
	public File getDownload(File targetDir) {
		return FileUtils.getFile(targetDir, "mods", mod.getModFileName());
	}

}
