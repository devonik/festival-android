package devnik.trancefestivalticker.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Property;

import java.io.Serializable;

/**
 * Created by nik on 07.03.2018.
 */
@Entity
public class FestivalDetail implements Serializable {
    @Id
    private Long festival_detail_id;
    private Long festival_detail_images_id;
    //@Property(nameInDb = "festival_id")
    private Long festivalId;
    private String description;
    private String homepage_url;
    private String ticket_url;
    private Double geoLatitude;
	private Double geoLongitude;
    private String syncStatus;

    static final long serialVersionUID = 43L;

    @Generated(hash = 57074278)
    public FestivalDetail(Long festival_detail_id, Long festival_detail_images_id,
            Long festivalId, String description, String homepage_url,
            String ticket_url, Double geoLatitude, Double geoLongitude,
            String syncStatus) {
        this.festival_detail_id = festival_detail_id;
        this.festival_detail_images_id = festival_detail_images_id;
        this.festivalId = festivalId;
        this.description = description;
        this.homepage_url = homepage_url;
        this.ticket_url = ticket_url;
        this.geoLatitude = geoLatitude;
        this.geoLongitude = geoLongitude;
        this.syncStatus = syncStatus;
    }

    @Generated(hash = 1066729021)
    public FestivalDetail() {
    }

    public Long getFestival_detail_id() {
        return this.festival_detail_id;
    }

    public void setFestival_detail_id(Long festival_detail_id) {
        this.festival_detail_id = festival_detail_id;
    }

    public Long getFestival_detail_images_id() {
        return this.festival_detail_images_id;
    }

    public void setFestival_detail_images_id(Long festival_detail_images_id) {
        this.festival_detail_images_id = festival_detail_images_id;
    }

    public Long getFestivalId() {
        return this.festivalId;
    }

    public void setFestivalId(Long festivalId) {
        this.festivalId = festivalId;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHomepage_url() {
        return this.homepage_url;
    }

    public void setHomepage_url(String homepage_url) {
        this.homepage_url = homepage_url;
    }

    public String getTicket_url() {
        return this.ticket_url;
    }

    public void setTicket_url(String ticket_url) {
        this.ticket_url = ticket_url;
    }

    public Double getGeoLatitude() {
        return this.geoLatitude;
    }

    public void setGeoLatitude(Double geoLatitude) {
        this.geoLatitude = geoLatitude;
    }

    public Double getGeoLongitude() {
        return this.geoLongitude;
    }

    public void setGeoLongitude(Double geoLongitude) {
        this.geoLongitude = geoLongitude;
    }

    public String getSyncStatus() {
        return this.syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }


}
