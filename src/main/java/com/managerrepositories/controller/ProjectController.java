package com.managerrepositories.controller;

import com.managerrepositories.model.FileNode;
import com.managerrepositories.model.Project;
import com.managerrepositories.service.WorkspaceScanner;
import com.managerrepositories.ui.Devicon;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.Preferences;
import java.nio.file.Files;

/** Controla a navegação e os dados de demonstração do catálogo de projetos. */
public class ProjectController {
    @FXML
    private ListView<Project> sidebarProjects;
    @FXML
    private TilePane projectGrid;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> languageFilter;
    @FXML
    private ToggleButton onlyStarred, allTab, starredTab, recentTab, workspaceTab;
    @FXML
    private Label summaryLabel, resultLabel, starCountLabel, fileCountLabel;
    @FXML
    private VBox detailPanel;
    @FXML
    private Label detailName, detailPath, detailFiles, detailSize, detailOpened;
    @FXML
    private FlowPane tagPane;
    @FXML
    private TreeView<FileNode> fileTree;

    private final List<Project> projects = new ArrayList<>();
    private Project selected;
    private String sidebarMode = "all";
    private boolean listView;
    private static final String WORKSPACE_KEY = "workspace.path";
    private final Preferences preferences = Preferences.userNodeForPackage(ProjectController.class);
    private Path workspace;

