package org.cdc.generator.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static File tryToConvertExeToJar(File exeFile) throws IOException {
        ZipFile zipFile = new ZipFile(exeFile);
        File result = new File(exeFile.getParentFile(), ".cache");
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(result));
        zipFile.stream().forEach(a -> {
            try {
                zipOutputStream.putNextEntry(a);
                zipFile.getInputStream(a).transferTo(zipOutputStream);
                zipOutputStream.closeEntry();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        zipOutputStream.close();
        zipFile.close();
        return result;
    }
}
