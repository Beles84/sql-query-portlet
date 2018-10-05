package kz.portlet.sample.DO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/*Json libraries*/
import jxl.write.WritableWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import oracle.jdbc.OracleTypes;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class SqlExecutor extends Executor{

    private static final String FETCH_EXPLAIN_PLAN_QUERY = "select plan_table_output from table(dbms_xplan.display())";

    private static final String LAST_ERROR_POSITION_QUERY = "DECLARE\n"
            + "  c        INTEGER := DBMS_SQL.open_cursor();\n"
            + "  errorpos integer := -1;\n"
            + "BEGIN\n"
            + "  BEGIN\n"
            + "    DBMS_SQL.parse(c, :sqltext, DBMS_SQL.native);\n"
            + "  EXCEPTION\n"
            + "    WHEN OTHERS THEN\n"
            + "      errorpos := DBMS_SQL.LAST_ERROR_POSITION();\n"
            + "  END;\n"
            + "  :errorpos := errorpos;\n"
            + "  DBMS_SQL.close_cursor(c);\n"
            + "END;";

    public String connectionStringProperty;
    public String usernameProperty;
    public String passwordProperty;

    private Integer limitProperty = 1000;
    private Integer timeoutProperty = 300;
    private QuerySettings settings;

    private QueryType queryType;
    private boolean usingConnectionPool;

    //private ResultsTable queryResultTable;
    public ByteArrayOutputStream xlsResultTable;
    private JSONArray textResults;
    private String[] fieldLists;
    public long executionTimeMillis;
    private int affectedRowsCount=0;
    public int rowsCount;
    private String exceptionMessage;
    private int sqlErrorPosition = -1;
    private boolean isMaintenance;


    /* Конструктор */
    public SqlExecutor(QuerySettings settings, String _user, String _password, String _url, QueryType qType, String limit, String timeOut) {

        this.connectionStringProperty= _url;
        this.usernameProperty = _user;
        this.passwordProperty = _password;
        this.queryType = qType;

        //this.limitProperty = Integer.valueOf(limit).intValue();
        //this.timeoutProperty = Integer.valueOf(timeOut).intValue();

        //System.out.println("qType:"+qType);
        //System.out.println("queryType:"+queryType);
        this.settings = settings;
        usingConnectionPool = false;
        Connection connection = null;
        try {
            connection = getConnection();
            if (connection != null && connection.getMetaData() != null) {
                connectionStringProperty = connection.getMetaData().getURL();
            }
        } catch (SQLException e) {
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    private Connection getConnection() {
        Connection conn = null;
        try {
            if (usingConnectionPool) {
                Context context = new InitialContext();
                DataSource d = (DataSource) context.lookup(settings.getPoolName());
                conn = d.getConnection();
            } else {
                Class.forName("oracle.jdbc.OracleDriver");
                conn = DriverManager.getConnection(connectionStringProperty, usernameProperty, passwordProperty);
            }
        } catch (SQLException ex) {
            saveException(ex);
        } catch (NamingException ex) {
            saveException(ex);
        } catch (ClassNotFoundException ex) {
            saveException(ex);
        }
        return conn;
    }

    private Connection getReporterConnection() {
        Connection conn = null;
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection("jdbc:oracle:thin:@10.8.1.85:1521:showcase", "reporter", "reporter_2015");
            //conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl", "REPORTER", "REPORTER_2015");
        } catch (SQLException ex) {
            saveException(ex);
        } catch (ClassNotFoundException ex) {
            saveException(ex);
        }
        return conn;
    }

    private void saveException(Exception ex) {
        exceptionMessage = ex.getMessage();
    }

    public void runQuery(String query,boolean isMaintenance) {
        this.isMaintenance = isMaintenance;
        runQuery(query);
    }

    /*
     *  Вопольнить запрос
     * */
    public void runQuery(String query) {
        if (!isQuerySafe(query)) {
            return;
        }
        Connection conn = null;
        Statement statement = null;
        ResultSet resultSet = null;

        exceptionMessage = null;

        sqlErrorPosition = -1;
        //queryResultTable = null;
        try {
            if(isMaintenance)
                conn = getReporterConnection();
            else
                conn = getConnection();

            if (conn == null) {
                return;
            }
            statement = conn.createStatement();
            statement.setMaxRows(limitProperty);
            statement.setQueryTimeout(timeoutProperty);

            long timeBeforeQueryMillis = System.currentTimeMillis();
            if (queryType == QueryType.SELECT) {
                resultSet = statement.executeQuery(query);
                parseResultSet(resultSet);

            } else if (queryType == QueryType.INSERT_OR_UPDATE) {
                affectedRowsCount = statement.executeUpdate(query);
            } else if (queryType == QueryType.EXPLAIN_PLAN) {
                statement.execute("EXPLAIN PLAN FOR " + query);
                resultSet = statement.executeQuery(FETCH_EXPLAIN_PLAN_QUERY);
                //System.out.println("####");
                //System.out.println(resultSet.toString());
                parseResultSet(resultSet);
            }
            long timeAfterQueryMillis = System.currentTimeMillis();
            executionTimeMillis = timeAfterQueryMillis - timeBeforeQueryMillis;
        } catch (SQLException ex) {
            sqlErrorPosition = retrieveErrorPosition(conn, query);
            saveException(ex);
        } finally {
            isMaintenance = false;
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException sqle) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException sqle) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException sqle) {
                }
            }
        }
    }

    private int retrieveErrorPosition(Connection connection, String query) {
        CallableStatement callStatement = null;
        try {
            callStatement = connection.prepareCall(LAST_ERROR_POSITION_QUERY);
            callStatement.setString(1, query);
            callStatement.registerOutParameter(2, OracleTypes.INTEGER);
            callStatement.execute();
            return callStatement.getInt(2);
        } catch (SQLException ex) {
        } finally {
            if (callStatement != null) {
                try {
                    callStatement.close();
                } catch (SQLException sqle) {
                }
            }
        }
        return -1;
    }

    private boolean isQuerySafe(String query) {
        String queryText = query.toLowerCase();
        if (queryText.contains("where")) {
            return true;
        }
        if (queryText.contains("update")) {
            exceptionMessage = "UPDATE without WHERE";
            return false;
        }
        if (queryText.contains("delete")) {
            exceptionMessage = "DELETE without WHERE";
            return false;
        }
        return true;
    }

    private void parseResultSet(ResultSet resultSet) throws SQLException, UnsupportedOperationException {

        /* Convert to JSON from ResultSet */
        textResults = convert(resultSet);

    }

    /**
     * @return the connectionStringProperty
     */
    public String getConnectionStringProperty() {
        return connectionStringProperty;
    }

    public void setConnectionStringProperty(String connectionStringProperty) {
        this.connectionStringProperty = connectionStringProperty;
    }

    public String getUsernameProperty() {
        return usernameProperty;
    }

    public void setUsernameProperty(String usernameProperty) {
        this.usernameProperty = usernameProperty;
    }

    public String getPasswordProperty() {
        return passwordProperty;
    }

    public void setPasswordProperty(String passwordProperty) {
        this.passwordProperty = passwordProperty;
    }

    /**            return;

     * @return the limitProperty
     */
