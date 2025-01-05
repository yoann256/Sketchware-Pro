package pro.sketchware.activities.resources.editors.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import a.a.a.XmlBuilderHelper;
import pro.sketchware.activities.resources.editors.models.StyleModel;

public class StylesEditorManager {

    public boolean isDataLoadingFailed;

    public String getAttributesCode(StyleModel style) {
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

    public LinkedHashMap<String, String> convertAttributesToMap(String attributesCode) {
        LinkedHashMap<String, String> attributesMap = new LinkedHashMap<>();


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

    public String convertStylesToXML(ArrayList<StyleModel> stylesList) {
        // Create an instance of XmlBuilderHelper
        XmlBuilderHelper stylesFileBuilder = new XmlBuilderHelper();

        // Iterate through the list of styles and add them to the builder
        for (StyleModel style : stylesList) {
            // Provide the style name and parent (or null if no parent)
            String parentStyle = (style.getParent() != null && !style.getParent().isEmpty()) ? style.getParent() : null;
            stylesFileBuilder.addStyle(style.getStyleName(), parentStyle);

            // Add attributes as <item> elements to the current style
            Map<String, String> attributes = style.getAttributes();
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                stylesFileBuilder.addItemToStyle(style.getStyleName(), entry.getKey(), entry.getValue());
            }
        }

        // Generate and return the XML code
        return stylesFileBuilder.toCode();
    }

    public ArrayList<StyleModel> parseStylesFile(String content) {
        isDataLoadingFailed = false;
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

                    LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
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

        } catch (Exception ignored) {
            isDataLoadingFailed = !content.trim().isEmpty();
        }

        return styles;
    }

}
