import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.*;

public class AssetUploader {
    public void upload(File asset, String destination) throws IOException, InterruptedException {
        String path = asset.getAbsolutePath();
        String newFilePath = imageMagick(resize(path, 200, 70));
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        fileInputStream = new FileInputStream(newFilePath);
        fileOutputStream = new FileOutputStream(new File(destination.concat(newFilePath.substring(newFilePath.lastIndexOf("/")+1))));
        int read;
        while ((read = fileInputStream.read()) != -1) {
            fileOutputStream.write(read);
        }
        if (fileInputStream != null) fileInputStream.close();
        if (fileOutputStream != null) fileOutputStream.close();
    }

    private String imageMagick(String[] cmd) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(cmd);
        process.waitFor();
        return cmd[cmd.length - 1];
    }

    private String[] resize(String fileName, int height, int quality) {
        return new String[]{"/usr/local/bin/convert", fileName, "-quiet", "-strip", "-quality", String.valueOf(quality), "-resize", "x" + height, "-colorspace", "RGB", fileName + "." + height + ".jpg"};
    }

}
