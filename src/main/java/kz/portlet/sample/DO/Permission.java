package kz.portlet.sample.DO;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.RoleModel;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;

public class Permission {
    public static boolean getCheckPermission(long userId, String strRole){
        boolean yes=false;
        if (userId > 0) {
            try {
                User us = UserLocalServiceUtil.getUser(userId);

                for(RoleModel r : us.getRoles()) {
                    System.out.println(r.getName());
                    if (r.getName().equals(strRole))
                        yes = true;
                }
            } catch (SystemException e) {
                e.printStackTrace();
            } catch (PortalException e) {
                e.printStackTrace();
            }
        }
        return yes;
    }
}
