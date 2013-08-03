package cpw.mods.fml.installer.mods.installer;

import java.io.File;

import cpw.mods.fml.installer.mods.ModInfo;

public abstract class ModInstallMethod {
 
	protected final ModInfo mod;

	public ModInstallMethod(ModInfo info) {
		this.mod = info;
	}
	
	public ModInfo getModInfo() {
		return mod;
	}
	
	public abstract File getDownload(File targetDir);
}
