package pro.sketchware.styles;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.besome.sketch.editor.property.PropertyInputItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.gson.Gson;

import a.a.a.aB;
import mod.hey.studios.code.SrcCodeEditor;
import mod.hey.studios.code.SrcCodeEditorLegacy;
import mod.hey.studios.util.Helper;
import mod.hilal.saif.activities.tools.ConfigActivity;

import pro.sketchware.R;
import pro.sketchware.databinding.PropertyPopupInputTextBinding;
import pro.sketchware.databinding.PropertyPopupParentAttrBinding;
import pro.sketchware.databinding.StyleEditorAddAttrBinding;
import pro.sketchware.databinding.StyleEditorAddBinding;
import pro.sketchware.databinding.StylesActivityBinding;
import pro.sketchware.utility.FileUtil;
import pro.sketchware.utility.SketchwareUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StylesActivity extends AppCompatActivity {

    private StylesActivityBinding binding;
    private StylesAdapter adapter;
    private PropertyInputItem.AttributesAdapter attributesAdapter;
    private ArrayList<StyleModel> stylesList;
    private boolean isComingFromSrcCodeEditor = true;
    private StylesEditorManager stylesEditorManager;
    private final AttributeSuggestions attributeSuggestions = new AttributeSuggestions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = StylesActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.addNewStyle.setOnClickListener(view -> showAddStyleDialog());

        stylesEditorManager = new StylesEditorManager(this, stylesList);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy < 0) {
                    if (!binding.addNewStyle.isExtended()) {
                        binding.addNewStyle.extend();
                    }
                } else if (dy > 0) {
                    if (binding.addNewStyle.isExtended()) {
                        binding.addNewStyle.shrink();
                    }
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isComingFromSrcCodeEditor) {
            stylesList = new ArrayList<>();
            try {
                stylesList = stylesEditorManager.parseStylesFile(FileUtil.readFile(getIntent().getStringExtra("content")));
            } catch (Exception e) {
                SketchwareUtil.toastError(e.getMessage());
            }
            adapter = new StylesAdapter(stylesList, this);
            binding.recyclerView.setAdapter(adapter);
        }
        isComingFromSrcCodeEditor = false;
    }

    @Override
    public void onBackPressed() {
        Gson gson = new Gson();
        if (!gson.toJson(stylesList).equals(gson.toJson(stylesEditorManager.parseStylesFile(FileUtil.readFile(getIntent().getStringExtra("content")))))) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Warning")
                    .setMessage("You have unsaved changes. Are you sure you want to exit?")
                    .setPositiveButton("Exit", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        } else {
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.styles_editor_menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.filter(newText.toLowerCase());
                    return false;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }
            });
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            saveStylesFile();
        } else if (id == R.id.action_open_editor) {
            isComingFromSrcCodeEditor = true;
            saveStylesFile();
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), ConfigActivity.isLegacyCeEnabled() ? SrcCodeEditorLegacy.class : SrcCodeEditor.class);
            intent.putExtra("title", getIntent().getStringExtra("title"));
            intent.putExtra("content", getIntent().getStringExtra("content"));
            intent.putExtra("xml", getIntent().getStringExtra("xml"));
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void showAddStyleDialog() {
        aB dialog = new aB(this);
        StyleEditorAddBinding binding = StyleEditorAddBinding.inflate(getLayoutInflater());
        dialog.b("Create new string");
        dialog.b("Create", v1 -> {
            String styleName = Objects.requireNonNull(binding.styleName.getText()).toString();
            String parent = Objects.requireNonNull(binding.styleParent.getText()).toString();

            if (styleName.isEmpty()) {
                SketchwareUtil.toastError("Style name Input is Empty");
                return;
            }

            StyleModel style = new StyleModel(styleName, parent);
            stylesList.add(style);
            adapter.notifyItemInserted(stylesList.size() - 1);
        });
        dialog.a(getString(R.string.cancel), Helper.getDialogDismissListener(dialog));
        dialog.a(binding.getRoot());
        dialog.show();
    }

    public void showEditStyleDialog(int position) {
        StyleModel style = stylesList.get(position);
        aB dialog = new aB(this);
        StyleEditorAddBinding binding = StyleEditorAddBinding.inflate(getLayoutInflater());

        binding.styleName.setText(style.getStyleName());
        binding.styleParent.setText(style.getParent());

        dialog.b("Edit : " + style.getStyleName());
        dialog.b("Edit", v1 -> {
            String styleName = Objects.requireNonNull(binding.styleName.getText()).toString();
            String parent = Objects.requireNonNull(binding.styleParent.getText()).toString();

            if (styleName.isEmpty()) {
                SketchwareUtil.toastError("Style name Input is Empty");
                return;
            }

            style.setStyleName(styleName);
            style.setParent(parent);

            adapter.notifyItemChanged(position);
        });
        dialog.a(getString(R.string.cancel), Helper.getDialogDismissListener(dialog));
        dialog.a(binding.getRoot());
        dialog.show();
    }

    public void showStyleAttributesDialog(int position) {
        StyleModel style = stylesList.get(position);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        var binding = PropertyPopupParentAttrBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());
        dialog.show();

        binding.title.setText(style.getStyleName() + " attributes");

        attributesAdapter = new PropertyInputItem.AttributesAdapter();
        attributesAdapter.setOnItemClickListener(
                new PropertyInputItem.AttributesAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(Map<String, String> attributes, String attr) {
                        showAttributeDialog(style, attr);
                    }

                    @Override
                    public void onItemLongClick(Map<String, String> attributes, String attr) {
                        new MaterialAlertDialogBuilder(StylesActivity.this)
                                .setTitle("Warning")
                                .setMessage("Are you sure you want to delete " + attr + "?")
                                .setPositiveButton(R.string.common_word_yes, (d, w) -> {
                                    attributes.remove(attr);
                                    style.setAttributes(attributes);
                                    attributesAdapter.submitList(new ArrayList<>(attributes.keySet()));
                                })
                                .setNegativeButton("Cancel", null)
                                .create()
                                .show();
                    }
                });
        binding.recyclerView.setAdapter(attributesAdapter);
        var dividerItemDecoration =
                new DividerItemDecoration(
                        binding.recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        binding.recyclerView.addItemDecoration(dividerItemDecoration);
        var attributes = style.getAttributes();
        attributesAdapter.setAttributes(attributes);
        List<String> keys = new ArrayList<>(attributes.keySet());
        attributesAdapter.submitList(keys);

        binding.add.setOnClickListener(
                v -> showAttributeDialog(style, ""));
        binding.sourceCode.setVisibility(View.VISIBLE);
        binding.sourceCode.setOnClickListener(
                v -> showAttributesEditorDialog(style));
    }

    private void showAttributeDialog(StyleModel style, String attr) {
        boolean isEditing = !attr.isEmpty();

        aB dialog = new aB(this);
        StyleEditorAddAttrBinding binding = StyleEditorAddAttrBinding.inflate(getLayoutInflater());
        setupAutoComplete(binding.styleName, binding.styleParent);

        if (isEditing) {
            binding.styleName.setText(attr);
            binding.styleParent.setText(style.getAttribute(attr));
        }

        dialog.b(isEditing ? "Edit : " + style.getAttribute(attr) : "Create new attribute");

        dialog.b(Helper.getResString(R.string.common_word_save), v1 -> {
            String attribute = Objects.requireNonNull(binding.styleName.getText()).toString();
            String value = Objects.requireNonNull(binding.styleParent.getText()).toString();

            if (attribute.isEmpty() || value.isEmpty()) {
                SketchwareUtil.toastError("Please fill in all fields");
                return;
            }

            if (!attribute.equals(attr)) style.getAttributes().remove(attr);

            style.addAttribute(attribute, value);
            attributesAdapter.submitList(new ArrayList<>(style.getAttributes().keySet()));
            attributesAdapter.notifyDataSetChanged();
        });

        dialog.a(getString(R.string.cancel), Helper.getDialogDismissListener(dialog));
        dialog.a(binding.getRoot());
        dialog.show();
    }

    public void showAttributesEditorDialog(StyleModel style) {
        aB dialog = new aB(this);
        PropertyPopupInputTextBinding binding = PropertyPopupInputTextBinding.inflate(getLayoutInflater());

        binding.edInput.setText(stylesEditorManager.getAttributesCode(style));

        dialog.b("Edit all " + style.getStyleName() + " attributes");
        dialog.b(Helper.getResString(R.string.common_word_save), v1 -> {
            try {
                Map<String, String> attributes = stylesEditorManager.convertAttributesToMap(binding.edInput.getText().toString());
                style.setAttributes(attributes);
                attributesAdapter.submitList(new ArrayList<>(attributes.keySet()));
            } catch (Exception e) {
                SketchwareUtil.toastError("Failed to parse attributes. Please check the format");
            }
        });
        dialog.a(getString(R.string.cancel), Helper.getDialogDismissListener(dialog));
        dialog.a(binding.getRoot());
        dialog.show();
    }

    private void saveStylesFile() {
        FileUtil.writeFile(getIntent().getStringExtra("content"), stylesEditorManager.convertStylesToXML());
        SketchwareUtil.toast(Helper.getResString(R.string.common_word_saved));
    }

    private void setupAutoComplete(MaterialAutoCompleteTextView attrView, MaterialAutoCompleteTextView valueView) {
        String[] attributes = attributeSuggestions.ATTRIBUTE_TYPES.keySet().toArray(new String[0]);
        List<String> initialValues = attributeSuggestions.getSuggestions(AttributeSuggestions.SuggestionType.DEFAULT);

        ArrayAdapter<String> attrAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, attributes);
        attrView.setAdapter(attrAdapter);

        ArrayAdapter<String> valueAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, initialValues);
        valueView.setAdapter(valueAdapter);

        attrView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String attribute = s.toString().trim().toLowerCase();

                List<String> suggestions = attributeSuggestions.getSuggestions(attribute);

                if (suggestions != null && !suggestions.isEmpty()) {
                    ArrayAdapter<String> newValueAdapter = new ArrayAdapter<>(StylesActivity.this, android.R.layout.simple_dropdown_item_1line, suggestions);
                    valueView.setAdapter(newValueAdapter);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

}
