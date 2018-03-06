package devnik.trancefestivalticker.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by nik on 06.03.2018.
 */
@Entity
public class WhatsNew {
    @Id
    private Long id;
    private String updatedFestivals;
    private String insertedFestivals;

    @Generated(hash = 975411442)
    public WhatsNew(Long id, String updatedFestivals, String insertedFestivals) {
        this.id = id;
        this.updatedFestivals = updatedFestivals;
        this.insertedFestivals = insertedFestivals;
    }

    @Generated(hash = 596195766)
    public WhatsNew() {
    }

    public String getUpdatedFestivals() {
        return updatedFestivals;
    }

    public void setUpdatedFestivals(String updatedFestivals) {
        this.updatedFestivals = updatedFestivals;
    }

    public String getInsertedFestivals() {
        return insertedFestivals;
    }

    public void setInsertedFestivals(String insertedFestivals) {
        this.insertedFestivals = insertedFestivals;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
