package com.axibase.chartstesting.screenshotmatcher.proxy.configuration.filters;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by aleksandr on 28.10.16.
 */
public class PropertyNameFilterTest {
    @Test
    public void testFilterDashes() throws Exception {
        LineFilter filter = new PropertyNameFilter();
        assertEquals("Filters dashes", "helloworld = foo", filter.filter("hello-world = foo", null));
    }

}