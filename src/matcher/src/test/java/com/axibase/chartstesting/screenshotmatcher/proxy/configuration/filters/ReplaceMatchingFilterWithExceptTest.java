package com.axibase.chartstesting.screenshotmatcher.proxy.configuration.filters;

import static org.junit.Assert.*;

/**
 * Created by aleksandr on 28.10.16.
 */
public class ReplaceMatchingFilterWithExceptTest {
    private LineFilter etReplacer = new ReplaceMatchingFilterWithExcept("endtime.*", "endtime =\\s*\\d{4}-\\d{2}-\\d{2}.*", "");
}