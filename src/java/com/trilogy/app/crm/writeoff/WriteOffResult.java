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

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;


/**
 * 
 * 
 * @author alpesh.champeneri@redknee.com
 */
final public class WriteOffResult extends CsvReport
{

    WriteOffResult(Context ctx, String ban, long writeOffAmount)
    {
        super(11);
        fields[BAN] = ban;
        fields[WriteOffAmount] = String.valueOf(writeOffAmount);
        fields[FormattedWriteOffAmount] = WriteOffSupport.getFormattedCurrencyValue(ctx, writeOffAmount);
    }

    private static final int BAN = 0;
    private static final int BillCycleId = 1;
    private static final int LastName = 2;
    private static final int FirstName = 3;
    private static final int CreditTransactionRefNum = 4;
    private static final int AccountName = 5;
    private static final int AccountState = 6;
    private static final int WriteOffAmount = 7;
    private static final int FormattedWriteOffAmount = 8;
    private static final int WriteOffDate = 9;
    private static final int IsAccountResponsible = 10;


    public WriteOffResult setBillCycleId(int billCycleId)
    {
        fields[BillCycleId] = String.valueOf(billCycleId);
        return this;
    }


    public WriteOffResult setLastName(String lastName)
    {
        fields[LastName] = lastName;
        return this;
    }


    public WriteOffResult setFirstName(String firstName)
    {
        fields[FirstName] = firstName;
        return this;
    }


    public WriteOffResult setCreditTxnRefNum(long num)
    {
        fields[CreditTransactionRefNum] = String.valueOf(num);
        return this;
    }


    public WriteOffResult setAccountName(String name)
    {
        fields[AccountName] = name;
        return this;
    }


    public WriteOffResult setAccountState(int accountState)
    {
        fields[AccountState] = String.valueOf(accountState);
        return this;
    }


    public WriteOffResult setDate(Date date)
    {
        fields[WriteOffDate] = DATE_FORMAT.format(date);
        return this;
    }


    public WriteOffResult setAccountResponsible(boolean bResponsible)
    {
        fields[IsAccountResponsible] = bResponsible ? "t" : "f";
        return this;
    }
}
