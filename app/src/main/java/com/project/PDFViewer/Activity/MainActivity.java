package com.project.PDFViewer.Activity;

import static com.itextpdf.text.factories.RomanAlphabetFactory.getString;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.project.PDFViewer.Adapter.PdfAdapter;
import com.project.PDFViewer.Model.PdfFile;
import com.project.PDFViewer.R;
import com.project.PDFViewer.ViewModel.PdfViewModel;
import com.project.PDFViewer.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private PdfAdapter pdfAdapter;
    List<PdfFile> pdfFil = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        View decor = MainActivity.this.getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(getColor(R.color.white));
        setSupportActionBar(binding.toolbar);


    }

    @Override
    protected void onStart() {
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        pdfAdapter = new PdfAdapter(new ArrayList<>());
                        binding.rvFiles.setAdapter(pdfAdapter);

                        PdfViewModel pdfViewModel = new ViewModelProvider(MainActivity.this).get(PdfViewModel.class);
                        pdfViewModel.getPdfFiles().observe(MainActivity.this, pdfFiles -> {
                            pdfFil = pdfFiles;
                            pdfAdapter.setData(pdfFiles);
                        });
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();
        super.onStart();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();


        searchView.setQueryHint("Search Here...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            public boolean onQueryTextChange(String newText) {
                if (newText != null) {
                    pdfAdapter.filter(newText);
                } else {
                    Toast.makeText(MainActivity.this, "No File Found", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.search) {
            return true;
        } else if (i == R.id.sort) {
            showPopupMenu(findViewById(R.id.sort));
            return true;
        } else if (i == R.id.settings) {
            Toast.makeText(this, "We are work for it...", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }


    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.sort_file, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.sortA_to_Z) {
                pdfAdapter.sortByNameAscending();
                return true;
            }else if (id == R.id.sortZ_to_A) {
                pdfAdapter.sortByNameDescending();
                return true;
            } else if (id == R.id.sort1_to_10) {
                pdfAdapter.sortByDateAscending();
                return true;
            } else if (id == R.id.sort10_to_1){
                pdfAdapter.sortByDateDescending();
                return true;
        }
            else
                return false;
        });
        popupMenu.show();
    }







}