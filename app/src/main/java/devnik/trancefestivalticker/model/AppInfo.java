package devnik.trancefestivalticker.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class AppInfo {
    @Id
    private Long id;
    private String lastSync;
    //Default no
    private String finishedTourGuide = "no";
    private String whatsNewDone = "no";
    @Generated(hash = 339659477)
    public AppInfo(Long id, String lastSync, String finishedTourGuide,
            String whatsNewDone) {
        this.id = id;
        this.lastSync = lastSync;
        this.finishedTourGuide = finishedTourGuide;
        this.whatsNewDone = whatsNewDone;
    }
    @Generated(hash = 1656151854)
    public AppInfo() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getLastSync() {
        return this.lastSync;
    }
    public void setLastSync(String lastSync) {
        this.lastSync = lastSync;
    }
    public String getFinishedTourGuide() {
        return this.finishedTourGuide;
    }
    public void setFinishedTourGuide(String finishedTourGuide) {
        this.finishedTourGuide = finishedTourGuide;
    }
    public String getWhatsNewDone() {
        return this.whatsNewDone;
    }
    public void setWhatsNewDone(String whatsNewDone) {
        this.whatsNewDone = whatsNewDone;
    }
}
