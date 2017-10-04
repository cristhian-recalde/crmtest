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
package com.trilogy.app.crm.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.SeverityEnum;
import com.trilogy.framework.xlog.log.SeverityLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.BirthdayPlan;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.ff.FriendsAndFamilyCrmBeanInterface;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;

/**
 * Creates the an auxiliary service that corresponds to the Friends and Family bean.
 * @author victor.stratan@redknee.com
 */
public class FriendsAndFamilyPlanAuxiliaryServiceCreationHome extends HomeProxy
{
    public FriendsAndFamilyPlanAuxiliaryServiceCreationHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }

    public Object create(final Context ctx, final Object arg) throws HomeException
    {
        final FriendsAndFamilyCrmBeanInterface plan = (FriendsAndFamilyCrmBeanInterface) super.create(ctx, arg);

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
            extension.setCallingGroupType(plan.getCallingGroupType());
            extensions.add(extension);
            
            service.setAuxiliaryServiceExtensions(ExtensionSupportHelper.get(ctx).wrapExtensions(ctx, extensions));
            
            service.setChargingModeType(ServicePeriodEnum.MONTHLY);
            service.setCharge(plan.getMonthlyCharge());
            service.setSmartSuspension(plan.getSmartSuspension());
            service.setActivationFee(plan.getActivationFee());
            service.setAdjustmentTypeDescription("Birthday Plan " + plan.getName());
            service.setGLCode(plan.getAdjustmentGLCode());

            service.setInvoiceDescription("Birthday Plan " + plan.getName());
            service.setTaxAuthority(plan.getTaxAuthority());

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
                        CallingGroupERLogMsg.generateBPCreationER(
                                (BirthdayPlan) plan,
                                CallingGroupERLogMsg.SUCCESS_RESULT_CODE,
                                ctx);

                        new OMLogMsg(Common.OM_MODULE, Common.OM_BP_CREATION_SUCCESS).log(ctx);
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

                    CallingGroupERLogMsg.generateBPCreationER(
                            (BirthdayPlan) plan,
                            CallingGroupERLogMsg.ERROR_PROVISIONING_AUXILIARY_SERVICE,
                            ctx);

                    new OMLogMsg(Common.OM_MODULE, Common.OM_BP_CREATION_FAIL).log(ctx);

                    throw new HomeException(msg);
                }
            }
        }

        return plan;
    }



    public void remove(final Context ctx, final Object obj) throws HomeException
    {
        new OMLogMsg(Common.OM_MODULE, Common.OM_BP_DELETION_ATTEMPT).log(ctx);

        final FriendsAndFamilyCrmBeanInterface plan = (FriendsAndFamilyCrmBeanInterface) obj;

        // Deelete the bean itself
        super.remove(ctx, plan);

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
                    home.remove(ctx, auxiliaryService);
                }
            }
            CallingGroupERLogMsg.generateBPDeletionER(
                    (BirthdayPlan) plan,
                    CallingGroupERLogMsg.SUCCESS_RESULT_CODE,
                    ctx);

            new OMLogMsg(Common.OM_MODULE, Common.OM_BP_DELETION_SUCCESS).log(ctx);
        }
        catch (HomeException e)
        {
            CallingGroupERLogMsg.generateBPDeletionER(
                    (BirthdayPlan) plan,
                    CallingGroupERLogMsg.ERROR_PROVISIONING_AUXILIARY_SERVICE,
                    ctx);

            new OMLogMsg(Common.OM_MODULE, Common.OM_BP_DELETION_FAIL).log(ctx);

            throw new HomeException(e.getMessage(), e);
        }
    }
}
