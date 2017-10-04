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

import java.util.Map;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.extension.auxiliaryservice.core.GroupChargingAuxSvcExtension;
import com.trilogy.app.crm.home.pipelineFactory.AdjustmentTypeHomePipelineFactory;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;


/**
 * Creates new adjustment types for new auxiliary services, and look-up existing
 * adjustment types for existing auxiliary services.
 *
 * @author gary.anderson@redknee.com
 */
public class AuxiliaryServiceAdjustmentTypeCreationHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a a new AuxiliaryServiceAdjustmentTypeCreationHome.
     *
     * @param delegate
     *            The home to which we delegate.
     */
    public AuxiliaryServiceAdjustmentTypeCreationHome(final Home delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object bean) throws HomeException
    {
        final AuxiliaryService service = (AuxiliaryService) super.create(ctx, bean);

        try
        {
            final AdjustmentType type = createAdjustmentType(ctx, service);
            service.setAdjustmentType(type.getCode());

            GroupChargingAuxSvcExtension groupChargingAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, service, GroupChargingAuxSvcExtension.class);
            if (groupChargingAuxSvcExtension!=null)
            {
                final AdjustmentType groupType = createAdjustmentType(ctx, service);
                groupChargingAuxSvcExtension.setGroupAdjustmentType(groupType.getCode());
            }

            super.store(ctx, service);
        }
        catch (final HomeException exception)
        {
            /*
             * If an exception occurs, then we want to act as if the service creation
             * failed, so we must attempt to remove it. We can ignore any complaints
             * during the removal.
             */
            try
            {
                super.remove(ctx, service);
            }
            catch (final Throwable th)
            {
                new MinorLogMsg(this, "Failed to remove defunct auxiliary service from database.", th).log(ctx);
            }

            throw exception;
        }

        return service;
    }


    /**
     * Creates and returns a new AdjustmentType for the given AuxiliaryService.
     *
     * @param ctx
     *            The operating context.
     * @param service
     *            The AuxiliaryService for which to create a new AdjustmentType.
     * @return A new AdjustmentType.
     * @throws HomeException
     *             Thrown if there are problems creating the adjustment type.
     */
    private AdjustmentType createAdjustmentType(final Context ctx, final AuxiliaryService service) throws HomeException
    {
        AdjustmentType type = null;
        try
        {
            type = (AdjustmentType) XBeans.instantiate(AdjustmentType.class, ctx);
        }
        catch (final Exception e)
        {
            throw new HomeException("Failed to instantiate AdjustmentType", e);
        }
        final AdjustmentTypeEnum adjustmentCategory;
        final AdjustmentTypeActionEnum adjustmentAction;
        if(AuxiliaryServiceTypeEnum.Discount == service.getType())
        {
            adjustmentCategory = AdjustmentTypeEnum.DiscountAuxiliaryServices;
            adjustmentAction = AdjustmentTypeActionEnum.CREDIT;
        } else
        {
            adjustmentCategory = AdjustmentTypeEnum.AuxiliaryServices;
            adjustmentAction = AdjustmentTypeActionEnum.EITHER;
        }

        if(AuxiliaryServiceTypeEnum.MultiSIM == service.getType())
        {
            // CSR Input field will be used for SIM specific service transactions.
            // The SIM will be put in the field
            type.setCsrInputOnInvoice(true);
        }
        
        type.setParentCode(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx,
            adjustmentCategory));
        type.setName(service.getName());
        type.setDesc(service.getAdjustmentTypeDescription());
        type.setAction(adjustmentAction);
        type.setCategory(false);
        type.setLoyaltyEligible(true);

        final Map spidInformation = type.getAdjustmentSpidInfo();
        final Object key = Integer.valueOf(service.getSpid());

        AdjustmentInfo information = (AdjustmentInfo) spidInformation.get(key);

        if (information == null)
        {
            information = new AdjustmentInfo();
            spidInformation.put(key, information);
        }

        information.setSpid(service.getSpid());
        information.setGLCode(service.getGLCode());
        information.setInvoiceDesc(service.getInvoiceDescription());
        information.setTaxAuthority(service.getTaxAuthority());

        final Home home = (Home) ctx.get(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_SYSTEM_HOME);
        type = (AdjustmentType) home.create(ctx, type);

        return type;
    }
}
