package cpw.mods.fml.installer.download;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.ProgressMonitor;

import cpw.mods.fml.installer.IMonitor;
import cpw.mods.fml.installer.SimpleInstaller;

public class DownloadUtils {

	public static IMonitor buildMonitor() {
		if (SimpleInstaller.headless) {
			return new IMonitor() {

				@Override
				public void setMaximum(int max) {
				}

				@Override
				public void setNote(String note) {
				}

				@Override
				public void setProgress(int progress) {
				}

				@Override
				public void close() {
				}

				@Override
				public int getMaximum() {
					return -1;
				}

			};
		} else {
			return new IMonitor() {
				private ProgressMonitor monitor;
				{
					monitor = new ProgressMonitor(null, "Downloading libraries", "Libraries are being analyzed", 0, 1);
					monitor.setMillisToPopup(0);
					monitor.setMillisToDecideToPopup(0);
				}

				@Override
				public void setMaximum(int max) {
					monitor.setMaximum(max);
				}

				@Override
				public void setNote(String note) {
					monitor.setNote(note);
				}

				@Override
				public void setProgress(int progress) {
					monitor.setProgress(progress);
				}

				@Override
				public void close() {
					monitor.close();
				}

				@Override
				public int getMaximum() {
					return monitor.getMaximum();
				}
			};
		}
	}
	
    public static byte[] readFully(InputStream stream) throws IOException
    {
        byte[] data = new byte[4096];
        ByteArrayOutputStream entryBuffer = new ByteArrayOutputStream();
        int len;
        do
        {
            len = stream.read(data);
            if (len > 0)
            {
                entryBuffer.write(data, 0, len);
            }
        } while (len != -1);

        return entryBuffer.toByteArray();
    }
}
