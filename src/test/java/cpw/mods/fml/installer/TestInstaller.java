package cpw.mods.fml.installer;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestInstaller {

	@BeforeClass
	public static void setup() throws IOException {
		String mcVersion = "1.6.2";
		
		String[] dirs = new String[] {
			"versions", "versions" + File.separatorChar + mcVersion,
			"libraries",
			"mods",
			"config"
		};
		
		File basedir = new File("." + File.separatorChar + "test-minecraft");
		for (String dir : dirs) {
			File directory = new File(basedir, dir);
			if (!directory.exists()) {
				directory.mkdirs();
			}
		}
		
		String[][] files = new String[][] {
			{mcVersion + ".jar", "https://s3.amazonaws.com/Minecraft.Download/versions/" + mcVersion + "/" + mcVersion + ".jar", "versions" + File.separatorChar + "1.6.2"}
		};
		
		for (String[] download : files) {
			try {
				File file = new File(basedir, download[2] + File.separatorChar + download[0]);
				
				if (!file.exists()) {
					DownloadFile downloadFile = new DownloadFile(new URL(download[1]), file);
					downloadFile.run(DownloadUtils.buildMonitor(), 0);
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
				fail(e.getMessage());
				return;
			}
		}
		
		FileUtils.copyFile(new File("src/test/resources/launcher_profiles.json"), new File("test-minecraft/launcher_profiles.json"));
		FileUtils.copyFile(new File("src/test/resources/" + mcVersion + ".json"), new File("test-minecraft/versions/" + mcVersion + "/" + mcVersion + ".json"));
	}
	
	@Test
	public void run() {
		try {
			SimpleInstaller.headless = true;
			SimpleInstaller.installdir = new File("test-minecraft");
			SimpleInstaller.profileFileLocation = new File("src/test/resources/test.json").toURI().toURL();
			assertTrue(SimpleInstaller.runInstaller());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@AfterClass
	public static void cleanup() {
		try {
			FileUtils.deleteDirectory(new File("test-minecraft"));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("Cleanup successful");
	}
	
}
