package com.bogdan.client.util;

import com.bogdan.client.common.Constant;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.file.Paths;

@UtilityClass
public class FileUtils {

    public static File getDownloadFile(String fileName) {
        return Paths.get(Constant.DOWNLOAD_DIR, fileName).toFile();
    }

    public static File getSharedFile(String fileName) {
        return Paths.get(Constant.SHARED_DIR, fileName).toFile();
    }

    public static void createDirectories() {
        new File(Constant.SHARED_DIR).mkdirs();
        new File(Constant.DOWNLOAD_DIR).mkdirs();
    }

}
