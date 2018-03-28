package devnik.trancefestivalticker.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

import java.io.Serializable;

/**
 * Created by niklas on 26.03.18.
 */
@Entity
public class MusicGenre implements Serializable {
    @Id
    private Long id;
    private String name;
    static final long serialVersionUID = 44L;
    @Generated(hash = 370455646)
    public MusicGenre(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    @Generated(hash = 1283460751)
    public MusicGenre() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
