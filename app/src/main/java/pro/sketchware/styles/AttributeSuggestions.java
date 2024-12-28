package pro.sketchware.styles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AttributeSuggestions {

    public final HashMap<String, SuggestionType> ATTRIBUTE_TYPES = new HashMap<>();
    public final HashMap<String, SuggestionType> ATTRIBUTE_SUGGESTIONS = new HashMap<>();
    private final HashMap<SuggestionType, List<String>> SUGGESTIONS = new HashMap<>();

    public enum SuggestionType {
        BOOLEAN, DIMENSION, COLOR, TEXT, NUMBER, DRAWABLE, FONT, GRAVITY, DEFAULT
    }

    public AttributeSuggestions() {
        ATTRIBUTE_SUGGESTIONS.put("android:textColor", SuggestionType.COLOR);
        ATTRIBUTE_SUGGESTIONS.put("android:textSize", SuggestionType.DIMENSION);
        ATTRIBUTE_SUGGESTIONS.put("android:fontFamily", SuggestionType.FONT);
        ATTRIBUTE_SUGGESTIONS.put("android:textAllCaps", SuggestionType.BOOLEAN);
        ATTRIBUTE_SUGGESTIONS.put("android:textAppearance", SuggestionType.DRAWABLE);
        ATTRIBUTE_SUGGESTIONS.put("android:background", SuggestionType.DRAWABLE);
        ATTRIBUTE_SUGGESTIONS.put("android:hint", SuggestionType.TEXT);
        ATTRIBUTE_SUGGESTIONS.put("android:lineSpacingExtra", SuggestionType.DIMENSION);
        ATTRIBUTE_SUGGESTIONS.put("android:letterSpacing", SuggestionType.DIMENSION);
        ATTRIBUTE_SUGGESTIONS.put("android:colorPrimary", SuggestionType.COLOR);
        ATTRIBUTE_SUGGESTIONS.put("android:colorAccent", SuggestionType.COLOR);
        ATTRIBUTE_SUGGESTIONS.put("android:colorControlNormal", SuggestionType.COLOR);
        ATTRIBUTE_SUGGESTIONS.put("android:colorControlActivated", SuggestionType.COLOR);
        ATTRIBUTE_SUGGESTIONS.put("android:colorControlHighlight", SuggestionType.COLOR);
        ATTRIBUTE_SUGGESTIONS.put("android:statusBarColor", SuggestionType.COLOR);
        ATTRIBUTE_SUGGESTIONS.put("android:navigationBarColor", SuggestionType.COLOR);
        ATTRIBUTE_SUGGESTIONS.put("android:windowBackground", SuggestionType.DRAWABLE);
        ATTRIBUTE_SUGGESTIONS.put("android:showAsAction", SuggestionType.TEXT);
        ATTRIBUTE_SUGGESTIONS.put("android:alpha", SuggestionType.NUMBER);
        ATTRIBUTE_SUGGESTIONS.put("android:elevation", SuggestionType.DIMENSION);
        ATTRIBUTE_SUGGESTIONS.put("android:layout_gravity", SuggestionType.GRAVITY);

        // Adding broader keys for general types
        ATTRIBUTE_TYPES.put("size", SuggestionType.DIMENSION);
        ATTRIBUTE_TYPES.put("height", SuggestionType.DIMENSION);
        ATTRIBUTE_TYPES.put("width", SuggestionType.DIMENSION);
        ATTRIBUTE_TYPES.put("color", SuggestionType.COLOR);
        ATTRIBUTE_TYPES.put("drawable", SuggestionType.DRAWABLE);
        ATTRIBUTE_TYPES.put("gravity", SuggestionType.GRAVITY);
        ATTRIBUTE_TYPES.put("padding", SuggestionType.DIMENSION);
        ATTRIBUTE_TYPES.put("margin", SuggestionType.DIMENSION);
        ATTRIBUTE_TYPES.put("lines", SuggestionType.NUMBER);

        // Initializing the suggestions for each type
        SUGGESTIONS.put(SuggestionType.TEXT, generateTextsSuggestions());
        SUGGESTIONS.put(SuggestionType.BOOLEAN, Arrays.asList("true", "false"));
        SUGGESTIONS.put(SuggestionType.DIMENSION, generateDimensionSuggestions());
        SUGGESTIONS.put(SuggestionType.COLOR, Arrays.asList("#FFFFFF", "#000000", "#FF0000", "#00FF00", "#0000FF"));
        SUGGESTIONS.put(SuggestionType.NUMBER, generateNumberSuggestions());
        SUGGESTIONS.put(SuggestionType.DRAWABLE, generateDrawableSuggestions());
        SUGGESTIONS.put(SuggestionType.FONT, Arrays.asList("sans-serif", "serif", "monospace"));
        SUGGESTIONS.put(SuggestionType.GRAVITY, Arrays.asList("center", "left", "right", "top", "bottom"));
        SUGGESTIONS.put(SuggestionType.DEFAULT, Arrays.asList("5dp", "10dp", "15dp", "#FFFFFF", "#FF0000"));
    }

    public SuggestionType getSuggestionType(String attribute) {
        for (String attr : ATTRIBUTE_TYPES.keySet()) {
            if (attribute.contains(attr)) {
                return ATTRIBUTE_TYPES.get(attr);
            }
        }
        return SuggestionType.DEFAULT;
    }

    public List<String> getSuggestions(SuggestionType type) {
        return SUGGESTIONS.get(type);
    }

    public List<String> getSuggestions(String attr) {
        SuggestionType type = getSuggestionType(attr);
        return SUGGESTIONS.get(type);
    }

    private List<String> generateTextsSuggestions() {
        // TODO: get XMLStrings list
        return new ArrayList<>();
    }

    private List<String> generateDimensionSuggestions() {
        List<String> suggestions = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            suggestions.add(i + "dp");
        }
        return suggestions;
    }

    private List<String> generateNumberSuggestions() {
        int start = 0;
        int end = 10;
        List<String> suggestions = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            suggestions.add(String.valueOf(i));
        }
        return suggestions;
    }

    private List<String> generateDrawableSuggestions() {
        return Arrays.asList("@drawable/sample_image", "@drawable/ic_launcher", "@drawable/custom_background");
    }
}
