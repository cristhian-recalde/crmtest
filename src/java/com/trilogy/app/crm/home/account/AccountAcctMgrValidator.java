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

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.bean.account.AccountManager;
import com.trilogy.app.crm.bean.account.AccountTypeSelectionEnum;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.web.control.CustomAccountManagerKeyWebControl;


/**
 * Validates that the AccountManager exists in the system. This is used to validate values
 * entered through the API or Bulkloader.
 * 
 * @author ltang
 * 
 */
public class AccountAcctMgrValidator implements Validator
{

    protected static AccountAcctMgrValidator instance__ = null;


    /**
     * Prevents initialization
     */
    private AccountAcctMgrValidator()
    {
    }


    public static AccountAcctMgrValidator instance()
    {
        if (instance__ == null)
        {
            instance__ = new AccountAcctMgrValidator();
        }

        return instance__;
    }


    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        final Account account = (Account) obj;

        SysFeatureCfg sysCfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);

        // Validate account manager only if drop down feature is enabled
        if (sysCfg.getEnableAccountManagerDropdown())
        {
            String accountMgr = account.getAccountMgr();
            if (accountMgr != null)
            {
                accountMgr = accountMgr.trim();
                if (accountMgr.length() > 0
                    && !accountMgr.equals(CustomAccountManagerKeyWebControl.DEFAULT.getValue()))
                {
                    final CompoundIllegalStateException el = new CompoundIllegalStateException();

                    // Validate account manager Id
                    AccountManager existingMgr = null;
                    try
                    {
                        existingMgr = HomeSupportHelper.get(ctx).findBean(ctx, AccountManager.class, accountMgr);
                    }
                    catch (Exception e)
                    {
                        new MinorLogMsg(this, "Exception encountered looking up Account Manager Id " + accountMgr, e).log(ctx);
                    }
                    if (existingMgr == null)
                    {
                        el.thrown(new IllegalPropertyArgumentException(AccountXInfo.ACCOUNT_MGR, "Invalid Account Manager " + accountMgr + "."));
                    }
                    else
                    {
                        // Validate account manager selection configuration
                        AccountTypeSelectionEnum accountManagerSelection = sysCfg.getAccountManagerSelection();
                        if (AccountTypeSelectionEnum.ROOT.equals(accountManagerSelection))
                        {
                            if (account.getParentBAN() != null && account.getParentBAN().length() > 0)
                            {
                                el.thrown(new IllegalPropertyArgumentException(AccountXInfo.ACCOUNT_MGR,
                                        "Unable to assign Account Manager " + accountMgr + " to non root accounts."));
                            }
                        }
                        else if (AccountTypeSelectionEnum.RESPONSIBLE.equals(accountManagerSelection))
                        {
                            if (!account.getResponsible())
                            {
                                el.thrown(new IllegalPropertyArgumentException(AccountXInfo.ACCOUNT_MGR,
                                        "Unable to assign Account Manager " + accountMgr + " to non responsible accounts."));
                            }
                        }
                    }

                    el.throwAll();
                }
            }
        }
    }
}
