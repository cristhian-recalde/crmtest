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
package com.trilogy.app.crm.writeoff;

import com.trilogy.framework.xhome.context.Context;

/**
 * 
 * 
 * @author alpesh.champeneri@redknee.com
 */
final class VatReclaimableReport extends CsvReport
{

    public VatReclaimableReport(Context ctx, int spid, String ban, long vatReclaimable)
    {
        super(Size);
        VatReclaimableReport.ctx = ctx;
        fields[SPID] = String.valueOf(spid);
        fields[BAN] = ban;
        fields[VatReclaimableAmount] = String.valueOf(vatReclaimable);
        fields[FormattedVatReclaimableAmount] = WriteOffSupport.getFormattedCurrencyValue(ctx, vatReclaimable);
    }

    private static Context ctx;
    private static final int SPID = 0;
    private static final int AccountType = 1;
    private static final int BAN = 2;
    private static final int BillCycleId = 3;
    private static final int LastName = 4;
    private static final int FirstName = 5;
    private static final int AccountName = 6;
    private static final int AccountState = 7;
    private static final int QualifyingAmount = 8;
    private static final int FormattedQualifyingAmount = 9;
    
    private static final int VatReclaimableAmount = 10;
    private static final int FormattedVatReclaimableAmount = 11;
    
    private static final int IsAccountResponsible = 12;
    private static final int ExtTxnNum = IsAccountResponsible + 1;
    private static final int Size = ExtTxnNum + 1;

    public VatReclaimableReport setQualifyingAmount(long amount)
    {
        fields[QualifyingAmount] = String.valueOf(amount);
        return this;
    }
    public VatReclaimableReport setFormattedQualifyingAmount(long amount)
    {
        fields[FormattedQualifyingAmount] = WriteOffSupport.getFormattedCurrencyValue(ctx, amount);
        return this;
    }

    public VatReclaimableReport setAccountType(long acctType)
    {
        fields[AccountType] = String.valueOf(acctType);
        return this;
    }


    public VatReclaimableReport setBillCycleId(int billCycleId)
    {
        fields[BillCycleId] = String.valueOf(billCycleId);
        return this;
    }


    public VatReclaimableReport setLastName(String lastName)
    {
        fields[LastName] = lastName;
        return this;
    }


    public VatReclaimableReport setFirstName(String firstName)
    {
        fields[FirstName] = firstName;
        return this;
    }


    public VatReclaimableReport setSpid(int spid)
    {
        fields[SPID] = String.valueOf(spid);
        return this;
    }


    public VatReclaimableReport setAccountName(String name)
    {
        fields[AccountName] = name;
        return this;
    }


    public VatReclaimableReport setAccountState(int accountState)
    {
        fields[AccountState] = String.valueOf(accountState);
        return this;
    }


    public VatReclaimableReport setAccountResponsible(boolean bResponsible)
    {
        fields[IsAccountResponsible] = bResponsible ? "t" : "f";
        return this;
    }


    public VatReclaimableReport setExtTxnNum(long num)
    {
        fields[ExtTxnNum] = String.valueOf(num);
        return this;
    }
}
