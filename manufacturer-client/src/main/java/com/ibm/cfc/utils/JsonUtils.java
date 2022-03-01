package com.ibm.cfc.utils;

import com.owlike.genson.Genson;

public class JsonUtils {

    private final static Genson genson = new Genson();

    public static Genson getGenson() {
        return genson;
    }
}
