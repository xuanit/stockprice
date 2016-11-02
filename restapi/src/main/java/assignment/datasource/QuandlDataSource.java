package assignment.datasource;

import assignment.controller.CloseDate;
import assignment.model.Prices;
import assignment.service.ClosePriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xuan on 11/2/2016.
 */
@Component
public class QuandlDataSource {

    private static Logger logger = LoggerFactory.getLogger(QuandlDataSource.class);

    public static class InvalidTicker extends Exception{

    }

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

    public Prices getDataSet(String dataSetName, Map<String, Object> params) throws InvalidTicker {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(API_URL).append("{dataSet}.json?");
        for(String param : params.keySet()) {
            urlBuilder.append(param).append("={").append(param).append("}&");
        }
        Map<String, Object> paramsWithDataSet = new LinkedHashMap<>(params);
        paramsWithDataSet.put("dataSet", dataSetName);
        Response response = this.restTemplate.getForObject(urlBuilder.toString(), Response.class, paramsWithDataSet);
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
            prices.getCloseDates().add(new CloseDate(closeDate, closePrice));
        }
        return prices;
    }
}
