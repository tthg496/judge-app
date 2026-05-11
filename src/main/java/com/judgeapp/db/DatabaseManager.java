package com.judgeapp.db;

import java.sql.*;

public class DatabaseManager {
    private static final Object LOCK = new Object();
    private static Connection conn;

    private static String env(String key) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private static String databaseName() {
        String db = env("SQLSERVER_DB");
        return db == null ? "JudgeApp" : db;
    }

    // SQL Server connection:
    private static String sqlServerUrl(String db, boolean hasSqlLogin) {
        String host = env("SQLSERVER_HOST");
        if (host == null) host = "localhost";
        String instance = env("SQLSERVER_INSTANCE");
        String port = env("SQLSERVER_PORT");
        if (port == null) port = "1433";
        String encrypt = env("SQLSERVER_ENCRYPT");
        if (encrypt == null) encrypt = "false";

        StringBuilder url = new StringBuilder("jdbc:sqlserver://").append(host);
        if (instance != null) {
            url.append(";instanceName=").append(instance);
        } else if (port != null) {
            url.append(":").append(port);
        }
        url.append(";databaseName=").append(db)
            .append(";encrypt=").append(encrypt)
            .append(";trustServerCertificate=true;");

        if (!hasSqlLogin) {
            // Sử dụng Windows Authentication (Integrated Security)
            url.append("integratedSecurity=true;");
        }
        System.out.println("⏳ Đang thử kết nối tới: " + url);
        return url.toString();
    }

    private static Connection connect(String url, String user, String password, boolean hasSqlLogin) throws SQLException {
        return hasSqlLogin
            ? DriverManager.getConnection(url, user, password)
            : DriverManager.getConnection(url);
    }

    private static boolean isMissingDatabase(SQLException e) {
        String message = e.getMessage();
        return e.getErrorCode() == 4060
            || (message != null && message.contains("Cannot open database"));
    }

    private static void createDatabase(String db, String user, String password, boolean hasSqlLogin) throws SQLException {
        String dbLiteral = db.replace("'", "''");
        String dbIdentifier = db.replace("]", "]]");
        try (Connection master = connect(sqlServerUrl("master", hasSqlLogin), user, password, hasSqlLogin);
             Statement stmt = master.createStatement()) {
            stmt.execute("IF DB_ID(N'" + dbLiteral + "') IS NULL CREATE DATABASE [" + dbIdentifier + "]");
        }
        System.out.println("✅ Database created or already exists: " + db);
    }

    private static Connection connectToConfiguredDatabase(String user, String password, boolean hasSqlLogin)
            throws SQLException {
        String db = databaseName();
        try {
            return connect(sqlServerUrl(db, hasSqlLogin), user, password, hasSqlLogin);
        } catch (SQLException e) {
            if (!isMissingDatabase(e)) {
                throw e;
            }
            createDatabase(db, user, password, hasSqlLogin);
            return connect(sqlServerUrl(db, hasSqlLogin), user, password, hasSqlLogin);
        }
    }

    public static Connection getConnection() throws SQLException {
        synchronized (LOCK) {
            if (conn == null || conn.isClosed()) {
                // Sử dụng tài khoản SA với SQL Server Authentication
                String user = env("SQLSERVER_USER");
                String password = env("SQLSERVER_PASSWORD");
                
                if (user == null) user = "sa";
                if (password == null) password = "123456";
                
                try {
                    System.out.println("🔐 Đang kết nối với tài khoản: " + user);
                    conn = connectToConfiguredDatabase(user, password, true);
                } catch (SQLException e) {
                    System.err.println("======================================================");
                    System.err.println("❌ LỖI KẾT NỐI CÓ SỬ DỤNG TÀI KHOẢN SA ❌");
                    System.err.println("Vui lòng kiểm tra:");
                    System.err.println("1. SQL Server đang chạy và có đang lắng nghe trên localhost:1433");
                    System.err.println("2. Tài khoản 'sa' và mật khẩu là đúng");
                    System.err.println("3. Biến môi trường SQLSERVER_USER và SQLSERVER_PASSWORD (nếu cần thay đổi)");
                    System.err.println("======================================================");
                    throw new SQLException("Lỗi kết nối DB: " + e.getMessage(), e);
                }
            }
            return conn;
        }
    }

    public static void initDatabase() throws SQLException {
        Connection c = getConnection();
        try (Statement stmt = c.createStatement()) {
            // SQL Server: use IF OBJECT_ID(...) to mimic CREATE TABLE IF NOT EXISTS.
            stmt.execute("""
                IF OBJECT_ID('problems', 'U') IS NULL
                BEGIN
                    CREATE TABLE problems (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        title NVARCHAR(255) NOT NULL,
                        content NVARCHAR(MAX) NULL,
                        time_limit FLOAT NOT NULL DEFAULT(1.0),
                        memory_limit INT NOT NULL DEFAULT(256)
                    )
                END
            """);

            stmt.execute("""
                IF OBJECT_ID('testcases', 'U') IS NULL
                BEGIN
                    CREATE TABLE testcases (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        problem_id INT NOT NULL,
                        input NVARCHAR(MAX) NULL,
                        expected_output NVARCHAR(MAX) NULL,
                        is_sample BIT NOT NULL DEFAULT(0),
                        CONSTRAINT FK_testcases_problems
                            FOREIGN KEY(problem_id) REFERENCES problems(id)
                    )
                END
            """);

            stmt.execute("""
                IF OBJECT_ID('submissions', 'U') IS NULL
                BEGIN
                    CREATE TABLE submissions (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        problem_id INT NOT NULL,
                        code NVARCHAR(MAX) NULL,
                        language NVARCHAR(50) NULL,
                        verdict NVARCHAR(20) NULL,
                        runtime FLOAT NULL,
                        submitted_at DATETIME NOT NULL DEFAULT(GETDATE())
                    )
                END
            """);
        }
        System.out.println("✅ SQL Server database initialized!");
    }
}
