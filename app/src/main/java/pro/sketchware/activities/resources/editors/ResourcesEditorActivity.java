package pro.sketchware.activities.resources.editors;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayoutMediator;

import a.a.a.wq;

import pro.sketchware.R;
import pro.sketchware.databinding.ResourcesEditorsActivityBinding;
import pro.sketchware.activities.resources.editors.adapters.EditorsAdapter;
import pro.sketchware.activities.resources.editors.fragments.ColorsEditor;
import pro.sketchware.activities.resources.editors.fragments.StringsEditor;
import pro.sketchware.activities.resources.editors.fragments.StylesEditor;
import pro.sketchware.activities.resources.editors.fragments.ThemesEditor;

public class ResourcesEditorActivity extends AppCompatActivity {

    private ResourcesEditorsActivityBinding binding;

    public String sc_id;
    public String variant;
    public String stringsFilePath;
    public String colorsFilePath;
    public String stylesFilePath;
    public String themesFilePath;

    public boolean isDefaultVariant;

    public final StringsEditor stringsEditor = new StringsEditor();
    public final ColorsEditor colorsEditor = new ColorsEditor();
    public final StylesEditor stylesEditor = new StylesEditor();
    public final ThemesEditor themesEditor = new ThemesEditor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ResourcesEditorsActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);

        sc_id = getIntent().getStringExtra("sc_id");
        variant = getIntent().getStringExtra("variant");

        String projectResourcesDirectory =
                String.format(
                        "%s/files/resource/values%s/",
                        wq.b(sc_id),
                        variant
                );

        stringsFilePath = projectResourcesDirectory + "strings.xml";
        colorsFilePath = projectResourcesDirectory + "colors.xml";
        stylesFilePath = projectResourcesDirectory + "styles.xml";
        themesFilePath = projectResourcesDirectory + "themes.xml";

        setupViewPager();

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("strings" + variant + ".xml");
                    break;
                case 1:
                    tab.setText("colors" + variant + ".xml");
                    break;
                case 2:
                    tab.setText("styles" + variant + ".xml");
                    break;
                case 3:
                    tab.setText("themes" + variant + ".xml");
                    break;
            }
        }).attach();

        binding.fab.setOnClickListener(view -> {
            int currentItem = binding.viewPager.getCurrentItem();
            if (currentItem == 0) {
                stringsEditor.addStringDialog();
            } else if (currentItem == 1) {
                colorsEditor.showColorEditDialog(null, -1);
            } else if (currentItem == 2) {
                stylesEditor.showAddStyleDialog();
            } else if (currentItem == 3) {
                themesEditor.showAddThemeDialog();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (stringsEditor.isInitialized && stringsEditor.checkForUnsavedChanges()
                || colorsEditor.isInitialized && colorsEditor.checkForUnsavedChanges()
                || stylesEditor.isInitialized && stylesEditor.checkForUnsavedChanges()
                || themesEditor.isInitialized && themesEditor.checkForUnsavedChanges()
        ) {
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
        if (binding.viewPager.getCurrentItem() == 0 && stringsEditor.isInitialized) {
            stringsEditor.updateStringsList();
        } else if (binding.viewPager.getCurrentItem() == 1 && colorsEditor.isInitialized) {
            colorsEditor.updateColorsList();
        } else if (binding.viewPager.getCurrentItem() == 2 && stylesEditor.isInitialized) {
            stylesEditor.updateStylesList();
        } else if (binding.viewPager.getCurrentItem() == 3 && themesEditor.isInitialized) {
            themesEditor.updateThemesList();
        }
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.resources_editor_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    newText = newText.toLowerCase().trim();
                    int currentItem = binding.viewPager.getCurrentItem();
                    if (currentItem == 0) {
                        stringsEditor.adapter.filter(newText);
                    } else if (currentItem == 1) {
                        colorsEditor.adapter.filter(newText);
                    } else if (currentItem == 2) {
                        stylesEditor.adapter.filter(newText);
                    } else if (currentItem == 3) {
                        themesEditor.adapter.filter(newText);
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }
            });
        }
        MenuItem getDefaultItem = menu.findItem(R.id.action_get_default);
        if (getDefaultItem != null) {
            getDefaultItem.setVisible(
                    !stringsEditor.checkDefaultString(stringsFilePath)
                    && binding.viewPager.getCurrentItem() != 0
            );
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            stringsEditor.saveStringsFile();
            colorsEditor.saveColorsFile();
            stylesEditor.saveStylesFile();
            themesEditor.saveThemesFile();
        } else if (id != R.id.action_search) {
            int currentItem = binding.viewPager.getCurrentItem();
            if (currentItem == 0) {
                stringsEditor.handleOnOptionsItemSelected(id);
            } else if (currentItem == 1) {
                colorsEditor.handleOnOptionsItemSelected();
            } else if (currentItem == 2) {
                stylesEditor.handleOnOptionsItemSelected();
            } else if (currentItem == 3) {
                themesEditor.handleOnOptionsItemSelected();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager() {
        EditorsAdapter adapter = new EditorsAdapter(this);
        binding.viewPager.setAdapter(adapter);
    }

}
