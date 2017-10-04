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
package com.trilogy.app.crm.home.account;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.AccountsDiscount;
import com.trilogy.app.crm.bean.DiscountClass;
import com.trilogy.app.crm.bean.DiscountClassXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * Validates the presence of proper Discount class is used
 * 
 * @author kumaran.sivasubramaniam@redknee.com
 */
public final class AccountDiscountClassValidator implements Validator
{

    private static final AccountDiscountClassValidator INSTANCE = new AccountDiscountClassValidator();


    /**
     * Prevents initialization
     */
    private AccountDiscountClassValidator()
    {
    }


    public static AccountDiscountClassValidator instance()
    {
        return INSTANCE;
    }


    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        final Account account = (Account) obj;
        final int discountClassId = account.getDiscountClass();
        
        //Only validate Postpaid and Hybrid accounts.
        if (discountClassId == Account.DEFAULT_DISCOUNTCLASS ||discountClassId == AccountsDiscount.DEFAULT_DISCOUNTCLASS || account.isPrepaid())
        {
            return;
        }
        
        final Object condition = new EQ(DiscountClassXInfo.ID, discountClassId);
        try
        {
            DiscountClass discountClass = HomeSupportHelper.get(ctx).findBean(ctx, DiscountClass.class, condition);
            if (discountClass == null && discountClassId != Account.DEFAULT_DISCOUNTCLASS)
            {
                cise.thrown(new IllegalPropertyArgumentException(AccountXInfo.DISCOUNT_CLASS, "Discount-Class ID ["
                        + discountClassId + "] was not found."));
            }
            else if (discountClass != null && discountClass.getSpid() != account.getSpid())
            {
                cise.thrown(new IllegalPropertyArgumentException(AccountXInfo.DISCOUNT_CLASS, "Discount-Class ID ["
                        + discountClassId + "] Spid does not match Account Spid."));
            }
        }
        catch (Exception e)
        {
            final String errorMessage = String.format("Discount-Class ID [%s] coluld not be validated because of error [%s].", discountClassId, e.getMessage());
            new InfoLogMsg(this, errorMessage, e).log(ctx);
            cise.thrown(new IllegalPropertyArgumentException(AccountXInfo.DISCOUNT_CLASS, errorMessage));
        }

        cise.throwAll();
    }
}
