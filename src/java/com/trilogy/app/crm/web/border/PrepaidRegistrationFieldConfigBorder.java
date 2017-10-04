/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.border;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.config.AccountRequiredFieldConfig;
import com.trilogy.app.crm.config.AccountRequiredFieldConfigHome;
import com.trilogy.app.crm.config.AccountRequiredFieldConfigXInfo;
import com.trilogy.app.crm.factory.AccountRequiredFieldConfigFactory;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * This border filters the AccountRequiredFieldConfig home to only show prepaid registration entries.
 * 
 * It also installs a bean factory that sets the system type to prepaid and the registration only flag to true.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.6
 */
public class PrepaidRegistrationFieldConfigBorder implements Border
{

    /**
     * {@inheritDoc}
     */
    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
            throws ServletException, IOException
    {
        Context sCtx = ctx.createSubContext();
        
        And filter = new And();
        filter.add(new EQ(AccountRequiredFieldConfigXInfo.SYSTEM_TYPE, SubscriberTypeEnum.PREPAID));
        filter.add(new EQ(AccountRequiredFieldConfigXInfo.REGISTRATION_ONLY, true));
        
        try
        {
            Home home = HomeSupportHelper.get(sCtx).getHome(sCtx, AccountRequiredFieldConfig.class);
            sCtx.put(AccountRequiredFieldConfigHome.class, home.where(sCtx, filter));
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error installing custom " + AccountRequiredFieldConfig.class.getName() + " home.", e).log(sCtx);
        }
        
        XBeans.putBeanFactory(sCtx, AccountRequiredFieldConfig.class, new AccountRequiredFieldConfigFactory(SubscriberTypeEnum.PREPAID, true));
        
        delegate.service(sCtx, req, res);
    }

}
