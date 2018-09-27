package kz.portlet.sample.LIBS;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Library {

    protected Connection conn = null;
    protected Statement stmt = null;

    /* Convert to JSON from Object */
    public String getToJSON( Object Obj){
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        return gson.toJson(Obj);
    }

    public Connection getToDB(String db_user, String db_password, String db_url){

        // JDBC driver name and database URL
        final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
        final String DB_URL = db_url;

        //  Database credentials
        final String USER = db_user;
        final String PASS = db_password;



        try {
            //STEP 2: Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            //STEP 3: Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            success();
        } catch (ClassNotFoundException e) {
            e.getMessage();
            e.printStackTrace();
            errMsg = e.getMessage();
            fail();
        } catch (SQLException e) {
            e.printStackTrace();
            errMsg = e.getMessage();
            fail();
        }

        return conn;
    }

    public Statement initSql(){
        //STEP 4: Execute a query
        try {
            stmt =conn.createStatement();
        } catch (SQLException e) {
            errMsg = e.getMessage();
            fail();
            e.printStackTrace();
            return null;
        }
        return stmt;
    }

    public Boolean status=false;
    public String errMsg=null;

    /*getter and setter*/
    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public Boolean getStatusConnection() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public void success(){
        setStatus(true);
    }
    public void fail(){
        setStatus(false);
    }


}
