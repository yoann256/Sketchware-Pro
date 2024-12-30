package pro.sketchware.xml.resources.editors.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Xml;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Objects;

import a.a.a.XB;
import a.a.a.Zx;
import a.a.a.aB;
import a.a.a.xB;
import mod.hey.studios.code.SrcCodeEditor;
import mod.hey.studios.code.SrcCodeEditorLegacy;
import mod.hilal.saif.activities.tools.ConfigActivity;
import pro.sketchware.R;
import pro.sketchware.SketchApplication;
import pro.sketchware.databinding.ColorEditorAddBinding;
import pro.sketchware.databinding.ResourcesEditorFragmentBinding;
import pro.sketchware.xml.resources.editors.adapters.ColorsAdapter;
import pro.sketchware.xml.resources.editors.models.ColorItem;
import pro.sketchware.utility.PropertiesUtil;
import pro.sketchware.utility.FileUtil;
import pro.sketchware.utility.SketchwareUtil;
import pro.sketchware.utility.XmlUtil;
import pro.sketchware.xml.resources.editors.ResourcesEditorActivity;

public class ColorEditor extends Fragment {

    public static String contentPath;
    private final ArrayList<ColorItem> colorList = new ArrayList<>();
    private boolean isGoingToEditor;
    public boolean isInitialized = false;
    private ResourcesEditorFragmentBinding binding;
    public ColorsAdapter adapter;
    private Activity activity;
    private Zx colorpicker;

    public static String getColorValue(Context context, String colorValue, int referencingLimit) {
        if (colorValue == null || referencingLimit <= 0) {
            return null;
        }

        if (colorValue.startsWith("#")) {
            return colorValue;
        }
        if (colorValue.startsWith("?attr/")) {
            return getColorValueFromXml(context, colorValue.substring(6), referencingLimit - 1);
        }
        if (colorValue.startsWith("@color/")) {
            return getColorValueFromXml(context, colorValue.substring(7), referencingLimit - 1);

        } else if (colorValue.startsWith("@android:color/")) {
            return getColorValueFromSystem(colorValue, context);
        }
        return "#ffffff";
    }

    public static String getColorValueFromSystem(String colorValue, Context context) {
        String colorName = colorValue.substring(15);
        int colorId = context.getResources().getIdentifier(colorName, "color", "android");
        try {
            int colorInt = ContextCompat.getColor(context, colorId);
            return String.format("#%06X", (0xFFFFFF & colorInt));
        } catch (Exception e) {
            return "#ffffff";
        }
    }

