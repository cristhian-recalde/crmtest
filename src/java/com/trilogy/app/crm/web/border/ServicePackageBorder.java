/*
 * Created on Sep 28, 2005
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.border;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.*;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.webcontrol.WebController;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This border hides fields for the ServicePackage screen.
 *
 * @author psperneac
 *
 */
public class ServicePackageBorder implements Border
{

    public ServicePackageBorder()
    {
        super();
    }

    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
        throws ServletException, IOException
    {
        ctx=ctx.createSubContext();

        AbstractWebControl.setMode(ctx,"ServiceFee2.mandatory",ViewModeEnum.NONE);
     // Javascript embedded into BundleFee and ServiceFee web-control control the auto-selection and need them to be fields.
        //AbstractWebControl.setMode(ctx,"ServiceFee2.servicePeriod",ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx,"ServiceFee2.cltcDisabled",ViewModeEnum.NONE);

        AbstractWebControl.setMode(ctx,"BundleFee.mandatory",ViewModeEnum.NONE);
        //AbstractWebControl.setMode(ctx,"BundleFee.servicePeriod",ViewModeEnum.NONE);
     // Javascript embedded into BundleFee and ServiceFee web-control control the auto-selection and need them to be fields.
        AbstractWebControl.setMode(ctx,"BundleFee.startDate",ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx,"BundleFee.endDate",ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx,"BundleFee.paymentNum",ViewModeEnum.NONE);

        if ( WebController.isCmd("New", req ) || (WebController.isCmd("Preview", req) && !isEdit(ctx,req)) ||
                WebController.isCmd("Copy", req) )
        {
            AbstractWebControl.setMode(ctx,"ServicePackage.id",ViewModeEnum.NONE);
        }
        AbstractWebControl.setMode(ctx,"ServicePackage.packageFees",ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx,"ServicePackage.totalCharge",ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx,"ServiceFee2.fee",ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx,"BundleFee.fee",ViewModeEnum.NONE);

        if(isEdit(ctx,req) &&  !WebController.isCmd("Copy", req)  && !WebController.isCmd("New", req))
        {
            AbstractWebControl.setMode(ctx, "ServicePackage.type", ViewModeEnum.READ_ONLY);
            AbstractWebControl.setMode(ctx, "ServicePackage.chargingLevel", ViewModeEnum.READ_ONLY);
            AbstractWebControl.setMode(ctx, "ServicePackage.chargingMode", ViewModeEnum.READ_ONLY);
        }

        ServicePackage bean=getBean(ctx,req);

        if(bean!=null)
        {
            if(bean.getChargingLevel()==ChargingLevelEnum.ATTRIBUTES)
            {
                AbstractWebControl.setMode(ctx,"ServicePackage.totalCharge",ViewModeEnum.READ_ONLY);
                AbstractWebControl.setMode(ctx,"ServiceFee2.fee",ViewModeEnum.READ_WRITE);
                AbstractWebControl.setMode(ctx,"BundleFee.fee",ViewModeEnum.READ_WRITE);
            }
        }

        ctx.put("ACTIONS",false);

        delegate.service(ctx,req,res);
    }

    /**
     * Checks if the screen is in edit mode
     * @param ctx
     * @param req
     * @return true if the screen is in edit mode, false if not
     */
    private boolean isEdit(Context ctx, HttpServletRequest req)
    {
        String primaryKey = req.getParameter("key");
        if(primaryKey!=null && primaryKey.trim().length()>0)
        {
            return true;
        }

        return false;
    }

    /**
     * Gets the bean from the context
     * @param ctx
     * @param req
     * @return
     */
    private ServicePackage getBean(Context ctx, HttpServletRequest req)
    {
        ServicePackage bean=null;

        // 1. try to find the key and find the bean in the homes
        String primaryKey = req.getParameter("key");
        if(primaryKey!=null && primaryKey.trim().length()>0)
        {
            try
            {
                Integer key=Integer.valueOf(primaryKey);
                Home home = (Home) ctx.get(ServicePackageHome.class);
                bean=(ServicePackage) home.find(ctx, key);

                if(bean!=null)
                {
                    return bean;
                }
            }
            catch (NumberFormatException e)
            {
                if(LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(ctx,e.getMessage(),e).log(ctx);
                }
            }
            catch (HomeException e)
            {
                if(LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(ctx,e.getMessage(),e).log(ctx);
                }
            }

        }

        // 2. no key, must be new, get it from the context/parameters
        bean=(ServicePackage) new ServicePackageWebControl().fromWeb(ctx,req,"");

        return bean;
    }

}
