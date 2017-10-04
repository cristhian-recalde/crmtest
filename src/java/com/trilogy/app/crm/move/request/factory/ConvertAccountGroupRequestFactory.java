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
package com.trilogy.app.crm.move.request.factory;

import java.util.Collection;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;


/**
 * Creates an appropriate instance of an AccountMoveRequest from a given Account.
 * 
 * If given account is null, then a default AccountMoveRequest is created.
 *
 * @author Marcio Marques
 * @since 9.1.3
 */
class ConvertAccountGroupRequestFactory
{

    static ConvertAccountGroupTypeRequest getInstance(Context ctx, Account account)
    {
        ConvertAccountGroupTypeRequest request = new ConvertAccountGroupTypeRequest();

        if (account != null)
        {
            populateConversionAccountData(ctx, request, account);
        }

        /*
         * Set the BAN to a unique temporary 'unset' value so that it will be set during
         * AccountHome.create() Hash code is used because it is unique and
         * DEFAULT_MOVE_PREFIX + hashCode < Account.BAN_WIDTH
         */
        request.setNewBAN(MoveConstants.DEFAULT_MOVE_PREFIX + request.hashCode());

        return request;
    }

    private static void populateConversionAccountData(Context ctx, ConvertAccountGroupTypeRequest request, final Account account)
    {
        request.setExistingBAN(account);
        request.setNewParentBAN(account.getParentBAN());
        request.setExistingGroupType(account.getGroupType());
        request.setIndividualSubscriberSubAccount(account.isIndividual(ctx));

        request.setSpid(account.getSpid());
    }
}
