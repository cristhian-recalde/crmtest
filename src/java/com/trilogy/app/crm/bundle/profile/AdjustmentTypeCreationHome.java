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

package com.trilogy.app.crm.bundle.profile;

import java.util.Map;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.home.pipelineFactory.AdjustmentTypeHomePipelineFactory;


/**
 * Create corresponding AdjustmentTypes when Bundle creation is successful. Auxiliary
 * bundles get two AdjustmentTypes.
 *
 * @author candy.wong@redknee.com
 * @author kevin.greer@redknee.com
 */
public class AdjustmentTypeCreationHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>AdjustmentTypeCreationHome</code>.
     *
     * @param ctx
     *            The operating context.
     * @param delegate
     *            The delegate of this decorator.
     */
    public AdjustmentTypeCreationHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        BundleProfile bundle = (BundleProfile) super.create(ctx, obj);

        try
        {
            if (bundle.getAdjustmentType() <= 0)
            {
                createAdjustments(ctx, bundle);
                bundle = (BundleProfile) super.store(ctx, bundle);
            }
        }
        catch (final HomeException exception)
        {
            // If an exception occurs, then we want to act as if the bundle
            // creation failed, so we must attempt to remove it.  We can ignore
            // any complaints during the removal.
            try
            {
                super.remove(ctx, bundle);
            }
            catch (final Throwable throwable)
            {
                new MinorLogMsg(this, "Failed to remove defunct bundle from database.", throwable).log(ctx);
            }

            throw exception;
        }
        
        
        return bundle;
    }


    /**
     * Create adjustment type for the bundle.
     *
     * @param ctx
     *            The operating context.
     * @param bundle
     *            The bundle being created.
     * @throws HomeException
     *             Thrown if there are problems creating the adjustment type.
     */
    private void createAdjustments(final Context ctx, final BundleProfile bundle) throws HomeException
    {
        final Home adjTypeHome = (Home) ctx.get(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_SYSTEM_HOME);
        AdjustmentType adjType = createAdjustmentType(ctx, bundle);
        AdjustmentType ret = null;

        // recurring charges
        adjType.setParentCode(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx,
            AdjustmentTypeEnum.Bundles));

        try
        {
            ret = (AdjustmentType) adjTypeHome.create(ctx, adjType);
            if (ret != null)
            {
                bundle.setAdjustmentType(ret.getCode());
            }
        }
        catch (final HomeException hEx)
        {
            if(LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Failed to create 'Recurring Charges' AdjustmentType for Bundle ["
                        + bundle.getBundleId() + "]", hEx);
            }
            throw new HomeException("Failed to create 'Recurring Charges' AdjustmentType for Bundle ["
                + bundle.getBundleId() + "]", hEx);
        }

        if (bundle.isAuxiliary())
        {
            adjType = createAdjustmentType(ctx, bundle);

            // other charges.
            adjType.setParentCode(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx,
                AdjustmentTypeEnum.AuxiliaryBundles));

            try
            {
                ret = (AdjustmentType) adjTypeHome.create(ctx, adjType);
                if (ret != null)
                {
                    bundle.setAuxiliaryAdjustmentType(ret.getCode());
                }
            }
            catch (final HomeException hEx)
            {
                if(LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Failed to create 'Other Charges' AdjustmentType for Bundle ["
                            + bundle.getBundleId() + "]", hEx);
                }
                throw new HomeException("Failed to create 'Other Charges' AdjustmentType for Bundle ["
                    + bundle.getBundleId() + "]", hEx);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        BundleProfile bundle = (BundleProfile) obj;
        BundleProfile oldBundle = (BundleProfile) find(ctx, Long.valueOf(bundle.getBundleId()));

        final Home adjTypeHome = (Home) ctx.get(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_SYSTEM_HOME);
        AdjustmentType adjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, adjTypeHome, bundle.getAdjustmentType());

        if (adjustmentType != null)
        {
            if (updateAdjustmentType(ctx, adjustmentType, oldBundle, bundle))
            {
                adjTypeHome.store(ctx, adjustmentType);
            }
        }
        else
        {
            adjustmentType = createAdjustmentType(ctx, bundle);
            adjTypeHome.create(adjustmentType);
            bundle.setAdjustmentType(adjustmentType.getCode());
        }

        if (bundle.isAuxiliary())
        {
            adjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, adjTypeHome, bundle.getAuxiliaryAdjustmentType());
            if (adjustmentType != null)
            {
                if (updateAdjustmentType(ctx, adjustmentType, oldBundle, bundle))
                {
                    adjTypeHome.store(ctx, adjustmentType);
                }
            }
            else
            {
                adjustmentType = createAdjustmentType(ctx, bundle);

                // Other Charges
                adjustmentType.setParentCode(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx,
                    AdjustmentTypeEnum.Other));

                try
                {
                    adjustmentType = (AdjustmentType) adjTypeHome.create(ctx, adjustmentType);
                    if (adjustmentType != null)
                    {
                        bundle.setAuxiliaryAdjustmentType(adjustmentType.getCode());
                    }
                }
                catch (final HomeException hEx)
                {
                    if(LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Failed to create 'Other Charges' AdjustmentType for Bundle ["
                                + bundle.getBundleId() + "]", hEx);
                    }
                    throw new HomeException("Failed to create 'Other Charges' AdjustmentType for Bundle ["
                        + bundle.getBundleId() + "]", hEx);
                }
            }
        }

        // TODO: if the store fails, remove the adjustments
        return super.store(ctx, obj);
    }


    /**
     * Updates the adjustment when the bundle is updated.
     *
     * @param ctx
     *            The operating context.
     * @param adjustmentType
     *            Adjustment type of the bundle.
     * @param oldBundle
     *            Old bundle.
     * @param bundle
     *            New bundle.
     * @return Returns <code>true</code> if the adjustment type is updated,
     *         <code>false</code> otherwise.
     */
    private boolean updateAdjustmentType(final Context ctx, final AdjustmentType adjustmentType,
        final BundleProfile oldBundle, final BundleProfile bundle)
    {
        boolean ret = false;

        if (!SafetyUtil.safeEquals(oldBundle.getName(), bundle.getName()))
        {
            adjustmentType.setName(bundle.getName());
            ret = true;
        }
        else if (!SafetyUtil
            .safeEquals(oldBundle.getAdjustmentTypeDescription(), bundle.getAdjustmentTypeDescription()))
        {
            adjustmentType.setDesc(bundle.getAdjustmentTypeDescription());
            ret = true;
        }

        final Map spidInformation = adjustmentType.getAdjustmentSpidInfo();
        final Object key = Integer.valueOf(bundle.getSpid());
        AdjustmentInfo information = (AdjustmentInfo) spidInformation.get(key);

        if (information == null)
        {
            information = new AdjustmentInfo();
            spidInformation.put(key, information);
            information.setSpid(bundle.getSpid());
            ret = true;
        }
        else if (!SafetyUtil.safeEquals(oldBundle.getGLCode(), bundle.getGLCode()))
        {
            information.setGLCode(bundle.getGLCode());
            ret = true;
        }
        else if (!SafetyUtil.safeEquals(oldBundle.getInvoiceDesc(), bundle.getInvoiceDesc()))
        {
            information.setInvoiceDesc(bundle.getInvoiceDesc());
            ret = true;
        }
        else if (oldBundle.getTaxAuthority() != bundle.getTaxAuthority())
        {
            information.setTaxAuthority(bundle.getTaxAuthority());
            ret = true;
        }

        return ret;
    }


    /**
     * Creates a new adjustment type and adjustment info based on the information in the
     * bundle template.
     *
     * @param ctx
     *            The operating context.
     * @param bundle
     *            The bundle this adjustment is created for.
     * @return The created adjustment type.
     */
    protected AdjustmentType createAdjustmentType(Context ctx, BundleProfile bundle)
    {
        AdjustmentType type = null;

        try
        {
            type = (AdjustmentType) XBeans.instantiate(AdjustmentType.class, ctx);
        }
        catch (final Exception e)
        {
            type = new AdjustmentType();
        }

        type.setName(bundle.getName());
        type.setAction(AdjustmentTypeActionEnum.EITHER);
        type.setDesc(bundle.getAdjustmentTypeDescription());
        type.setCategory(false);
        type.setLoyaltyEligible(true);
        
        final Map spidInformation = type.getAdjustmentSpidInfo();
        final Object key = Integer.valueOf(bundle.getSpid());
        AdjustmentInfo information = (AdjustmentInfo) spidInformation.get(key);

        if (information == null)
        {
            information = new AdjustmentInfo();
            spidInformation.put(key, information);
        }

        information.setSpid(bundle.getSpid());
        information.setGLCode(bundle.getGLCode());
        information.setInvoiceDesc(bundle.getInvoiceDesc());
        information.setTaxAuthority(bundle.getTaxAuthority());

        return type;
    }
}
