package com.axibase.chartstesting.screenshotmatcher.core;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by aleksandr on 26.10.16.
 */
public class PortalTest {

    @Test
    public void testParsePath() throws Exception {
        Portal p = new Portal("075941a0/15");
        assertEquals("correct id", "075941a0", p.getConfigId());
        assertEquals("correct revision", "15", p.getRevisionString());
    }

}