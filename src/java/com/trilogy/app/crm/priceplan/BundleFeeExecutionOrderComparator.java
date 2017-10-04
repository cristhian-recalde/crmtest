/* 
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries. 
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.priceplan;

import java.util.Comparator;

import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author bdhavalshankh
 * @since 9.6
 *
 */
public class BundleFeeExecutionOrderComparator extends ContextAwareSupport implements Comparator<BundleFee>
{
    public final String PACKAGE_SOURCE = "Package";
    public final String AUXILIARY_SOURCE = "Auxiliary";
    
    public BundleFeeExecutionOrderComparator(Context ctx, boolean provisioning)
    {
        context_ = ctx;
        provisioning_ = provisioning;
    }

    public int compare(BundleFee o1, BundleFee o2)
    {
        boolean defaultCases = true;
        
        int result = 0;
        try
        {
           
            if (o1 == null)
            {
                result = -1;
            }
            else if (o2 == null)
            {
                result = 1;
            }
            
            else if (o1.getSource().startsWith(PACKAGE_SOURCE))
            {
                return -1;
            }
            else if (o2.getSource().startsWith(PACKAGE_SOURCE))
            {
                return 1;
            }

            if (AUXILIARY_SOURCE.equals(o1.getSource()))
            {
                return 1;
            }
            else if (AUXILIARY_SOURCE.equals(o2.getSource()))
            {
                return -1;
            }
            
            
            else
            {
                BundleProfile b1 = o1.getBundleProfile(getContext());
                BundleProfile b2 = o2.getBundleProfile(getContext());
                
                if (b1 == null)
                {
                    result = -1;
                }
                else if (b2 == null)
                {
                    result = 1;
                }
                else
                {
                    defaultCases = false;
                    if (provisioning_)
                    {
                        result = BundleProfile.PROVISIONING_ORDER.compare(b1, b2);
                    }
                    else
                    {
                        result = BundleProfile.UNPROVISIONING_ORDER.compare(b1, b2);
                    }
                    
                }
            }
            
        }
        catch (HomeException e)
        {
            LogSupport.minor(getContext(), this, "Unable to retrieve one of the bundles being compared: bundle1 = "
                    + o1.getId() + ", bundle2 = " + o2.getId() + "Exception = " + e.getMessage(), e);
        }
        catch (Exception e)
        {
            LogSupport.minor(getContext(), this, "Unable to retrieve one of the bundles being compared: bundle1 = "
                    + o1.getId() + ", bundle2 = " + o2.getId() + "Exception = " + e.getMessage(), e);
        }

        if (!provisioning_ && defaultCases)
        {
            result = -result;
        }
        
        return result;
    }
    
    private boolean provisioning_;

}
