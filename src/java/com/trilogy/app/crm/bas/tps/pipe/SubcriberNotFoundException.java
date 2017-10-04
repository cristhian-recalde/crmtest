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
package com.trilogy.app.crm.bas.tps.pipe;

import com.trilogy.framework.xhome.home.HomeException;

/**
 * This Class was added when porting the new Account Level Payment Splitting Logic from 
 * CRM 7.3
 * @since 7.3, ported to 8.2, Sept 21, 2009. 
 * 
 * 
 * @author Larry Xia
 * @author Angie Li
 *
 */

public class SubcriberNotFoundException extends HomeException 
{

    public SubcriberNotFoundException(String arg0) {
        super(arg0);
    }
    
    public SubcriberNotFoundException(Throwable arg0) {
        super(arg0);
    }
    
    public SubcriberNotFoundException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
}
