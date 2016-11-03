package assignment.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuan on 11/1/2016.
 */
public class Prices {

    private String ticker;

    private List<DateClose> dateCloses = new ArrayList<>();

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public List<DateClose> getDateCloses() {
        return dateCloses;
    }

    public void setDateCloses(List<DateClose> dateCloses) {
        this.dateCloses = dateCloses;
    }
}
