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
 */package com.trilogy.app.crm.web.action;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.ui.ChargingTemplate;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtensionHome;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtensionXInfo;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterChargingTemplateHome;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterChargingTemplateXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.web.action.DeleteAction;
import com.trilogy.framework.xhome.web.util.Link;

/**
 * Action responsible for deleting charging templates. The link is only shown
 * when the charging template is not in use.
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class ChargingTemplateDeleteAction extends DeleteAction implements Predicate
{

    public ChargingTemplateDeleteAction()
    {
        super();
    }

    public void writeLinkDetail(Context ctx, PrintWriter out, Object bean, Link link)
    {
        if (bean instanceof ChargingTemplate || bean instanceof com.redknee.app.crm.bean.ChargingTemplate)
        {
            if (f(ctx, bean))
            {
                super.writeLinkDetail(ctx, out, bean, link);
            }
        }
        else
        {
            return;
        }
    }

    public void writeLink(final Context ctx, final PrintWriter out, final Object bean, final Link link)
    {
        if (bean instanceof ChargingTemplate || bean instanceof com.redknee.app.crm.bean.ChargingTemplate)
        {
            if (f(ctx, bean))
            {
                super.writeLink(ctx, out, bean, link);
            }
        }
        else
        {
            return;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context
     * .Context, java.lang.Object)
     */
    public boolean f(Context ctx, Object bean) throws AbortVisitException
    {
        final long chargingTemplateId;
        if (bean instanceof com.redknee.app.crm.bean.ChargingTemplate)
        {
            chargingTemplateId = ((com.redknee.app.crm.bean.ChargingTemplate) bean).getIdentifier();
        }
        else
        {
            chargingTemplateId = ((ChargingTemplate) bean).getIdentifier();
        }
        
        Home ppsmSupporterChargingTemplateHome = (Home) ctx.get(PPSMSupporterChargingTemplateHome.class);
        Home ppsmSupporteeSubExtensionHome = (Home) ctx.get(PPSMSupporteeSubExtensionHome.class);
        try
        {
            if ((ppsmSupporterChargingTemplateHome.where(ctx,
                    new EQ(PPSMSupporterChargingTemplateXInfo.IDENTIFIER, Long.valueOf(chargingTemplateId))).selectAll(
                    ctx).size() == 0)
                    && (ppsmSupporteeSubExtensionHome.where(ctx,
                            new EQ(PPSMSupporteeSubExtensionXInfo.CHARGING_TEMPLATE, Long.valueOf(chargingTemplateId)))
                            .selectAll(ctx).size() == 0))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (HomeException e)
        {
            throw new AbortVisitException("Exception while checking if charging template "
                    + chargingTemplateId + " is in use: " + e.getMessage(), e);
        }
    }
}