    private static String getColorValueFromXml(Context context, String colorName, int referencingLimit) {
        try {
            String clrXml = FileUtil.readFileIfExist(contentPath);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(clrXml));
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "color".equals(parser.getName())) {
                    String nameAttribute = parser.getAttributeValue(null, "name");
                    if (colorName.equals(nameAttribute)) {
                        String colorValue = parser.nextText().trim();
                        if (colorValue.startsWith("@")) {
                            return getColorValue(context, colorValue, referencingLimit - 1);
                        } else {
                            return colorValue;
                        }
                    }
                }
                eventType = parser.next();
            }

        } catch (Exception ignored) {
        }
        return null;
    }

    public static String convertListToXml(ArrayList<ColorItem> colorList) {
        try {
            XmlSerializer xmlSerializer = Xml.newSerializer();
            StringWriter stringWriter = new StringWriter();

            xmlSerializer.setOutput(stringWriter);
            xmlSerializer.startDocument("UTF-8", true);
            xmlSerializer.text("\n");
            xmlSerializer.startTag(null, "resources");
            xmlSerializer.text("\n");

            for (ColorItem colorItem : colorList) {
                xmlSerializer.startTag(null, "color");
                xmlSerializer.attribute(null, "name", colorItem.getColorName());
                xmlSerializer.text(colorItem.getColorValue());
                xmlSerializer.endTag(null, "color");
                xmlSerializer.text("\n");
            }

            xmlSerializer.endTag(null, "resources");
            xmlSerializer.text("\n");
            xmlSerializer.endDocument();

            return stringWriter.toString();

        } catch (Exception ignored) {
        }
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ResourcesEditorFragmentBinding.inflate(inflater, container, false);
        initialize();
        updateColorsList();
        return binding.getRoot();
    }

    public void updateColorsList() {
        if (isGoingToEditor) {
            parseColorsXML(colorList, FileUtil.readFile(contentPath));
            adapter.notifyDataSetChanged();
        }
        isGoingToEditor = false;
    }

    private void initialize() {
        activity = requireActivity();

        contentPath = ((ResourcesEditorActivity) activity).colorsFilePath;

        colorpicker = new Zx(activity, 0xFFFFFFFF, false, false);

        parseColorsXML(colorList, FileUtil.readFile(contentPath));

        adapter = new ColorsAdapter(colorList, (ResourcesEditorActivity) activity);
        binding.recyclerView.setAdapter(adapter);
        isInitialized = true;
    }

    public boolean checkForUnsavedChanges() {
        String originalXml = FileUtil.readFile(contentPath);
        String newXml = convertListToXml(colorList);
        return !Objects.equals(XmlUtil.replaceXml(newXml), XmlUtil.replaceXml(originalXml));
    }

    public void handleOnOptionsItemSelected() {
        XmlUtil.saveXml(contentPath, convertListToXml(colorList));
        Intent intent = new Intent();
        intent.setClass(activity, ConfigActivity.isLegacyCeEnabled() ? SrcCodeEditorLegacy.class : SrcCodeEditor.class);
        intent.putExtra("title", "colors.xml");
        intent.putExtra("content", contentPath);
        isGoingToEditor = true;
        startActivity(intent);
    }

    public static void parseColorsXML(ArrayList<ColorItem> colorList, String colorXml) {
        try {
            colorList.clear();
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(colorXml));

            int eventType = parser.getEventType();
            String colorName = null;
            String colorValue = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("color".equals(tagName)) {
                            colorName = parser.getAttributeValue(null, "name");
                        }
                        break;
                    case XmlPullParser.TEXT:
                        colorValue = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if ("color".equals(tagName)) {
                            if ((colorName != null) && PropertiesUtil.isHexColor(getColorValue(SketchApplication.getContext(), colorValue, 4))) {
                                colorList.add(new ColorItem(colorName, colorValue));
                            }
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception ignored) {
        }
    }

    public void showDeleteDialog(int position) {
        aB dialog = new aB(activity);
        dialog.a(R.drawable.ic_mtrl_delete);
        dialog.b(xB.b().a(activity, R.string.color_editor_delete_color));
        dialog.a(xB.b().a(activity, R.string.picker_color_message_delete_all_custom_color));
        dialog.b(xB.b().a(activity, R.string.common_word_delete), v -> {
            colorList.remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, colorList.size());
            dialog.dismiss();
        });
        dialog.a(xB.b().a(activity, R.string.common_word_cancel), v -> dialog.dismiss());
        dialog.show();
    }

    public void showColorEditDialog(ColorItem colorItem, int position) {
        aB dialog = new aB(activity);
        ColorEditorAddBinding dialogBinding = ColorEditorAddBinding.inflate(getLayoutInflater());
        new XB(activity, dialogBinding.colorValueInputLayout, dialogBinding.colorPreview);

        if (colorItem != null) {
            dialogBinding.colorKeyInput.setText(colorItem.getColorName());
            dialogBinding.colorPreview.setBackgroundColor(PropertiesUtil.parseColor(getColorValue(activity.getApplicationContext(), colorItem.getColorValue(), 3)));

            if (colorItem.getColorValue().startsWith("@")) {
                dialogBinding.colorValueInput.setText(colorItem.getColorValue().replace("@", ""));
                dialogBinding.hash.setText("@");
                dialogBinding.colorValueInput.setEnabled(false);
                dialogBinding.hash.setEnabled(false);
                dialogBinding.colorValueInputLayout.setError(null);
            } else {
                dialogBinding.colorValueInput.setText(colorItem.getColorValue().replace("#", ""));
                dialogBinding.hash.setText("#");

            }

            dialog.b("Edit color");

        } else {
            dialog.b("Add new color");
            dialogBinding.colorPreview.setBackgroundColor(0xFFFFFF);
        }

        dialog.b("Save", v1 -> {
            String key = Objects.requireNonNull(dialogBinding.colorKeyInput.getText()).toString();
            String value = Objects.requireNonNull(dialogBinding.colorValueInput.getText()).toString();

            if (key.isEmpty() || value.isEmpty()) {
                SketchwareUtil.toast("Please fill in all fields", Toast.LENGTH_SHORT);
                return;
            }

            if (value.startsWith("#")) {
                if (!PropertiesUtil.isHexColor(value)) {
                    SketchwareUtil.toast("Please enter a valid HEX color", Toast.LENGTH_SHORT);
                }
                return;
            }

            if (colorItem != null) {
                colorItem.setColorName(key);

                if (dialogBinding.hash.getText().equals("@")) {
                    colorItem.setColorValue("@" + value);
                } else {
                    colorItem.setColorValue("#" + value);
                }

                adapter.notifyItemChanged(position);
            } else {
                addColor(key, value);
            }
            dialog.dismiss();
        });

        dialogBinding.colorPreviewCard.setOnClickListener(v -> {
            colorpicker.a(new Zx.b() {
                @Override
                public void a(int colorInt) {
                    String selectedColorHex = "#" + String.format("%06X", colorInt & 0x00FFFFFF);
                    dialogBinding.colorPreviewCard.setCardBackgroundColor(PropertiesUtil.parseColor(selectedColorHex));
                    dialogBinding.colorValueInput.setText(selectedColorHex.replace("#", ""));
                    dialogBinding.colorValueInput.setEnabled(true);
                    dialogBinding.hash.setEnabled(true);
                    dialogBinding.hash.setText("#");
                }

                @Override
                public void a(String var1, int var2) {
                }
            });
            colorpicker.showAtLocation(v, Gravity.CENTER, 0, 0);
        });

        if (colorItem != null) {
            dialog.configureDefaultButton("Delete", v1 -> {
                colorList.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, colorList.size());
                dialog.dismiss();
            });
        }

        dialog.a(getString(R.string.cancel), v1 -> dialog.dismiss());
        dialog.a(dialogBinding.getRoot());
        dialog.show();
    }

    private void addColor(String name, String value) {
        ColorItem newItem = new ColorItem(name, "#" + value);
        for (int i = 0; i < colorList.size(); i++) {
            if (colorList.get(i).getColorName().equals(name)) {
                colorList.set(i, newItem);
                adapter.notifyItemChanged(i);
                return;
            }
        }
        colorList.add(newItem);
        adapter.notifyItemInserted(colorList.size() - 1);
    }

    public void saveColorsFile() {
        if (isInitialized) {
            XmlUtil.saveXml(contentPath, ColorEditor.convertListToXml(colorList));
        }
    }
}
