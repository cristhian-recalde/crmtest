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

import com.trilogy.app.crm.bean.Note;
import com.trilogy.app.crm.bean.SupplementaryData;
import com.trilogy.app.crm.bean.SupplementaryDataEntityEnum;
import com.trilogy.app.crm.bean.SupplementaryDataHome;
import com.trilogy.app.crm.bean.SupplementaryDataXInfo;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * A Border for the supplementary screen that presets the BAN selection.
 *
 * @author Marcio Marques
 * @since 9.1.3
 */
public class SupplementaryDataBorder implements Border
{
    private Class<AbstractBean> class_;
    private SupplementaryDataEntityEnum entity_;
    
    public SupplementaryDataBorder(SupplementaryDataEntityEnum entity, Class<AbstractBean> clazz)
    {
        class_ = clazz;
        entity_ = entity;
    }

    public void service(Context ctx, final HttpServletRequest req, final HttpServletResponse res,
            final RequestServicer delegate) throws ServletException, IOException
    {
        Context subCtx = ctx.createSubContext();
        Context session = Session.getSession(subCtx);
        String id = "";
        
        AbstractBean bean = (AbstractBean) session.get(class_);
        
        if (subCtx.has(class_))
        {
            bean = (AbstractBean) subCtx.get(class_);
        }
        else
        {
            subCtx.put(class_, bean);
        }
        
        Home home = (Home) subCtx.get(SupplementaryDataHome.class);

        And filter = new And();
        
        filter.add(new EQ(SupplementaryDataXInfo.ENTITY, Integer.valueOf(entity_.getIndex())));
        
        if (bean != null)
        {
            Class idSupportClass = XBeans.getClass(ctx, class_, IdentitySupport.class);
            try
            {
                IdentitySupport idSupport = (IdentitySupport) idSupportClass.getMethod("instance").invoke(null);
                id = String.valueOf(idSupport.ID(bean));
                filter.add(new EQ(SupplementaryDataXInfo.IDENTIFIER, String.valueOf(idSupport.ID(bean))));
            }
            catch (Throwable t)
            {
                LogSupport.minor(subCtx, this, "Unable to instantiate object of type " + idSupportClass.getName() + ": " + t.getMessage(), t);
            }
        }
        
        home = home.where(subCtx, filter);
        
        subCtx.put(SupplementaryDataHome.class, home);

        final String identifier = id;
        XBeans.putBeanFactory(subCtx, SupplementaryData.class, new ContextFactory()
        {
           public Object create(Context ctx)
           {
               SupplementaryData bean = new SupplementaryData();
               bean.setEntity(entity_.getIndex());
               bean.setIdentifier(identifier);
               
               return bean;
           }
        });

        delegate.service(subCtx, req, res);
    }

}
