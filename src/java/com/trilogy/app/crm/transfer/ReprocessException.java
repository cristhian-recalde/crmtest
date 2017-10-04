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
package com.trilogy.app.crm.transfer;

import com.trilogy.framework.xhome.home.HomeException;

/**
 * Exception for Transfer Exception Reprocessing Pipeline.
 *
 * @author ling.tang@redknee.com
 */
public class ReprocessException extends HomeException
{
    public ReprocessException(final String s)
    {
        super(s);
    }

    public ReprocessException(final String s, final Throwable t)
    {
        super(s, t);
    }
}
