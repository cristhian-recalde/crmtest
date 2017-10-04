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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;


/**
 * Creates an appropriate instance of an AccountMoveRequest from a given Account.
 * 
 * If given account is null, then a default AccountMoveRequest is created.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
class ConvertAccountBillingRequestFactory
{

    static ConvertAccountBillingTypeRequest getInstance(Context ctx, Account account)
    {
        ConvertAccountBillingTypeRequest request = new ConvertAccountBillingTypeRequest();

        if (account != null)
        {
            populateConversionAccountData(request, account);

            try
            {
                Collection<Subscriber> subscriptions = account.getSubscriptions(ctx, SubscriptionTypeEnum.AIRTIME);
                if (subscriptions != null && subscriptions.size() > 0)
                {
                    Subscriber subscription = subscriptions.iterator().next();
                    if (subscription != null)
                    {
                        request.setSubscriptionClass(subscription.getSubscriptionClass());
                    }
                }
            }
            catch (HomeException e)
            {
                new InfoLogMsg(ConvertAccountBillingRequestFactory.class,
                        "Error looking up airtime subscriptions for account " + account.getBAN() + ".", e).log(ctx);
            }
        }

        /*
         * Set the BAN to a unique temporary 'unset' value so that it will be set during
         * AccountHome.create() Hash code is used because it is unique and
         * DEFAULT_MOVE_PREFIX + hashCode < Account.BAN_WIDTH
         */
        request.setNewBAN(MoveConstants.DEFAULT_MOVE_PREFIX + request.hashCode());

        return request;
    }

    private static void populateConversionAccountData(ConvertAccountBillingTypeRequest request, final Account account)
    {
        request.setExistingBAN(account);
        request.setNewParentBAN(account.getParentBAN());

        if ( account.isPostpaid())
        {
            request.setSystemType(SubscriberTypeEnum.PREPAID);
        }
        else 
        {
            request.setSystemType(SubscriberTypeEnum.POSTPAID);            
        }

        request.setLastName(account.getLastName());
        request.setFirstName(account.getFirstName());
        request.setBillingAddress1(account.getBillingAddress1());
        request.setBillingAddress2(account.getBillingAddress2());
        request.setBillingAddress3(account.getBillingAddress3());
        request.setContactName(account.getContactName());
        request.setContactTel(account.getContactTel());
        request.setBillingCity(account.getBillingCity());
        request.setBillingCountry(account.getBillingCountry());
        request.setDateOfBirth(account.getDateOfBirth());
        request.setOccupation(account.getOccupation());
        request.setIdentificationGroupList(account.getIdentificationGroupList());
        request.setSecurityQuestionsAndAnswers(account.getSecurityQuestionsAndAnswers());
        request.setSpid(account.getSpid());
    }
}
