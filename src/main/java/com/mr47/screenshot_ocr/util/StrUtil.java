package com.mr47.screenshot_ocr.util;


import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class StrUtil {

    @NotNull
    public static String getFileExtension(String fileName){
        if(fileName.indexOf('.') != -1){
           int index = fileName.lastIndexOf('.');
           if(index != 0){
               return fileName.substring(index);
           }
        }
        return "";
    }

    @NotNull
    @org.jetbrains.annotations.Contract("null -> fail")
    public static String getJsonFromFile(File file) throws IOException {
        if((file == null) || (!file.isFile())){
            throw new IOException("传入了null或者传入了非文件");
        }

        //获取文件内的json字符串
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        StringBuffer buffer = new StringBuffer();
        String str = br.readLine();
        while(str != null){
            buffer.append(str);
            buffer.append("\n");
            str = br.readLine();
        }
        //删除最后一个换行符
        buffer.deleteCharAt(buffer.length() - 1);

        return buffer.toString();
    }

    @NotNull
    public static List<String> outOfLimit(String str, int lineCharNumber){
        List<String> stringList = new ArrayList<>();
        String[] noBreakLineStrs = str.split("\n");
        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < noBreakLineStrs.length; i++){
            while(noBreakLineStrs[i].length() > lineCharNumber){
                builder.append(noBreakLineStrs[i], 0, lineCharNumber);
                stringList.add(builder.toString());
                builder.delete(0, builder.length());
                noBreakLineStrs[i] = noBreakLineStrs[i].substring(lineCharNumber);
            }
            builder.append(noBreakLineStrs[i]);
            if(i < (noBreakLineStrs.length -1)) builder.append("\n");
        }
        stringList.add(builder.toString());

        return stringList;
    }

    public static String generateShortUuid(){
        String[] chars = new String[] { "a", "b", "c", "d", "e", "f","g", "h", "i", "j", "k", "l", "m",
                "n", "o", "p", "q", "r", "s", "t","u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4",
                "5", "6","7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I","J", "K", "L", "M",
                "N", "O", "P", "Q", "R", "S", "T", "U", "V","W", "X", "Y", "Z" };

        StringBuilder shortBuilder = new StringBuilder();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 8; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuilder.append(chars[x % 0x3E]);
        }
        return shortBuilder.toString();
    }

    public static String sha1(String s) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        digest.update(s.getBytes());

        StringBuilder builder = new StringBuilder();
        byte[] bits = digest.digest();
        for (int bit : bits) {
            if (bit < 0) bit += 256;
            if (bit < 16) builder.append("0");
            builder.append(Integer.toHexString(bit));
        }

        return builder.toString();
    }

    public static boolean strIsAllNumber(String s){
        for(int i = 0; i < s.length(); i++){
            if(!Character.isDigit(s.charAt(i)))
                return false;
        }
        return true;
    }

    public static boolean strIsNullOrEmpty(String s){
        return (s == null) || (s.isEmpty());
    }
}
