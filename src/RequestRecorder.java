import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RequestRecorder {
    private final JdbcConnectionPool connectionPool;

    public RequestRecorder() {
        JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setURL("jdbc:h2:tcp://localhost/~/test;AUTO_SERVER=TRUE");
        jdbcDataSource.setUser("sa");
        jdbcDataSource.setPassword("");
        connectionPool = JdbcConnectionPool.create(jdbcDataSource);
    }

    public void recordRequest(UploadRequest request) {
        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.prepareStatement("insert into request(id, created_time, status) values(?,?,?)");
            statement.setString(1, request.id());
            statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            statement.setString(3, RequestStatus.IN_QUEUE.name());
            statement.execute();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void recordAsset(UploadRequest request) {
        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.prepareStatement("insert into asset(request_id, path) values(?,?)");
            for (File asset : request.assets()) {
                statement.setString(1, request.id());
                statement.setString(2, asset.getAbsolutePath());
                statement.execute();
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recordProgress(UploadRequest request) {
        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.prepareStatement("update request set completed = ?, total = ? where id = ?");
            statement.setInt(1, request.getCompleted());
            statement.setInt(2, request.getTotal());
            statement.setString(3, request.id());
            statement.execute();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recordCompleted(UploadRequest request) {
        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.prepareStatement("update request set completed_time = ?, status = ?, message = ? where id = ?");
            statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            statement.setString(2, RequestStatus.COMPLETED.name());
            statement.setString(3, request.getMessage());
            statement.setString(4, request.id());
            statement.execute();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeAsset(UploadRequest request, String path) {
        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.prepareStatement("delete from asset where request_id = ? and path = ?");
            statement.setString(1, request.id());
            statement.setString(2, path);
            statement.execute();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recordFailed(UploadRequest request) {
        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.prepareStatement("update request set completedTime = ?, status = ?, message = ?, failedAt = ? where id = ?");
            statement.setDate(1, (Date) Calendar.getInstance().getTime());
            statement.setString(2, "failed");
            statement.setString(3, request.getMessage());
            statement.setString(4, request.getFailedAt());
            statement.execute();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasFinished(UploadRequest request) {
        int count = -1;
        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.prepareStatement("select count(1) as count from asset where request_id = ?");
            statement.setString(1, request.id());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                count = resultSet.getInt("count");
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count == 0;
    }

    public ArrayList<UploadRequest> getRequestsInQueue() {
        ArrayList<UploadRequest> requests = new ArrayList<UploadRequest>();
        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement statement = connection.prepareStatement("select * from request where status = ? or status = ?");
            statement.setString(1, RequestStatus.IN_QUEUE.name());
            statement.setString(2, RequestStatus.IN_PROGRESS.name());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                UploadRequest request = buildRequest(resultSet);
                statement = connection.prepareStatement("select * from asset where request_id = ?");
                statement.setString(1, request.id());
                ResultSet assetsSet = statement.executeQuery();
                while(assetsSet.next()){
                    request.addAsset(buildAsset(assetsSet));
                }
                requests.add(request);
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    private File buildAsset(ResultSet assetsSet) {
        try {
            return new File(assetsSet.getString("path"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    private UploadRequest buildRequest(ResultSet resultSet) {
        try {
            String id = resultSet.getString("id");
            UploadRequest request = new UploadRequest(id);
            request.setTotal(resultSet.getInt("total"));
            request.setCompleted(resultSet.getInt("completed"));
            request.setStatus(resultSet.getString("status"));
            request.setFailedAt(resultSet.getString("failed_at"));
            request.setMessage(resultSet.getString("message"));
            request.setCreatedTime(resultSet.getTimestamp("created_time"));
            request.setCompletedTime(resultSet.getTimestamp("completed_time"));
            return request;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
