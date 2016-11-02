package assignment.service;

import assignment.controller.CloseDate;
import assignment.model.Prices;
import assignment.datasource.DataSet;
import assignment.datasource.QuandlDataSource;
import assignment.datasource.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by xuan on 11/1/2016.
 */
@Service
public class ClosePriceService {

    private static Logger logger = LoggerFactory.getLogger(ClosePriceService.class);

    static final String API = "https://www.quandl.com/api/v3/datasets/WIKI/";

    @Autowired
    private QuandlDataSource quandlDataSource;

    public ClosePriceService(QuandlDataSource quandlDataSource) {
        this.quandlDataSource = quandlDataSource;
    }

    public Prices getClosePrices(String ticker, LocalDate startDate, LocalDate endDate) throws QuandlDataSource.InvalidTicker {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("column_index", 4);
        params.put("start_date", startDate);
        params.put("end_date", endDate);
        return quandlDataSource.getDataSet(ticker, params);
    }
}
