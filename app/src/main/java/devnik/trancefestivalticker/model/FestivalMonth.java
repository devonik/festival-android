package devnik.trancefestivalticker.model;

import java.util.ArrayList;

/**
 * Created by nik on 21.02.2018.
 */

public class FestivalMonth {
    private CustomDate customDate;
    private ArrayList<Image> images;

    public CustomDate getCustomDate() {
        return customDate;
    }

    public ArrayList<Image> getImages() {
        return images;
    }

    public void setCustomDate(CustomDate customDate) {
        this.customDate = customDate;
    }

    public void setImages(ArrayList<Image> images) {
        this.images = images;
    }
}
