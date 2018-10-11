package kz.portlet.sample.model;

import kz.portlet.sample.DO.QuerySettings;
import kz.portlet.sample.DO.QueryType;

public class SQLSettings {
    private QuerySettings qs = null; // portet's settings
    private String user=null;        // user
    private String sql=null;         // SQL
    private String password=null;    // password
    private String dbUrl=null;       // url db
    private String limit = null;     // limit
    private String timeOut = null;   // time out
    private String act= null;
    private QueryType qt=QueryType.SELECT;

    public SQLSettings(QuerySettings qs,
                       String user,
                       String sql,
                       String password,
                       String dbUrl,
                       String limit,
                       String timeOut) {
        this.qs = qs;
        this.sql = sql;
        this.user = user;
        this.password = password;
        this.dbUrl = dbUrl;
        this.limit = limit;
        this.timeOut =timeOut;
    }

    public QuerySettings getQs() {
        return qs;
    }

    public void setQs(QuerySettings qs) {
        this.qs = qs;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(String timeOut) {
        this.timeOut = timeOut;
    }

    public QueryType getQt() {
        return qt;
    }

    public void setQt(QueryType qt) {
        this.qt = qt;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
