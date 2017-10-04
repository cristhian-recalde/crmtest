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

import com.trilogy.app.crm.bundle.QuotaTypeEnum;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.BundleProfileXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;


/**
 * Validator for the Re-purchable bundles.
 * 
 * @author vijay.gote
 * Since 9.6
 */
public class RePurchasableBundleValidator implements Validator
{

    /**
     * @param ctx
     * @param obj
     * @throws IllegalStateException
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        CompoundIllegalStateException el = new CompoundIllegalStateException();
        BundleProfile bundleProfile = (BundleProfile) obj;
        try
        {
            //--Re-Purchasable bundles must have Moving Quota scheme.
            if (bundleProfile.isRepurchasable()
                    && !(QuotaTypeEnum.MOVING_QUOTA.equals(bundleProfile.getQuotaScheme())))
            {
                el.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.QUOTA_SCHEME,
                        "Re-Purchasable bundle should have Moving Quota scheme"));
            }
        }
        finally
        {
            el.throwAll();
        }
    }
}
