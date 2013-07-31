package cpw.mods.fml.installer;

import argo.jdom.JsonNode;

public class ProfileInfo {

	private String name;
	private String uuid;
	
	public ProfileInfo(JsonNode node) {
		this.name = node.getStringValue("displayName");
		this.uuid = node.getStringValue("uuid");
	}
	
	public String getName() {
		return name;
	}
	
	public String getUUID() {
		return uuid;
	}
	
	public String toString() {
		return name;
	}
	
	private static ProfileInfo current;
	
	public static void setCurrent(ProfileInfo profile) {
		current = profile;
	}
	
	public static ProfileInfo getCurrent() {
		return current;
	}
}
