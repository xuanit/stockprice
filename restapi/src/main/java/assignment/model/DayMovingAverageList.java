package assignment.model;

import assignment.model.DayMovingAverage;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuan on 11/3/2016.
 */
public class DayMovingAverageList {

    public DayMovingAverageList(LocalDate startDate) {
        this.startDate = startDate;
    }

    @JsonSerialize(using = ToStringSerializer.class)
    private LocalDate startDate;

    @JsonProperty("Data")
    private List<DayMovingAverage> dayMovingAverages = new ArrayList<>();

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public List<DayMovingAverage> getDayMovingAverages() {
        return dayMovingAverages;
    }

    public void setDayMovingAverages(List<DayMovingAverage> dayMovingAverages) {
        this.dayMovingAverages = dayMovingAverages;
    }
}
