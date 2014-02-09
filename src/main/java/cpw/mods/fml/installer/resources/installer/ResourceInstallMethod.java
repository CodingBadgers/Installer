package cpw.mods.fml.installer.resources.installer;

import java.io.File;

import cpw.mods.fml.installer.resources.ResourceInfo;

public abstract class ResourceInstallMethod {
 
	protected final ResourceInfo mod;

	public ResourceInstallMethod(ResourceInfo info) {
		this.mod = info;
	}
	
	public ResourceInfo getModInfo() {
		return mod;
	}
	
	public abstract File getDownload(File targetDir);
}
