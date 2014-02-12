package cpw.mods.fml.installer.resources.installer;

import java.io.File;

import org.apache.commons.io.FileUtils;

import cpw.mods.fml.installer.resources.ResourceInfo;


public class ResourceInstaller extends ResourceInstallMethod {

	public ResourceInstaller(ResourceInfo info) {
		super(info);
	}

	@Override
	public File getDownload(File targetDir) {
		return FileUtils.getFile(targetDir, "resourcepacks", mod.getModFileName());
	}

}
