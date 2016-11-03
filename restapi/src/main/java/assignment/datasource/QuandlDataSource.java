package assignment.datasource;

import assignment.controller.DateClose;
import assignment.model.Prices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by xuan on 11/2/2016.
 */
@Component
public class QuandlDataSource {

    private static Logger logger = LoggerFactory.getLogger(QuandlDataSource.class);

    public static class InvalidTicker extends Exception{

    }

    public static final String START_DATE = "start_date";
    public static final String END_DATE = "end_date";
    public static final String COLUMN_INDEX = "column_index";
    public static final String LIMIT = "limit";
    public static final String ORDER = "order";
    public static final int CLOSE_COLUMN = 4;
    public static final int DATE_COLUMN = 0;
    public static final String ORDER_ASC = "asc";

    @Autowired
    private RestTemplate restTemplate;

    public QuandlDataSource(RestTemplate restTemplate) {
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            DefaultResponseErrorHandler defaultHandler = new DefaultResponseErrorHandler();
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                if(response.getStatusCode() == HttpStatus.NOT_FOUND){
                    return  false;
                }
                return defaultHandler.hasError(response);
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                defaultHandler.handleError(response);
            }
        });
        this.restTemplate = restTemplate;
    }
    private static  final String API_URL = "https://www.quandl.com/api/v3/datasets/WIKI/";

    public Prices getClosePrices(String dateSetName, LocalDate startDate, LocalDate endDate) throws InvalidTicker {
        Prices prices = this.getDataSet(dateSetName);
        List<DateClose> dateCloses = prices.getDateCloses();
        DateClose dummyStartDateClose = new DateClose(startDate, BigDecimal.ZERO);
        DateClose dummyEndDateClose = new DateClose(endDate, BigDecimal.ZERO);
        int startCloseDateIndex = Collections.binarySearch(dateCloses, dummyStartDateClose);
        startCloseDateIndex = startCloseDateIndex >=0? startCloseDateIndex: Math.abs(startCloseDateIndex + 1);
        int endCloseDateIndex = Math.abs(Collections.binarySearch(dateCloses, dummyEndDateClose) + 1);
        List<DateClose> filteredDateCloses = dateCloses.subList(startCloseDateIndex, endCloseDateIndex);
        prices.setDateCloses(filteredDateCloses);
        return prices;
    }

    public Prices getClosePrices(String tickerSymbol, LocalDate startDate, int limit) throws InvalidTicker {
        Prices prices = this.getDataSet(tickerSymbol);
        List<DateClose> dateCloses = prices.getDateCloses();
        DateClose startDateClose = new DateClose(startDate, BigDecimal.ZERO);
        int startCloseDateIndex = Math.abs(Collections.binarySearch(dateCloses, startDateClose));
        int endIndex = startCloseDateIndex + limit < dateCloses.size()? startCloseDateIndex + limit: dateCloses.size();
        prices.setDateCloses(dateCloses.subList(startCloseDateIndex, endIndex));
        return prices;
    }

    public Prices getClosePrices(String tickerSymbol, int limit) throws InvalidTicker {
        Prices prices = this.getDataSet(tickerSymbol);
        List<DateClose> dateCloses = prices.getDateCloses();
        int startIndex = dateCloses.size() > limit? dateCloses.size() - limit: 0;
        prices.setDateCloses(dateCloses.subList(startIndex, dateCloses.size()));
        return prices;
    }

    private Prices getDataSet(String dataSetName) throws InvalidTicker {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(API_URL).append("{dataSet}.json?");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("api_key", "N1us7CxC5N1tiCVFTdsk");//for testing at local
        params.put(QuandlDataSource.COLUMN_INDEX, QuandlDataSource.CLOSE_COLUMN);
        params.put(ORDER, ORDER_ASC);
        for(String param : params.keySet()) {
            urlBuilder.append(param).append("={").append(param).append("}&");
        }
        params.put("dataSet", dataSetName);
        Response response = this.restTemplate.getForObject(urlBuilder.toString(), Response.class, params);
        checkErrors(response);
        Prices prices = convertToPrices(response);
        return prices;
    }

    private void checkErrors(Response response) throws InvalidTicker {
        if(response.getError() != null) {
            if("QECx02".equals(response.getError().getCode())) {
                throw new InvalidTicker();
            }
            throw new UnknownError(response.getError().getMessage());
        }
    }

    private Prices convertToPrices(Response response) {
        if(response.getDataset() == null) {
            return  null;
        }
        DataSet dataSet = response.getDataset();
        Prices prices = new Prices();
        prices.setTicker(dataSet.getDataSetCode());
        DecimalFormat bigDecimalFormat = new DecimalFormat();
        bigDecimalFormat.setParseBigDecimal(true);
        for(List<String> closeDateData : dataSet.getData()) {
            LocalDate closeDate = LocalDate.parse(closeDateData.get(0));
            BigDecimal closePrice  = null;
            try {
                closePrice =(BigDecimal)bigDecimalFormat.parse(closeDateData.get(1));
            } catch (ParseException e) {
                logger.error("Error while parsing close date", e);
                continue;
            }
            prices.getDateCloses().add(new DateClose(closeDate, closePrice));
        }
        return prices;
    }
}
