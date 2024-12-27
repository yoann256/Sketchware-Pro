package pro.sketchware.styles;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.besome.sketch.editor.property.PropertyInputItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StylesActivity extends AppCompatActivity {

    private StylesActivityBinding binding;
    private StylesAdapter adapter;
    private PropertyInputItem.AttributesAdapter attributesAdapter;
    private ArrayList<StyleModel> stylesList;
    private boolean isComingFromSrcCodeEditor = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = StylesActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.addNewStyle.setOnClickListener(view -> showAddStyleDialog());

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
                stylesList = parseStylesFile(FileUtil.readFile(getIntent().getStringExtra("content")));
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
        if (!gson.toJson(stylesList).equals(gson.toJson(parseStylesFile(FileUtil.readFile(getIntent().getStringExtra("content")))))) {
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
                        showAttributeDialog(position, attr);
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
                v -> showAttributeDialog(position, ""));
        binding.sourceCode.setVisibility(View.VISIBLE);
        binding.sourceCode.setOnClickListener(
                v -> showAttributesEditorDialog(position));
    }

    private void showAttributeDialog(int position, String attr) {
        boolean isEditing = !attr.isEmpty();
        StyleModel style = stylesList.get(position);

        aB dialog = new aB(this);
        StyleEditorAddAttrBinding binding = StyleEditorAddAttrBinding.inflate(getLayoutInflater());

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

    public void showAttributesEditorDialog(int position) {
        StyleModel style = stylesList.get(position);
        aB dialog = new aB(this);
        PropertyPopupInputTextBinding binding = PropertyPopupInputTextBinding.inflate(getLayoutInflater());

        binding.edInput.setText(getAttributesCode(style));

        dialog.b("Edit all " + style.getStyleName() + " attributes");
        dialog.b(Helper.getResString(R.string.common_word_save), v1 -> {
            try {
                Map<String, String> attributes = convertAttributesToMap(binding.edInput.getText().toString());
                style.setAttributes(attributes);
                style.setAttributes(convertAttributesToMap(binding.edInput.getText().toString()));
                attributesAdapter.submitList(new ArrayList<>(attributes.keySet()));
            } catch (Exception e) {
                SketchwareUtil.toastError("Failed to parse attributes. Please check the format");
            }
        });
        dialog.a(getString(R.string.cancel), Helper.getDialogDismissListener(dialog));
        dialog.a(binding.getRoot());
        dialog.show();
    }

    private String getAttributesCode(StyleModel style) {
        StringBuilder attributesCode = new StringBuilder();

        for (Map.Entry<String, String> entry : style.getAttributes().entrySet()) {
            attributesCode
                    .append("<item name=\"")
                    .append(entry.getKey())
                    .append("\">")
                    .append(entry.getValue())
                    .append("</item>\n");
        }

        return attributesCode.toString().trim();
    }

    private Map<String, String> convertAttributesToMap(String attributesCode) {
        Map<String, String> attributesMap = new HashMap<>();


        String[] lines = attributesCode.split("\n");
        for (String line : lines) {
            if (line.startsWith("<item name=\"") && line.endsWith("</item>")) {
                int nameStart = line.indexOf("\"") + 1;
                int nameEnd = line.indexOf("\"", nameStart);
                String name = line.substring(nameStart, nameEnd);

                int valueStart = line.indexOf(">", nameEnd) + 1;
                int valueEnd = line.lastIndexOf("</item>");
                String value = line.substring(valueStart, valueEnd).trim();

                attributesMap.put(name, value);
            }
        }

        return attributesMap;
    }


    private void saveStylesFile() {
        FileUtil.writeFile(getIntent().getStringExtra("content"), convertStylesToXML());
        SketchwareUtil.toast(Helper.getResString(R.string.common_word_saved));
    }

    private String convertStylesToXML() {
        StringBuilder xmlContent = new StringBuilder();

        xmlContent.append("<resources>\n");

        for (StyleModel style : stylesList) {
            xmlContent.append("    <style name=\"").append(style.getStyleName()).append("\"");

            if (style.getParent() != null && !style.getParent().isEmpty()) {
                xmlContent.append(" parent=\"").append(style.getParent()).append("\"");
            }

            xmlContent.append(">\n");

            Map<String, String> attributes = style.getAttributes();
            for (String attrName : attributes.keySet()) {
                String attrValue = attributes.get(attrName);
                xmlContent.append("        <item name=\"").append(attrName).append("\">").append(attrValue).append("</item>\n");
            }

            xmlContent.append("    </style>\n\n");
        }

        xmlContent.append("</resources>");

        return xmlContent.toString();
    }

    private ArrayList<StyleModel> parseStylesFile(String content) {
        ArrayList<StyleModel> styles = new ArrayList<>();
        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(content));
            Document document = builder.parse(inputSource);
            document.getDocumentElement().normalize();

            NodeList nodeList = document.getElementsByTagName("style");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    String styleName = element.getAttribute("name");

                    String parent = element.hasAttribute("parent") ? element.getAttribute("parent") : null;

                    HashMap<String, String> attributes = new HashMap<>();
                    NodeList childNodes = element.getChildNodes();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        Node childNode = childNodes.item(j);
                        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element childElement = (Element) childNode;
                            String attrName = childElement.getAttribute("name");
                            String attrValue = childElement.getTextContent().trim();
                            attributes.put(attrName, attrValue);
                        }
                    }

                    styles.add(new StyleModel(styleName, parent, attributes));
                }
            }

        } catch (Exception ignored) {}

        return styles;
    }

}
