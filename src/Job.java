import javax.swing.*;
import java.io.File;

public class Job implements Runnable {
    private UploadRequest request;
    private File asset;
    private Exception exception;

    public Job(File asset, UploadRequest uploadRequest) {
        this.asset = asset;
        this.request = uploadRequest;
    }

    public UploadRequest getRequest(){
        return request;
    }

    @Override
    public void run() {
        System.out.println("run");
    }

    public File getAsset() {
        return asset;
    }

    public Exception getException() {
        return exception;
    }
}
