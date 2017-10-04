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
package com.trilogy.app.crm.account.state.agent;

import java.util.Iterator;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.BlackTypeEnum;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.blacklist.BlackListSupport;
import com.trilogy.app.crm.support.EnumStateSupportHelper;

/**
 * Agent for blacklisting account.
 *
 * @author joe.chen@redknee.com
 */
public class AccountStateBlackListAgent extends AccountStateAgentHome
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>AccountStateBlackListAgent</code>.
     *
     * @param ctx
     *            The operating context.
     * @param delegate
     *            Delegate of this agent.
     */
    public AccountStateBlackListAgent(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onStateChange(final Context ctx, final Account oldAccount, final Account newAccount)
        throws HomeException
    {

        boolean addToBlackList = false;
        BlackTypeEnum colour = null;
        List accountIdList = newAccount.getIdentificationList();

        /*
         * [Cindy] 2007-11-26: [HLD 38717] ID is blacklisted when entering IN_COLLECTION.
         * The old code used to blacklist when transitioning from IN_COLLECTION ->
         * INACTIVE.
         */
        if (EnumStateSupportHelper.get(ctx).isEnteringState(oldAccount, newAccount, AccountStateEnum.IN_COLLECTION))
        {
            colour = BlackTypeEnum.BLACK;
            addToBlackList = true;
        }

        /*
         * [Cindy] 2007-11-26: [HLD 38716] ID is graylisted when entering IN_ARREARS. The
         * old code used to graylist when entering any state *other than* IN_ARREARS.
         */
        else if (EnumStateSupportHelper.get(ctx).isEnteringState(oldAccount, newAccount, AccountStateEnum.IN_ARREARS))
        {
            colour = BlackTypeEnum.GRAY;
            addToBlackList = true;
        }

        /*
         * [Cindy] 2007-11-27: [HLD 38716] ID is removed from graylist when leaving
         * IN_ARREARS. It is by choice that even if the same ID is associated with more
         * than one IN_ARREARS account, and one account is moving out of IN_ARREARS, the
         * ID is still removed from graylist.
         */
        else if (EnumStateSupportHelper.get(ctx).isLeavingState(oldAccount, newAccount, AccountStateEnum.IN_ARREARS))
        {
            if(null != accountIdList)
            {
                Iterator i = accountIdList.iterator();
                while(i.hasNext())
                {
                    AccountIdentification ai = (AccountIdentification)i.next();
                    BlackListSupport.removeIdFromGrayList(ctx, ai.getIdType(), ai.getIdNumber());
                }
            }
        }

        if (addToBlackList)
        {
            if(null != accountIdList)
            {
                Iterator i = accountIdList.iterator();
                while(i.hasNext())
                {
                    AccountIdentification ai = (AccountIdentification)i.next();
                    addToBlacklist(ctx, ai.getIdType(), ai.getIdNumber(), newAccount.getBAN(), newAccount.getState(), colour);
                }
            }
        }
    }


    /**
     * Adds account ID(s) to blacklist.
     *
     * @param context
     *            The operating context.
     * @param idType
     *            ID type.
     * @param idNumber
     *            ID number.
     * @param ban
     *            Account BAN.
     * @param state
     *            Account new state.
     * @param colour
     *            Colour of the blacklist item.
     * @throws HomeException
     *             Thrown if there are problems adding the IDs of the account to
     *             blacklist.
     */
    protected void addToBlacklist(final Context context, final int idType, final String idNumber, final String ban,
        final AccountStateEnum state, final BlackTypeEnum colour) throws HomeException
    {
        if (idNumber != null && idNumber.trim().length() > 0)
        {
            BlackListSupport.addIdToBlackList(context, idType, idNumber, ban, state, colour);
        }
    }
}
