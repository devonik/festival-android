package devnik.trancefestivalticker.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

/**
 * Created by nik on 06.03.2018.
 */
@Entity
public class WhatsNew {
    @Id
    private Long id;
    private String content;
    private Date createdDate;
    @Generated(hash = 1904455757)
    public WhatsNew(Long id, String content, Date createdDate) {
        this.id = id;
        this.content = content;
        this.createdDate = createdDate;
    }
    @Generated(hash = 596195766)
    public WhatsNew() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getContent() {
        return this.content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Date getCreatedDate() {
        return this.createdDate;
    }
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
