/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved. author:
 * simar.singh@redknee.com
 */
package com.trilogy.app.crm.web.agent;

import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.auth.AuthSPI;
import com.trilogy.framework.auth.AuthSPIProxy;
import com.trilogy.framework.auth.LoginException;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.menu.XMenu;
import com.trilogy.framework.xhome.menu.XMenuHome;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xhome.web.xmenu.Constants;


/**
 * 
 * @author simar.singh@redknee.com 
 * Restrict's authentication service from authenticating non-web users
 * Non-Web status come's from user's group
 *         
 */
public class RestrictWebUserAuthSPI extends AuthSPIProxy
{

    public RestrictWebUserAuthSPI(AuthSPI delegate)
    {
        super(delegate);
    }


    @Override
    public void login(Context ctx, String username, String password) throws LoginException
    {
        try
        {
            String webUser = WebAgents.getParameter(ctx, "username");
            if (null != webUser && !webUser.isEmpty() && webUser.equals(username))
            {
                if (isUserRestricted(ctx, username) && isMenuVisible(ctx))
                {
                    throw new LoginException("User [" + username
                            + "] is restricted from web-access. Please contact the administrator.");
                }
            }
        }
        catch (Throwable t)
        {
            if (t instanceof LoginException)
            {
                throw (LoginException)t;
            }
            new MajorLogMsg(this, "Could not apply web-access restricitons to the login restrict.", t).log(ctx);
        }
        super.login(ctx, username, password);
    }


    /*
     * Is the user restricted ?
     */
    private boolean isUserRestricted(Context ctx, String username) throws HomeException
    {
        User user = HomeSupportHelper.get(ctx).findBean(ctx, User.class, username);
        if (null != user)
        {
            CRMGroup group = HomeSupportHelper.get(ctx).findBean(ctx, CRMGroup.class, user.getGroup());
            if (null != group && group.isRestrictWebAccess())
            {
                return true;
            }
        }
        return false;
    }


    /*
     * Some menu's are non-GUI ex Statement-Sevicer.
     */
    private boolean isMenuVisible(Context ctx) throws HomeException
    {
        String cmd = WebAgents.getParameter(ctx, Constants.XMENU_CMD);
        if (null == cmd || cmd.isEmpty() || cmd.equals("Login"))
        {
            return true;
        }
        XMenu menu = (XMenu) ((Home) ctx.get(XMenuHome.class)).find(ctx, cmd);
        return (menu != null) ? menu.isEnabled() && menu.isVisible() : false;
    }
}
