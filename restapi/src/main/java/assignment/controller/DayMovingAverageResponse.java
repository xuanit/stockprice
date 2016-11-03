package assignment.controller;

import assignment.model.DayMovingAverage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by xuan on 11/2/2016.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DayMovingAverageResponse extends BaseResponse {

    @JsonProperty("200dma")
    private DayMovingAverage dayMovingAverage;

    public DayMovingAverage getDayMovingAverage() {
        return dayMovingAverage;
    }

    public void setDayMovingAverage(DayMovingAverage dayMovingAverage) {
        this.dayMovingAverage = dayMovingAverage;
    }
}
