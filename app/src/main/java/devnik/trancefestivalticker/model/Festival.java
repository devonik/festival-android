package devnik.trancefestivalticker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinEntity;
import org.greenrobot.greendao.annotation.ToMany;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Transient;

/**
 * Created by nik on 02.03.2018.
 */

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Festival implements Serializable {
    @Id
    Long festival_id;
    Long festival_detail_id;
    String name;
    String thumbnail_image_url;
    Date datum_start;
    Date datum_end;
    String syncStatus;
    @ToMany
    @JoinEntity(
            entity = MusicGenreFestivals.class,
            sourceProperty = "festival_id",
            targetProperty = "music_genre_id"
    )
    List<MusicGenre> musicGenres;


    static final long serialVersionUID = 42L;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1543684630)
    private transient FestivalDao myDao;

    @Generated(hash = 146868241)
    public Festival(Long festival_id, Long festival_detail_id, String name,
            String thumbnail_image_url, Date datum_start, Date datum_end,
            String syncStatus) {
        this.festival_id = festival_id;
        this.festival_detail_id = festival_detail_id;
        this.name = name;
        this.thumbnail_image_url = thumbnail_image_url;
        this.datum_start = datum_start;
        this.datum_end = datum_end;
        this.syncStatus = syncStatus;
    }

    @Generated(hash = 863498718)
    public Festival() {
    }

    public Long getFestival_id() {
        return this.festival_id;
    }

    public void setFestival_id(Long festival_id) {
        this.festival_id = festival_id;
    }

    public Long getFestival_detail_id() {
        return this.festival_detail_id;
    }

    public void setFestival_detail_id(Long festival_detail_id) {
        this.festival_detail_id = festival_detail_id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnail_image_url() {
        return this.thumbnail_image_url;
    }

    public void setThumbnail_image_url(String thumbnail_image_url) {
        this.thumbnail_image_url = thumbnail_image_url;
    }

    public Date getDatum_start() {
        return this.datum_start;
    }

    public void setDatum_start(Date datum_start) {
        this.datum_start = datum_start;
    }

    public Date getDatum_end() {
        return this.datum_end;
    }

    public void setDatum_end(Date datum_end) {
        this.datum_end = datum_end;
    }

    public String getSyncStatus() {
        return this.syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 463840777)
    public List<MusicGenre> getMusicGenres() {
        if (musicGenres == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            MusicGenreDao targetDao = daoSession.getMusicGenreDao();
            List<MusicGenre> musicGenresNew = targetDao
                    ._queryFestival_MusicGenres(festival_id);
            synchronized (this) {
                if (musicGenres == null) {
                    musicGenres = musicGenresNew;
                }
            }
        }
        return musicGenres;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1350397244)
    public synchronized void resetMusicGenres() {
        musicGenres = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }


    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1732136369)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getFestivalDao() : null;
    }
}
