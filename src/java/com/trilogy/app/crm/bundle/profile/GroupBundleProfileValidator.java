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

import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.bundle.GroupChargingTypeEnum;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;


public class GroupBundleProfileValidator implements Validator
{

    @Override
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        if(null != obj && obj instanceof BundleProfile)
        {
            BundleProfile bundleProfile = (BundleProfile) obj;
            if( GroupChargingTypeEnum.GROUP_BUNDLE ==  bundleProfile.getGroupChargingScheme()  && !bundleProfile.isAuxiliary())
                
            {
                throw new IllegalStateException("Group Bundle need to be auxilairy");
            }
        }
        
    }
}
