package devnik.trancefestivalticker.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class FestivalTicketPhase {
    @Id
    private Long festival_ticket_phase_id;
    private Long festivalId;
    private String title;
    private Double price;
    private String syncStatus;
    private String sold;
    private String started;
    @Generated(hash = 1039139067)
    public FestivalTicketPhase(Long festival_ticket_phase_id, Long festivalId,
            String title, Double price, String syncStatus, String sold,
            String started) {
        this.festival_ticket_phase_id = festival_ticket_phase_id;
        this.festivalId = festivalId;
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
    public Long getFestivalId() {
        return this.festivalId;
    }
    public void setFestivalId(Long festivalId) {
        this.festivalId = festivalId;
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
}
