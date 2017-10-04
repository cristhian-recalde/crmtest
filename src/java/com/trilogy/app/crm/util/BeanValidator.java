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
package com.trilogy.app.crm.util;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;

/**
 * @author jchen
 */
public class BeanValidator {

    /**
     * Validating phone length
     * @param propertyName
     * @param phoneNumber
     */
    public static void validatePhoneLength(String propertyName, String phoneNumber)
    {
        if ( phoneNumber != null && phoneNumber.length() > 0 && phoneNumber.length() < 7)
            throw new IllegalPropertyArgumentException(propertyName, "Length smaller than 7.");
    }
}
