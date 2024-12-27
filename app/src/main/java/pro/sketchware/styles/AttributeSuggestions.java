package pro.sketchware.styles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AttributeSuggestions {

    public final HashMap<String, SuggestionType> ATTRIBUTE_TYPES = new HashMap<>();
    private final HashMap<SuggestionType, List<String>> SUGGESTIONS = new HashMap<>();

    public enum SuggestionType {
        BOOLEAN, DIMENSION, COLOR, TEXT, NUMBER, DEFAULT
    }

    public AttributeSuggestions() {
        ATTRIBUTE_TYPES.put("android:indeterminateOnly", SuggestionType.BOOLEAN);
        ATTRIBUTE_TYPES.put("android:textSize", SuggestionType.DIMENSION);
        ATTRIBUTE_TYPES.put("android:layout_width", SuggestionType.DIMENSION);
        ATTRIBUTE_TYPES.put("android:layout_height", SuggestionType.DIMENSION);
        ATTRIBUTE_TYPES.put("android:background", SuggestionType.COLOR);
        ATTRIBUTE_TYPES.put("android:textColor", SuggestionType.COLOR);
        ATTRIBUTE_TYPES.put("android:maxLines", SuggestionType.NUMBER);

        SUGGESTIONS.put(SuggestionType.BOOLEAN, Arrays.asList("true", "false"));
        SUGGESTIONS.put(SuggestionType.DIMENSION, generateDimensionSuggestions());
        SUGGESTIONS.put(SuggestionType.COLOR, Arrays.asList("#FFFFFF", "#000000", "#FF0000", "#00FF00", "#0000FF"));
        SUGGESTIONS.put(SuggestionType.NUMBER, generateNumberSuggestions());
        SUGGESTIONS.put(SuggestionType.DEFAULT, Arrays.asList("5dp", "10dp", "15dp", "#FFFFFF", "#FF0000"));
    }

    public SuggestionType getSuggestionType(String attribute) {
        for (String attr : ATTRIBUTE_TYPES.keySet()) {
            if (attr.contains(attribute)) {
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

    private List<String> generateDimensionSuggestions() {
        List<String> suggestions = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
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
}
