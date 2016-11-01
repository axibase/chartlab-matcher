package com.axibase.chartstesting.screenshotmatcher.proxy.configuration.filters;

import com.axibase.chartstesting.screenshotmatcher.core.Portal;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by aleksandr on 31.10.16.
 */
public class DateReplaceFilterTest {
    @Test
    public void filter() throws Exception {
        Portal p = new Portal("1", "1");
        p.setEndtime("hello");
        LineFilter f = new DateReplaceFilter();
        assertEquals("replace Date with Date(hello)", "Date(hello)", f.filter("Date()", p));
    }

}