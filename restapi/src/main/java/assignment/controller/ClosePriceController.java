package assignment.controller;

import assignment.controller.response.ClosePriceResponse;
import assignment.datasource.DefaultDataSource;
import assignment.model.InvalidTickerException;
import assignment.model.Prices;
import assignment.service.ClosePriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ClosePriceController.class);

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

        logger.debug("Print it");
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
        } catch (InvalidTickerException ex) {
            response.addError(INVALID_TICKER_ERROR);
            return new ResponseEntity<ClosePriceResponse>(response, HttpStatus.NOT_FOUND);
        }
        response.setPrices(prices);
        return  new ResponseEntity<ClosePriceResponse>(response, HttpStatus.OK);
    }


}
