package cpw.mods.fml.installer.mods;

import java.lang.reflect.Constructor;

import cpw.mods.fml.installer.mods.installer.*;

public enum InstallMethod {

	MODS(ModInstaller.class),
	LIBRARY(LibraryInstaller.class),
	;

	private Constructor<? extends ModInstallMethod> ctor;

	private InstallMethod(Class<? extends ModInstallMethod> clazz) {
		try {
			this.ctor = clazz.getConstructor(ModInfo.class);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
	
	public ModInstallMethod getInstaller(ModInfo mod) throws ReflectiveOperationException {
		return ctor.newInstance(mod);
	}
	
}
