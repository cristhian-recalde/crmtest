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

import java.util.Set;

import com.trilogy.app.crm.bean.DiscountClass;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * 
 * Adapter to set the values of services, aux services, bundles and aux bundles
 * 
 * @author ankit.nagpal@redknee.com
 * since 9_7_2
 */
public class DiscountClassAdapter implements Adapter {

    /**
     * {@inheritDoc}
     */
    public Object adapt(Context ctx, Object obj) throws HomeException {
        
        DiscountClass discountClass = (DiscountClass) obj;
  
        //Adapt Service level discounts
        discountClass.setServiceLevelDiscount(discountClass.getServiceLevelDiscounts());
        
        return discountClass;
    }

    /**
     * {@inheritDoc}
     */
    public Object unAdapt(Context ctx, Object obj) throws HomeException {
    	DiscountClass discountClass = (DiscountClass) obj;
        //dependenygroup.setHashset("from unAdapt");
        
        //UnAdapt Services
        Set set = discountClass.getServiceLevelDiscount();
        discountClass.setServiceLevelDiscounts(set);

        return discountClass;
    }

}

