package pro.sketchware.xml.resources.editors;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayoutMediator;

import a.a.a.wq;

import pro.sketchware.databinding.ResourcesEditorsActivityBinding;
import pro.sketchware.xml.resources.editors.adapters.EditorsAdapter;
import pro.sketchware.xml.resources.editors.fragments.ColorEditor;
import pro.sketchware.xml.resources.editors.fragments.StringEditor;

public class ResourcesEditorsActivity extends AppCompatActivity {

    private ResourcesEditorsActivityBinding binding;
    public String sc_id;
    public String stringsFilePath;
    public String colorsFilePath;

    public final StringEditor stringEditor = new StringEditor();
    public final ColorEditor colorsEditor = new ColorEditor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ResourcesEditorsActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);

        sc_id = getIntent().getStringExtra("sc_id");
        String projectResourcesDirectory = wq.b(sc_id)+ "/files/resource/values/" ;
        stringsFilePath = projectResourcesDirectory + "strings.xml";
        colorsFilePath = projectResourcesDirectory + "colors.xml";

        setupViewPager();

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("strings.xml");
                    break;
                case 1:
                    tab.setText("colors.xml");
                    break;
                case 2:
                    tab.setText("styles.xml");
                    break;
                // TODO: add themes.xml
            }
        }).attach();

        binding.fab.setOnClickListener(view -> {
            int currentItem = binding.viewPager.getCurrentItem();
            if (currentItem == 0) {
                stringEditor.addStringDialog();
            } else if (currentItem == 1) {
                colorsEditor.showColorEditDialog(null, -1);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if ((stringEditor.checkForUnsavedChanges() && stringEditor.isInitialized)
                || (colorsEditor.checkForUnsavedChanges() && colorsEditor.isInitialized)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Warning")
                    .setMessage("You have unsaved changes. Are you sure you want to exit?")
                    .setPositiveButton("Exit", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
            return;
        }

        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onResume() {
        if (binding.viewPager.getCurrentItem() == 0 && stringEditor.isInitialized) {
            stringEditor.updateStringsList();
        } else if (binding.viewPager.getCurrentItem() == 1 && colorsEditor.isInitialized) {
            colorsEditor.updateColorsList();
        }
        super.onResume();
    }

    private void setupViewPager() {
        EditorsAdapter adapter = new EditorsAdapter(this);
        binding.viewPager.setAdapter(adapter);
    }

}
