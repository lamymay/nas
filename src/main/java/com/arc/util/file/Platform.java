package com.arc.util.file;

import java.util.Set;

public class Platform {

    public static final String DS_Store = ".DS_Store";
    public static final String Thumbs_db = "Thumbs.db";
    public static final String desktop_ini = "desktop.ini";

    public static final Set<String> SYSTEM_DEFAULT_FILENAMES = Set.of(
            DS_Store, Thumbs_db, desktop_ini
    );

    public static boolean isSystemDefault(String filename) {
        return SYSTEM_DEFAULT_FILENAMES.contains(filename);
    }
}


//删除项目中的所有.DS_Store。这会跳过不在项目中的 .DS_Store
//1.find . -name .DS_Store -print0 | xargs -0 git rm -f --ignore-unmatch
//将 .DS_Store 加入到 .gitignore
//2.echo .DS_Store >> ~/.gitignore
//更新项目
//3.git add --all
//4.git commit -m '.DS_Store banished!'