//    Integer getLimitProperty() {
//        return limitProperty;
//    }

    /**
     * @return the timeoutProperty
     */
//    Integer getTimeoutProperty() {
//        return timeoutProperty;
//    }

    /**
     * @param usingConnectionPool the usingConnectionPool to set
     */
    void setUsingConnectionPool(boolean usingConnectionPool) {
        this.usingConnectionPool = usingConnectionPool;
    }

    /**
     */
    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    /**
     * @return the queryResult
     */
//    //ResultsTable getQueryResultTable() {
//        return queryResultTable;
//    }

    /**
     * @return the executionTimeMillis
     */
    public long getExecutionTimeMillis() {
        return executionTimeMillis;
    }

    /**
     * @return the affectedRowsNumber
     */
    public int getAffectedRowsCount() {
        return affectedRowsCount;
    }

    /**
     * @return the rowsNumber
     */
    public int getRowsCount() {
        return rowsCount;
    }

    /**
     * @return the exceptionMessage
     */
    public String getExceptionMessage() {
        return exceptionMessage;
    }
    
    public int getSqlErrorPosition() {
        return sqlErrorPosition;
    }

//    public void setLimitProperty(int limitProperty) {
//        this.limitProperty = limitProperty;
//    }

//    public void setTimeoutProperty(int timeoutProperty) {
//        this.timeoutProperty = timeoutProperty;
//    }

    /**
     * @return the textResults
     */
    public JSONArray getTextResults() {
        return textResults;
    }

    public String[] getFieldLists() {
        return fieldLists;
    }


    public JSONArray convert(ResultSet rs ) throws SQLException, JSONException
    {
        rowsCount = 0;
        JSONArray json = new JSONArray();
        ResultSetMetaData rsmd = rs.getMetaData();

        Set<String> field = new HashSet<String>();


        int j=1;
        while(rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();


            for (int i=1; i<numColumns+1; i++) {
                String column_name = rsmd.getColumnName(i);

                if(rsmd.getColumnType(i)==java.sql.Types.ARRAY){
                    obj.putOpt(column_name, rs.getArray(column_name));
                    field.add(column_name);
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.BIGINT){
                    obj.put(column_name, rs.getInt(column_name));
                    field.add(column_name);
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.BOOLEAN){
                    obj.put(column_name, rs.getBoolean(column_name));
                    field.add(column_name);
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.BLOB){
                    obj.put(column_name, rs.getBlob(column_name));
                    field.add(column_name);
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.DOUBLE){
                    obj.put(column_name, rs.getDouble(column_name));
                    field.add(column_name);
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.FLOAT){
                    obj.put(column_name, rs.getFloat(column_name));
                    field.add(column_name);
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.INTEGER){
                    obj.put(column_name, rs.getInt(column_name));
                    field.add(column_name);
                }
                else if(rsmd.getColumnType(i)==Types.LONGVARCHAR){
                    obj.put(column_name, rs.getString(column_name));
                    field.add(column_name);
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.VARCHAR){
                    obj.put(column_name, rs.getString(column_name));
                    field.add(column_name);
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.TINYINT){
                    obj.put(column_name, rs.getInt(column_name));
                    field.add(column_name);
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.SMALLINT){
                    obj.put(column_name, rs.getInt(column_name));
                    field.add(column_name);
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.DATE){
                    obj.put(column_name, rs.getDate(column_name));
                    field.add(column_name);
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.TIMESTAMP){
                    obj.put(column_name, rs.getTimestamp(column_name));
                    field.add(column_name);
                }
                else{
                    obj.put(column_name, rs.getObject(column_name));
                    field.add(column_name);
                }
            }
            json.put(obj);
            rowsCount++;
        }
        return json;
    }

    public ByteArrayOutputStream getXlsResultTable() {
        return xlsResultTable;
    }

    public void setXlsResultTable(ByteArrayOutputStream xlsResultTable) {
        this.xlsResultTable = xlsResultTable;
    }
}