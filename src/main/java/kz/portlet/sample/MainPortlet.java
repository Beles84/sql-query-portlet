package kz.portlet.sample;

import com.converter.Beauty;
import com.converter.Config;
import com.converter.Convert;
import com.converter.SQL;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserGroupRole;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserGroupRoleLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import jxl.write.*;

import kz.portlet.sample.model.EActions;
import javax.portlet.*;
import java.io.*;
import java.lang.Boolean;
import java.util.List;

import kz.portlet.sample.DO.*;
import kz.portlet.sample.model.SQLSettings;
import org.json.JSONException;
import org.json.JSONObject;

public class MainPortlet extends MVCPortlet {
    @Override
    public void serveResource (
            ResourceRequest resourceRequest, ResourceResponse resourceResponse)
            throws IOException, PortletException {

        OutputStream out = resourceResponse.getPortletOutputStream();


        String _act = getParam("act", resourceRequest);
        EActions act = EActions.valueOf(_act);

        byte[] response = null;

        // getting params
        String _sqlQuery = getParam("sql", resourceRequest);

        SQLSettings settings = new SQLSettings(
                new QuerySettings(resourceRequest.getPreferences()),
                getParam("user", resourceRequest),
                getParam("sql", resourceRequest),
                getParam("password", resourceRequest),
                getParam("jdbc", resourceRequest),
                getParam("limit",resourceRequest),
                getParam("timeOut",resourceRequest)
        );


        switch (act) {
            case DO_SELECT: {
                JSONObject json = SQL.doSQL(settings, QueryType.SELECT);

                response = json.toString().getBytes();
                out.write(response);
                break;
            }
            case DO_UPDATE: {
                JSONObject json = SQL.doSQL(settings, QueryType.INSERT_OR_UPDATE);

                response = json.toString().getBytes();
                out.write(response);
                break;
            }
            case DO_PLAN:{
                JSONObject json = SQL.doSQL(settings, QueryType.EXPLAIN_PLAN);
                response = json.toString().getBytes();
                out.write(response);
                break;
            }
            case DO_BEAUTY: {
                String s = Beauty.toDO(_sqlQuery);
                response = s.getBytes();
                out.write(response);
                break;
            }
            case DO_SAVE: {
                Config.setPreference("SQL",_sqlQuery);
                response = "SQL query saved".getBytes();
                out.write(response);
                break;
            }
            case DO_CONFIG_SAVE:{
                Config.setPreference("user",settings.getUser());
                Config.setPreference("password", settings.getPassword());
                Config.setPreference("db_URL", settings.getDbUrl());
                Config.setPreference("limit",settings.getLimit());
                Config.setPreference("timeOut",settings.getTimeOut());
                response = "Configure saved".getBytes();
                out.write(response);
                break;
            }
            case DO_CONFIG_LOAD:{
                JSONObject conf = new JSONObject();
                conf.put("user",Config.getPreference("user"));
                conf.put("password",Config.getPreference("password"));
                conf.put("db_URL",Config.getPreference("jdbc"));
                conf.put("limit",Config.getPreference("limit"));
                conf.put("timeOut",Config.getPreference("timeOut"));
                response = conf.toString().getBytes();
                out.write(response);
                break;
            }
            case DO_OPEN:{
                response = Config.getPreference("SQL").getBytes();
                out.write(response);
                break;
            }
            case DO_EXPORT:{

                String fileName = "resuls.xls";
                resourceResponse.setProperty("Content-Disposition", "attachment;filename=" + fileName);
                resourceResponse.setContentType("application/vnd.ms-excel");

                byte[] excelBytes = new byte[0];
                try {
                    JSONObject json = SQL.doSQL(settings, QueryType.SELECT);
                    //System.out.println(json);
                    excelBytes = Convert.JSONtoByteEXL(json.getJSONArray("data"),settings.getSql());
                } catch (WriteException e) {
                    e.printStackTrace();
                    System.out.println("EXL error ");
                }
                out.write(excelBytes);
                break;
            }
            case DO_PERMISSION:{
                Boolean isNb=false;
                JSONObject json = new JSONObject();

                long userId = PortalUtil.getUserId(resourceRequest);

                isNb = Permission.getCheckPermission(userId, "SQLDeveloper");
                if (resourceRequest.isUserInRole("SQLDeveloper")){
                    System.out.println("[x] SQLDeveloper");
                }
                System.out.println("IS NB?"+isNb);

                try {
                    json.put("success",true);
                    json.put("permission", isNb);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                out.write(json.toString().getBytes());
                break;
            }
            default:
                break;
        }

        super.serveResource(resourceRequest, resourceResponse);
    }
    /* Получить параметры */
    private String getParam(String name, ResourceRequest resourseRequest) {
        return PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(resourseRequest)).getParameter(name);
    }
    }
