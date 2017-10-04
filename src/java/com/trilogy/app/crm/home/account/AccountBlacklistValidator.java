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
package com.trilogy.app.crm.home.account;

import java.util.Iterator;
import java.util.List;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BlackTypeEnum;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.account.AccountIdentificationXInfo;
import com.trilogy.app.crm.blacklist.BlackListSupport;

/**
 * Validate an account against the blacklist.
 *
 * @author candy.wong@redknee.com
 */
public class AccountBlacklistValidator implements Validator
{

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final Account account = (Account) obj;
        final CompoundIllegalStateException el = new CompoundIllegalStateException();

        final Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
		BlackTypeEnum overridden = null;
        try
        {
			if (SafetyUtil.safeEquals(
			    ctx.get(HomeOperationEnum.class, HomeOperationEnum.STORE),
			    HomeOperationEnum.CREATE))
            {
                /*
                 * TT 7112300016: account creation should fail if any IDs are on
                 * black list.
                 */
                List accountIdList = account.getIdentificationList();
                if(null != accountIdList)
                {
                    Iterator i = accountIdList.iterator();
                    while(i.hasNext())
                    {
                        AccountIdentification ai = (AccountIdentification)i.next();
                        BlackListSupport.validateIdBlacklist(ctx, ai.getIdType(), ai.getIdNumber(), AccountIdentificationXInfo.ID_NUMBER, el);
                    }
                }
            }
			overridden =
			    BlackListSupport.validateAccountBlacklist(ctx, account,
			        oldAccount, el);
			ctx.put(BlackListSupport.BLACKLIST_OVERRIDE_COLOUR, overridden);
        }
        catch (final HomeException hEx)
        {
            final String msg = "fail to validate account identifications due to Service Access configuration error";
            el.thrown(new IllegalPropertyArgumentException("Account.blackList", msg));
            new MinorLogMsg(this, msg, hEx).log(ctx);
        }

        el.throwAll();

    }
}
