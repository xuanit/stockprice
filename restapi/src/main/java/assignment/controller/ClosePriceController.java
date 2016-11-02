package assignment.controller;

import assignment.datasource.QuandlDataSource;
import assignment.model.Prices;
import assignment.service.ClosePriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Created by xuan on 11/1/2016.
 */
@RestController
@RequestMapping("api/v2/{tickersymbol}/")
public class ClosePriceController {

    @Autowired
    private ClosePriceService closePriceService;

    public ClosePriceController(ClosePriceService closePriceService) {
        this.closePriceService = closePriceService;
    }

    private static final String DATE_FORMAT_ERROR =  "%s is not recognzied.Please provide YYYY-MM-DD";

    @RequestMapping("closePrice")
    public HttpEntity<ClosePriceResponse> getClosePrices(@PathVariable("tickersymbol") String ticker,
                                                         @RequestParam("startDate") String startDateParam, @RequestParam("endDate") String endDateParam ){
        LocalDate startDate = null;
        LocalDate endDate = null;
        ClosePriceResponse response = new ClosePriceResponse();
        try{
            startDate = LocalDate.parse(startDateParam);
        }catch (DateTimeParseException ex){
            response.addError(String.format(DATE_FORMAT_ERROR, "startDate"));
        }
        try{
            endDate = LocalDate.parse(endDateParam);
        }catch (DateTimeParseException ex) {
            response.addError(String.format(DATE_FORMAT_ERROR, "endDate"));
        }
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
            response.addError("Invalid ticker");
            return new ResponseEntity<ClosePriceResponse>(response, HttpStatus.NOT_FOUND);
        }
        response.setPrices(prices);
        return  new ResponseEntity<ClosePriceResponse>(response, HttpStatus.OK);
    }

    @RequestMapping("test")
    public HttpEntity<Void> test(@RequestParam(value = "date", required = false) LocalDate date) {
        return new ResponseEntity<Void>(HttpStatus.OK);
    }
}
