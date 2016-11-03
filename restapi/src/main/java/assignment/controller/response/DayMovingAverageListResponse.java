package assignment.controller.response;

import assignment.model.DayMovingAverageList;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by xuan on 11/3/2016.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DayMovingAverageListResponse extends BaseResponse {

    @JsonProperty("200dmas")
    private DayMovingAverageList dayMovingAverageList;

    public DayMovingAverageList getDayMovingAverageList() {
        return dayMovingAverageList;
    }

    public void setDayMovingAverageList(DayMovingAverageList dayMovingAverageList) {
        this.dayMovingAverageList = dayMovingAverageList;
    }
}
