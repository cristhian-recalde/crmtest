/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import java.util.Map;

import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.DiscountClass;
import com.trilogy.app.crm.home.pipelineFactory.AdjustmentTypeHomePipelineFactory;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * Creates new adjustment types for new Discount Classes,
 * 
 * @author simar.singh@redknee.com
 */
public class DiscountClassAdjustmentTypeCreationHome extends HomeProxy
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
    public DiscountClassAdjustmentTypeCreationHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, Object bean) throws HomeException
    {
        if (bean instanceof DiscountClass)
        {
            DiscountClass adjustmentTypeCreationAware = (DiscountClass) bean;
            final AdjustmentType type = createAdjustmentType(ctx, adjustmentTypeCreationAware);
            adjustmentTypeCreationAware.setAdjustmentType(type.getCode());
        }
        return super.create(ctx, bean);
    }


    /**
     * Creates and returns a new AdjustmentType for the given AuxiliaryService.
     * 
     * @param ctx
     *            The operating context.
     * @param adjustmentTypeCreationAware
     *            The AuxiliaryService for which to create a new AdjustmentType.
     * @return A new AdjustmentType.
     * @throws HomeException
     *             Thrown if there are problems creating the adjustment type.
     */
    private AdjustmentType createAdjustmentType(final Context ctx, final DiscountClass adjustmentTypeCreationAware)
            throws HomeException
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
        type.setParentCode(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx,
                AdjustmentTypeEnum.Discount));
        type.setName(adjustmentTypeCreationAware.getName());
        type.setDesc(adjustmentTypeCreationAware.getAdjustmentTypeDescription());
        type.setAction(AdjustmentTypeActionEnum.CREDIT);
        type.setCategory(false);
        type.setLoyaltyEligible(true);
        
        final Map spidInformation = type.getAdjustmentSpidInfo();
        final Object key = Integer.valueOf(adjustmentTypeCreationAware.getSpid());
        AdjustmentInfo information = (AdjustmentInfo) spidInformation.get(key);
        if (information == null)
        {
            information = new AdjustmentInfo();
            spidInformation.put(key, information);
        }
        information.setSpid(adjustmentTypeCreationAware.getSpid());
        information.setGLCode(adjustmentTypeCreationAware.getGLCode());
        information.setInvoiceDesc(adjustmentTypeCreationAware.getInvoiceDescription());
        information.setTaxAuthority(adjustmentTypeCreationAware.getTaxAuthority());
        final Home home = (Home) ctx.get(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_SYSTEM_HOME);
        type = (AdjustmentType) home.create(ctx, type);
        return type;
    }
}
