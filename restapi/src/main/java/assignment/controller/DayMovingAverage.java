package assignment.controller;

import java.math.BigDecimal;

/**
 * Created by xuan on 11/2/2016.
 */
public class DayMovingAverage {

    private String ticker;

    private BigDecimal avg;

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public BigDecimal getAvg() {
        return avg;
    }

    public void setAvg(BigDecimal avg) {
        this.avg = avg;
    }
}
