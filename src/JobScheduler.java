import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class JobScheduler extends ThreadPoolExecutor {
    private ArrayList<UploadRequest> requestList;
    private RequestRecorder recorder;

    public JobScheduler(int i, int i1, long l, TimeUnit timeUnit, BlockingQueue<Runnable> runnables) {
        super(i, i1, l, timeUnit, runnables);
        this.requestList = new ArrayList<UploadRequest>();
    }

    public JobScheduler() {
        this(1, 1, 5, TimeUnit.MINUTES, new LinkedBlockingDeque<Runnable>());
    }

    public void schedule(UploadRequest request) {
        recorder = new RequestRecorder();
        recorder.recordRequest(request);
        request.inQueue();
        synchronized (requestList) {
            requestList.add(request);
        }
        ArrayList<Job> jobs = new ArrayList<Job>();
        List<File> assets = request.assets();
        for (File asset : assets) {
            jobs.add(new Job(asset, request));
        }
        recorder.recordAsset(request);
        recorder.recordProgress(request);
        if (jobs.size() == 0) {
            request.completed();
            recorder.recordCompleted(request);
        }

        for (Job job : jobs) {
            execute(job);
        }
    }

    @Override
    protected void beforeExecute(Thread thread, Runnable runnable) {
        super.beforeExecute(thread, runnable);
        Job job = (Job) runnable;
        job.getRequest().start();
    }

    @Override
    protected void afterExecute(Runnable runnable, Throwable throwable) {
        super.afterExecute(runnable, throwable);
        Job job = (Job) runnable;
        UploadRequest request = job.getRequest();
        if (!request.hasException()) {
            recorder.removeAsset(request,job.getAsset().getAbsolutePath());
            request.processed();
            recorder.recordProgress(request);
            if (recorder.hasFinished(request)) {
                request.completed();
                recorder.recordCompleted(request);
            }
        }else{
            request.failed(job);
            recorder.recordFailed(request);
        }
    }

    public ArrayList<UploadRequest> getRequestListInQueue(){
        ArrayList<UploadRequest> inQueue = new ArrayList<UploadRequest>();
        for (UploadRequest request : requestList) {
            if(request.status()==RequestStatus.IN_QUEUE){
                inQueue.add(request);
            }
        }
        return inQueue;
    }

    public ArrayList<UploadRequest> getRequestListInProgress(){
        ArrayList<UploadRequest> inProgress = new ArrayList<UploadRequest>();
        for (UploadRequest request : requestList) {
            if(request.status()==RequestStatus.IN_PROGRESS){
                inProgress.add(request);
            }
        }
        return inProgress;
    }

    public ArrayList<UploadRequest> getRequestListCompleted(){
        ArrayList<UploadRequest> completed = new ArrayList<UploadRequest>();
        for (UploadRequest request : requestList) {
            if(request.status()==RequestStatus.COMPLETED){
                completed.add(request);
            }
        }
        return completed;
    }

    public void resumeQueue(){

    }

}
