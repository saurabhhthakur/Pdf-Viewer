package com.project.PDFViewer.ViewModel;

import android.os.Environment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.project.PDFViewer.Model.PdfFile;

public class PdfViewModel extends ViewModel {
    private final MutableLiveData<List<PdfFile>> pdfFilesLiveData = new MutableLiveData<>();
    public LiveData<List<PdfFile>> getPdfFiles() {
        if (pdfFilesLiveData.getValue() == null) {
            loadPdfFiles();
        }
        return pdfFilesLiveData;
    }

    private void loadPdfFiles() {
        List<PdfFile> pdfFiles = new ArrayList<>();
        String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        fetchPdfFilesFromDirectory(new File(externalStoragePath), pdfFiles);

        pdfFilesLiveData.setValue(pdfFiles);
    }

    private void fetchPdfFilesFromDirectory(File directory, List<PdfFile> pdfFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    fetchPdfFilesFromDirectory(file, pdfFiles);
                } else if (file.isFile() && file.getName().endsWith(".pdf")) {
                    pdfFiles.add(new PdfFile(file.getName(), file.getAbsolutePath(), file.length(), file.lastModified()));
                }
            }
        }
    }



    public void renamePdfFile(PdfFile pdfFileModel, String newName) {
        File pdfFile = new File(pdfFileModel.getFilePath());
        if (pdfFile.exists()) {
            File newFile = new File(pdfFile.getParent(), newName);
            if (pdfFile.renameTo(newFile)) {
                pdfFileModel.setFileName(newName);
                pdfFileModel.setFilePath(newFile.getAbsolutePath());
                List<PdfFile> pdfFiles = pdfFilesLiveData.getValue();
                if (pdfFiles != null) {
                    int index = pdfFiles.indexOf(pdfFileModel);
                    if (index != -1) {
                        pdfFiles.set(index, pdfFileModel);
                        pdfFilesLiveData.setValue(pdfFiles);
                    }
                }
            }
        }
    }

    public void deletePdfFile(PdfFile pdfFileModel) {
        File pdfFile = new File(pdfFileModel.getFilePath());
        if (pdfFile.exists() && pdfFile.delete()) {
            List<PdfFile> pdfFiles = pdfFilesLiveData.getValue();
            if (pdfFiles != null) {
                pdfFiles.remove(pdfFileModel);
                pdfFilesLiveData.setValue(pdfFiles);
            }
        }
    }


}
