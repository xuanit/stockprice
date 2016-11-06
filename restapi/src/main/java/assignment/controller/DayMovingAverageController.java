package assignment.controller;

import assignment.controller.response.DayMovingAverageListResponse;
import assignment.controller.response.DayMovingAverageResponse;
import assignment.datasource.DefaultDataSource;
import assignment.model.DayMovingAverage;
import assignment.model.DayMovingAverageList;
import assignment.model.InvalidTickerException;
import assignment.service.ClosePriceService;
import assignment.service.DayMovingAverageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

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
    private DayMovingAverageService dayMovingAverageService;

    public DayMovingAverageController(StringToLocalDateConverter converter, DayMovingAverageService dayMovingAverageService) {
        this.stringToLocalDateConverter = converter;
        this.dayMovingAverageService = dayMovingAverageService;
    }

    @RequestMapping(path = "200dma", method = RequestMethod.GET)
    public HttpEntity<DayMovingAverageListResponse> getDayMovingAverageList(@RequestParam("startDate") String startDateParam,
                                                                            @RequestParam("ticker") String idsParam) {
        DayMovingAverageListResponse response = new DayMovingAverageListResponse();
        LocalDate startDate = stringToLocalDateConverter.convert(startDateParam, "start date", response);
        idsParam = idsParam.replace(" ", "");
        String[] ids = idsParam.split(",");
        if(response.hasError()) {
            return new ResponseEntity<DayMovingAverageListResponse>(response, HttpStatus.NOT_FOUND);
        }
        DayMovingAverageList dayMovingAverageList = dayMovingAverageService.get200DMAList(Arrays.asList(ids), startDate);
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
        Optional<DayMovingAverage> optDayMovingAverage = Optional.empty();
        try {
            optDayMovingAverage = dayMovingAverageService.get200DMA(ticker, startDate);
            if(!optDayMovingAverage.isPresent()){
                Optional<LocalDate> optFirstStartDateHaving200DMA = dayMovingAverageService.getFirstStartDateHaving200DMA(ticker);
                if(optFirstStartDateHaving200DMA.isPresent()){
                    response.addError("There is not enough data for 200 day moving average calculation. The first start date having enough data is " + optFirstStartDateHaving200DMA.get().toString());
                }else {
                    response.addError("There is not enough data for 200 day moving average calculation.");
                }

            }else {
                optDayMovingAverage.get().setStartDate(null);
                response.setDayMovingAverage(optDayMovingAverage.get());
            }
        } catch (InvalidTickerException ex) {
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
