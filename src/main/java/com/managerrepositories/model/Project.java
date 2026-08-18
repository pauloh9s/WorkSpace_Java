package com.managerrepositories.model;

import java.util.List;

public record Project(String name, String path, String language, String color, String lastOpened,
                      int files, String size, List<String> tags, boolean starred, List<FileNode> tree) {
    public Project withStarred(boolean value) {
        return new Project(name, path, language, color, lastOpened, files, size, tags, value, tree);
    }
}
