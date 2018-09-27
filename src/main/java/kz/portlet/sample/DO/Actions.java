package kz.portlet.sample.DO;

import kz.portlet.sample.model.MyResponce;
import kz.portlet.sample.LIBS.Library;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Actions extends Library {

    private Connection connect = null;

    private String user;
    private String password;
    private String url;

    private Statement stmt = null;
    public String errMsg=null;


    /*
     * do SELECT
     */
    public byte[] doSelect(String user, String password, String sqlStatement, String jdbc_url){



          MyResponce myResponce = new MyResponce("You are greet programmer", "ERROR");
         return getToJSON(myResponce).getBytes();


    }
    /*
     * do UPDATE
     * */
    public byte[] doUpdate(){
        return "update".getBytes();
    }

}
