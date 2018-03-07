package devnik.trancefestivalticker.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by nik on 02.03.2018.
 */
@Entity
public class Festival implements Serializable {
    @Id
    Long festival_id;
    Long festival_detail_id;
    String name;
    String thumbnail_image_url;
    Date datum_start;
    Date datum_end;
    String homepage_url;
    String ticket_url;
    String syncStatus;

    static final long serialVersionUID = 42L;

    @Generated(hash = 1632792361)
    public Festival(Long festival_id, Long festival_detail_id, String name,
            String thumbnail_image_url, Date datum_start, Date datum_end,
            String homepage_url, String ticket_url, String syncStatus) {
        this.festival_id = festival_id;
        this.festival_detail_id = festival_detail_id;
        this.name = name;
        this.thumbnail_image_url = thumbnail_image_url;
        this.datum_start = datum_start;
        this.datum_end = datum_end;
        this.homepage_url = homepage_url;
        this.ticket_url = ticket_url;
        this.syncStatus = syncStatus;
    }

    @Generated(hash = 863498718)
    public Festival() {
    }

    public Long getFestival_id() {
        return festival_id;
    }

    public Long getFestival_detail_id() {
        return festival_detail_id;
    }

    public void setFestival_detail_id(Long festival_detail_id) {
        this.festival_detail_id = festival_detail_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnail_image_url() {
        return thumbnail_image_url;
    }

    public void setThumbnail_image_url(String thumbnail_image_url) {
        this.thumbnail_image_url = thumbnail_image_url;
    }

    public Date getDatum_start() {
        return datum_start;
    }

    public void setDatum_start(Date datum) {
        this.datum_start = datum;
    }

    public Date getDatum_end() {
        return datum_end;
    }

    public void setDatum_end(Date datum_end) {
        this.datum_end = datum_end;
    }

    public String getHomepage_url() {
        return homepage_url;
    }

    public void setHomepage_url(String homepage_url) {
        this.homepage_url = homepage_url;
    }

    public String getTicket_url() {
        return ticket_url;
    }

    public void setTicket_url(String ticket_url) {
        this.ticket_url = ticket_url;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String sync_status) {
        this.syncStatus = sync_status;
    }

    public void setFestival_id(Long festival_id) {
        this.festival_id = festival_id;
    }
}
