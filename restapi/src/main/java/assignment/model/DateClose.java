package assignment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by xuan on 11/1/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DateClose implements Comparable<DateClose> {


    private final LocalDate date;

    private final BigDecimal price;

    public DateClose(LocalDate date, BigDecimal price){
        this.date = date;
        this.price = price;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public int compareTo(DateClose dateClose) {
        if(dateClose == null && date == null && dateClose.date == null) {
            throw new NullPointerException("provided close date is null");
        }
        return date.compareTo(dateClose.date);
    }
}
