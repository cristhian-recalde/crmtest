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
package com.trilogy.app.crm.api.rmi.support;

/**
 * @author sbanerjee
 *
 */
public class ErrorMessageConstants
{
    public static final String ERROR_BUNDLE_TOPUP_SERVICE_DOWN = "Could not invoke 'bundle topup' as the service 'com.redknee.app.crm.client.urcs.AccountOperationsClientV4' is down";
    public static final String BUNDLE_TOPUP_ROLL_BACK_FAILED = "Bundle Topup Roll-Back failed: ";
    public static final String BUNDLE_TOPUP_CHARGES_DEBIT_FAILED = "Bundle Topup charges debit failed: ";
    public static final String BUNDLE_TOPUP_FAILED_URCS_FAILURE = "'bundle topup' failed at URCS: ";
}
