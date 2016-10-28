package com.axibase.chartstesting.screenshotmatcher.proxy.configuration.filters;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by aleksandr on 25.10.16.
 */
public class TrimmingFilterTest {
    private final LineFilter filter = new TrimmingFilter();

    @Test
    public void testTrimSpaces() throws Exception {
        String line = "   test   text    ";
        assertEquals("Spaces before and after text are omitted", "test   text", filter.filter(line, null));
    }

    @Test
    public void testTrimTabs() throws Exception {
        String line = "\t\ttest\ttext\t\t\t";
        assertEquals("Tabs before and after text are omitted", "test\ttext", filter.filter(line, null));

    }

    @Test
    public void testTrimNewLines() throws Exception {
        String line = "\n\n\ntest\n\n\ntext\n\n\n\n";
        assertEquals("New lines before and after text are omitted", "test\n\n\ntext", filter.filter(line, null));
    }

    @Test
    public void testTrimMisc() throws Exception {
        String line = " \n  \t test \n\t text\n\t\n\t  \n    ";
        assertEquals("Misc whitespaces before and after text are omitted", "test \n\t text", filter.filter(line, null));

    }
}