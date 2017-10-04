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
package com.trilogy.app.crm.transfer.contract;

/**
 * 
 * @author arturo.medina@redknee.com
 *
 */
public class TransferAgreementNotFoundException extends Exception
{

    public TransferAgreementNotFoundException()
    {
    }

    public TransferAgreementNotFoundException(String message)
    {
        super(message);
    }

    public TransferAgreementNotFoundException(Throwable cause)
    {
        super(cause);
    }

    public TransferAgreementNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
