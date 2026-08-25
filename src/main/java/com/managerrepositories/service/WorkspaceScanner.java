package com.managerrepositories.service;

import com.managerrepositories.model.FileNode;
import com.managerrepositories.model.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** Le apenas metadados do workspace para montar os projetos exibidos no DEVHUB. */
public final class WorkspaceScanner {
    private static final int MAX_TREE_DEPTH = 5;
    private static final Map<String, Language> LANGUAGES = Map.ofEntries(
            Map.entry("java", new Language("Java", "#f89820")),
            Map.entry("kt", new Language("Kotlin", "#a97bff")),
            Map.entry("py", new Language("Python", "#39ff8a")),
            Map.entry("js", new Language("JavaScript", "#f7df1e")),
            Map.entry("jsx", new Language("JavaScript", "#f7df1e")),
            Map.entry("ts", new Language("TypeScript", "#00e5ff")),
            Map.entry("tsx", new Language("TypeScript", "#00e5ff")),
            Map.entry("go", new Language("Go", "#00add8")),
            Map.entry("rs", new Language("Rust", "#ff6b35")),
            Map.entry("c", new Language("C", "#6d9ee8")),
            Map.entry("cpp", new Language("C++", "#ff4466")),
            Map.entry("cs", new Language("C#", "#a97bff")),
            Map.entry("php", new Language("PHP", "#8892bf")),
            Map.entry("rb", new Language("Ruby", "#cc342d")),
            Map.entry("swift", new Language("Swift", "#ff6b35")),
            Map.entry("html", new Language("HTML", "#e34c26")),
            Map.entry("css", new Language("CSS", "#663399"))
    );
    private static final DateTimeFormatter MODIFIED = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    private WorkspaceScanner() { }

    /** Cada subpasta imediata do workspace é tratada como um projeto. */
    public static List<Project> scan(Path workspace) throws IOException {
        try (Stream<Path> entries = Files.list(workspace)) {
            return entries.filter(Files::isDirectory)
                    .filter(path -> !isHidden(path))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase()))
                    .map(WorkspaceScanner::scanProjectSafely)
                    .toList();
        }
    }

    private static Project scanProjectSafely(Path directory) {
        try { return scanProject(directory); }
        catch (IOException ex) {
            return new Project(directory.getFileName().toString(), directory.toString(), "Unknown", "#77778c", "indisponivel", 0, "0 B", List.of("sem acesso"), false, List.of());
        }
    }

    private static Project scanProject(Path directory) throws IOException {
        Map<Language, Integer> counts = new HashMap<>();
        long[] bytes = {0}; int[] files = {0}; Instant[] modified = {Instant.EPOCH};
        try (Stream<Path> paths = Files.walk(directory, MAX_TREE_DEPTH)) {
            paths.filter(Files::isRegularFile).forEach(file -> {
                files[0]++;
                try {
                    bytes[0] += Files.size(file);
                    BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
                    if (attributes.lastModifiedTime().toInstant().isAfter(modified[0])) modified[0] = attributes.lastModifiedTime().toInstant();
                } catch (IOException ignored) { }
                Language language = languageOf(file);
                if (language != null) counts.merge(language, 1, Integer::sum);
            });
        }
        List<Map.Entry<Language, Integer>> ranked = counts.entrySet().stream().sorted(Map.Entry.<Language, Integer>comparingByValue().reversed()).toList();
        Language primary = ranked.isEmpty() ? new Language("Files", "#77778c") : ranked.getFirst().getKey();
        String mainLanguage = primary.name;
        List<String> tags = ranked.stream().limit(4).map(entry -> entry.getKey().name + " " + entry.getValue()).toList();
        if (tags.isEmpty()) tags = List.of("sem codigo identificado");
        return new Project(directory.getFileName().toString(), directory.toAbsolutePath().toString(), mainLanguage, primary.color,
                modified[0].equals(Instant.EPOCH) ? "sem arquivos" : MODIFIED.format(modified[0]), files[0], formatBytes(bytes[0]), tags, false, tree(directory, 0));
    }

    private static List<FileNode> tree(Path directory, int depth) throws IOException {
        if (depth >= MAX_TREE_DEPTH) return List.of();
        try (Stream<Path> entries = Files.list(directory)) {
            return entries.filter(path -> !isHidden(path)).sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase())).limit(150)
                    .map(path -> nodeSafely(path, depth)).toList();
        }
    }
    private static FileNode nodeSafely(Path path, int depth) {
        try { return Files.isDirectory(path) ? new FileNode(path.getFileName().toString(), true, tree(path, depth + 1)) : FileNode.file(path.getFileName().toString()); }
        catch (IOException ex) { return FileNode.file(path.getFileName().toString()); }
    }
    private static Language languageOf(Path file) {
        String name = file.getFileName().toString(); int index = name.lastIndexOf('.');
        return index < 1 ? null : LANGUAGES.get(name.substring(index + 1).toLowerCase());
    }
    private static boolean isHidden(Path path) { try { return Files.isHidden(path) || path.getFileName().toString().startsWith("."); } catch (IOException ex) { return true; } }
    private static String formatBytes(long bytes) { if (bytes < 1024) return bytes + " B"; if (bytes < 1_048_576) return String.format("%.1f KB", bytes / 1024d); return String.format("%.1f MB", bytes / 1_048_576d); }
    private record Language(String name, String color) { }
}
