package cpw.mods.fml.installer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import argo.format.PrettyJsonFormatter;
import argo.jdom.JdomParser;
import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;
import argo.saj.InvalidSyntaxException;
import static argo.jdom.JsonNodeFactories.*;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import cpw.mods.fml.installer.mods.ModInfo;

public class ClientInstall implements ActionType {

    @Override
    public boolean run(File target)
    {
        if (!target.exists())
        {
            JOptionPane.showMessageDialog(null, "There is no minecraft installation at this location!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        File launcherProfiles = new File(target,"launcher_profiles.json");
        if (!launcherProfiles.exists())
        {
            JOptionPane.showMessageDialog(null, "There is no minecraft launcher profile at this location, you need to run the launcher first!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        File versionRootDir = new File(target,"versions");
        File versionTarget = new File(versionRootDir,VersionInfo.getVersionTarget());
        if (!versionTarget.mkdirs() && !versionTarget.isDirectory())
        {
            if (!versionTarget.delete())
            {
                JOptionPane.showMessageDialog(null, "There was a problem with the launcher version data. You will need to clear "+versionTarget.getAbsolutePath()+" manually", "Error", JOptionPane.ERROR_MESSAGE);

            }
            else
            {
                versionTarget.mkdirs();
            }
        }

        File versionJsonFile = new File(versionTarget,VersionInfo.getVersionTarget()+".json");
        File clientJarFile = new File(versionTarget, VersionInfo.getVersionTarget()+".jar");
        File minecraftJarFile = VersionInfo.getMinecraftFile(versionRootDir);
        try
        {
            Files.copy(minecraftJarFile, clientJarFile);
        }
        catch (IOException e1)
        {
            JOptionPane.showMessageDialog(null, "You need to run the version "+VersionInfo.getMinecraftVersion()+" manually at least once", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        File targetLibraryFile = VersionInfo.getLibraryPath(new File(target,"libraries"));
        if (!targetLibraryFile.getParentFile().mkdirs() && !targetLibraryFile.getParentFile().isDirectory())
        {
            if (!targetLibraryFile.getParentFile().delete())
            {
                JOptionPane.showMessageDialog(null, "There was a problem with the launcher version data. You will need to clear "+targetLibraryFile.getAbsolutePath()+" manually", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            else
            {
                targetLibraryFile.getParentFile().mkdirs();
            }
        }


        JsonRootNode versionJson = object(VersionInfo.getVersionInfo().getFields());

        try
        {
            BufferedWriter newWriter = Files.newWriter(versionJsonFile, Charsets.UTF_8);
            PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(versionJson,newWriter);
            newWriter.close();
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null, "There was a problem writing the launcher version data,  is it write protected?", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try
        {
            
            List<ModInfo> mods = VersionInfo.getModInfo();
            File modsFolder = new File(target, "mods");
            
            if (!modsFolder.exists()) {
            	modsFolder.mkdir();
            } else {
            	File[] existingFiles = modsFolder.listFiles(new FileFilter() {
    				@Override
    				public boolean accept(File file) {
    					return file.isFile();
    				}
            	});
            	
            	for (File file : existingFiles) {
            		if (!file.delete()) {
            			JOptionPane.showMessageDialog(null, "Error deleting file " + file.getName() + " from your mods folder please delete this file, this may affect your minecraft stability.", "Error", JOptionPane.ERROR_MESSAGE);
            	        continue;
            		}
            	}
            }
            
            // Download accompanying mods
            List<DownloadFile> downloads = new ArrayList<>();
            
            ProgressMonitor monitor = new ProgressMonitor(null, "Downloading libraries", "Gathering mod information", 0, 1);
            monitor.setMillisToPopup(0);
            monitor.setMillisToDecideToPopup(0);
            monitor.setMaximum(mods.size() + 2);
            int totalCount = 0;
            
            if (!targetLibraryFile.exists()) {
	            DownloadFile download = new DownloadFile(VersionInfo.getForgeDownloadUrl(), targetLibraryFile);
	            totalCount = download.getFileSize();
	            downloads.add(download);
	            monitor.setProgress(1);
            } else {
            	totalCount = 1;
            }
            
            monitor.setMaximum(totalCount);
            int i = 1;
            
            for (ModInfo mod : mods) {
            	DownloadFile download = mod.createDownload(target, monitor);
            	totalCount += download.getFileSize();
            	downloads.add(download);
	            monitor.setProgress(i++);	            
            }
            
            monitor.setMaximum(totalCount);
            int count = 0;
            
            for (DownloadFile download : downloads) {
            	count = download.run(monitor, count);
            }
            
            monitor.setProgress(monitor.getMaximum());
        	
        }
        catch (Exception e) 
        {
        	e.printStackTrace();
        	JOptionPane.showMessageDialog(null, "There was a unexpected exception (" + e.getClass().getSimpleName() + ")", "Error", JOptionPane.ERROR_MESSAGE);    	
            return false;
        }

        JdomParser parser = new JdomParser();
        JsonRootNode jsonProfileData;

        try
        {
            jsonProfileData = parser.parse(Files.newReader(launcherProfiles, Charsets.UTF_8));
        }
        catch (InvalidSyntaxException e)
        {
            JOptionPane.showMessageDialog(null, "The launcher profile file is corrupted. Re-run the minecraft launcher to fix it!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        catch (Exception e)
        {
            throw Throwables.propagate(e);
        }
        
        JsonField[] fields = new JsonField[] {
            field("name", string(VersionInfo.getProfileName())),
            field("lastVersionId", string(VersionInfo.getVersionTarget())),
            field("launcherVisibilityOnGameClose", string("keep the launcher open")),
            field("javaArgs", string("-Xmx1G -Dfml.ignoreInvalidMinecraftCertificates=true -Dfml.ignorePatchDiscrepancies=true")),
        };
        
        if (ProfileInfo.getCurrent() != null) {
        	fields = Arrays.copyOf(fields, 5);
        	fields[4] = field("playerUUID", string(ProfileInfo.getCurrent().getUUID()));
        }

        HashMap<JsonStringNode, JsonNode> profileCopy = Maps.newHashMap(jsonProfileData.getNode("profiles").getFields());
        HashMap<JsonStringNode, JsonNode> rootCopy = Maps.newHashMap(jsonProfileData.getFields());
        profileCopy.put(string(VersionInfo.getProfileName()), object(fields));
        JsonRootNode profileJsonCopy = object(profileCopy);

        rootCopy.put(string("profiles"), profileJsonCopy);
        rootCopy.put(string("selectedProfile"), string(VersionInfo.getProfileName()));

        jsonProfileData = object(rootCopy);

        try
        {
            BufferedWriter newWriter = Files.newWriter(launcherProfiles, Charsets.UTF_8);
            PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(jsonProfileData,newWriter);
            newWriter.close();
        }
        catch (Exception e)
        {
        	e.printStackTrace();
            JOptionPane.showMessageDialog(null, "There was a problem writing the launch profile,  is it write protected?", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }

    @Override
    public boolean isPathValid(File targetDir)
    {
        if (targetDir.exists())
        {
            File launcherProfiles = new File(targetDir,"launcher_profiles.json");
            return launcherProfiles.exists();
        }
        return false;
    }


    public String getFileError(File targetDir)
    {
        if (targetDir.exists())
        {
            return "The directory is missing a launcher profile. Please run the minecraft launcher first";
        }
        else
        {
            return "There is no minecraft directory set up. Either choose an alternative, or run the minecraft launcher to create one";
        }
    }

    @Override
    public String getSuccessMessage()
    {
        return String.format("Successfully installed client profile %s for version %s into launcher", VersionInfo.getProfileName(), VersionInfo.getMinecraftVersion());
    }
}
