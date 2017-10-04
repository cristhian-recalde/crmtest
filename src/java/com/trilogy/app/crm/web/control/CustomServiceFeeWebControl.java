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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.*;
import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.bean.core.ServiceFee2;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;

/**
 * Modified version of ServiceFeeTableWebControl.
 * @author joe.chen@redknee.com
 **/
public class CustomServiceFeeWebControl extends ServiceFee2TableWebControl
{
    /**
     * Convert the Set to a MAP for super.toWeb.
     * @param ctx
     *            the operating context
     * @param out
     *            the page to write in HTML
     * @param name
     *            The name of the label?
     * @param obj
     *          the bean to be displayed
     */
    @Override
    public void toWeb(Context ctx,
            final PrintWriter out,
            final String name,
            final Object obj)
    {
        try
        {

            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, " ** " + obj.getClass().getName(), null)
                        .log(ctx);
            }

            final Map services = toMap((List) obj);
            final List list = new ArrayList();
            int parentSpid = -1;
            TechnologyEnum technology = TechnologyEnum.GSM;
            final PricePlan pp = (PricePlan) WebControllerWebControl57
                    .getParent(ctx);

            if (pp != null)
            {
                parentSpid = pp.getSpid();
                technology = pp.getTechnology();
            }

            final int mode = ctx.getInt("MODE", DISPLAY_MODE);

            ctx = ctx.createSubContext();

            // Disable creation of new AdjustmentInfo's
            ctx.put(NUM_OF_BLANKS, -1);

            // The actions need to be explicitly turned off. See
            // GTAC(5013014851) for
            // details.
            ctx.put("ACTIONS", false);

            ctx.put("ServiceFee2.serviceId.mode",
                            com.redknee.framework.xhome.webcontrol.ViewModeEnum.READ_ONLY);
            ctx.put("ServiceFee2.cltcDisabled.mode",
                    com.redknee.framework.xhome.webcontrol.ViewModeEnum.NONE);

            ctx.put("ServiceFee2.chargeFailureAction.mode",
                    com.redknee.framework.xhome.webcontrol.ViewModeEnum.READ_ONLY);
            
            Home servicesHome = (Home) ctx.get(ServiceHome.class);
            if (technology != null)
            {
                servicesHome = servicesHome.where(ctx, new EQ(
                        ServiceXInfo.TECHNOLOGY, technology));
            }
            servicesHome.where(ctx,
                    new EQ(ServiceXInfo.SPID, Integer.valueOf(parentSpid)))
                    .forEach(ctx, new Visitor()
                    {

                        /**
                         * the serial version iud
                         */
                        private static final long serialVersionUID = -5625792605507265782L;

                        public void visit(final Context ctx, final Object obj)
                        {
                            final Service service = (Service) obj;
                            final Object key = Long.valueOf(service.getID());
                            ServiceFee2 fee = (ServiceFee2) services.get(key);

                            if (fee == null)
                            {
                                if (service.getChargeScheme() == ServicePeriodEnum.ONE_TIME)
                                {
                                    fee.setServicePeriod(ServicePeriodEnum.ONE_TIME);
                                    // Javascript embedded into BundleFee and ServiceFee web-control control the auto-selection and need them to be fields.
                                    //ctx.put("ServiceFee2.servicePeriod.mode", com.redknee.framework.xhome.webcontrol.ViewModeEnum.READ_ONLY);
                                }

                                // DZ: only display all the services at creation time
                                if (mode == CREATE_MODE)
                                {
                                    fee = new ServiceFee2();
                                    fee.setServiceId(service.getID());
                                    fee.setChecked(false);
                                }
                            }
                            else
                            {
                                fee.setChecked(true);
                            }

                            list.add(fee);
                        }
                    });

            super.toWeb(ctx, out, name, list);
        }
        catch (final HomeException e)
        {
            LogSupport.debug(ctx, this, "Home exception : " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void outputCheckBox(final Context ctx, final PrintWriter out,
            final String name, final Object bean, final boolean isChecked)
    {
        final ServiceFee2 fee = (ServiceFee2) bean;

        out.print("<input type=\"hidden\" name=\"");
        out.print(name);
        out.print(SEPERATOR);
        out.print("serviceId\" value=\"");
        out.print(fee.getServiceId());
        out.println("\" />");

        super.outputCheckBox(ctx, out, name, bean, fee.isChecked());
    }

    /**
     * converts the list of service fees to a Map with the service id on it.
     * @param list the list of services
     * @return the converted map
     */
    private Map toMap(final List list)
    {
        Iterator i = null;
        final Map services = new HashMap();

        for (i = list.iterator(); i.hasNext();)
        {
            final ServiceFee2 serviceFee2 = (ServiceFee2) i.next();

            services.put(Long.valueOf(serviceFee2.getServiceId()), serviceFee2);
        }

        return services;
    }

    /**
     * Overrides the super class method.
     * @param ctx the operating context
     * @param selectionMap the map to be displayed
     * @param req the servlet request
     * @param name the label?
     */
    public void fromWeb(final Context ctx, final Map selectionMap, final ServletRequest req, final String name)
    {
        super.fromWeb(ctx, selectionMap, req, name);
    }
}
