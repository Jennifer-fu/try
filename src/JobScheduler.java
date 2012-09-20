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
    private static JobScheduler scheduler = new JobScheduler();

    private JobScheduler(int i, int i1, long l, TimeUnit timeUnit, BlockingQueue<Runnable> runnables) {
        super(i, i1, l, timeUnit, runnables);
        this.requestList = new ArrayList<UploadRequest>();
        recorder = new RequestRecorder();
    }

    private JobScheduler() {
        this(8, 10, 5, TimeUnit.MINUTES, new LinkedBlockingDeque<Runnable>());
    }

    public static JobScheduler getInstance() {
        return scheduler;
    }

    public void schedule(UploadRequest request) {
        recorder.recordRequest(request);
        request.inQueue();
        process(request);
    }

    private void process(UploadRequest request) {
        synchronized (requestList) {
            requestList.add(request);
        }
        List<File> assets = request.assets();
        recorder.recordAsset(request);
        recorder.recordProgress(request);
        if (assets.size() == 0) {
            request.completed();
            recorder.recordCompleted(request);
        }
        for (File asset : assets) {
            execute(new Job(asset, request));
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
            recorder.removeAsset(request, job.getAsset().getAbsolutePath());
            request.processed();
            recorder.recordProgress(request);
            if (recorder.hasFinished(request)) {
                request.completed();
                recorder.recordCompleted(request);
            }
        } else {
            request.failed(job);
            recorder.recordFailed(request);
        }
    }

    public ArrayList<UploadRequest> getRequestListInQueue() {
        ArrayList<UploadRequest> inQueue = new ArrayList<UploadRequest>();
        for (UploadRequest request : requestList) {
            if (request.status() == RequestStatus.IN_QUEUE) {
                inQueue.add(request);
            }
        }
        return inQueue;
    }

    public ArrayList<UploadRequest> getRequestListInProgress() {
        ArrayList<UploadRequest> inProgress = new ArrayList<UploadRequest>();
        for (UploadRequest request : requestList) {
            if (request.status() == RequestStatus.IN_PROGRESS) {
                inProgress.add(request);
            }
        }
        return inProgress;
    }

    public ArrayList<UploadRequest> getRequestListCompleted() {
        ArrayList<UploadRequest> completed = new ArrayList<UploadRequest>();
        for (UploadRequest request : requestList) {
            if (request.status() == RequestStatus.COMPLETED) {
                completed.add(request);
            }
        }
        return completed;
    }

    public void resumeQueue() {
        ArrayList<UploadRequest> requests = recorder.getRequestsInQueue();
        for (UploadRequest request : requests) {
            process(request);
        }
    }
}
