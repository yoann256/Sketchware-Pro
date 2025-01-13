package pro.sketchware.activities.resources.editors.utils;

import androidx.annotation.NonNull;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import a.a.a.XmlBuilderHelper;
import pro.sketchware.activities.resources.editors.models.StyleModel;

public class StylesEditorManager {

    public boolean isDataLoadingFailed;
    public LinkedHashMap<Integer, String> notesMap = new LinkedHashMap<>();

    public String convertStylesToXML(ArrayList<StyleModel> stylesList, HashMap<Integer, String> notesMap) {
        XmlBuilderHelper stylesFileBuilder = new XmlBuilderHelper();

        int styleIndex = 0;
        for (StyleModel style : stylesList) {
            String parentStyle = (style.getParent() != null && !style.getParent().isEmpty()) ? style.getParent() : null;
            stylesFileBuilder.addStyle(style.getStyleName(), parentStyle);

            if (notesMap.containsKey(styleIndex)) {
                stylesFileBuilder.addComment(notesMap.get(styleIndex), styleIndex);
            }

            Map<String, String> attributes = style.getAttributes();
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                stylesFileBuilder.addItemToStyle(style.getStyleName(), entry.getKey(), entry.getValue());
            }
            styleIndex++;
        }

        return stylesFileBuilder.toCode();
    }

    public ArrayList<StyleModel> parseStylesFile(String content) {
        isDataLoadingFailed = false;
        ArrayList<StyleModel> styles = new ArrayList<>();
        notesMap.clear();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(content));
            Document document = builder.parse(inputSource);
            document.getDocumentElement().normalize();

            NodeList allNodes = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < allNodes.getLength(); i++) {
                Node node = allNodes.item(i);

                if (node.getNodeType() == Node.COMMENT_NODE) {
                    Comment comment = (Comment) node;
                    String commentText = comment.getTextContent().trim();
                    notesMap.put(styles.size(), commentText);
                } else if (node.getNodeType() == Node.ELEMENT_NODE && "style".equals(node.getNodeName())) {
                    Element element = (Element) node;
                    String styleName = element.getAttribute("name");
                    String parent = element.hasAttribute("parent") ? element.getAttribute("parent") : null;

                    LinkedHashMap<String, String> attributes = getAttributes(element);

                    styles.add(new StyleModel(styleName, parent, attributes));
                }
            }

        } catch (Exception ignored) {
            isDataLoadingFailed = !content.trim().isEmpty();
        }

        return styles;
    }

    private static @NonNull LinkedHashMap<String, String> getAttributes(Element element) {
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
        return attributes;
    }
}
