package cpw.mods.fml.installer.mods;

public enum FileType {

	JAR(".jar"),
	ZIP(".zip"),
	LITEMOD(".litemod"),
	;
	
	private String ext;

	private FileType(String extention) {
		this.ext = extention;
	}
	
	public String createFileName(ModInfo info) {
		return info.getModName() + "-" + info.getModVersion() + ext;
	}
}
