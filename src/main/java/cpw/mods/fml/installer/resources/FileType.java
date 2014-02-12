package cpw.mods.fml.installer.resources;


public enum FileType {

	JAR(".jar"),
	ZIP(".zip"),
	LITEMOD(".litemod"),
	;
	
	private String ext;

	private FileType(String extention) {
		this.ext = extention;
	}
	
	public String createFileName(ResourceInfo info) {
		return info.getModName() + "-" + info.getModVersion() + ext;
	}
}
