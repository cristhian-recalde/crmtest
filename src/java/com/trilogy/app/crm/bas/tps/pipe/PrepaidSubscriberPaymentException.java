/**
 * @Filename : PrepaidSubscriberPaymentException.java
 * @Author   : Daniel Zhang
 * @Date     : Jul 16, 2004
 * 
 *  Copyright (c) Redknee, 2004
 *        - all rights reserved
 */

package com.trilogy.app.crm.bas.tps.pipe;

import com.trilogy.framework.xhome.home.HomeException;

/**
 * Class Description:
 * 
 * Changed to extend HomeException when Porting Account Level Payment Splitting Strategy from CRM 7.3
 * @since 7.3, ported to 8.2 Sept 21, 2009.
 */
public class PrepaidSubscriberPaymentException extends HomeException {
    
    /**
     * Constructor
     * @param arg0
     */
    public PrepaidSubscriberPaymentException(String arg0) {
        super(arg0);
    }
    /**
     * Constructor
     * @param arg0
     */
    public PrepaidSubscriberPaymentException(Throwable arg0) {
        super(arg0);
    }
    /**
     * Constructor
     * @param arg0
     * @param arg1
     */
    public PrepaidSubscriberPaymentException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
}
