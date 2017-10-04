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
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.ActivationFeeModeEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.PersonalListPlan;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.SeverityEnum;
import com.trilogy.framework.xlog.log.SeverityLogMsg;

/**
 * @author margarita.alp@redknee.com
 */
public class PersonalPlanListAuxiliaryServiceCreationHome extends HomeProxy
{
    public PersonalPlanListAuxiliaryServiceCreationHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }

    public Object create(final Context ctx, final Object arg) throws HomeException
    {
        debugLogMsg(ctx, "create: " + arg);

        final PersonalListPlan plan = (PersonalListPlan) super.create(ctx, arg);

        String errorReason = "";
        if (plan != null)
        {
            AuxiliaryService service;
            try
            {
                service = (AuxiliaryService)XBeans.instantiate(AuxiliaryService.class, ctx);
            }
            catch(Throwable t)
            {
                service = new AuxiliaryService();
                new MinorLogMsg(this, "XBeans can't isntantiate", t).log(ctx);
            }
            service.setName(plan.getName());
            service.setSpid(plan.getSpid());
            service.setType(AuxiliaryServiceTypeEnum.CallingGroup);

            Collection<Extension> extensions = new ArrayList<Extension>();

            CallingGroupAuxSvcExtension extension = new CallingGroupAuxSvcExtension();
            extension.setAuxiliaryServiceId(service.getID());
            extension.setSpid(service.getSpid());
            extension.setCallingGroupIdentifier(plan.getID());
            extension.setCallingGroupType(CallingGroupTypeEnum.PLP);
            extensions.add(extension);
            
            service.setAuxiliaryServiceExtensions(ExtensionSupportHelper.get(ctx).wrapExtensions(ctx, extensions));
            
            service.setChargingModeType(ServicePeriodEnum.MONTHLY);
            service.setSmartSuspension(true);
            service.setActivationFee(ActivationFeeModeEnum.PRORATE);
            service.setCharge(plan.getMonthlyCharge());
            service.setSmartSuspension(plan.getSmartSuspension());
            service.setActivationFee(plan.getActivationFee());
            service.setAdjustmentTypeDescription("PLP " + plan.getName());
            service.setGLCode(plan.getAdjustmentGLCode());

            service.setInvoiceDescription("PLP " + plan.getName());
            service.setTaxAuthority(plan.getTaxAuthority());

            //set the Aux Service Type to ANY as supposed to GSM TT: 6111441545
            service.setTechnology(TechnologyEnum.ANY);

            final Home home = (Home) ctx.get(AuxiliaryServiceHome.class);

            try
            {
                if (home != null)
                {
                    service = (AuxiliaryService) home.create(ctx, service);
                    if (service != null)
                    {
                        plan.setAuxiliaryService(service.getIdentifier());
                        CallingGroupERLogMsg.generatePLPCreationER(
                                plan,
                                CallingGroupERLogMsg.SUCCESS_RESULT_CODE,
                                ctx);

                        new OMLogMsg(Common.OM_MODULE, Common.OM_PLP_CREATION_SUCCESS).log(ctx);
                    }
                }
            }
            catch (HomeException e)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, e.getMessage(), e).log(ctx);
                }

                errorReason = e.getLocalizedMessage();
            }
            finally
            {
                if (home == null || service == null || !errorReason.equals(""))
                {
                    final String msg = "Can't create auxiliary service: " + errorReason;

                    new SeverityLogMsg(
                            SeverityEnum.MAJOR,
                            this.getClass().getName(),
                            msg,
                            null).log(ctx);

                    CallingGroupERLogMsg.generatePLPCreationER(
                            plan,
                            CallingGroupERLogMsg.ERROR_PROVISIONING_AUXILIARY_SERVICE,
                            ctx);

                    new OMLogMsg(Common.OM_MODULE, Common.OM_PLP_CREATION_FAIL).log(ctx);

                    throw new HomeException(msg);
                }
            }
        }

        return plan;
    }

    public void remove(final Context ctx, final Object arg) throws HomeException
    {
        debugLogMsg(ctx, "remove: " + arg);

        new OMLogMsg(Common.OM_MODULE, Common.OM_PLP_DELETION_ATTEMPT).log(ctx);

        final PersonalListPlan plan = (PersonalListPlan) arg;

        try
        {
            // Delete the associated auxiliary service
            final Home home = (Home) ctx.get(AuxiliaryServiceHome.class);
            if (home != null)
            {
                final AuxiliaryService auxiliaryService = (AuxiliaryService)
                        home.find(ctx, Long.valueOf(plan.getAuxiliaryService()));

                if (auxiliaryService != null)
                {
                    try
                    {
                        home.remove(ctx, auxiliaryService);
                    }
                    catch (Exception e)
                    {
                        CallingGroupERLogMsg.generatePLPDeletionER(
                                plan,
                                CallingGroupERLogMsg.ERROR_PROVISIONING_AUXILIARY_SERVICE,
                                ctx);

                        new OMLogMsg(Common.OM_MODULE, Common.OM_PLP_DELETION_FAIL).log(ctx);
                        
                        throw new HomeException(e);
                    }
                }

                // TT#9062900011: Delete the plp itself only after removing the auxiliary services.
                super.remove(ctx, plan);
            }
            else
            {
                CallingGroupERLogMsg.generatePLPDeletionER(
                        plan,
                        CallingGroupERLogMsg.ERROR_PROVISIONING_AUXILIARY_SERVICE,
                        ctx);

                new OMLogMsg(Common.OM_MODULE, Common.OM_PLP_DELETION_FAIL).log(ctx);

                throw new HomeException("AuxiliaryServiceHome not found in context.");
            }
            CallingGroupERLogMsg.generatePLPDeletionER(
                    plan,
                    CallingGroupERLogMsg.SUCCESS_RESULT_CODE,
                    ctx);

            new OMLogMsg(Common.OM_MODULE, Common.OM_PLP_DELETION_SUCCESS).log(ctx);
        }
        catch (HomeException e)
        {            
            throw new HomeException(e.getMessage(), e);
        }
    }

    private void debugLogMsg(final Context ctx, final String msg)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, msg, null).log(ctx);
        }
    }
}
