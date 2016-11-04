package assignment.controller;

import assignment.controller.response.DayMovingAverageListResponse;
import assignment.controller.response.DayMovingAverageResponse;
import assignment.datasource.QuandlDataSource;
import assignment.model.DayMovingAverage;
import assignment.model.DayMovingAverageList;
import assignment.service.ClosePriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by xuan on 11/3/2016.
 */
@RestController
@RequestMapping("api/v2/")
public class DayMovingAverageController {

    @Autowired
    private StringToLocalDateConverter stringToLocalDateConverter;

    private static final String INVALID_TICKER_ERROR = "Invalid ticker";

    @Autowired
    private ClosePriceService closePriceService;

    public DayMovingAverageController(StringToLocalDateConverter converter, ClosePriceService closePriceService) {
        this.stringToLocalDateConverter = converter;
        this.closePriceService = closePriceService;
    }

    @RequestMapping(path = "200dma", method = RequestMethod.GET)
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

    @RequestMapping(path = "{tickersymbol}/200dma", method = RequestMethod.GET)
    public HttpEntity<DayMovingAverageResponse> get200DMA(@PathVariable("tickersymbol")String ticker, @RequestParam("startDate") String startDateParam){
        DayMovingAverageResponse response = new DayMovingAverageResponse();
        LocalDate startDate = stringToLocalDateConverter.convert(startDateParam, "startDate", response);
        if(response.getErrors() != null) {
            return new ResponseEntity<DayMovingAverageResponse>(response, HttpStatus.NOT_FOUND);
        }
        DayMovingAverage dayMovingAverage = null;
        try {
            dayMovingAverage = this.closePriceService.get200DMA(ticker, startDate);
            if(dayMovingAverage == null){
                LocalDate firstStartDateHaving200DMA = this.closePriceService.getFirstStartDateHaving200DMA(ticker);
                response.addError("There is not enough data for 200 day moving average calculation. The first start date having enough data is " + firstStartDateHaving200DMA.toString());
            }else {
                dayMovingAverage.setStartDate(null);
                response.setDayMovingAverage(dayMovingAverage);
            }
        } catch (QuandlDataSource.InvalidTicker invalidTicker) {
            response.addError(INVALID_TICKER_ERROR);
            return new ResponseEntity<DayMovingAverageResponse>(response, HttpStatus.NOT_FOUND);
        }
        if(response.getErrors() != null) {
            return new ResponseEntity<DayMovingAverageResponse>(response, HttpStatus.NOT_FOUND);
        }else {
            return new ResponseEntity<DayMovingAverageResponse>(response, HttpStatus.OK);
        }
    }
}
