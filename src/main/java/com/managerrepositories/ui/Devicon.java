package com.managerrepositories.ui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javafx.scene.layout.StackPane;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

/**
 * Cria ícones Devicon locais de acordo com o nome ou a extensão de um arquivo.
 */
public final class Devicon {
    private static final Map<String, String> ICONS = Map.ofEntries(
            Map.entry("java", "java"), Map.entry("ts", "typescript"), Map.entry("tsx", "typescript"),
            Map.entry("js", "javascript"), Map.entry("jsx", "javascript"), Map.entry("py", "python"),
            Map.entry("go", "go"), Map.entry("rs", "rust"), Map.entry("kt", "kotlin"),
            Map.entry("c", "c"), Map.entry("cs", "csharp"), Map.entry("php", "php"),
            Map.entry("rb", "ruby"), Map.entry("swift", "swift"), Map.entry("html", "html5"),
            Map.entry("htm", "html5"), Map.entry("css", "css3"), Map.entry("cpp", "cplusplus"),
            Map.entry("cc", "cplusplus"), Map.entry("cxx", "cplusplus"), Map.entry("json", "json"),
            Map.entry("md", "markdown"), Map.entry("yml", "yaml"), Map.entry("yaml", "yaml"));
    private static final Map<String, String> FILE_ICONS = Map.ofEntries(
            Map.entry("package.json", "npm"), Map.entry("pom.xml", "maven"),
            Map.entry("readme.md", "markdown"), Map.entry(".gitignore", "git"));
    private static final double VIEWBOX_SIZE = 128d;

    private Devicon() {
    }

    public static Node forFile(String fileName, double size) {
        String icon = iconName(fileName);
        if (icon == null)
            return fallback();

        try (InputStream stream = Devicon.class.getResourceAsStream("/icons/devicon/" + icon + ".svg")) {
            if (stream == null)
                return fallback();
            return loadSvg(stream, size);
        } catch (Exception ignored) {
            return fallback();
        }
    }

    private static String iconName(String fileName) {
        String name = fileName.toLowerCase(Locale.ROOT);
        String byName = FILE_ICONS.get(name);
        if (byName != null)
            return byName;
        int dot = name.lastIndexOf('.');
        return dot < 0 || dot == name.length() - 1 ? null : ICONS.get(name.substring(dot + 1));
    }

    private static Node loadSvg(InputStream stream, double size) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Document document = factory.newDocumentBuilder().parse(stream);
        NodeList paths = document.getElementsByTagName("path");
        Group icon = new Group();

        for (int index = 0; index < paths.getLength(); index++) {
            Element element = (Element) paths.item(index);
            String pathData = element.getAttribute("d");
            if (pathData.isBlank())
                continue;
            SVGPath path = new SVGPath();
            path.setContent(pathData);
            path.setFill(fillOf(element));
            icon.getChildren().add(path);
        }
        icon.setScaleX(size / VIEWBOX_SIZE);
        icon.setScaleY(size / VIEWBOX_SIZE);

        StackPane container = new StackPane(icon);
        container.setMinSize(size, size);
        container.setPrefSize(size, size);
        container.setMaxSize(size, size);

        return container;
    }

    private static Color fillOf(Element element) {
        String fill = element.getAttribute("fill");
        if (fill == null || fill.isBlank() || "none".equals(fill))
            return Color.web("#a6a6bb");
        try {
            return Color.web(fill);
        } catch (IllegalArgumentException ignored) {
            return Color.web("#a6a6bb");
        }
    }

    private static Label fallback() {
        Label fallback = new Label("◦");
        fallback.getStyleClass().add("file-icon-fallback");
        return fallback;
    }
}
