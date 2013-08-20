package cpw.mods.fml.installer;

public interface IMonitor {
    void setMaximum(int max);
    void setNote(String note);
    void setProgress(int progress);
    int getMaximum();
    void close();
}
