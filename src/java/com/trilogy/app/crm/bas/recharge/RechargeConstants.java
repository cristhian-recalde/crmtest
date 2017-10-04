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

package com.trilogy.app.crm.bas.recharge;

import com.trilogy.app.crm.bean.SubscriberStateEnum;


/**
 * Constants used in recharge process.
 *
 * @author larry.xia@redknee.com
 */
public interface RechargeConstants
{
	public static final String SUBSCRIBER_SERVICES_HOME = "SubscriberServiceHome";

    /**
     * Default recurring charge rate.
     */
    double DEFAULT_RECURRING_CHARGE_RATE = 1.0;

    /**
     * Result code for success.
     */
    int RECHARGE_SUCCESS = 0;

    /**
     * Result code for failure due to low ABM balance.
     */
    int RECHARGE_FAIL_ABM_LOWBALANCE = 1;

    /**
     * Result code for failure due to XHome.
     */
    int RECHARGE_FAIL_XHOME = 3;

    /**
     * Result code for failure due to price plan integrity.
     */
    int RECHARGE_FAIL_DATA_INTEGRITY_PRICEPLAN = 4;

    /**
     * Result code for failure due to expiration on ABM.
     */
    int RECHARGE_FAIL_ABM_EXPIRED = 2;

    /**
     * Result code for failure due to invalid ABM profile.
     */
    int RECHARGE_FAIL_ABM_INVALIDPROFILE = 6;

    /**
     * Result code for failure due to unknown OCG error.
     */
    int RECHARGE_FAIL_OCG_UNKNOWN = 5;

    /**
     * Result code for failure due to unknown error.
     */
    int RECHARGE_FAIL_UNKNOWN = 7;

    /**
     * Result code for failure due to duplicated charges.
     */
    int RECHARGE_FAIL_DUPLICATIONCHECKING = 8;

    /**
     * Result code for failure due to service not found.
     */
    int RECHARGE_FAIL_SERVICENOTFOUND = 9;

    /**
     * Result code for failure due to VPN leader not found.
     */
    int RECHARGE_FAIL_VPN_LEADER_NOT_FOUND = 10;

    /**
     * Result code for failure due to subscriber auxiliary service not found.
     */
    int RECHARGE_FAIL_SUBSCRIBER_AUXSERVICE_NOT_FOUND = 11;

    /**
     * Result code for subscriber level data.
     */
    int RECHARGE_RESULT_DATA_SUBSCRIBER_LEVEL = 12;

    /**
     * Result code for account level data.
     */
    int RECHARGE_RESULT_DATA_ACCOUNT_LEVEL = 13;

    /**
     * Result code for zero charge.
     */
    int RECHARGE_ZERO_CHARGE = 14;

    /**
     * Result code for failure due to invalid ABM state.
     */
    int RECHARGE_FAIL_ABM_INVALID_STATE = 15;

    /**
     * OCG success result code.
     */
    int OCG_RESULT_SUCCESS = 0;

    /**
     * OCG failure result code.
     */
    int OCG_RESULT_UNKNOWN = -1;

    /**
     * Account level error dummy charge item ID.
     */
    int ACCOUNT_LEVEL_ERROR_DUMMY_CHARGED_ITEM_ID = -1;

    /**
     * Subscriber level error dummy charge item ID.
     */
    int SUBSCRIBER_LEVEL_ERROR_DUMMY_CHARGED_ITEM_ID = -2;

    /**
     * System level error dummy charge item ID.
     */
    int SYSTEM_LEVEL_ERROR_DUMMY_CHARGED_ITEM_ID = -3;
    
    /*
	 * Key of caching spid extensions
	 *
	 * */
	
	public static final Object CACHED_SPID_EXTENSIONS = "CachedSpidExtensions";

    /**
     * Subscriber states eligible for recurring recharges.
     */
    int[] RECHARGE_SUBSCRIBER_STATES =
    {
        SubscriberStateEnum.ACTIVE_INDEX, SubscriberStateEnum.NON_PAYMENT_SUSPENDED_INDEX,
        SubscriberStateEnum.NON_PAYMENT_WARN_INDEX, SubscriberStateEnum.PROMISE_TO_PAY_INDEX,
    };

    int[] RECHARGE_SUBSCRIBER_STATES_SUSPENDED =
    {
        SubscriberStateEnum.SUSPENDED_INDEX, SubscriberStateEnum.IN_ARREARS_INDEX
    };
}
