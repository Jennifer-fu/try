import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class MonitorServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JobScheduler scheduler = (JobScheduler)request.getServletContext().getAttribute("scheduler");
        ArrayList<UploadRequest> inQueue = scheduler.getRequestListInQueue();
        ArrayList<UploadRequest> inProgress = scheduler.getRequestListInProgress();
        ArrayList<UploadRequest> completed = scheduler.getRequestListCompleted();

        response.getWriter().write("<html><head></head><body><h2>In Queue</h2>"+renderQueue(inQueue)+"<h2>In Progress</h2>"+ renderInProgress(inProgress)+"<h2>Completed</h2>" +renderCompleted(completed)+"</body></html>");
    }

    private String renderCompleted(ArrayList<UploadRequest> completed) {
        String html = "<table cellpadding='3' cellspacing='0' border='1'><thead><tr><td>From</td><td>To</td><td>Status</td><td>Submit At</td><td>Completed At</td><td>Elapsed</td><td>Message</td></tr></thead><tbody>";
        for (UploadRequest request : completed) {
            Timestamp createdTime = request.getCreatedTime();
            Timestamp completedTime = request.getCompletedTime();
            long elapsed = (completedTime.getTime() - createdTime.getTime())/1000;
            html += "<tr><td>"+request.getSource()+"</td><td>"+request.getDestination()+"</td><td>"+request.status()+"</td><td>"+ createdTime +"</td><td>"+ completedTime +"</td><td>"+Math.round(elapsed / 60)+" mins</td><td>"+request.getMessage()+"</td></tr>";
        }
        html += "</tbody></table>";
        return html;
    }

    private String renderInProgress(ArrayList<UploadRequest> inProgress) {
        String html = "<table cellpadding='3' cellspacing='0' border='1'><thead><tr><td>From</td><td>To</td><td>Percentage</td></tr></thead><tbody>";
        for (UploadRequest request : inProgress) {
            html += "<tr><td>"+request.getSource()+"</td><td>"+request.getDestination()+"</td><td>"+request.getProgress()+"%</td></tr>";
        }
        html += "</tbody></table>";
        return html;

    }

    private String renderQueue(ArrayList<UploadRequest> inQueue) {
        String html = "<table cellpadding='3' cellspacing='0' border='1'><thead><tr><td>From</td><td>To</td></tr></thead><tbody>";
        for (UploadRequest request : inQueue) {
            html += "<tr><td>"+request.getSource()+"</td><td>"+request.getDestination()+"</td></tr>";
        }
        html += "</tbody></table>";
        return html;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
