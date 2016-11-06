package assignment.datasource;

import assignment.model.Prices;
import net.sf.ehcache.Ehcache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Created by xuan on 11/5/2016.
 */
@Component
public class CacheMonitor {

    private static final Logger logger = LoggerFactory.getLogger(CacheMonitor.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private DataHolder dataHolder;

    public CacheMonitor(CacheManager cacheManager, DataHolder dataHolder){
        this.cacheManager = cacheManager;
        this.dataHolder = dataHolder;
    }

    private static final String CACHE_NAME = "closedates";

    @Scheduled(fixedDelay =1 * 60 * 1000 )
    public void run(){
        try{
            LocalDateTime startDate = LocalDateTime.now();
            if(logger.isDebugEnabled()){
                logger.debug("cache monitor started." + startDate.toString());
            }

            Cache cache = cacheManager.getCache(CACHE_NAME);
            Ehcache ehcache = (Ehcache)cache.getNativeCache();
            List<String> keys  = ehcache.getKeys();
            for(String key: keys){
                Prices prices = cache.get(key, Prices.class);
                if(prices != null){
                    dataHolder.refreshDateSet(key, prices.getEtag());
                }
            }

            if(logger.isDebugEnabled()) {
                LocalDateTime endDate = LocalDateTime.now();
                ZoneId zoneId = ZoneId.systemDefault(); // or: ZoneId.of("Europe/Oslo");
                long startMilliSeconds = startDate.atZone(zoneId).toInstant().toEpochMilli();
                long endMilliSeconds  = endDate.atZone(zoneId).toInstant().toEpochMilli();
                long duration = endMilliSeconds - startMilliSeconds;
                logger.debug("Cache monitor ran in (milliseconds) " + duration + " for " + keys.size() + " entities");
            }
        }catch (Exception ex){
           logger.error("Error while refreshing cache.", ex);
        }
    }
}
