package devnik.trancefestivalticker.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

import java.io.Serializable;
import java.util.Objects;

@Entity
public class FestivalTicketPhase implements Serializable {
    @Id
    private Long festival_ticket_phase_id;
    private Long festival_id;
    private String title;
    private Double price;
    private String syncStatus;
    private String sold;
    private String started;
    static final long serialVersionUID = 45L;
    @Generated(hash = 1536001098)
    public FestivalTicketPhase(Long festival_ticket_phase_id, Long festival_id,
            String title, Double price, String syncStatus, String sold,
            String started) {
        this.festival_ticket_phase_id = festival_ticket_phase_id;
        this.festival_id = festival_id;
        this.title = title;
        this.price = price;
        this.syncStatus = syncStatus;
        this.sold = sold;
        this.started = started;
    }
    @Generated(hash = 1953732859)
    public FestivalTicketPhase() {
    }
    public Long getFestival_ticket_phase_id() {
        return this.festival_ticket_phase_id;
    }
    public void setFestival_ticket_phase_id(Long festival_ticket_phase_id) {
        this.festival_ticket_phase_id = festival_ticket_phase_id;
    }
    public Long getFestival_id() {
        return this.festival_id;
    }
    public void setFestival_id(Long festival_id) {
        this.festival_id = festival_id;
    }
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public Double getPrice() {
        return this.price;
    }
    public void setPrice(Double price) {
        this.price = price;
    }
    public String getSyncStatus() {
        return this.syncStatus;
    }
    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }
    public String getSold() {
        return this.sold;
    }
    public void setSold(String sold) {
        this.sold = sold;
    }
    public String getStarted() {
        return this.started;
    }
    public void setStarted(String started) {
        this.started = started;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FestivalTicketPhase that = (FestivalTicketPhase) o;
        return Objects.equals(festival_ticket_phase_id, that.festival_ticket_phase_id) &&
                Objects.equals(festival_id, that.festival_id) &&
                Objects.equals(title, that.title) &&
                Objects.equals(price, that.price) &&
                Objects.equals(syncStatus, that.syncStatus) &&
                Objects.equals(sold, that.sold) &&
                Objects.equals(started, that.started);
    }

    @Override
    public int hashCode() {

        return Objects.hash(festival_ticket_phase_id, festival_id, title, price, syncStatus, sold, started);
    }
}
