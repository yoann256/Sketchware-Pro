package mod.yoann256.inappupdate;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;

import mod.yoann256.github.GithubHelper;
import pro.sketchware.R;
import pro.sketchware.databinding.UpdateDialogBinding;

import android.view.View;
import android.view.LayoutInflater;

import android.app.Dialog;

public class InAppUpdateDialog extends DialogFragment {
    GithubHelper gh = new GithubHelper();
    private UpdateDialogBinding binding;

    private String getLastCommit() {
        return gh.getLastCommit();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = UpdateDialogBinding.inflate(getLayoutInflater());

        // Optional: Customize the view using binding
        binding.msg.setText("A new version has been release from commit " + getLastCommit() + "!");

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Update Available")
                .setView(binding.getRoot())
                .setPositiveButton("Update", (dialog, which) -> {

                })
                .setNegativeButton("Cancel", null)
                .create();
    }

}
