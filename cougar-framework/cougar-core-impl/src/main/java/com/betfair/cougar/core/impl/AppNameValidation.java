package com.betfair.cougar.core.impl;

import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class AppNameValidation {
    public AppNameValidation(String appName) {
        if (StringUtils.isBlank(appName)) {
            throw new IllegalArgumentException("'cougar.app.name' is a mandatory property");
        }
        if (appName.startsWith("-")) {
            throw new IllegalArgumentException("'cougar.app.name' must not start with a '-': '"+appName+"'");
        }
    }
}
