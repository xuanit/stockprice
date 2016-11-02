package assignment.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * Created by xuan on 11/1/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloseDate {

    public CloseDate(LocalDate date, BigDecimal price){
        this.date = date;
        this.price = price;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    private LocalDate date;

    private BigDecimal price;
}
