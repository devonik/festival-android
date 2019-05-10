package devnik.trancefestivalticker.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class UserTickets{
    @Id
    private Long id;
    private Long festivalId;
    private String ticketUri;
    private String ticketType;
    @Generated(hash = 883490993)
    public UserTickets(Long id, Long festivalId, String ticketUri,
            String ticketType) {
        this.id = id;
        this.festivalId = festivalId;
        this.ticketUri = ticketUri;
        this.ticketType = ticketType;
    }
    @Generated(hash = 1656532916)
    public UserTickets() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getFestivalId() {
        return this.festivalId;
    }
    public void setFestivalId(Long festivalId) {
        this.festivalId = festivalId;
    }
    public String getTicketUri() {
        return this.ticketUri;
    }
    public void setTicketUri(String ticketUri) {
        this.ticketUri = ticketUri;
    }
    public String getTicketType() {
        return this.ticketType;
    }
    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

}
