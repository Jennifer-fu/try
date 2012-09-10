import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UploadRequest {

    private String source;
    private String destination;

    public UploadRequest(String source, String destination) {
        this.source = source;
        this.destination = destination;
    }


    public void validate() throws RequestException {
        File src = new File(source);
        if(!src.exists())
            throw new RequestException("source folder not exist: "+source);
    }

    public void synchronizeFolderStructure() {
        ensureFolder(destination);
        File src = new File(source);
        List<File> directories = new ArrayList<File>();
        GetFolders(src, directories);
        for (File directory : directories) {
            String relativePath = directory.getAbsolutePath().substring(source.length());
            ensureFolder(destination,relativePath);
        }

    }

    private void ensureFolder(String parentFolder, String relativePath) {
        File dir = new File(parentFolder.concat("/").concat(relativePath));
        if(!dir.exists())
            dir.mkdirs();
    }


    private void GetFolders(File src, List<File> directories) {
        FileFilter directoryFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };
        File[] dirs = src.listFiles(directoryFilter);
        directories.addAll(Arrays.asList(dirs));
        for (File dir : dirs) {
            GetFolders(dir, directories);
        }
    }

    private void ensureFolder(String folderPath) {
        File folder = new File(folderPath);
        if(!folder.exists())
            folder.mkdirs();
    }

}
