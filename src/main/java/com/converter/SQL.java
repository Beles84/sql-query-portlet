package com.converter;

import kz.portlet.sample.DO.QuerySettings;
import kz.portlet.sample.DO.QueryType;
import kz.portlet.sample.DO.SqlExecutor;
import kz.portlet.sample.model.SQLSettings;
import org.json.JSONArray;
import org.json.JSONObject;

public class SQL {
    public static JSONObject doSQL(SQLSettings settings, QueryType qt){

        SqlExecutor executor = new SqlExecutor(
                settings.getQs(),
                settings.getUser(),
                settings.getPassword(),
                settings.getDbUrl(),
                qt,
                settings.getLimit(),
                settings.getTimeOut()
        );

        executor.runQuery(settings.getSql());

        String sError = executor.getExceptionMessage();

        JSONObject error = new JSONObject()
                .put("error_pos",executor.getSqlErrorPosition())
                .put("error_msg", sError);


        JSONObject detail = new JSONObject();
                if (qt == QueryType.INSERT_OR_UPDATE) {
                    detail.put("rows", executor.getAffectedRowsCount());
                    System.out.println("affect: "+executor.getAffectedRowsCount());

                    detail.put("time", executor.getExecutionTimeMillis());
                } else {
                    detail.put("rows", executor.getRowsCount());
                    detail.put("time", executor.getExecutionTimeMillis());
                }

        JSONObject json = new JSONObject()
                .put("data", executor.getTextResults())
                .put("detail", detail)
                .put("error",error);

        return json;
    }
}