import java.io.File;
import java.io.FileFilter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UploadRequest {

    private File source;
    private File destination;
    private String id;
    private int completed, total;
    private String message;
    private RequestStatus status;
    private Exception exception;
    private String failedAt;
    private Timestamp createdTime;
    private Timestamp completedTime;
    private List<File> assets;


    public UploadRequest(String source, String destination) {
        this.source = new File(source);
        this.destination = new File(destination);
        this.id = generateId(source, destination);
        this.assets = new ArrayList<File>();
    }

    public UploadRequest(String id) {
        String[] dirs = id.split("_");
        this.source = new File(dirs[0]);
        this.destination = new File(dirs[1]);
        this.id = id;
        this.assets = new ArrayList<File>();
    }

    private String generateId(String source, String destination) {
        return source.concat("_").concat(destination).concat("_" + System.currentTimeMillis());
    }

    public String id() {
        return id;
    }


    public void validate() throws RequestException {
        if (!source.exists())
            throw new RequestException("source folder not exist: " + source);
    }

    public void synchronizeFolderStructure() {
        ensureFolder(destination);
        List<File> directories = new ArrayList<File>();
        GetFolders(source, directories);
        for (File directory : directories) {
            String relativePath = directory.getAbsolutePath().substring(source.getAbsolutePath().length());
            ensureFolder(destination, relativePath);
        }

    }

    public List<File> assets() {
        if (assets.size() > 0) return assets;
        ArrayList<File> files = new ArrayList<File>();
        GetFiles(source, files);
        total = files.size();
        return files;
    }

    private void GetFiles(File src, ArrayList<File> files) {
        File[] dirs = src.listFiles(directoryFilter);
        files.addAll(Arrays.asList(src.listFiles(fileFilter)));
        for (File dir : dirs) {
            GetFiles(dir, files);
        }
    }

    private void ensureFolder(File parentFolder, String relativePath) {
        File dir = new File(parentFolder.getAbsolutePath().concat("/").concat(relativePath));
        if (!dir.exists())
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

    private void ensureFolder(File folder) {
        if (!folder.exists())
            folder.mkdirs();
    }

    FileFilter fileFilter = new FileFilter() {
        public boolean accept(File file) {
            return file.isFile() && file.getName().endsWith(".jpg");
        }
    };

    FileFilter directoryFilter = new FileFilter() {
        public boolean accept(File file) {
            return file.isDirectory();
        }
    };


    public void inQueue() {
        status = RequestStatus.IN_QUEUE;
        createdTime = new Timestamp(System.currentTimeMillis());
    }

    public void completed() {
        status = RequestStatus.COMPLETED;
        completedTime = new Timestamp(System.currentTimeMillis());
    }

    public RequestStatus status() {
        return status;
    }

    public int getCompleted() {
        return completed;
    }

    public int getTotal() {
        return total;
    }

    public double getProgress() {
        return ((double) completed / (double) total) * 100;
    }

    public String getMessage() {
        return message;
    }

    public synchronized void start() {
        if (status == RequestStatus.IN_QUEUE)
            status = RequestStatus.IN_PROGRESS;
    }

    public boolean hasException() {
        return exception != null;
    }

    public void processed() {
        completed++;
    }

    public void failed(Job job) {
        status = RequestStatus.FAILED;
        failedAt = job.getAsset().getAbsolutePath();
        message += job.getException().getMessage();
    }

    public String getFailedAt() {
        return failedAt;
    }

    public String getDestination() {
        return destination.getAbsolutePath();
    }

    public String getSource() {
        return source.getAbsolutePath();
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public Timestamp getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(Timestamp completedTime) {
        this.completedTime = completedTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFailedAt(String failedAt) {
        this.failedAt = failedAt;
    }

    public void setStatus(String status) {
        this.status = RequestStatus.valueOf(status);
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void addAsset(File file) {
        assets.add(file);
    }
}
