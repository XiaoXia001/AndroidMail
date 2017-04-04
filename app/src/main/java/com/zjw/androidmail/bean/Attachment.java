package com.zjw.androidmail.bean;

import java.io.File;
import java.io.Serializable;

/**
 * Created by zhaojinwei on 2017/4/4.
 */

public class Attachment implements Serializable {

    private static final long serialVersionUID = 1L;

    private String filePath;
    private String fileName;
    private long fileSize;

    public Attachment(){
        super();
    }

    public Attachment(String filePath, String fileName, long fileSize){
        super();
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public static String convertStorage(long size){

        long kb = 1024;
        long mb = 1024 * kb;
        long gb = 1024 * mb;

        if (size >= gb){
            return String.format("%.1f GB", (float) size / gb);
        }else if (size >= mb){
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        }else if (size >= kb){
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        }else {
            return String.format("%d B", size);
        }
    }

    public static String getNameFromFilePath(String filePath){
        int pos = filePath.lastIndexOf("/");
        if (pos != -1){
            return filePath.substring(pos + 1);
        }
        return "";
    }

    public static Attachment getFileInfo(String filePath){
        File file = new File(filePath);
        if (!file.exists()){
            return null;
        }
        Attachment fileInfo = new Attachment();
        fileInfo.fileName = getNameFromFilePath(filePath);
        fileInfo.filePath = filePath;
        fileInfo.fileSize = file.length();
        return fileInfo;
    }

    public String getFilePath(){
        return filePath;
    }

    public void setFilePath(String filePath){
        this.filePath = filePath;
    }

    public String getFileName(){
        return fileName;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }

    public long getFileSize(){
        return fileSize;
    }

    public void setFileSize(long fileSize){
        this.fileSize = fileSize;
    }
}
