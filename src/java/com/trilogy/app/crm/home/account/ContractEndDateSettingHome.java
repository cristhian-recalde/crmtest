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

import java.util.Calendar;
import java.util.Date;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.payment.Contract;
import com.trilogy.app.crm.bean.payment.ContractFeeFrequencyEnum;
import com.trilogy.app.crm.support.ContractSupport;

/**
 * This Home decorator sets the contract end date according to the contract selected 
 * and the contract start date.
 *
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class ContractEndDateSettingHome extends HomeProxy
{
    public ContractEndDateSettingHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }

    public static Contract getContract(Context ctx, Account account)
    {
        if (account == null)
        {
            return null;
        }

        Contract contract = null;
        long id = account.getContract();
        if (id < 0)
        {
            return null;
        }

        try
        {
            contract = ContractSupport.findContract(ctx, id);
        }
        catch (HomeException exception)
        {
            LogSupport.info(ctx, ContractEndDateSettingHome.class, 
                "Cannot retrieve contract " + account.getContract() + " of account " + 
                account.getBAN(), exception);
        }

        return contract;
    }

    /**
     * @param contract
     * @param account
     * @return Date [Calculated Contract's End DATE based upon the Account's ContractStartDate & contract configuration]
     */
    public static Date calculateContractEndDate(Contract contract, Account account)
    {
        Date contractStartDate = account.getContractStartDate();
        Date contractEndDate = null;
        ContractFeeFrequencyEnum frequency = contract.getDurationFrequency();
        int duration = (int)contract.getDuration();
        int contractNumOfDays = 0;

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(contractStartDate);

        if(frequency == ContractFeeFrequencyEnum.DAY)
        {
            calendar.add(Calendar.DAY_OF_YEAR, duration);
        }
        else if(frequency == ContractFeeFrequencyEnum.MONTH)
        {
            calendar.add(Calendar.MONTH, duration);
        }
        else if(frequency == ContractFeeFrequencyEnum.YEAR)
        {
            calendar.add(Calendar.YEAR, duration);
        }

        calendar.set(Calendar.MILLISECOND,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        contractEndDate = calendar.getTime();

        return contractEndDate;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final Account newAccount = (Account) obj;

        Contract contract = getContract(ctx, newAccount);
        if (contract != null)
        {
            newAccount.setContractEndDate(calculateContractEndDate(contract, newAccount));
        }
        return super.create(ctx, obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        final Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
        final Account newAccount = (Account) obj;

        if (oldAccount.getContract() != newAccount.getContract() ||
            !SafetyUtil.safeEquals(oldAccount.getContractStartDate(), 
            newAccount.getContractStartDate()))
        {
            Contract contract = getContract(ctx, newAccount);
            if (contract != null)
            {
                newAccount.setContractEndDate(calculateContractEndDate(contract, newAccount));
            }
        }
        return super.store(ctx, obj);
    }
}

