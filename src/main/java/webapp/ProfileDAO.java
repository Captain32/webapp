package webapp;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

public class ProfileDAO {
    private static final MyS3 myS3 = new MyS3();
    private static final StatsDClient statsd = new NonBlockingStatsDClient("statsd", "localhost", 8125);

    public static void addProfile(Profile profile, InputStream profile_pic) throws SQLException, IOException {
        Connection connection = MyRDS.getConnection();
        String sql = "INSERT INTO Profile (id, file_name, url, upload_date, user_id) "
                + "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement preparedStmt = connection.prepareStatement(sql);
        preparedStmt.setString(1, profile.getId());
        preparedStmt.setString(2, profile.getFile_name());
        preparedStmt.setString(3, profile.getUrl());
        preparedStmt.setString(4, profile.getUpload_date());
        preparedStmt.setString(5, profile.getUser_id());
        long startTime = System.currentTimeMillis();
        preparedStmt.execute();
        statsd.recordExecutionTimeToNow("rds.insert.timer", startTime);
        myS3.PutObject(profile.getUrl(), profile_pic);
    }

    public static Profile getProfile(String user_id) throws SQLException {
        Connection connection = MyRDS.getReplicaConnection();
        String sql = "SELECT * FROM Profile where user_id = ?";
        PreparedStatement preparedStmt = connection.prepareStatement(sql);
        preparedStmt.setString(1, user_id);
        long startTime = System.currentTimeMillis();
        ResultSet rs = preparedStmt.executeQuery();
        statsd.recordExecutionTimeToNow("rds.select.timer", startTime);
        if (rs.next()) {
            String id = rs.getString("id");
            String file_name = rs.getString("file_name");
            String url = rs.getString("url");
            String upload_date = rs.getString("upload_date");
            return new Profile(id, file_name, url, upload_date, user_id);
        }
        return null;
    }

    public static Profile deleteProfile(String user_id) throws SQLException {
        Profile profile = getProfile(user_id);
        if (profile != null) {
            Connection connection = MyRDS.getConnection();
            String sql = "DELETE FROM Profile WHERE user_id = ?";
            PreparedStatement preparedStmt = connection.prepareStatement(sql);
            preparedStmt.setString(1, profile.getUser_id());
            long startTime = System.currentTimeMillis();
            preparedStmt.execute();
            statsd.recordExecutionTimeToNow("rds.delete.timer", startTime);
            myS3.DeleteObject(profile.getUrl());
            return profile;
        }
        return null;
    }
}
