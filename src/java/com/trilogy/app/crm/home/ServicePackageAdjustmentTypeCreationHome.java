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

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.bean.ServicePackage;
import com.trilogy.app.crm.home.pipelineFactory.AdjustmentTypeHomePipelineFactory;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;


/**
 * This home creates and adjustment type for every service package that is created.
 *
 * @author paul.sperneac@redknee.com
 */
public class ServicePackageAdjustmentTypeCreationHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>ServicePackageAdjustmentTypeCreationHome</code>.
     *
     * @param delegate
     *            The delegate of this home.
     */
    public ServicePackageAdjustmentTypeCreationHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException, HomeInternalException
    {
        final ServicePackage pack = (ServicePackage) obj;

        createAdjustmentType(ctx, pack);

        return super.create(ctx, obj);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException, HomeInternalException
    {
        final ServicePackage newPackage = (ServicePackage) obj;
        final ServicePackage oldPackage = (ServicePackage) find(ctx, Integer.valueOf(newPackage.getId()));

        final Home aHome = (Home) ctx.get(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_SYSTEM_HOME);
        final AdjustmentType at = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, newPackage.getAdjustmentCode());

        if (at != null)
        {
            if (updateAdjustmentType(ctx, at, oldPackage, newPackage))
            {
                aHome.store(ctx, at);
            }
        }
        else
        {
            createAdjustmentType(ctx, newPackage);
        }

        return super.store(ctx, obj);
    }


    /**
     * Updates an existing adjustment type with the changes from the package change.
     *
     * @param ctx
     *            The operating context.
     * @param at
     *            The adjustment type to be updated.
     * @param oldPackage
     *            The old service package.
     * @param newPackage
     *            The new service package.
     * @return Returns <code>true</code> if the service package needs the be updated,
     *         <code>false</code> otherwise.
     */
    protected boolean updateAdjustmentType(final Context ctx, final AdjustmentType at, final ServicePackage oldPackage,
        final ServicePackage newPackage)
    {
        boolean ret = false;

        if (!SafetyUtil.safeEquals(oldPackage.getName(), newPackage.getName()))
        {
            at.setName(newPackage.getName());
            ret = true;
        }

        else if (!SafetyUtil.safeEquals(oldPackage.getAdjustmentTypeDescription(), newPackage
            .getAdjustmentTypeDescription()))
        {
            at.setDesc(newPackage.getAdjustmentTypeDescription());
            ret = true;
        }

        final Map spidInformation = at.getAdjustmentSpidInfo();
        final Object key = Integer.valueOf(newPackage.getSpid());
        AdjustmentInfo information = (AdjustmentInfo) spidInformation.get(key);

        if (information == null)
        {
            information = new AdjustmentInfo();
            spidInformation.put(key, information);
            information.setSpid(newPackage.getSpid());
            ret = true;
        }
        else if (!SafetyUtil.safeEquals(oldPackage.getAdjustmentGLCode(), newPackage.getAdjustmentGLCode()))
        {
            information.setGLCode(newPackage.getAdjustmentGLCode());
            ret = true;
        }
        else if (!SafetyUtil.safeEquals(oldPackage.getAdjustmentInvoiceDescription(), newPackage
            .getAdjustmentInvoiceDescription()))
        {
            information.setInvoiceDesc(newPackage.getAdjustmentInvoiceDescription());
            ret = true;
        }
        else if (oldPackage.getTaxAuthority() != newPackage.getTaxAuthority())
        {
            information.setTaxAuthority(newPackage.getTaxAuthority());
            ret = true;
        }

        at.setCategory(false);

        return ret;
    }


    /**
     * Creates an adjustment type from a service package information.
     *
     * @param ctx
     *            The operating context.
     * @param pack
     *            The service package.
     * @return The created adjustment type.
     * @throws HomeException
     *             Thrown if there are problems creating the adjustment type.
     */
    protected AdjustmentType createAdjustmentType(final Context ctx, final ServicePackage pack) throws HomeException
    {
        final Home adjTypeHome = (Home) ctx.get(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_SYSTEM_HOME);
        final AdjustmentType adjType = servicePackageToAdjustmentType(ctx, pack);

        // Recurring Charges
        adjType.setParentCode(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx,
            AdjustmentTypeEnum.RecurringCharges));

        try
        {
            final AdjustmentType ret = (AdjustmentType) adjTypeHome.create(ctx, adjType);
            pack.setAdjustmentCode(ret.getCode());

            return ret;
        }
        catch (final HomeException hEx)
        {
            throw new HomeException("Failed to create 'Recurring Charges' AdjustmentType for Package Template ["
                + pack.getId() + "]", hEx);
        }
    }


    /**
     * Create an adjustment type object from the provided service package.
     *
     * @param ctx
     *            The operating context.
     * @param pack
     *            The service package for which an adjustment type is created.
     * @return The created adjustment type object.
     */
    protected AdjustmentType servicePackageToAdjustmentType(final Context ctx, final ServicePackage pack)
    {
        AdjustmentType adjType = null;

        try
        {
            adjType = (AdjustmentType) XBeans.instantiate(AdjustmentType.class, ctx);
        }
        catch (final Exception e)
        {
            adjType = new AdjustmentType();
        }

        adjType.setName(pack.getName());
        adjType.setDesc(pack.getAdjustmentTypeDescription());
        adjType.setAction(AdjustmentTypeActionEnum.EITHER);
        adjType.setCategory(false);
        adjType.setLoyaltyEligible(true);
        
        final Map spidInformation = adjType.getAdjustmentSpidInfo();
        final Object key = Integer.valueOf(pack.getSpid());

        AdjustmentInfo information = (AdjustmentInfo) spidInformation.get(key);

        if (information == null)
        {
            information = new AdjustmentInfo();
            spidInformation.put(key, information);
        }

        information.setSpid(pack.getSpid());
        information.setGLCode(pack.getAdjustmentGLCode());
        information.setInvoiceDesc(pack.getAdjustmentInvoiceDescription());
        information.setTaxAuthority(pack.getTaxAuthority());

        return adjType;
    }
}
