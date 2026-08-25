package com.managerrepositories.model;

import java.util.List;

public record FileNode(String name, boolean folder, List<FileNode> children) {
    public static FileNode file(String name) { return new FileNode(name, false, List.of()); }
    public static FileNode folder(String name, FileNode... children) { return new FileNode(name, true, List.of(children)); }
}
