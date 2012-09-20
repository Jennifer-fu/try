import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UploadServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String source = request.getParameter("source");
        String destination = request.getParameter("destination");
        UploadRequest uploadRequest = new UploadRequest(source, destination);
        try {
            uploadRequest.validate();
            uploadRequest.synchronizeFolderStructure();
        } catch (Exception e) {
            response.setStatus(400);
            response.getWriter().write(e.getMessage());
        }

        JobScheduler scheduler = JobScheduler.getInstance();
        if(request.getServletContext().getAttribute("scheduler")==null){
            request.getServletContext().setAttribute("scheduler", scheduler);
        }
        scheduler.schedule(uploadRequest);

    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }
}
