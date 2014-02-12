package cpw.mods.fml.installer.resources.installer;

import cpw.mods.fml.installer.resources.ResourceInfo;


public enum InstallMethod {

	MODS(),
	LIBRARY(),
	SHADER(),
	RESOURCE(),
	;

	private InstallMethod() {
	}
	
	public static ResourceInstallMethod getInstaller(InstallMethod method, ResourceInfo mod) {
		switch (method) {
		case MODS:
			return new ModInstaller(mod);
		case LIBRARY:
			return new LibraryInstaller(mod);
		case SHADER:
			return new ShaderInstaller(mod);
		case RESOURCE:
			return new ResourceInstaller(mod);
		
		}
		return null;
	}
	
}
