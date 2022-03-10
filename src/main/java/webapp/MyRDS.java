package webapp;

import java.sql.*;
import java.util.Properties;

public class MyRDS {
    static private Connection connection;
    static private Connection replicaConnection;

    private static final String KEY_STORE_FILE_PATH = "/home/ubuntu/clientkeystore.jks";
    private static final String KEY_STORE_PASS = "csye6225";

    private MyRDS() {
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null) {
            String dbName = System.getenv("RDS_DB_NAME");
            String userName = System.getenv("RDS_USERNAME");
            String password = System.getenv("RDS_PASSWORD");
            String hostname = System.getenv("RDS_HOSTNAME");
            String port = System.getenv("RDS_PORT");

            //可以通过设置System的property指定使用的jks证书ssl连接数据库，但是会导致MyS3中的s3存储桶client无法认证(证书不同)，故不能使用
            //转为使用properties.setProperty方法，设置trustCertificateKeyStoreUrl和trustCertificateKeyStorePassword即可
            //注意File Path前要加上"file:"，很坑
            //System.setProperty("javax.net.ssl.trustStore", KEY_STORE_FILE_PATH);
            //System.setProperty("javax.net.ssl.trustStorePassword", KEY_STORE_PASS);
            Properties properties = new Properties();
            properties.setProperty("sslMode", "VERIFY_IDENTITY");
            properties.setProperty("trustCertificateKeyStoreUrl", "file:" + KEY_STORE_FILE_PATH);
            properties.setProperty("trustCertificateKeyStorePassword", KEY_STORE_PASS);
            properties.put("user", userName);
            properties.put("password", password);

            String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName;
            connection = DriverManager.getConnection(jdbcUrl, properties);

            Statement stat = connection.createStatement();
            ResultSet rs = stat.executeQuery("show tables in " + dbName + " where Tables_in_" + dbName + " = 'User'");
            if (!rs.next()) {
                stat.executeUpdate("CREATE TABLE User("
                        + "id varchar(64) NOT NULL COMMENT 'id',"
                        + "first_name varchar(50) DEFAULT NULL COMMENT 'first name',"
                        + "last_name varchar(50) DEFAULT NULL COMMENT 'last name',"
                        + "password varchar(256) DEFAULT NULL COMMENT 'password',"
                        + "salt varchar(256) DEFAULT NULL COMMENT 'salt',"
                        + "user_name varchar(64) DEFAULT NULL COMMENT 'user_name',"
                        + "verified tinyint DEFAULT 0 COMMENT 'verified',"
                        + "account_created varchar(64) DEFAULT NULL COMMENT 'account_created',"
                        + "account_updated varchar(64) DEFAULT NULL COMMENT 'account_updated',"
                        + "verified_on varchar(64) DEFAULT NULL COMMENT 'verified_on',"
                        + "PRIMARY KEY (id)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='user';"
                );
            }
            rs = stat.executeQuery("show tables in " + dbName + " where Tables_in_" + dbName + " = 'Profile'");
            if (!rs.next()) {
                stat.executeUpdate("CREATE TABLE Profile("
                        + "id varchar(64) NOT NULL COMMENT 'id',"
                        + "file_name varchar(128) DEFAULT NULL COMMENT 'file name',"
                        + "url varchar(128) DEFAULT NULL COMMENT 'url',"
                        + "upload_date varchar(64) DEFAULT NULL COMMENT 'upload_date',"
                        + "user_id varchar(64) NOT NULL COMMENT 'user id',"
                        + "PRIMARY KEY (id)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='profile';"
                );
            }
        }
        return connection;
    }

    public static Connection getReplicaConnection() throws SQLException {
        if (connection == null) {
            getConnection();
        }
        if (replicaConnection == null) {
            String dbName = System.getenv("RDS_DB_NAME");
            String userName = System.getenv("RDS_USERNAME");
            String password = System.getenv("RDS_PASSWORD");
            String hostname = System.getenv("RDS_REPLICA_HOSTNAME");
            String port = System.getenv("RDS_PORT");
            String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + password;
            replicaConnection = DriverManager.getConnection(jdbcUrl);
        }
        return replicaConnection;
    }
}
