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
package com.trilogy.app.crm.bundle.home;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.ActivationFeeCalculationEnum;
import com.trilogy.app.crm.bundle.ActivationTypeEnum;
import com.trilogy.app.crm.bundle.BundleSegmentEnum;
import com.trilogy.app.crm.bundle.CategoryAssociationTypeEnum;
import com.trilogy.app.crm.bundle.ExpiryTypeEnum;
import com.trilogy.app.crm.bundle.GroupChargingTypeEnum;
import com.trilogy.app.crm.bundle.QuotaTypeEnum;
import com.trilogy.app.crm.bundle.RecurrenceTypeEnum;
import com.trilogy.app.crm.support.BundleSupport;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.framework.xhome.beans.xi.XInfo;
import com.trilogy.framework.xhome.beans.xi.XInfoAdapter;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.product.bundle.manager.api.BundleCategoryAssociation;
import com.trilogy.product.bundle.manager.api.BundleProfileApi;
import com.trilogy.product.bundle.manager.api.UnitTypeEnum;

/**
 * Adapts the CRM Bundle profile to the RMI Bundle manager bundles
 * All enums the XInfo doesn't understand will be mapped here
 * @author arturo.medina@redknee.com
 *
 */
public class BundleProfileXInfoAdapter extends XInfoAdapter
{

