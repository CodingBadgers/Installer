package cpw.mods.fml.installer.resources.installer;

import java.io.File;

import org.apache.commons.io.FileUtils;

import cpw.mods.fml.installer.resources.ResourceInfo;

public class ShaderInstaller extends ResourceInstallMethod {

	public ShaderInstaller(ResourceInfo info) {
		super(info);
	}

	@Override
	public File getDownload(File targetDir) {
		return FileUtils.getFile(targetDir, "shaderpacks", mod.getModFileName());
	}

}
