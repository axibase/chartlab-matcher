package com.axibase.chartstesting.screenshotmatcher.proxy.configuration.filters;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by aleksandr on 25.10.16.
 */
public class ReplaceMatchingFilterTest {
    private final LineFilter filter = new ReplaceMatchingFilter("hello\\s*\\w+", "test");

    @Test
    public void testReplacingMatching() throws Exception {
        assertEquals("replaces \"hello world\" with \"test\"", "test", filter.filter("hello world", null));
        assertEquals("replaces \"helloworld\" with \"test\"", "test", filter.filter("helloworld", null));
        assertEquals("replaces \"hello\nworld\" with \"test\"", "test", filter.filter("hello\nworld", null));
        assertEquals("replaces \"hello \n d\" with \"test\"", "test", filter.filter("hello \n d", null));
    }

    @Test
    public void testNotReplacingMatching() throws Exception {
        assertEquals("does not replace \"hell world\" ", "hell world", filter.filter("hell world", null));
        assertEquals("does not replace \"hellworld\" ", "hellworld", filter.filter("hellworld", null));
        assertEquals("does not replace \"hello-world\" ", "hello-world", filter.filter("hello-world", null));
        assertEquals("does not replace \"  hello world\" ", "  hello world", filter.filter("  hello world", null));
    }
}