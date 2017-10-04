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
package com.trilogy.app.crm.home.accountmanager;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.PatternMismatchException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.util.pattern.XPattern;

import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.bean.account.AccountManager;
import com.trilogy.app.crm.bean.account.AccountManagerXInfo;


/**
 * Validates the Account Manager Id against the configured Regex
 * 
 * @author ltang
 * 
 */
public class AccountManagerIdValidator implements Validator
{

    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        CompoundIllegalStateException el = new CompoundIllegalStateException();

        AccountManager bean = (AccountManager) obj;
        SysFeatureCfg cfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);

        String id = bean.getAccountMgrId();
        XPattern pattern = cfg.getAccountManagerIDXPattern();
        if (!pattern.matcher(id).matches())
        {
            el.thrown(new PatternMismatchException(AccountManagerXInfo.ACCOUNT_MGR_ID, id, pattern.getPattern()));
        }

        el.throwAll();
    }
}
