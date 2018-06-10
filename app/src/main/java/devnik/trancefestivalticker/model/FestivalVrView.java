package devnik.trancefestivalticker.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

import java.io.Serializable;

@Entity
public class FestivalVrView implements Serializable {
    @Id
    private Long id;
    private Long festivalDetailId;
    private String url;
    private String description;
    private String type;
    static final long serialVersionUID = 45L;
    @Generated(hash = 1687385630)
    public FestivalVrView(Long id, Long festivalDetailId, String url,
            String description, String type) {
        this.id = id;
        this.festivalDetailId = festivalDetailId;
        this.url = url;
        this.description = description;
        this.type = type;
    }
    @Generated(hash = 153890135)
    public FestivalVrView() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getFestivalDetailId() {
        return this.festivalDetailId;
    }
    public void setFestivalDetailId(Long festivalDetailId) {
        this.festivalDetailId = festivalDetailId;
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
    public String getType() {
        return this.type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
