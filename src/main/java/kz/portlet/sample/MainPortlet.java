package kz.portlet.sample;

import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import kz.portlet.sample.model.EActions;

import javax.portlet.*;
import java.io.IOException;
import java.io.OutputStream;
import kz.portlet.sample.DO.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainPortlet extends MVCPortlet {
    private Actions actions = new Actions();

    @Override
    public void serveResource (
            ResourceRequest resourceRequest, ResourceResponse resourceResponse)
            throws IOException, PortletException {

        OutputStream out = resourceResponse.getPortletOutputStream();

        String _act = ParamUtil.getString(resourceRequest, "act"); //getParam("op", resourceRequest)
        EActions act = EActions.valueOf(_act);

        byte[] response = null;

        // getting params
        String _user = getParam("user", resourceRequest);
        String _password = getParam("password", resourceRequest);
        String _dburl = getParam("jdbc", resourceRequest);
        String _sqlQuery = getParam("sql", resourceRequest);


        switch (act) {
            case DO_SELECT: {

                SqlExecutor executor = new SqlExecutor(
                        new QuerySettings(resourceRequest.getPreferences()),
                        _user,
                        _password,
                        _dburl,
                        QueryType.SELECT
                );
                //System.out.println(_sqlQuery);
                executor.runQuery(_sqlQuery);

                String sError = executor.getExceptionMessage();

                JSONObject error = new JSONObject()
                        .put("error_pos",executor.getSqlErrorPosition())
                        .put("error_msg", sError);

                JSONObject detail = new JSONObject()
                        .put("rows", executor.getRowsCount())
                        .put("time", executor.getExecutionTimeMillis());


                JSONObject json = new JSONObject()
                            .put("data", executor.getTextResults())
                            .put("detail", detail)
                            .put("error",error);

                String s = json.toString();
                System.out.println(s);

                response = s.getBytes();
                out.write(response);
                break;
            }
            case DO_UPDATE: {
                break;
            }
            case DO_PLAN:{
                SqlExecutor executor = new SqlExecutor(
                        new QuerySettings(resourceRequest.getPreferences()),
                        _user,
                        _password,
                        _dburl,
                        QueryType.EXPLAIN_PLAN
                );
                //System.out.println(_sqlQuery);
                executor.runQuery(_sqlQuery);

                String sError = executor.getExceptionMessage();

                JSONObject error = new JSONObject()
                        .put("error_pos",executor.getSqlErrorPosition())
                        .put("error_msg", sError);

                JSONObject detail = new JSONObject()
                        .put("rows", executor.getRowsCount())
                        .put("time", executor.getExecutionTimeMillis());


                JSONObject json = new JSONObject()
                        .put("data", executor.getTextResults())
                        .put("detail", detail)
                        .put("error",error);

                String s = json.toString();
                System.out.println(s);

                response = s.getBytes();
                out.write(response);
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
