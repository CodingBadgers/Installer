package cpw.mods.fml.installer.download;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Iterator;

import cpw.mods.fml.installer.IMonitor;

public class DownloadList extends ArrayList<Download> implements Closeable {

	private static final long serialVersionUID = 282031329153578653L;

	public boolean setup() {
		boolean failed = false;
		
		Iterator<Download> itr = iterator();
		while(itr.hasNext()) {
			if (!itr.next().setup()) {
				failed = true;
			}
		}
		
		return failed;
	}

	public void download(IMonitor monitor) {
		int count = 0;
		
		Iterator<Download> itr = iterator();
		while(itr.hasNext()) {
			count = itr.next().download(monitor, count);
		}
	}
	public void close() {
		Iterator<Download> itr = iterator();
		while(itr.hasNext()) {
			itr.next().close();
		}
	}
}
