package com.besome.sketch.editor.manage.library.material3;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import pro.sketchware.databinding.ManageLibraryMaterial3Binding;

public class Material3LibraryActivity extends AppCompatActivity {

    private ManageLibraryMaterial3Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ManageLibraryMaterial3Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

}
