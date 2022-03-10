package webapp;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
public class UserDAO {
    private static final StatsDClient statsd = new NonBlockingStatsDClient("statsd", "localhost", 8125);

    public static void addUser(User user) throws SQLException {
        Connection connection = MyRDS.getConnection();
        String sql = "INSERT INTO User (id, first_name, last_name, password, salt, user_name, account_created, account_updated, verified, verified_on) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStmt = connection.prepareStatement(sql);
        preparedStmt.setString(1, user.getId());
        preparedStmt.setString(2, user.getFirst_name());
        preparedStmt.setString(3, user.getLast_name());
        preparedStmt.setString(4, user.getPassword());
        preparedStmt.setString(5, user.getSalt());
        preparedStmt.setString(6, user.getUsername());
        preparedStmt.setString(7, user.getAccount_created());
        preparedStmt.setString(8, user.getAccount_updated());
        preparedStmt.setInt(9, user.getVerified() ? 1 : 0);
        preparedStmt.setString(10, user.getVerified_on());
        long startTime = System.currentTimeMillis();
        preparedStmt.execute();
        statsd.recordExecutionTimeToNow("rds.insert.timer", startTime);
    }

    public static void updateUser(User user) throws SQLException {
        Connection connection = MyRDS.getConnection();
        String sql = "UPDATE User set password = ?, first_name = ?, last_name = ?, account_updated = ? where user_name = ?";
        PreparedStatement preparedStmt = connection.prepareStatement(sql);
        preparedStmt.setString(1, user.getPassword());
        preparedStmt.setString(2, user.getFirst_name());
        preparedStmt.setString(3, user.getLast_name());
        preparedStmt.setString(4, user.getAccount_updated());
        preparedStmt.setString(5, user.getUsername());
        long startTime = System.currentTimeMillis();
        preparedStmt.execute();
        statsd.recordExecutionTimeToNow("rds.update.timer", startTime);
    }

    public static void verifyUser(String id, String verifiedOn) throws SQLException {
        Connection connection = MyRDS.getConnection();
        String sql = "UPDATE User set verified = 1, verified_on = ? where id = ?";
        PreparedStatement preparedStmt = connection.prepareStatement(sql);
        preparedStmt.setString(1, verifiedOn);
        preparedStmt.setString(2, id);
        preparedStmt.execute();
    }

    public static User getUser(String userName, String password) throws SQLException {
        User user = getUser(userName);
        if (user != null && user.getPassword().equals(BCrypt.hashpw(password, user.getSalt())))
            return user;
        return null;
    }

    public static User getUser(String userName) throws SQLException {
        Connection connection = MyRDS.getReplicaConnection();
        String sql = "SELECT * FROM User where user_name = ? and verified = 1";
        PreparedStatement preparedStmt = connection.prepareStatement(sql);
        preparedStmt.setString(1, userName);
        long startTime = System.currentTimeMillis();
        ResultSet rs = preparedStmt.executeQuery();
        statsd.recordExecutionTimeToNow("rds.select.timer", startTime);
        if (rs.next()) {
            String id = rs.getString("id");
            String first_name = rs.getString("first_name");
            String last_name = rs.getString("last_name");
            String password = rs.getString("password");
            String salt = rs.getString("salt");
            String user_name = rs.getString("user_name");
            String account_created = rs.getString("account_created");
            String account_updated = rs.getString("account_updated");
            boolean verified = rs.getInt("verified") == 1;
            String verified_on = rs.getString("verified_on");
            return new User(id, user_name, password, salt, first_name, last_name, account_created, account_updated, true, verified, verified_on);
        }
        return null;
    }
}
