package devnik.trancefestivalticker.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by niklas on 26.03.18.
 */
@Entity
public class MusicGenreFestivals {
    @Id
    private Long id;
    private Long festival_id;
    private Long music_genre_id;
    @Generated(hash = 1041273776)
    public MusicGenreFestivals(Long id, Long festival_id, Long music_genre_id) {
        this.id = id;
        this.festival_id = festival_id;
        this.music_genre_id = music_genre_id;
    }
    @Generated(hash = 1516235214)
    public MusicGenreFestivals() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getFestival_id() {
        return this.festival_id;
    }
    public void setFestival_id(Long festival_id) {
        this.festival_id = festival_id;
    }
    public Long getMusic_genre_id() {
        return this.music_genre_id;
    }
    public void setMusic_genre_id(Long music_genre_id) {
        this.music_genre_id = music_genre_id;
    }
    
}
