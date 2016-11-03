package assignment.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by xuan on 11/2/2016.
 */
public class DayMovingAverage {

    public DayMovingAverage(String ticker, BigDecimal avg) {
        this.ticker = ticker;
        this.avg = avg;
    }

    private String ticker;

    @JsonSerialize(using = ToStringSerializer.class)
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
