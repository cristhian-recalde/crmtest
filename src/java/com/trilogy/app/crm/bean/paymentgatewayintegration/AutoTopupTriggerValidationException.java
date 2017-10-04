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
package com.trilogy.app.crm.bean.paymentgatewayintegration;

import com.trilogy.framework.xhome.home.HomeException;


/**
 * 
 *
 * @author chandrachud.ingale
 * @since 9.9
 */
public class AutoTopupTriggerValidationException extends HomeException
{

    private static final long serialVersionUID = -5202157170448478588L;


    /**
     * @param s
     */
    public AutoTopupTriggerValidationException(String s)
    {
        super(s);
    }


    /**
     * @param t
     */
    public AutoTopupTriggerValidationException(Throwable t)
    {
        super(t);
    }


    /**
     * @param s
     * @param t
     */
    public AutoTopupTriggerValidationException(String s, Throwable t)
    {
        super(s, t);
    }

}
