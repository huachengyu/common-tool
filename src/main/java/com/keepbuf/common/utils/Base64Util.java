package com.keepbuf.common.utils;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;

/**
 * Created by IntelliJ IDEA.
 *
 * @author huacy
 * @since 2017/12/03
 */
public class Base64Util {

    public static String base64Encode(final String value) {
        return Base64.encodeBase64String(value.getBytes(StandardCharsets.UTF_8));
    }

    public static String base64Decode(final String value) {
        byte[] decodeValue = Base64.decodeBase64(value);
        return new String(decodeValue, StandardCharsets.UTF_8);
    }
}