    /**
     * Default constructor accepts the BundleProfile as input and the BM bundle profile as destination
     * @param source
     * @param destination
     */
    public BundleProfileXInfoAdapter(XInfo source, XInfo destination)
    {
        super(source, destination);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object adapt(Context ctx, Object source) throws HomeException
    {
        BundleProfile profile = (BundleProfile) super.adapt(ctx, source);
        
        if (((BundleProfileApi) source).getIsCurrency())
        {
            profile.setAssociationType(CategoryAssociationTypeEnum.CURRENCY);
        }
        else
        {
            profile.setAssociationType(CategoryAssociationTypeEnum.SINGLE_UNIT);
        }
        
        HashMap categoryIds = new HashMap();
        
        Iterator iter = ((BundleProfileApi) source).getCategoryIds().entrySet().iterator();
        
        int i = 1;
        while (iter.hasNext())
        {
            BundleCategoryAssociation assocApi = (BundleCategoryAssociation) ((Entry) iter.next()).getValue();
            com.redknee.app.crm.bean.core.BundleCategoryAssociation assoc = new com.redknee.app.crm.bean.core.BundleCategoryAssociation();
            assoc.setCategoryId(assocApi.getBundleCategoryId());
            assoc.setType(assocApi.getUnitType().getIndex());
            assoc.setRate(assocApi.getRate());
            categoryIds.put(Integer.valueOf(i), assoc);
            i++;
        }
        profile.setBundleCategoryIds(categoryIds);
        
        adaptEnumTypes(profile, (BundleProfileApi) source);
        
        return profile;
    }

    /**
     * Calls the Support method for every unit type the BM adapts in the systems
     * @param profile
     * @param source
     */
    private void adaptEnumTypes(BundleProfile profile, BundleProfileApi source)
    {
        final BundleSupport bundleSupport = BundleSupportHelper.get();
        
        profile.setSegment((BundleSegmentEnum)
                bundleSupport.mapBundleEnums(source.getSegment(), profile.getSegment()));
        
        profile.setQuotaScheme((QuotaTypeEnum)
                bundleSupport.mapBundleEnums(source.getQuotaScheme(), profile.getQuotaScheme()));
        
        profile.setActivationFeeCalculation((ActivationFeeCalculationEnum)
                bundleSupport.mapBundleEnums(source.getActivationFeeCalculation(), profile.getActivationFeeCalculation()));
        
        profile.setGroupChargingScheme((GroupChargingTypeEnum)
                bundleSupport.mapBundleEnums(source.getGroupChargingScheme(), profile.getGroupChargingScheme()));
        
        profile.setActivationScheme((ActivationTypeEnum)
                bundleSupport.mapBundleEnums(source.getActivationScheme(), profile.getActivationScheme()));
        
        profile.setExpiryScheme((ExpiryTypeEnum)
                bundleSupport.mapBundleEnums(source.getExpiryScheme(), profile.getExpiryScheme()));
        
        profile.setRecurrenceScheme((RecurrenceTypeEnum)
                bundleSupport.mapBundleEnums(source.getRecurrenceScheme(), profile.getRecurrenceScheme()));
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object unAdapt(Context ctx, Object destination) throws HomeException
    {
        return unAdapt(ctx, destination, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object unAdapt(Context ctx, Object destination, Object source) throws HomeException
    {
        if (destination instanceof BundleProfile)
        {
            if (((BundleProfile)destination).getValidity() == 0)
            {
                ((BundleProfile)destination).setValidity(1);
            }
        }
        BundleProfileApi profile = (BundleProfileApi) super.unAdapt(ctx, destination, source);

        profile.setIsCurrency(((BundleProfile) destination).isCurrency());
        
        profile.setCategoryIds(new HashMap());
        
        Iterator iter = ((BundleProfile) destination).getBundleCategoryIds().entrySet().iterator();
        
        int i = 1;
        while (iter.hasNext())
        {
            com.redknee.app.crm.bean.core.BundleCategoryAssociation assoc = (com.redknee.app.crm.bean.core.BundleCategoryAssociation) ((Entry) iter.next()).getValue();
            BundleCategoryAssociation assocApi = new BundleCategoryAssociation();
            assocApi.setBundleCategoryId(assoc.getCategoryId());
            if (((BundleProfile) destination).isCurrency())
            {
                assocApi.setUnitType(UnitTypeEnum.CURRENCY);
            }
            if (((BundleProfile) destination).isCrossService())
            {
                assocApi.setUnitType(UnitTypeEnum.CROSS_UNIT);
            }
            else
            {
                assocApi.setUnitType(UnitTypeEnum.get((short)assoc.getType()));
            }
            assocApi.setPerUnit(1);
            assocApi.setRate((int) assoc.getRate());
            profile.getCategoryIds().put(Integer.valueOf(i), assocApi);
            i++;
        }
        
        unAdaptEnumTypes(profile, (BundleProfile) destination);
        
        return profile;
    }

    /**
     * Adapt all the unitTypes from the CRM profile to the Model BM bean
     * @param profile
     * @param destination
     */
    private void unAdaptEnumTypes(BundleProfileApi profile,
            BundleProfile destination)
    {
        final BundleSupport bundleSupport = BundleSupportHelper.get();
        
        profile.setSegment((com.redknee.product.bundle.manager.api.BundleSegmentEnum)
                bundleSupport.mapBundleEnums(destination.getSegment(), profile.getSegment()));
        
        profile.setQuotaScheme((com.redknee.product.bundle.manager.api.QuotaTypeEnum)
                bundleSupport.mapBundleEnums(destination.getQuotaScheme(), profile.getQuotaScheme()));
        
        profile.setActivationFeeCalculation((com.redknee.product.bundle.manager.api.ActivationFeeCalculationEnum)
                bundleSupport.mapBundleEnums(destination.getActivationFeeCalculation(), profile.getActivationFeeCalculation()));
        
        profile.setGroupChargingScheme((com.redknee.product.bundle.manager.api.GroupChargingTypeEnum)
                bundleSupport.mapBundleEnums(destination.getGroupChargingScheme(), profile.getGroupChargingScheme()));
        
        profile.setActivationScheme((com.redknee.product.bundle.manager.api.ActivationTypeEnum)
                bundleSupport.mapBundleEnums(destination.getActivationScheme(), profile.getActivationScheme()));
        
        profile.setExpiryScheme((com.redknee.product.bundle.manager.api.ExpiryTypeEnum)
                bundleSupport.mapBundleEnums(destination.getExpiryScheme(), profile.getExpiryScheme()));
        
        profile.setRecurrenceScheme((com.redknee.product.bundle.manager.api.RecurrenceTypeEnum)
                bundleSupport.mapBundleEnums(destination.getRecurrenceScheme(), profile.getRecurrenceScheme()));
        
    }


    /**
     * 
     */
    private static final long serialVersionUID = -8704836362011615342L;

}
