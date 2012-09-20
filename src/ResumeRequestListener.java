import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionBindingEvent;

public class ResumeRequestListener implements ServletContextListener {

    public ResumeRequestListener() {
    }

    public void contextInitialized(ServletContextEvent context) {
        JobScheduler scheduler = JobScheduler.getInstance();
        context.getServletContext().setAttribute("scheduler",scheduler);
        scheduler.resumeQueue();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }


}