    @FXML
    private void initialize() {
        Path defaultWorkspace = Path.of(
                System.getProperty("user.home"),
                "Documents",
                "WorkSpace");

        String savedPath = preferences.get(
                WORKSPACE_KEY,
                defaultWorkspace.toString());

        Path initialWorkspace = Path.of(savedPath);

        if (Files.isDirectory(initialWorkspace)) {
            loadWorkspace(initialWorkspace);
        }
        languageFilter.getItems().addAll("Todos os idiomas", "TypeScript", "Go", "Python", "Rust", "JavaScript", "C++");
        languageFilter.setValue("Todos os idiomas");
        sidebarProjects.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(Project item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                HBox row = new HBox(8, languageMark(item), label(item.name(), "mono side-project"));
                row.setPadding(new Insets(5, 8, 5, 12));
                if (item.starred())
                    row.getChildren().add(label("★", "star"));
                setGraphic(row);
            }
        });
        sidebarProjects.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> select(value));
        detailPanel.setVisible(false);
        detailPanel.setManaged(false);
        filterProjects();
    }

    @FXML
    public void showAll() {
        sidebarMode = "all";
        filterProjects();
    }

    @FXML
    public void showStarred() {
        sidebarMode = "starred";
        filterProjects();
    }

    @FXML
    public void showRecent() {
        sidebarMode = "recent";
        filterProjects();
    }

    private void loadWorkspace(Path folder) {
        try {
            workspace = folder;
            projects.clear();
            projects.addAll(WorkspaceScanner.scan(workspace));

            sidebarMode = "all";
            allTab.setSelected(true);
            clearSelection();
            refreshLanguageOptions();
            filterProjects();
        } catch (IOException exception) {
            Alert alert = new Alert(
                    Alert.AlertType.ERROR,
                    "Não foi possível ler a pasta:\n" + exception.getMessage(),
                    ButtonType.OK);
            alert.showAndWait();
        }
    }

    /**
     * Abre a seleção da pasta que conterá os projetos locais. Nenhum arquivo é
     * alterado.
     */
    @FXML
    public void chooseWorkspace() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Selecione a pasta Workspace");
        if (workspace != null && workspace.toFile().isDirectory())
            chooser.setInitialDirectory(workspace.toFile());
        File selectedDirectory = chooser
                .showDialog(projectGrid.getScene() == null ? null : (Stage) projectGrid.getScene().getWindow());
        if (selectedDirectory == null) {
            allTab.setSelected(true);
            return;
        }
        Path selectedWorkspace = selectedDirectory.toPath();
        preferences.put(WORKSPACE_KEY, selectedWorkspace.toString());

        loadWorkspace(selectedWorkspace);
    }

    @FXML
    public void filterProjects() {
        String query = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String language = languageFilter.getValue();
        List<Project> visible = projects.stream()
                .filter(p -> sidebarMode.equals("all") || (sidebarMode.equals("starred") ? p.starred() : true))
                .filter(p -> !onlyStarred.isSelected() || p.starred())
                .filter(p -> language == null || language.equals("Todos os idiomas") || p.language().equals(language))
                .filter(p -> p.name().toLowerCase().contains(query)
                        || p.tags().stream().anyMatch(t -> t.contains(query)))
                .sorted(sidebarMode.equals("recent") ? Comparator.comparing(Project::lastOpened)
                        : Comparator.comparing(Project::name))
                .toList();
        sidebarProjects.getItems().setAll(visible);
        rebuildCards(visible);
        int totalFiles = projects.stream().mapToInt(Project::files).sum();
        int stars = (int) projects.stream().filter(Project::starred).count();
        summaryLabel.setText(workspace == null
                ? projects.size() + " projetos · " + totalFiles + " arquivos"
                : "WORKSPACE: " + workspace.getFileName() + " · " + projects.size() + " projetos");
        resultLabel.setText(visible.size() + (visible.size() == 1 ? " projeto" : " projetos"));
        starCountLabel.setText(String.valueOf(stars));
        fileCountLabel.setText(String.valueOf(totalFiles));
    }

    private void rebuildCards(List<Project> visible) {
        projectGrid.getChildren().clear();
        for (Project project : visible)
            projectGrid.getChildren().add(createCard(project));
    }

    private void refreshLanguageOptions() {
        String previous = languageFilter.getValue();
        List<String> languages = projects.stream().map(Project::language).distinct().sorted().toList();
        languageFilter.getItems().setAll("Todos os idiomas");
        languageFilter.getItems().addAll(languages);
        languageFilter.setValue(languages.contains(previous) ? previous : "Todos os idiomas");
    }

    private VBox createCard(Project p) {
        VBox card = new VBox(9);
        card.getStyleClass().add("project-card");
        card.setPadding(new Insets(13));
        HBox top = new HBox(7, languageMark(p), label(p.language().toUpperCase(), "language-label"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button star = new Button(p.starred() ? "★" : "☆");
        star.getStyleClass().add("star-button");
        star.setOnAction(e -> {
            replace(p, p.withStarred(!p.starred()));
            filterProjects();
        });
        top.getChildren().addAll(spacer, star);
        Label name = label(p.name(), "project-name mono");
        Label path = label(p.path(), "project-path mono");
        HBox info = new HBox(10, label(p.files() + " files", "card-info mono"), label(p.size(), "card-info mono"));
        FlowPane tags = new FlowPane(5, 5);
        p.tags().forEach(tag -> tags.getChildren().add(label(tag, "tag mono")));
        card.getChildren().addAll(top, name, path, info, tags);
        card.setOnMouseClicked(e -> {
            sidebarProjects.getSelectionModel().select(p);
            select(p);
        });
        return card;
    }

    private void select(Project project) {
        if (project == null)
            return;
        selected = project;
        detailPanel.setVisible(true);
        detailPanel.setManaged(true);
        detailName.setText(project.name() + "  ·  " + project.language());
        detailPath.setText(project.path());
        detailFiles.setText(String.valueOf(project.files()));
        detailSize.setText(project.size());
        detailOpened.setText(project.lastOpened());
        tagPane.getChildren().setAll(project.tags().stream().map(t -> label(t, "tag mono")).toList());
        TreeItem<FileNode> root = new TreeItem<>(new FileNode(project.name(), true, project.tree()));
        root.setExpanded(true);
        project.tree().forEach(node -> root.getChildren().add(treeItem(node)));
        fileTree.setRoot(root);
        fileTree.setShowRoot(false);
        fileTree.setCellFactory(tree -> new TreeCell<>() {
            @Override
            protected void updateItem(FileNode n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(n.name());
                setGraphic(fileGraphic(n));
            }
        });
    }

    private TreeItem<FileNode> treeItem(FileNode node) {
        TreeItem<FileNode> item = new TreeItem<>(node);
        node.children().forEach(child -> item.getChildren().add(treeItem(child)));
        return item;
    }

    private Node fileGraphic(FileNode node) {
        return node.folder() ? label("▸", "folder-icon") : Devicon.forFile(node.name(), 12);
    }

    @FXML
    public void clearSelection() {
        selected = null;
        sidebarProjects.getSelectionModel().clearSelection();
        detailPanel.setVisible(false);
        detailPanel.setManaged(false);
    }

    @FXML
    public void deleteSelected() {
        if (selected != null) {
            projects.remove(selected);
            clearSelection();
            filterProjects();
        }
    }

    @FXML
    public void toggleView() {
        listView = !listView;
        projectGrid.setPrefTileWidth(listView ? 720 : 230);
        filterProjects();
    }

    @FXML
    public void openAddDialog() {
        Dialog<Project> dialog = new Dialog<>();
        dialog.setTitle("Adicionar projeto");
        dialog.setHeaderText("Novo projeto no DEVHUB");
        ButtonType add = new ButtonType("Adicionar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(add, ButtonType.CANCEL);
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(18));
        TextField name = new TextField();
        name.setPromptText("meu-projeto");
        TextField path = new TextField();
        path.setPromptText("~/dev/meu-projeto");
        ComboBox<String> lang = new ComboBox<>();
        lang.getItems().addAll("Java", "TypeScript", "Python", "Go", "Rust");
        lang.setValue("Java");
        form.addRow(0, new Label("Nome"), name);
        form.addRow(1, new Label("Caminho"), path);
        form.addRow(2, new Label("Linguagem"), lang);
        dialog.getDialogPane().setContent(form);
        dialog.setResultConverter(button -> button == add && !name.getText().isBlank() ? new Project(name.getText(),
                path.getText().isBlank() ? "~/dev/" + name.getText() : path.getText(), lang.getValue(), "#00e5ff",
                "agora", 0, "0 B", List.of("novo"), false, List.of(FileNode.file("README.md"))) : null);
        dialog.showAndWait().ifPresent(p -> {
            projects.add(p);
            filterProjects();
            sidebarProjects.getSelectionModel().select(p);
        });
    }

    private void replace(Project before, Project after) {
        projects.set(projects.indexOf(before), after);
        if (selected == before)
            selected = after;
    }

    private Label label(String text, String style) {
        Label label = new Label(text);
        label.getStyleClass().addAll(style.split(" "));
        return label;
    }

    private Circle languageMark(Project p) {
        Circle dot = new Circle(5, Color.web(p.color()));
        dot.getStyleClass().add("language-dot");
        return dot;
    }

    private List<Project> sampleProjects() {
        return List.of(
                project("dashboard-ui", "~/dev/dashboard-ui", "TypeScript", "#00e5ff", "2 min ago", 84, "3.2 MB", true,
                        "react", "vite", "tailwind"),
                project("api-gateway", "~/dev/api-gateway", "Go", "#00add8", "1 hr ago", 42, "1.8 MB", false, "grpc",
                        "microservice"),
                project("ml-pipeline", "~/research/ml-pipeline", "Python", "#39ff8a", "3 hr ago", 31, "22.1 MB", true,
                        "pytorch", "data", "training"),
                project("core-engine", "~/dev/core-engine", "Rust", "#ff6b35", "yesterday", 67, "5.6 MB", false,
                        "systems", "wasm"),
                project("e2e-tests", "~/dev/e2e-tests", "JavaScript", "#f7df1e", "2 days ago", 28, "0.9 MB", false,
                        "playwright", "testing"),
                project("compiler-project", "~/research/compiler-project", "C++", "#ff4466", "3 days ago", 119,
                        "8.3 MB", true, "llvm", "compiler"));
    }

    private Project project(String n, String path, String lang, String color, String opened, int files, String size,
            boolean star, String... tags) {
        return new Project(n, path, lang, color, opened, files, size, List.of(tags), star, List.of(
                FileNode.folder("src", FileNode.file("main." + extension(lang)),
                        FileNode.file("App." + extension(lang)),
                        FileNode.folder("components", FileNode.file("Button." + extension(lang)),
                                FileNode.file("Navbar." + extension(lang)))),
                FileNode.file("README.md"), FileNode.file(configName(lang))));
    }

    private String extension(String language) {
        return switch (language) {
            case "Python" -> "py";
            case "Go" -> "go";
            case "Rust" -> "rs";
            case "C++" -> "cpp";
            default -> language.equals("JavaScript") ? "js" : "tsx";
        };
    }

    private String configName(String language) {
        return switch (language) {
            case "Go" -> "go.mod";
            case "Rust" -> "Cargo.toml";
            case "Python" -> "requirements.txt";
            default -> "package.json";
        };
    }
}
