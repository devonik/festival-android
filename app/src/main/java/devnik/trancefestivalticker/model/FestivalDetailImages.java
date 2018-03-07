package devnik.trancefestivalticker.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by nik on 07.03.2018.
 */
@Entity
public class FestivalDetailImages {
    @Id
    private Long festival_detail_images_id;
    private Long festivalDetailId;
    private String title;
    private String url;
    private String description;
    private String syncStatus;
    @Generated(hash = 1487521965)
    public FestivalDetailImages(Long festival_detail_images_id,
            Long festivalDetailId, String title, String url, String description,
            String syncStatus) {
        this.festival_detail_images_id = festival_detail_images_id;
        this.festivalDetailId = festivalDetailId;
        this.title = title;
        this.url = url;
        this.description = description;
        this.syncStatus = syncStatus;
    }
    @Generated(hash = 133490986)
    public FestivalDetailImages() {
    }
    public Long getFestival_detail_images_id() {
        return this.festival_detail_images_id;
    }
    public void setFestival_detail_images_id(Long festival_detail_images_id) {
        this.festival_detail_images_id = festival_detail_images_id;
    }
    public Long getFestivalDetailId() {
        return this.festivalDetailId;
    }
    public void setFestivalDetailId(Long festivalDetailId) {
        this.festivalDetailId = festivalDetailId;
    }
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getSyncStatus() {
        return this.syncStatus;
    }
    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

}
