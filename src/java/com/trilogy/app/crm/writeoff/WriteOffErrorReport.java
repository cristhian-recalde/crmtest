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

import java.io.PrintWriter;
import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;


/**
 * 
 * 
 * @author alpesh.champeneri@redknee.com
 */
final class WriteOffErrorReport extends CsvReport
{

    WriteOffErrorReport(ResultCode rCode, String msg)
    {
        super(Size);
        fields[Time] = FULL_DATE_FORMAT.format(new Date());
        fields[ErrorCode] = String.valueOf(rCode.ordinal());
        if (msg != null && msg.length() > 0)
        {
            fields[Message] = msg;
        }
        else
        {
            fields[Message] = rCode.toString();
        }
    }


    WriteOffErrorReport(Account account, ResultCode rCode, String msg)
    {
        this(rCode, msg);
        fields[BAN] = account.getBAN();
        fields[AccountState] = account.getState().toString();
    }


    WriteOffErrorReport(Account account, Subscriber sub, ResultCode rCode, String msg)
    {
        this(account, rCode, msg);
        fields[SubId] = sub.getId();
        fields[SubState] = sub.getState().toString();
    }


    public void setWriteOffAmount(long amount)
    {
        fields[WriteOffAmount] = String.valueOf(amount);
    }

    private static final int Time = 0;
    private static final int ErrorCode = Time + 1;
    private static final int Message = ErrorCode + 1;
    private static final int BAN = Message + 1;
    private static final int AccountState = BAN + 1;
    private static final int SubId = AccountState + 1;
    private static final int SubState = SubId + 1;
    private static final int WriteOffAmount = SubState + 1;
    private static final int Size = WriteOffAmount + 1;

    public static enum ResultCode {
        SYSTEM_ERROR {

            public String toString()
            {
                return "System Error";
            }
        },
        GENERAL_ERROR {

            public String toString()
            {
                return "General Error";
            }
        },
        ACCOUNT_WRITE_OFF_FAILURE {

            public String toString()
            {
                return "Failed to write off account";
            }
        },
        WRITE_OFF_TRXN_FAILURE {

            public String toString()
            {
                return "Failed to apply Write Off transaction";
            }
        },
        // WRITE_OFF_TAX_FAILURE {public String toString(){return
        // "Failed to apply Write Off Tax transaction";}},
        SUBSCRIBER_DEACTIVATION_FAILURE {

            public String toString()
            {
                return "Failed to deactivate the subscriber";
            }
        },
        ACCOUNT_DEACTIVATION_FAILURE {

            public String toString()
            {
                return "Failed to deactivate the account";
            }
        }
    }


    public static void logError(PrintWriter errLog, ResultCode code, String msg, Account account, Subscriber sub)
    {
        WriteOffErrorReport er = new WriteOffErrorReport(account, sub, code, msg);
        er.print(errLog);
    }


    public static void logError(PrintWriter errLog, ResultCode code, String msg, Account account)
    {
        WriteOffErrorReport er = new WriteOffErrorReport(account, code, msg);
        er.print(errLog);
    }


    public static void logError(PrintWriter errLog, ResultCode rCode, String msg)
    {
        WriteOffErrorReport er = new WriteOffErrorReport(rCode, msg);
        er.print(errLog);
    };
}
