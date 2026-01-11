package com.arc.util.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class TrashUtil {

    private static final Logger log = LoggerFactory.getLogger(TrashUtil.class);

    // 仅非 headless 环境才初始化
    private static Desktop desktop;

    static {
        try {
            if (!GraphicsEnvironment.isHeadless()) {
                desktop = Desktop.getDesktop();
            }
        } catch (Exception e) {
            log.warn("Desktop not available, running in headless mode", e);
        }
    }

    public static boolean moveToTrash(File file) {


        if (file == null) {
            log.warn("File is null: " + file);
            return false;
        }
        try {
            file = new File(file.getCanonicalPath());
        } catch (IOException e) {
            log.error("file.getCanonicalPath() failed, fallback to  original file", e);
        }

        if (!file.exists()) {
            log.warn("File does not exist: " + file);
            return false;
        }

        // GUI 优先使用 Desktop
        if (desktop != null) {
            try {
                return desktop.moveToTrash(file);
            } catch (Exception e) {
                log.error("Desktop.moveToTrash failed, fallback to OS commands", e);
            }
        }

        // headless fallback
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                String cmd = "powershell.exe Remove-Item -Path \"" + file.getAbsolutePath() + "\" -Confirm:$false -Recycle";
                Process p = Runtime.getRuntime().exec(cmd);
                return p.waitFor() == 0;
            } else if (os.contains("mac")) {
                String[] cmd = {"osascript", "-e", "tell application \"Finder\" to delete POSIX file \"" + file.getAbsolutePath() + "\""};
                Process p = Runtime.getRuntime().exec(cmd);
                return p.waitFor() == 0;
            } else {
                try {
                    Process p = Runtime.getRuntime().exec(new String[]{"trash-put", file.getAbsolutePath()});
                    if (p.waitFor() == 0) return true;
                } catch (Exception ignored) {
                }
                return file.delete();
            }
        } catch (Exception e) {
            log.error("moveToTrash fallback failed, trying direct delete", e);
            return file.delete();
        }
    }


    public static void main(String[] args) {
        boolean toTrash = TrashUtil.moveToTrash(new File("/Users/may/Desktop/", "IMG_20231217_232035_974.jpg"));
        System.out.println(toTrash);


    }
}