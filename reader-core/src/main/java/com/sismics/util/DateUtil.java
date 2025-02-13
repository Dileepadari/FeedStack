package com.sismics.util;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Map.Entry;

/**
 * Date utilities.
 *
 * @author jtremeaux
 */
public class DateUtil {
    private static final Logger log = LoggerFactory.getLogger(DateUtil.class);

    private final static ImmutableMap<String, String> TIMEZONE_CODE_MAP = new ImmutableMap.Builder<String, String>()
            .put("ACDT", " +10:30")
            .put("ACST", " +09:30")
            .put("ACT", " +08")
            .put("ADT", " -03")
            .put("AEDT", " +11")
            .put("AEST", " +10")
            .put("AFT", " +04:30")
            .put("AKDT", " -08")
            .put("AKST", " -09")
            .put("AMST", " +05")
            .put("AMT", " +04")
            .