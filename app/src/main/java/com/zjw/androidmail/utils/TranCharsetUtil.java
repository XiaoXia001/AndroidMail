package com.zjw.androidmail.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by zhaojinwei on 2017/4/7.
 */

public class TranCharsetUtil {

    private static final String PRE_FIX_UTF = "&#x";

    private static final String POS_FIX_UTF = ";";

    public TranCharsetUtil(){

    }

    public static String XmlFormalize(String sTemp){
        StringBuffer stringBuffer = new StringBuffer();

        if (sTemp == null || sTemp.equals("")){
            return "";
        }
        String s = TranCharsetUtil.TranEncodeTOGB(sTemp);
        for (int i = 0; i < s.length(); i++){
            char cChar = s.charAt(i);
            if (TranCharsetUtil.isGB2312(cChar)){
                stringBuffer.append(PRE_FIX_UTF);
                stringBuffer.append(Integer.toHexString(cChar));
                stringBuffer.append(POS_FIX_UTF);
            }else {
                switch ((int) cChar) {
                    case 32:
                        stringBuffer.append("&#32;");
                        break;
                    case 34:
                        stringBuffer.append("&quot;");
                        break;
                    case 38:
                        stringBuffer.append("&amp;");
                        break;
                    case 60:
                        stringBuffer.append("&lt;");
                        break;
                    case 62:
                        stringBuffer.append("&gt;");
                        break;
                    default:
                        stringBuffer.append(cChar);
                }
            }
        }
        return stringBuffer.toString();
    }

    public static String TranEncodeTOGB(String str){
        try {
            String strEncode = TranCharsetUtil.getEncoding(str);
            String temp = new String(str.getBytes(strEncode), "GBK");
            return temp;
        }catch (IOException ex){
            return null;
        }
    }

    public static boolean isGB2312(char c){
        Character character = Character.valueOf(c);
        String sCharacter = character.toString();
        try {
            byte[] bb = sCharacter.getBytes("gb2312");
            if (bb.length > 1){
                return true;
            }
        }catch (UnsupportedEncodingException ex){
            return false;
        }
        return false;
    }

    public static String getEncoding(String str){

        String encode = "GB2312";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))){
                String s = encode;
                return s;
            }
        }catch (Exception e){
        }

        encode = "ISO-8859-1";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))){
                String s1 = encode;
                return s1;
            }
        }catch (Exception e1){
        }

        encode = "UTF-8";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))){
                String s2 = encode;
                return s2;
            }
        }catch (Exception e2){
        }

        encode = "GBK";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))){
                String s3 = encode;
                return s3;
            }
        }catch (Exception e3){
        }
        return "";
    }
}
