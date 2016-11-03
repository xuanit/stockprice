package assignment.controller;

import assignment.controller.response.DayMovingAverageListResponse;
import assignment.model.DayMovingAverageList;
import assignment.service.ClosePriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by xuan on 11/3/2016.
 */
@RestController
@RequestMapping("api/v2/200dma")
public class DayMovingAverageController {

    @Autowired
    private StringToLocalDateConverter stringToLocalDateConverter;

    @Autowired
    private ClosePriceService closePriceService;

    public DayMovingAverageController(StringToLocalDateConverter converter, ClosePriceService closePriceService) {
        this.stringToLocalDateConverter = converter;
        this.closePriceService = closePriceService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public HttpEntity<DayMovingAverageListResponse> getDayMovingAverageList(@RequestParam("startDate") String startDateParam,
                                                                            @RequestParam("ticker") String idsParam) {
        DayMovingAverageListResponse response = new DayMovingAverageListResponse();
        LocalDate startDate = this.stringToLocalDateConverter.convert(startDateParam, "start date", response);
        idsParam = idsParam.replace(" ", "");
        String[] ids = idsParam.split(",");
        if(response.hasError()) {
            return new ResponseEntity<DayMovingAverageListResponse>(response, HttpStatus.NOT_FOUND);
        }
        DayMovingAverageList dayMovingAverageList = this.closePriceService.get200DMAList(Arrays.asList(ids), startDate);
        response.setDayMovingAverageList(dayMovingAverageList);
        return new ResponseEntity<DayMovingAverageListResponse>(response, HttpStatus.OK);
    }
}
