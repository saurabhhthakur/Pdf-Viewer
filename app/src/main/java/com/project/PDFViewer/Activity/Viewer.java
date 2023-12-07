package com.project.PDFViewer.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.project.PDFViewer.R;
import com.project.PDFViewer.databinding.ActivityViewerBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class Viewer extends AppCompatActivity {
    ActivityViewerBinding binding;
    private String name, path;
    private int totalPages;
    private final int extraVerticalSpacing = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarViewer);
        View decor = Viewer.this.getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(getColor(R.color.white));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        name = getIntent().getStringExtra("name");
        path = getIntent().getStringExtra("path");
        getSupportActionBar().setTitle(name);

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            if (getIntent().getData() != null) {
                File file = new File(getIntent().getData().getPath());
                String a = file.getPath();
                String b;

                if (a.startsWith("/external_files/" + file.getName())) {
                    b = a.replace("/external_files", "/storage/emulated/0");
                    share(b);
                } else if (a.startsWith("/external_files")) {
                    b = a.replace("/external_files", "/storage/emulated/0");
                    share(b);
                } else {
                    share(file.getPath());
                }
                getSupportActionBar().setTitle(file.getName());
                viewPdfUri(getIntent().getData());
            } else {
                Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            share(path);
            viewPdf(path);
        }


    }


    public void viewPdf(String path) {
        binding.pdfView.fromFile(new File(path))
                .spacing(50)
                .defaultPage(0)
                .enableSwipe(true)
                .onLoad(nbPages -> totalPages = nbPages)
                .scrollHandle(new DefaultScrollHandle(this))
                .onDraw((canvas, pageWidth, pageHeight, displayedPage) -> {
                    if (displayedPage < totalPages - 1) {
                        Paint paint = new Paint();
                        paint.setColor(getResources().getColor(R.color.black));
                        canvas.drawRect(0, pageHeight, pageWidth, pageHeight + extraVerticalSpacing, paint);
                    }
                })
                .onPageChange((page, pageCount) -> setTitle(String.format("%s %s / %s", name, page + 1, pageCount)))
                .load();
    }

    public void viewPdfUri(Uri path3) {
        binding.pdfView.fromUri(path3)
                .spacing(50)
                .defaultPage(0)
                .enableSwipe(true)
                .onLoad(nbPages -> totalPages = nbPages)
                .scrollHandle(new DefaultScrollHandle(this))
                .onDraw((canvas, pageWidth, pageHeight, displayedPage) -> {
                    if (displayedPage < totalPages - 1) {
                        Paint paint = new Paint();
                        paint.setColor(getResources().getColor(R.color.black));
                        canvas.drawRect(0, pageHeight, pageWidth, pageHeight + extraVerticalSpacing, paint);
                    }
                })
                .onPageChange((page, pageCount) -> setTitle(String.format("%s %s / %s", name, page + 1, pageCount)))
                .load();
    }

    private void share(String path2) {
        binding.share.setOnClickListener(v -> {
            File pdfFile = new File(path2);
            printPDF(pdfFile);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void printPDF(File pdfFile) {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        String jobName = getString(R.string.app_name) + " Document";

        PrintDocumentAdapter printAdapter = new PrintDocumentAdapter() {
            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
                if (!cancellationSignal.isCanceled()) {
                    PrintDocumentInfo.Builder builder = new PrintDocumentInfo.Builder(getString(R.string.app_name) + " Document");
                    builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                            .build();
                    callback.onLayoutFinished(builder.build(), newAttributes != oldAttributes);
                } else {
                    callback.onLayoutFailed(null);
                }
            }

            @Override
            public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
                try {
                    InputStream inputStream = new FileInputStream(pdfFile);
                    OutputStream outputStream = new FileOutputStream(destination.getFileDescriptor());
                    byte[] buf = new byte[16384];
                    int size;
                    while ((size = inputStream.read(buf)) >= 0 && !cancellationSignal.isCanceled()) {
                        outputStream.write(buf, 0, size);
                    }
                    callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onWriteFailed(null);
                }
            }


        };

        printManager.print(jobName, printAdapter, null);
    }


}



