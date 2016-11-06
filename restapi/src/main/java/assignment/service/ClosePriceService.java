package assignment.service;

import assignment.datasource.DataSource;
import assignment.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Created by xuan on 11/1/2016.
 */
@Service
public class ClosePriceService {

    private static Logger logger = LoggerFactory.getLogger(ClosePriceService.class);

    @Autowired
    private DataSource dataSource;

    public ClosePriceService(DataSource quandlDataSource) {
        this.dataSource = quandlDataSource;
    }

    public Prices getClosePrices(String ticker, LocalDate startDate, LocalDate endDate) throws InvalidTickerException {
        if(logger.isDebugEnabled()) {
            logger.debug("get close prices of {} from {} to {}", ticker, startDate, endDate);
        }
        return dataSource.getClosePrices(ticker, startDate, endDate);
    }
}
