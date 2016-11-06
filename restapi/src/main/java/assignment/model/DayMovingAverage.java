package assignment.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by xuan on 11/2/2016.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DayMovingAverage {

    public DayMovingAverage(String ticker, BigDecimal avg) {
        this.ticker = ticker;
        this.avg = avg;
    }

    public DayMovingAverage(String ticker, BigDecimal avg, LocalDate startDate) {
        this.ticker = ticker;
        this.avg = avg;
        this.startDate = startDate;
    }

    private String ticker;

    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal avg;

    @JsonSerialize(using = ToStringSerializer.class)
    private LocalDate startDate;

    @JsonProperty("Error")
    private String errorMessage;

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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
}
