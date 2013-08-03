package cpw.mods.fml.installer.mods.installer;

import java.io.File;

import org.apache.commons.io.FileUtils;

import cpw.mods.fml.installer.mods.ModInfo;

public class ModInstaller extends ModInstallMethod {

	public ModInstaller(ModInfo info) {
		super(info);
	}

	@Override
	public File getDownload(File targetDir) {
		return FileUtils.getFile(targetDir, "mods", mod.getModFileName());
	}

}
