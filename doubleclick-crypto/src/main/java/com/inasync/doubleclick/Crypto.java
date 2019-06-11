package com.inasync.doubleclick;

import com.google.doubleclick.crypto.DoubleClickCrypto;
import com.google.doubleclick.crypto.DoubleClickCrypto.Keys;
import com.google.common.io.BaseEncoding;
import com.google.protos.adx.NetworkBid.BidRequest.Hyperlocal;
import com.google.protos.adx.NetworkBid.BidRequest.HyperlocalSet;

import java.security.SignatureException;
import java.util.Base64;
import java.util.Base64.*;
import java.util.Arrays;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
    private static final Decoder _base64UrlDecoder = Base64.getUrlDecoder();
    private static final Encoder _base64UrlEncoder = Base64.getUrlEncoder();

    public static void main(String[] args) throws Exception {
        if (args.length < 5) { return; }

        DoubleClickCrypto.Keys keys = new DoubleClickCrypto.Keys(
            new SecretKeySpec(Base64.getDecoder().decode(args[3]), "HmacSHA1")
          , new SecretKeySpec(Base64.getDecoder().decode(args[4]), "HmacSHA1")
        );

        Object result;
        switch (args[0]) {
            case "encrypt":
                result = encrypt(keys, args[1], args[2], BaseEncoding.base16().decode(args[5]));
                break;

            case "decrypt":
                result = decrypt(keys, args[1], args[2]);
                break;

            default:
                result = null;
                break;
        }

        System.out.println(result);
    }

    private static String encrypt(DoubleClickCrypto.Keys keys, String type, String value, byte[] iv) {
        switch (type) {
            case "base":
                DoubleClickCrypto baseCrypto = new DoubleClickCrypto(keys);
                // System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(_base64UrlDecoder.decode(value)));
                // System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(com.google.common.primitives.Bytes.concat(_base64UrlDecoder.decode(value), iv)));
                return _base64UrlEncoder.encodeToString(baseCrypto.encrypt(com.google.common.primitives.Bytes.concat(iv, _base64UrlDecoder.decode(value), new byte[4])));

            case "price":
                DoubleClickCrypto.Price priceCrypto = new DoubleClickCrypto.Price(keys);
                return priceCrypto.encodePriceValue(Double.parseDouble(value), iv);
        }
        return null;
    }

    private static Object decrypt(DoubleClickCrypto.Keys keys, String type, String value) throws SignatureException {
        byte[] buff;
        switch (type) {
            case "base":
                DoubleClickCrypto baseCrypto = new DoubleClickCrypto(keys);
                buff = baseCrypto.decrypt(_base64UrlDecoder.decode(value));
                return _base64UrlEncoder.encodeToString(Arrays.copyOfRange(buff, 16, buff.length - 4));

            case "price":
                DoubleClickCrypto.Price priceCrypto = new DoubleClickCrypto.Price(keys);
                return priceCrypto.decodePriceValue(value);
        }
        return null;
    }
}
