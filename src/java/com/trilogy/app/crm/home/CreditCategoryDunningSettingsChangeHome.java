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

import java.util.Date;

import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.CreditCategoryXInfo;
import com.trilogy.app.crm.dunning.DunningProcessServer;
import com.trilogy.app.crm.dunning.visitor.accountprocessing.DunningProcessingAccountXStatementVisitor;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Home responsible to run the dunning process on all dunned accounts with a given credit
 * category when this credit category becomes dunning exempt.
 * 
 * @author Marcio Marques
 * @since 9.0
 * 
 */
public class CreditCategoryDunningSettingsChangeHome extends HomeProxy
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public CreditCategoryDunningSettingsChangeHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * {@inheritDoc}
     */
    public Object store(Context ctx, Object obj) throws HomeException
    {
        CreditCategory creditCategory = (CreditCategory) obj;
        CreditCategory oldCreditCategory = (CreditCategory) find(ctx, new EQ(CreditCategoryXInfo.CODE, Integer
                .valueOf(creditCategory.getCode())));
        CreditCategory result = (CreditCategory) super.store(ctx, creditCategory);
        if (result.isDunningExempt() && !oldCreditCategory.isDunningExempt())
        {
            DunningProcessingAccountXStatementVisitor visitor = new DunningProcessingAccountXStatementVisitor(
                    new Date(), null);
            try
            {
                
            	visitor.visit(ctx, AccountSupport.getQueryForDunnProcessWithCrediCategory(ctx, Integer.valueOf(creditCategory.getCode())));
            }
            catch (AgentException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Unable to set all dunned accounts on credit category '");
                sb.append(creditCategory.getCode());
                sb.append("' to '");
                sb.append(AccountStateEnum.ACTIVE);
                sb.append("' :");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }
        }
        return result;
    }
}
