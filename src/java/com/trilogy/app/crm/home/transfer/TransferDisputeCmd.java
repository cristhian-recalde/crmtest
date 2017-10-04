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

package com.trilogy.app.crm.home.transfer;

public class TransferDisputeCmd
{
    public TransferDisputeCmd(long transferDisputeId, String cmd)
    {
        transferDisputeId_ = transferDisputeId;
        cmd_ = cmd;
    }

    public long transferDisputeId_;
    public String cmd_;

    public final static String CANCEL_CMD = "CANCEL";
    public final static String ACCEPT_CMD = "ACCEPT";
}