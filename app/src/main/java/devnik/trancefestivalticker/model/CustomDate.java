package devnik.trancefestivalticker.model;

import java.io.Serializable;

/**
 * Created by nik on 20.02.2018.
 */

public class CustomDate implements Serializable {
    private String month;
    private String year;

    public CustomDate(){}
    public CustomDate(String month, String year){
        this.month = month;
        this.year = year;
    }

    public String getMonth() {
         return month;
    }

    public void setMonth(String month) {this.month = month;}

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

}
