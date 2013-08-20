package cpw.mods.fml.installer.mods.installer;

import cpw.mods.fml.installer.mods.ModInfo;

public enum InstallMethod {

	MODS(),
	LIBRARY(),
	;

	private InstallMethod() {
	}
	
	public static ModInstallMethod getInstaller(InstallMethod method, ModInfo mod) {
		switch (method) {
		case MODS:
			return new ModInstaller(mod);
		case LIBRARY:
			return new LibraryInstaller(mod);
		}
		return null;
	}
	
}
