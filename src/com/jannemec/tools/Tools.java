/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jannemec.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author u935
 */
public class Tools {
    
    final static public boolean isJar(Class cl) {
        final File jarFile = new File(cl.getProtectionDomain().getCodeSource().getLocation().getPath());
        return(jarFile.isFile());
    }
    
    public String getJarPath() throws UnsupportedEncodingException {
        File currentDir = new File("");
        //System.out.println("Working Directory : " + currentDir.getAbsoluteFile());
        return(currentDir.getAbsoluteFile().getAbsolutePath());
    }
    
    /*protected String a() {
        String path = ClassLoader.getSystemClassLoader().getResource(".").getPath();
        path =  (new File(path)).getAbsolutePath();
        String decodedPath = URLDecoder.decode(path, "UTF-8");
        return(decodedPath);
    }*/
    
    public static InputStream getPropertyFile(Class cl, String path) throws IOException {
        final File jarFile = new File(cl.getProtectionDomain().getCodeSource().getLocation().getPath());
        InputStream in = null;
        if(jarFile.isFile()) {  // Run with JAR file
            URL url = ClassLoader.getSystemResource(path);
            if (url != null) {
                in = url.openStream();
                //in = cl.getClassLoader().getResourceAsStream(path);
            }
        }
        if (in == null) { // not found or IDE
            in = new FileInputStream(cl.getResource("/").getPath() + path);  
        }
        return(in);
    }

    Connection conn = null;
    public void dbfConnect(String connectionString) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        // create a connection to the database
        Class.forName("org.sqlite.JDBC");
        //Class.forName("SQLite.JDBCDriver").newInstance();
        this.conn = DriverManager.getConnection(connectionString);
        System.out.println("Connection to SQLite has been established.");
    }
    
    public void dbfCreateTable() throws SQLException {
        if (this.conn != null) {
            String sqlCreate = "CREATE TABLE IF NOT EXISTS datalog"
            + "  (id              INTEGER PRIMARY KEY AUTOINCREMENT  NOT NULL,"
            + "   dttm            DATETIME                           NOT NULL,"
            + "   name            VARCHAR(15)                        NOT NULL,"
            + "   valdouble       REAL,"
            + "   valstring       VARCHAR(20),"
            + "   valint          INT,"
            + "   valother        TEXT)";

            Statement stmt = this.conn.createStatement();
            stmt.execute(sqlCreate);
        } else {
            throw(new SQLException("Connection not estabilished"));
        }
    }
    
    public void dbfKlimaCreateTable() throws SQLException {
        if (this.conn != null) {
            String sqlCreate = "CREATE TABLE IF NOT EXISTS klimalog"
            + "  (id              INTEGER PRIMARY KEY AUTOINCREMENT  NOT NULL,"
            + "   dttm            DATETIME                           NOT NULL,"
            + "   temperature     REAL,"
            + "   pressure        REAL,"
            + "   humidity        REAL,"
            + "   lightfull       REAL,"
            + "   lightinfra      REAL,"
            + "   lightvisible    REAL,"
            + "   rain            INT)";

            Statement stmt = this.conn.createStatement();
            stmt.execute(sqlCreate);
        } else {
            throw(new SQLException("Connection not estabilished"));
        }
    }
    
    public void storeKlimaValues(Date dt, double temperature, double pressure, double humidity, double lightfull, double lightinfra, double lightvisible, boolean rain) throws SQLException {
        Statement stmt = this.conn.createStatement();
        String sql = "INSERT INTO klimalog (dttm, temperature, pressure, humidity, lightfull, lightinfra, lightvisible, rain) " +
                        "VALUES ('" + Tools.getFormatter().format(dt) + "', " + Double.toString(temperature) + ", " + Double.toString(pressure)
                + ", " + Double.toString(humidity) +", " + Double.toString(lightfull) +", " + Double.toString(lightinfra)
                + ", " + Double.toString(lightvisible) + ", " + (rain ? 1 : 0) + ");"; 
        stmt.executeUpdate(sql);
    }
    
    public void storeStringValue(Date dt, String name, String val) throws SQLException {
        Statement stmt = this.conn.createStatement();
        String sql = "INSERT INTO datalog (dttm, name, valstring) " +
                        "VALUES ('" + Tools.getFormatter().format(dt) + "', '" + name + "', '" + val +"');"; 
        stmt.executeUpdate(sql);
    }
    
    public void storeIntValue(Date dt, String name, int val) throws SQLException {
        Statement stmt = this.conn.createStatement();
        String sql = "INSERT INTO datalog (dttm, name, valint) " +
                        "VALUES ('" + Tools.getFormatter().format(dt) + "', '" + name + "', " + Integer.toString(val) +");"; 
        stmt.executeUpdate(sql);
    }
    
    public void storeDoubleValue(Date dt, String name, double val) throws SQLException {
        Statement stmt = this.conn.createStatement();
        String sql = "INSERT INTO datalog (dttm, name, valdouble) " +
                        "VALUES ('" + Tools.getFormatter().format(dt) + "', '" + name + "', " + Double.toString(val) +");"; 
        stmt.executeUpdate(sql);
    }
    
    static protected SimpleDateFormat formatSQL = null;
    
    protected static SimpleDateFormat getFormatter() {
        if (Tools.formatSQL == null) {
            Tools.formatSQL = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        }
        return(Tools.formatSQL);
    }
}
