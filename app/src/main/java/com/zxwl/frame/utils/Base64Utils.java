package com.zxwl.frame.utils;

import android.util.Base64;

public class Base64Utils {

    public static String decode(String str) {
//        String strBase64 = Base64.encodeToString(str.getBytes(), Base64.DEFAULT);
        //base64解码
        String result = new String(Base64.decode(str.getBytes(), Base64.URL_SAFE));
        return result;
    }

}
