/**
 * @Filename : SubscriberMisConfigedException.java
 * @Author   : Daniel Zhang
 * @Date     : Jul 17, 2004
 * 
 *  Copyright (c) Redknee, 2004
 *        - all rights reserved
 */

package com.trilogy.app.crm.bas.recharge;

import com.trilogy.framework.xhome.home.HomeException;

/**
 * Class Description:
 */
public class SubscriberMisConfigedException extends HomeException {

	/**
	 * Constructor
	 * @param arg0
	 * @param arg1
	 */
	public SubscriberMisConfigedException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
