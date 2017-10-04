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
package com.trilogy.app.crm.bundle.profile;

import java.util.Collection;

import com.trilogy.app.crm.CoreCrmConstants;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.BundleProfileXInfo;
import com.trilogy.app.crm.bundle.FlexTypeEnum;
import com.trilogy.app.crm.bundle.InvalidBundleApiException;
import com.trilogy.app.crm.bundle.RecurrenceTypeEnum;
import com.trilogy.app.crm.bundle.rateplan.RatePlanAssociation;
import com.trilogy.app.crm.bundle.rateplan.RatePlanAssociationXInfo;
import com.trilogy.app.crm.bundle.service.CRMBundleCategory;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;


/**
 * @author ksivasubramaniam
 * @since 9.5.1
 */
public class FlexBundleValidator implements Validator
{

    /**
     * {@inheritDoc}
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        CompoundIllegalStateException el = new CompoundIllegalStateException();
        BundleProfile bundle = (BundleProfile) obj;
        CRMBundleCategory service = (CRMBundleCategory) ctx.get(CRMBundleCategory.class);
        try
        {
            if (bundle.isFlex())
            {
                if (bundle.getFlexType() == FlexTypeEnum.ROOT)
                {
                    if (bundle.getRoot() != CoreCrmConstants.NONE_BUNDLE_ID)
                    {
                        el.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.ROOT,
                                "Can not set root for a ROOT Flextype bundle "));
                    }
                   
                    HomeOperationEnum homeOp = (HomeOperationEnum) ctx.get(HomeOperationEnum.class);
                    if (homeOp != null)
                    {
                        if (homeOp == HomeOperationEnum.CREATE)
                        {
                            And and = new And();
                            and.add(new EQ(RatePlanAssociationXInfo.CATEGORY_ID, Long.valueOf(bundle
                                    .getBundleCategoryId())));
                            and.add(new EQ(RatePlanAssociationXInfo.SPID, Integer.valueOf(bundle.getSpid())));
                            try
                            {
                                Collection<RatePlanAssociation> coll = HomeSupportHelper.get(ctx).getBeans(ctx,
                                        RatePlanAssociation.class, and);
                                for (RatePlanAssociation rpa : coll)
                                {
                                    BundleProfile duplicateRootBundle = BundleSupportHelper.get(ctx).getBundleProfile(
                                            ctx, rpa.getBundleId());
                                    if (duplicateRootBundle.isFlex()
                                            && duplicateRootBundle.getFlexType() == FlexTypeEnum.ROOT
                                            && duplicateRootBundle.getSegment() == bundle.getSegment())
                                    {
                                        el.thrown(new IllegalPropertyArgumentException(
                                                BundleProfileXInfo.BUNDLE_CATEGORY_IDS,
                                                "There can only be 1 ROOT flextype bundle per bundle category per segment."));
                                    }
                                }
                            }
                            catch (HomeException homeEx)
                            {
                            }
                            catch (InvalidBundleApiException iEx)
                            {
                            }
                        }
                    }
                }
                else
                {
                    if (bundle.getRoot() == CoreCrmConstants.NONE_BUNDLE_ID)
                    {
                        el.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.ROOT,
                                "Root is required for a Secondary Bundle"));
                    }
                }
                long catID = bundle.getBundleCategoryId();
                long nextRefBundleID = bundle.getNextBundleRef();
                validateBundleCategory(ctx, nextRefBundleID, BundleProfileXInfo.NEXT_BUNDLE_REF, catID, el);
                validateBundleCategory(ctx, bundle.getRoot(), BundleProfileXInfo.ROOT, catID, el);
                if (bundle.isCurrency() || bundle.isCrossService())
                {
                    el.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.ASSOCIATION_TYPE,
                            "Flex bundle can not be currency  or cross service bundle"));
                }
            }
        }
        finally
        {
            el.throwAll();
        }
    }


    private static void validateBundleCategory(Context ctx, long bundleId, PropertyInfo xinfo, long catID,
            CompoundIllegalStateException el)
    {
        if (bundleId >= 0)
        {
            try
            {
                BundleProfile nextBundleProfile = BundleSupportHelper.get(ctx).getBundleProfile(ctx, bundleId);
                if (nextBundleProfile != null)
                {
                    if (nextBundleProfile.getBundleCategoryId() != catID)
                    {
                        el.thrown(new IllegalPropertyArgumentException(xinfo, xinfo.getName()
                                + " has to belong to same bundle category - " + catID));
                    }
                }
                else
                {
                    el.thrown(new IllegalPropertyArgumentException(xinfo, xinfo.getName()
                            + " does not belong to any bundle category"));
                }
            }
            catch (HomeException homeEx)
            {
                el.thrown(new IllegalPropertyArgumentException(xinfo, xinfo.getName()
                        + " does not belong to any bundle category"));
            }
            catch (InvalidBundleApiException apiEx)
            {
                el.thrown(new IllegalPropertyArgumentException(xinfo, xinfo.getName()
                        + " does not belong to any bundle category"));
            }
        }
    }
}
