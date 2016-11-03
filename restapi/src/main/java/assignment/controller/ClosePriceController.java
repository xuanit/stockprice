package assignment.controller;

import assignment.controller.response.ClosePriceResponse;
import assignment.controller.response.DayMovingAverageResponse;
import assignment.datasource.QuandlDataSource;
import assignment.model.DayMovingAverage;
import assignment.model.Prices;
import assignment.service.ClosePriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Created by xuan on 11/1/2016.
 */
@RestController
@RequestMapping("api/v2/{tickersymbol}/")
public class ClosePriceController {

    @Autowired
    private ClosePriceService closePriceService;

    private static final String INVALID_TICKER_ERROR = "Invalid ticker";

    @Autowired
    private StringToLocalDateConverter stringToLocalDateConverter;

    public ClosePriceController(ClosePriceService closePriceService, StringToLocalDateConverter stringToLocalDateConverter) {
        this.closePriceService = closePriceService;
        this.stringToLocalDateConverter = stringToLocalDateConverter;
    }

    @RequestMapping(value = "closePrice", method = RequestMethod.GET)
    public HttpEntity<ClosePriceResponse> getClosePrices(@PathVariable("tickersymbol") String ticker,
                                                         @RequestParam("startDate") String startDateParam, @RequestParam("endDate") String endDateParam ){
        ClosePriceResponse response = new ClosePriceResponse();
        LocalDate startDate = stringToLocalDateConverter.convert(startDateParam, "startDate", response);
        LocalDate endDate = stringToLocalDateConverter.convert(endDateParam, "endDate", response);
        if(startDate != null && endDate != null && startDate.isAfter(endDate)){
            response.addError("startDate is after endDate.");
        }
        if(response.getErrors() != null) {
            return new ResponseEntity<ClosePriceResponse>(response, HttpStatus.NOT_FOUND);
        }
        Prices prices = null;
        try {
            prices = closePriceService.getClosePrices(ticker, startDate, endDate);
        } catch (QuandlDataSource.InvalidTicker invalidTicker) {
            response.addError(INVALID_TICKER_ERROR);
            return new ResponseEntity<ClosePriceResponse>(response, HttpStatus.NOT_FOUND);
        }
        response.setPrices(prices);
        return  new ResponseEntity<ClosePriceResponse>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "200dma", method = RequestMethod.GET)
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
