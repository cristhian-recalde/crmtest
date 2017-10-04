package com.trilogy.app.crm.priceplan;

import java.util.Iterator;

import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.core.BundleFee;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

public class PPVChargeValidator implements Validator
{
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        PricePlanVersion ppv = (PricePlanVersion) obj;
        final CompoundIllegalStateException el = new CompoundIllegalStateException();
        try {
            for ( Iterator i = ppv.getServicePackageVersion().getPackageFees().values().iterator(); i.hasNext();)
            {
                ServicePackageFee fee = (ServicePackageFee) i.next(); 
                if ( fee.getFee() < 0)
                {
                    el.thrown(new IllegalArgumentException("Charge for service package  " + fee.getPackageId() + " is negative."));
                }
            }
        
            for ( Iterator i = ppv.getServicePackageVersion().getBundleFees().values().iterator(); i.hasNext();)
            {
                BundleFee fee = (BundleFee) i.next(); 
                if (fee.getFee() < 0 )
                {
                    el.thrown(new IllegalArgumentException("Charge for bundle  " + fee.getId() + " is negative."));                    
                }
            }
        } catch ( Throwable t)
        {
            el.thrown(new IllegalArgumentException("Exception caught while validating PPV "));                                 
        } finally 
        {                
            el.throwAll();
        }
    }
}