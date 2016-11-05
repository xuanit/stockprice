package assignment.datasource;

import assignment.model.DateClose;
import assignment.model.Prices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xuan on 11/4/2016.
 */
@Component
public class DataHolder {

    private static Logger logger = LoggerFactory.getLogger(DataHolder.class);

    private static  final String API_URL = "https://www.quandl.com/api/v3/datasets/WIKI/";

    public static final String ORDER_ASC = "asc";

    public static final String ORDER = "order";

    @Autowired
    private RestTemplate restTemplate;

    public DataHolder(RestTemplate restTemplate) {
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

    @Cacheable("closedates")
    public Prices getDataSet(String dataSetName) throws QuandlDataSource.InvalidTicker {
        return callAPI(dataSetName, null);
    }

    private Prices callAPI(String dataSetName, String etag) throws QuandlDataSource.InvalidTicker {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(API_URL).append("{dataSet}.json?");
        Map<String, Object> params = new LinkedHashMap<>();
        //params.put("api_key", "N1us7CxC5N1tiCVFTdsk");//for testing at local
        params.put(QuandlDataSource.COLUMN_INDEX, QuandlDataSource.CLOSE_COLUMN);
        params.put(ORDER, ORDER_ASC);
        for(String param : params.keySet()) {
            urlBuilder.append(param).append("={").append(param).append("}&");
        }
        params.put("dataSet", dataSetName);
        HttpHeaders headers = new HttpHeaders();
        if(etag != null){
            headers.setIfNoneMatch(etag);
        }
        HttpEntity requestEntity = new HttpEntity(null, headers);
        ResponseEntity<Response> responseEntity = this.restTemplate.exchange(urlBuilder.toString(), HttpMethod.GET,requestEntity,
                Response.class, params);
        if(responseEntity.getStatusCode() == HttpStatus.NOT_MODIFIED) {
            return null;
        }
        //Response response = this.restTemplate.getForObject(urlBuilder.toString(), Response.class, params);
        Response response = responseEntity.getBody();
        checkErrors(response);
        Prices prices = convertToPrices(response);
        prices.setEtag(responseEntity.getHeaders().getETag());
        return prices;
    }

    @CachePut(cacheNames = "closedates", key="#dataSet", unless = "#result==null")
    public Prices refreshDateSet(String dataSet, String etag) throws QuandlDataSource.InvalidTicker {
        return this.callAPI(dataSet, etag);
    }
    private void checkErrors(Response response) throws QuandlDataSource.InvalidTicker {
        if(response.getError() != null) {
            if("QECx02".equals(response.getError().getCode())) {
                throw new QuandlDataSource.InvalidTicker();
            }
            throw new UnknownError(response.getError().getMessage());
        }
    }

    private Prices convertToPrices(Response response) {
        if(response.getDataset() == null) {
            return  null;
        }
        DataSet dataSet = response.getDataset();
        List<DateClose> dateCloses = new ArrayList<>();
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
            dateCloses.add(new DateClose(closeDate, closePrice));
        }
        Prices prices = new Prices(dataSet.getDataSetCode(), dateCloses);
        return prices;
    }
}
