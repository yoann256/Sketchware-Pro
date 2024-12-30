package pro.sketchware.xml.resources.editors;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayoutMediator;

import a.a.a.wq;

import pro.sketchware.R;
import pro.sketchware.databinding.ResourcesEditorsActivityBinding;
import pro.sketchware.utility.XmlUtil;
import pro.sketchware.xml.resources.editors.adapters.EditorsAdapter;
import pro.sketchware.xml.resources.editors.fragments.ColorEditor;
import pro.sketchware.xml.resources.editors.fragments.StringEditor;
import pro.sketchware.xml.resources.editors.fragments.StylesEditor;

public class ResourcesEditorsActivity extends AppCompatActivity {

    private ResourcesEditorsActivityBinding binding;
    public String sc_id;
    public String stringsFilePath;
    public String colorsFilePath;
    public String stylesFilePath;

    public final StringEditor stringEditor = new StringEditor();
    public final ColorEditor colorsEditor = new ColorEditor();
    public final StylesEditor stylesEditor = new StylesEditor();

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
        stylesFilePath = projectResourcesDirectory + "styles.xml";

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
            } else if (currentItem == 2) {
                stylesEditor.showAddStyleDialog();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (stringEditor.isInitialized && stringEditor.checkForUnsavedChanges()
                || colorsEditor.isInitialized && colorsEditor.checkForUnsavedChanges()
                || stylesEditor.isInitialized && stylesEditor.checkForUnsavedChanges()
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
        if (binding.viewPager.getCurrentItem() == 0 && stringEditor.isInitialized) {
            stringEditor.updateStringsList();
        } else if (binding.viewPager.getCurrentItem() == 1 && colorsEditor.isInitialized) {
            colorsEditor.updateColorsList();
        } else if (binding.viewPager.getCurrentItem() == 2 && stylesEditor.isInitialized) {
            stylesEditor.updateStylesList();
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
                        stringEditor.adapter.filter(newText);
                    } else if (currentItem == 1) {
                        colorsEditor.adapter.filter(newText);
                    } else if (currentItem == 2) {
                    stylesEditor.adapter.filter(newText);
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
                    !stringEditor.checkDefaultString(stringsFilePath)
                    && binding.viewPager.getCurrentItem() != 0
            );
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            XmlUtil.saveXml(stringsFilePath, StringEditor.convertListMapToXml(stringEditor.listmap));
            XmlUtil.saveXml(colorsFilePath, ColorEditor.convertListToXml(colorsEditor.colorList));
            stylesEditor.saveStylesFile();
        } else if (id == R.id.action_search) {
            int currentItem = binding.viewPager.getCurrentItem();
            if (currentItem == 0) {
                stringEditor.handleOnOptionsItemSelected(id);
            } else if (currentItem == 1) {
                colorsEditor.handleOnOptionsItemSelected();
            } else if (currentItem == 2) {
                stylesEditor.handleOnOptionsItemSelected();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager() {
        EditorsAdapter adapter = new EditorsAdapter(this);
        binding.viewPager.setAdapter(adapter);
    }

}
