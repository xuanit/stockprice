package assignment.model;

import assignment.controller.CloseDate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuan on 11/1/2016.
 */
public class Prices {

    private String ticker;

    private List<CloseDate> closeDates = new ArrayList<>();

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public List<CloseDate> getCloseDates() {
        return closeDates;
    }

    public void setCloseDates(List<CloseDate> closeDates) {
        this.closeDates = closeDates;
    }
}
