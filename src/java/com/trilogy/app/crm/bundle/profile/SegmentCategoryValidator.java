/*
 * SegmentCategoryValidator.java
 *
 * Author : victor.stratan@redknee.com
 * Date: Jun 5, 2006
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bundle.profile;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeOperationEnum;

import com.trilogy.app.crm.bean.core.BundleCategoryAssociation;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.BundleCategory;
import com.trilogy.app.crm.bundle.BundleCategoryAssociationXInfo;
import com.trilogy.app.crm.bundle.BundleProfileXInfo;
import com.trilogy.app.crm.bundle.BundleSegmentEnum;
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.app.crm.bundle.exception.BundleManagerException;
import com.trilogy.app.crm.bundle.exception.CategoryNotExistException;
import com.trilogy.app.crm.bundle.service.CRMBundleCategory;
import com.trilogy.app.crm.bundle.service.CRMBundleProfile;

/**
 * Validate that the Bundle Segment and Bundle Category are correlated correctly.
 * 
 * @author victor.stratan@redknee.com
 */
public class SegmentCategoryValidator implements Validator
{
    /**
     * {@inheritDoc}
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        HomeOperationEnum op = (HomeOperationEnum) ctx.get(HomeOperationEnum.class, HomeOperationEnum.CREATE);

        // Bundle Segment type and Bundle Category type are final,
        // so we'll check only on CREATE
        if (op == HomeOperationEnum.CREATE)
        {
            BundleProfile bundle = (BundleProfile)obj;
            CompoundIllegalStateException el = new CompoundIllegalStateException();

            CRMBundleCategory catService = (CRMBundleCategory)ctx.get(CRMBundleCategory.class);
            CRMBundleProfile bundleService = (CRMBundleProfile) ctx.get(CRMBundleProfile.class);

            Iterator<Map.Entry<?, BundleCategoryAssociation>> iter = bundle.getBundleCategoryIds().entrySet().iterator();
            try
            {
                while (iter.hasNext())
                {
                    BundleCategoryAssociation association = iter.next().getValue();
                    try
                    {
                        Integer categoryId = Integer.valueOf(association.getCategoryId());
                        BundleCategory category = catService.getCategory(ctx, association.getCategoryId());
        
                        if (category == null)
                        {
                            el.thrown(new IllegalPropertyArgumentException(BundleCategoryAssociationXInfo.CATEGORY_ID,
                                    "Category " + association.getCategoryId() + " does not exist in service provider "
                                            + bundle.getSpid()));
                        }
                        else
                        {
                            UnitTypeEnum type = category.getUnitType();
                            if (type == UnitTypeEnum.POINTS)
                            {
                                Collection<BundleProfile> result = bundleService.getBundleByCategory(ctx, association.getCategoryId());
                                if (result != null && result.size()>0)
                                {
                                    el.thrown(new IllegalPropertyArgumentException(BundleCategoryAssociationXInfo.CATEGORY_ID,
                                            "Cannot create 2 Bundles with the same Points Category. Category " + categoryId
                                                    + " is already used by Bundle " + result.iterator().next().getBundleId()));
                                }
                            }
                            else
                            {
                                // Unit Type not Points
                                if (bundle.getSegment() == BundleSegmentEnum.HYBRID)
                                {
                                    el.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.SEGMENT,
                                            "Only Point bundles can be of Hybrid segment."));
                                }
                            }
                        }
                    }
                    catch (CategoryNotExistException hEx)
                    {
                        el.thrown(new IllegalPropertyArgumentException(BundleCategoryAssociationXInfo.CATEGORY_ID,
                                "Cannot retrieve category " + association.getCategoryId() + " in service provider "
                                        + bundle.getSpid()));
                    }
                    catch (BundleManagerException hEx)
                    {
                        el.thrown(new IllegalPropertyArgumentException(BundleCategoryAssociationXInfo.CATEGORY_ID,
                                "Cannot retrieve category " + association.getCategoryId() + " in service provider "
                                        + bundle.getSpid()));
                    }
                }
            }
            finally
            {
                el.throwAll();
            }
        }
    }
}
