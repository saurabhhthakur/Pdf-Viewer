package com.project.PDFViewer.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ShareCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.project.PDFViewer.Model.PdfFile;
import com.project.PDFViewer.R;
import com.project.PDFViewer.ViewModel.PdfViewModel;
import com.project.PDFViewer.Activity.Viewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.PdfViewHolder> {
    private final List<PdfFile> pdfFiles;
    private Context context;
    private List<PdfFile> pdfFilesFull;

    public PdfAdapter(List<PdfFile> pdfFiles) {
        this.pdfFiles = pdfFiles;
    }

    public void setData(List<PdfFile> newData) {
        pdfFiles.clear();
        pdfFiles.addAll(newData);
        this.pdfFilesFull = new ArrayList<>(pdfFiles);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        pdfFiles.clear();
        if (TextUtils.isEmpty(query)) {
            pdfFiles.addAll(pdfFilesFull);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            String replaceWith = "<span style = 'background-color:yellow'>" + lowerCaseQuery + "</span>";
            String fileName;
            for (PdfFile pdfFile : pdfFilesFull) {
                fileName = pdfFile.getFileName().replaceAll(lowerCaseQuery, replaceWith);
                if (fileName.toLowerCase().contains(lowerCaseQuery)) {
                    pdfFiles.add(pdfFile);
                }
            }
        }
        notifyDataSetChanged();
    }


    @NonNull
    public PdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pdf_row, parent, false);
        return new PdfViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder holder, @SuppressLint("RecyclerView") int position) {
        PdfFile pdfFile = pdfFiles.get(position);
        context = holder.itemView.getContext();
        holder.bind(pdfFile, position);
    }

    @Override
    public int getItemCount() {
        return pdfFiles.size();
    }

    class PdfViewHolder extends RecyclerView.ViewHolder {
        private final TextView fileNameTextView;
        private final TextView textView;
        private final ImageView imageView;

        public PdfViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTextView = itemView.findViewById(R.id.file_name);
            textView = itemView.findViewById(R.id.file_path);
            imageView = itemView.findViewById(R.id.share);
        }

        public void bind(PdfFile pdfFile, int position) {
            fileNameTextView.setText(pdfFile.getFileName());
            textView.setText(pdfFile.getFileSize());
            int position2 = getAdapterPosition();
            PdfViewModel pdfViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(PdfViewModel.class);

            itemView.setOnLongClickListener(v -> {
                PopupMenu popup = new PopupMenu(itemView.getContext(), v);
                popup.getMenuInflater().inflate(R.menu.menu, popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(item -> {
                    int i = item.getItemId();
                    if (i == R.id.delete) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Delete File");
                        builder.setIcon(R.drawable.warning);
                        builder.setMessage("Are you sure you want to delete this PDF file?");
                        builder.setPositiveButton("Delete", (dialog, which) -> {
                            if (position2 != RecyclerView.NO_POSITION) {
                                PdfFile pdfFileModel = pdfFiles.get(position);
                                pdfViewModel.deletePdfFile(pdfFileModel);
                            }

                        });
                        builder.setNegativeButton("Cancel", null);
                        builder.show();
                        return true;
                    }
                    else if (i == R.id.rename) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Rename PDF");
                        builder.setIcon(R.drawable.rename);
                        final EditText inputNewName = new EditText(context);
                        inputNewName.setLayoutParams(new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        builder.setView(inputNewName);
                        inputNewName.setText(pdfFile.getFileName());
                        inputNewName.setPadding(30, 75, 30, 30);
                        builder.setPositiveButton("Rename", (dialog, which) -> {
                            String newName = inputNewName.getText().toString().trim();
                            if (newName.endsWith(".pdf")) {
                                if (!newName.isEmpty()) {
                                    if (position2 != RecyclerView.NO_POSITION) {
                                        PdfFile pdfFileModel = pdfFiles.get(position);
                                        pdfViewModel.renamePdfFile(pdfFileModel, newName);
                                    }
                                }
                            } else {
                                if (!newName.isEmpty()) {
                                    if (position2 != RecyclerView.NO_POSITION) {
                                        PdfFile pdfFileModel = pdfFiles.get(position);
                                        pdfViewModel.renamePdfFile(pdfFileModel, newName + ".pdf");
                                    }
                                }
                            }
                        });

                        builder.setNegativeButton("Cancel", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return true;
                    }
                    else if (i == R.id.detail) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("File Details");
                        builder.setIcon(R.drawable.info);
                        builder.setMessage("\nFile Name : " + pdfFile.getFileName() + "\n\nLocation : " + pdfFile.getFilePath()
                                + "\n\nSize : " + pdfFile.getFileSize() + "\n\nLast modified : " + pdfFile.getModifiedTimeDate() + "\n\n");
                        builder.setPositiveButton("Ok", (dialog, which) -> {
                            dialog.dismiss();

                        });
                        builder.show();

                        return true;
                    } else {
                        Intent intent = ShareCompat.IntentBuilder.from((Activity) itemView.getContext()).setType("application/pdf")
                                .setStream(Uri.parse(pdfFile.getFilePath())).
                                setChooserTitle("Choose app")
                                .createChooserIntent()
                                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        itemView.getContext().startActivity(intent);
                        return true;
                    }

                });

                return true;
            });

            itemView.setOnClickListener(v -> openPdfFile(itemView.getContext(), pdfFile));
        }

        private void openPdfFile(Context context, PdfFile pdfFile) {
            Intent intent = new Intent(context, Viewer.class);
            intent.putExtra("name", pdfFile.getFileName());
            intent.putExtra("path", pdfFile.getFilePath());
            context.startActivity(intent);
        }


    }


    public void sortByDateAscending() {
        Collections.sort(pdfFiles, (pdfFile1, pdfFile2) -> pdfFile1.getDate().compareTo(pdfFile2.getDate()));
        notifyDataSetChanged();
    }

    public void sortByDateDescending() {
        Collections.sort(pdfFiles, (pdfFile1, pdfFile2) -> pdfFile2.getDate().compareTo(pdfFile1.getDate()));
        notifyDataSetChanged();
    }


    public void sortByNameAscending() {
        Collections.sort(pdfFiles, (pdfFile1, pdfFile2) -> pdfFile1.getFileName().compareToIgnoreCase(pdfFile2.getFileName()));
        notifyDataSetChanged();
    }

    public void sortByNameDescending() {
        Collections.sort(pdfFiles, (pdfFile1, pdfFile2) -> pdfFile2.getFileName().compareToIgnoreCase(pdfFile1.getFileName()));
        notifyDataSetChanged();
    }


}


