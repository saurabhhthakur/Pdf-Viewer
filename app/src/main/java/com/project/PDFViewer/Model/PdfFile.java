package com.project.PDFViewer.Model;


import androidx.annotation.Keep;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Keep
public class PdfFile {
    private String fileName;
    private String filePath;
    private long fileSize;
    private String Time;
    private long lastModified;

    public PdfFile(String fileName, String filePath, long fileSize,long lastModified) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.lastModified = lastModified;

    }

    public String getFileSize() {
        DecimalFormat df = new DecimalFormat("0.00");
        float sizeKb = 1024.0f;
        float sizeMb = sizeKb * sizeKb;
        float sizeGb = sizeMb * sizeKb;
        float sizeTerra = sizeGb * sizeKb;

        if (fileSize < sizeKb) {
            return df.format(fileSize) + " B";
        } else if (fileSize < sizeMb)
            return df.format(fileSize / sizeKb) + " KB";
        else if (fileSize < sizeGb)
            return df.format(fileSize / sizeMb) + " MB";
        else if (fileSize < sizeTerra)
            return df.format(fileSize / sizeGb) + " GB";

        return "0B";
    }

    public String getModifiedTimeDate() {
        Date date = new Date(lastModified);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        Time = formatter.format(date);

        return Time;
    }

    public String getDate(){
        Date date = new Date(lastModified);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }
}


