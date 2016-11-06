package assignment.datasource;

import assignment.model.InvalidTickerException;
import assignment.model.Prices;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;

/**
 * Created by xuan on 11/6/2016.
 */
public interface DataHolder {

    /**
     * gets data set of the ticker. If the data set is in cache, return it. Otherwise, calls remote source to get dataset.
     * @param ticker ticker.
     * @return Prices data set of the ticker.
     * @throws InvalidTickerException invalid ticker
     */
    @Cacheable("closedates")
    Prices getDataSet(String ticker) throws InvalidTickerException;

    /**
     * gets latest dataset from remote source. If it has changed, put the new data set to cache and return an optimal of the new data set.
     * Otherwise, empty optimal is returned.
     * @param ticker ticker
     * @param etag
     * @return an optimal of the new date set, if it has changed at remote source. Otherwise, empty optimal.
     * @throws InvalidTickerException
     */
    @CachePut(cacheNames = "closedates", key="#ticker", unless="#result == null")
    Optional<Prices> refreshDateSet(String ticker, String etag) throws InvalidTickerException;
}
