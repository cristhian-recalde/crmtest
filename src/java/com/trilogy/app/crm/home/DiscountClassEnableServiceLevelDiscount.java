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
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.DiscountClass;
import com.trilogy.app.crm.bean.DiscountClassXInfo;
import com.trilogy.app.crm.bean.DiscountTypesEnum;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.app.crm.bean.*;


/**
 * The class validates that Invoice level discount is not modified to custom (service) level discount and vice-versa
 * 
 * @author ankit.nagpal@redknee.com
 * since 9_7_2
 */
public class DiscountClassEnableServiceLevelDiscount extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a a new AuxiliaryServiceAdjustmentTypeCreationHome.
     * 
     * @param delegate
     *            The home to which we delegate.
     */
    public DiscountClassEnableServiceLevelDiscount(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, Object bean) throws HomeException
    {
        DiscountClass discountClass = (DiscountClass) bean;
        
        if (discountClass.getDiscountType() == DiscountCriteriaEnum.FLAT) 
        {
        	discountClass.setDiscountPercentage(0);
        }
        else
        {
        	discountClass.setDiscountFlat(0);
        }
        	
        
        DiscountClass dc = HomeSupportHelper.get(ctx).findBean(ctx, DiscountClass.class, new EQ(DiscountClassXInfo.ID, ((DiscountClass) bean).getId()));
        if (dc.isEnableServiceLevelDiscount() != discountClass.isEnableServiceLevelDiscount())    
        {
        	throw new HomeException("Cannot modify service level discount to legacy discount and vice versa");
        }
        return super.store(ctx, discountClass);
    }
}
