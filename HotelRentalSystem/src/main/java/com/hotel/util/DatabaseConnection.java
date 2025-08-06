package com.hotel.util;

import java.sql.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Database connection utility class for Oracle database connectivity
 */
public class DatabaseConnection {
    private static final String CONFIG_FILE = "config/database.properties";
    private static String DB_URL;
    private static String DB_USERNAME;
    private static String DB_PASSWORD;
    private static String DB_DRIVER = "oracle.jdbc.driver.OracleDriver";
    
    // Connection pool settings
    private static final int MAX_CONNECTIONS = 10;
    private static Connection[] connectionPool = new Connection[MAX_CONNECTIONS];
    private static boolean[] connectionInUse = new boolean[MAX_CONNECTIONS];
    
    static {
        loadDatabaseConfig();
        initializeConnectionPool();
    }
    
    /**
     * Load database configuration from properties file
     */
    private static void loadDatabaseConfig() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            DB_URL = props.getProperty("db.url", "jdbc:oracle:thin:@localhost:1521:XE");
            DB_USERNAME = props.getProperty("db.username", "hotel_admin");
            DB_PASSWORD = props.getProperty("db.password", "password");
            DB_DRIVER = props.getProperty("db.driver", "oracle.jdbc.driver.OracleDriver");
        } catch (IOException e) {
            // Use default values if config file not found
            System.out.println("Database config file not found, using default values");
            DB_URL = "jdbc:oracle:thin:@localhost:1521:XE";
            DB_USERNAME = "hotel_admin";
            DB_PASSWORD = "password";
        }
    }
    
    /**
     * Initialize connection pool
     */
    private static void initializeConnectionPool() {
        try {
            Class.forName(DB_DRIVER);
            for (int i = 0; i < MAX_CONNECTIONS; i++) {
                connectionPool[i] = createNewConnection();
                connectionInUse[i] = false;
            }
            System.out.println("Database connection pool initialized successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("Oracle JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error initializing connection pool: " + e.getMessage());
        }
    }
    
    /**
     * Create a new database connection
     */
    private static Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }
    
    /**
     * Get a connection from the pool
     */
    public static synchronized Connection getConnection() throws SQLException {
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            if (!connectionInUse[i]) {
                // Check if connection is still valid
                if (connectionPool[i] == null || connectionPool[i].isClosed()) {
                    connectionPool[i] = createNewConnection();
                }
                connectionInUse[i] = true;
                return connectionPool[i];
            }
        }
        // If no connection available, create a new one
        return createNewConnection();
    }
    
    /**
     * Return connection to the pool
     */
    public static synchronized void releaseConnection(Connection connection) {
        if (connection == null) return;
        
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            if (connectionPool[i] == connection) {
                connectionInUse[i] = false;
                return;
            }
        }
        
        // If connection not from pool, close it
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
    
    /**
     * Close all connections in the pool
     */
    public static void closeAllConnections() {
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            if (connectionPool[i] != null) {
                try {
                    connectionPool[i].close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Test database connection
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                // Test with a simple query
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT 1 FROM DUAL")) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Execute a stored procedure with parameters
     */
    public static CallableStatement prepareCall(Connection conn, String sql) throws SQLException {
        return conn.prepareCall(sql);
    }
    
    /**
     * Execute a prepared statement with parameters
     */
    public static PreparedStatement prepareStatement(Connection conn, String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }
    
    /**
     * Safely close database resources
     */
    public static void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("Error closing ResultSet: " + e.getMessage());
            }
        }
        
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("Error closing Statement: " + e.getMessage());
            }
        }
        
        if (conn != null) {
            releaseConnection(conn);
        }
    }
    
    /**
     * Safely close database resources (overloaded method)
     */
    public static void closeResources(Connection conn, Statement stmt) {
        closeResources(conn, stmt, null);
    }
    
    /**
     * Get database metadata information
     */
    public static void printDatabaseInfo() {
        Connection conn = null;
        try {
            conn = getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            System.out.println("Database Product Name: " + metaData.getDatabaseProductName());
            System.out.println("Database Product Version: " + metaData.getDatabaseProductVersion());
            System.out.println("Driver Name: " + metaData.getDriverName());
            System.out.println("Driver Version: " + metaData.getDriverVersion());
            System.out.println("URL: " + metaData.getURL());
            System.out.println("Username: " + metaData.getUserName());
        } catch (SQLException e) {
            System.err.println("Error getting database info: " + e.getMessage());
        } finally {
            releaseConnection(conn);
        }
    }
}

