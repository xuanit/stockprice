package assignment.datasource;

import assignment.model.InvalidTickerException;
import assignment.model.Prices;
import net.sf.ehcache.Ehcache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Arrays;

/**
 * Created by xuan on 11/5/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class CacheMonitorTest {

    private CacheMonitor cacheMonitor;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @Mock
    private Ehcache ehcache;

    @Mock
    private DataHolder dataHolder;

    @Before
    public void init() {
        when(cacheManager.getCache("closedates")).thenReturn(cache);
        when(cache.getNativeCache()).thenReturn(ehcache);
        cacheMonitor = new CacheMonitor(cacheManager, dataHolder);
    }

    @Test
    public void testRunNormally() throws InvalidTickerException {
        when(ehcache.getKeys()).thenReturn(Arrays.asList("FB"));
        Prices prices = new Prices("FB", null, "etag");
        when(cache.get("FB", Prices.class)).thenReturn(prices);
        this.cacheMonitor.run();
        verify(dataHolder, times(1)).refreshDateSet("FB", "etag");
    }

    @Test
    public void testRunExceptionCaught() throws InvalidTickerException {
        when(ehcache.getKeys()).thenReturn(Arrays.asList("FB"));
        Prices prices = new Prices("FB", null, "etag");
        when(cache.get("FB", Prices.class)).thenReturn(prices);
        when(dataHolder.refreshDateSet(any(), any())).thenThrow(Exception.class);

        this.cacheMonitor.run();

        verify(dataHolder, times(1)).refreshDateSet(any(), any());
    }
}
