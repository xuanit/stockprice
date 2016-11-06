package assignment.datasource.quandl;

import assignment.datasource.*;
import assignment.model.DateClose;
import assignment.model.InvalidTickerException;
import assignment.model.Prices;
import com.sun.javaws.exceptions.InvalidArgumentException;
import org.apache.commons.lang3.ObjectUtils;
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
import java.security.cert.PKIXRevocationChecker;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by xuan on 11/4/2016.
 */
@Component
public class QuandlDataHolder implements DataHolder {

    private static Logger logger = LoggerFactory.getLogger(QuandlDataHolder.class);

    private static  final String API_URL = "https://www.quandl.com/api/v3/datasets/WIKI/";

    public static final String ORDER_ASC = "asc";

    public static final String ORDER = "order";

    @Autowired
    private RestTemplate restTemplate;

    public QuandlDataHolder(RestTemplate restTemplate) {
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

    @Override
    public Prices getDataSet(String ticker) throws InvalidTickerException {
        if(ticker == null) throw new NullPointerException("dataSet is null");

        return callAPI(ticker, null).get();
    }

    private Optional<Prices> callAPI(String dataSetName, String etag) throws InvalidTickerException {
        assert dataSetName != null;
        if(logger.isDebugEnabled()){
            logger.debug("Getting dataset {} with etag {}", dataSetName, etag);
        }

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(API_URL).append("{dataSet}.json?");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("api_key", "N1us7CxC5N1tiCVFTdsk");//for testing at local
        params.put(DefaultDataSource.COLUMN_INDEX, DefaultDataSource.CLOSE_COLUMN);
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
            if(logger.isDebugEnabled()){
                logger.debug("dataset has not changed.");
            }
            return Optional.empty();
        }
        //Response response = this.restTemplate.getForObject(urlBuilder.toString(), Response.class, params);
        Response response = responseEntity.getBody();
        checkErrors(response);
        String newEtag = responseEntity.getHeaders().getETag();
        Prices prices = convertToPrices(newEtag, response);
        return Optional.of(prices);
    }

    @Override
    public Optional<Prices> refreshDateSet(String ticker, String etag) throws InvalidTickerException {
        if(ticker == null) throw new NullPointerException("dataSet is null");

        return this.callAPI(ticker, etag);
    }
    private void checkErrors(Response response) throws InvalidTickerException {
        assert response != null;

        if(response.getError() != null) {
            if("QECx02".equals(response.getError().getCode())) {
                throw new InvalidTickerException(response.getError().getMessage());
            }
            throw new UnknownError(response.getError().getMessage());
        }
    }

    private Prices convertToPrices(String etag, Response response) {
        assert etag != null;
        assert response != null;

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
        Prices prices = new Prices(dataSet.getDataSetCode(), dateCloses, etag);
        return prices;
    }
}
