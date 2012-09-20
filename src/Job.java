import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Job implements Runnable {
    private UploadRequest request;
    private File asset;
    private Throwable exception;

    public Job(File asset, UploadRequest uploadRequest) {
        this.asset = asset;
        this.request = uploadRequest;
    }

    public UploadRequest getRequest(){
        return request;
    }

    @Override
    public void run() {
        System.out.println("run - "+asset.getAbsolutePath()+" "+Thread.currentThread().getName());
        if(request.status() == RequestStatus.FAILED)return;
        String absolutePath = asset.getAbsolutePath();
        String relativePath = absolutePath.substring(request.getSource().length(), absolutePath.lastIndexOf("/"));
        try {
            new AssetUploader().upload(asset, request.getDestination() + "/" + relativePath);
        } catch (Throwable e) {
            this.exception = e;
        }
    }

    public File getAsset() {
        return asset;
    }

    public Throwable getException() {
        return exception;
    }
}
