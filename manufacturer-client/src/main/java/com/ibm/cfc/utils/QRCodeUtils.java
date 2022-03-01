package com.ibm.cfc.utils;

import com.google.zxing.qrcode.QRCodeWriter;

public class QRCodeUtils {
    private static final QRCodeWriter qrCodeWriter = new QRCodeWriter();

    private QRCodeUtils() {
    }

    public static QRCodeWriter qrCodeWriter() {
        return qrCodeWriter;
    }
}
