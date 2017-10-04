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
package com.trilogy.app.crm.cltc;

/**
 * @author chandrachud.ingale
 * @since 9.6
 */
public class SubCltcOperationCode
{
    public static final int CLCT_DEFAULT                           = 0;
    public static final int DEBIT                                  = 0;
    public static final int CREDIT                                 = 1;
    public static final int INCREASE_CREDIT_LIMIT                  = 2;
    public static final int DECREASE_CREDIT_LIMIT                  = 3;
    public static final int REPLACE_SUBSCRIBER_ACCOUNT             = 4;
    public static final int UPDATE_SUBSCRIBER_ACCOUNT_BALANCE      = 5;
    public static final int UPDATE_SUBSCRIBER_ACCOUNT_CREDIT_LIMIT = 6;
    public static final int BUNDLE_BALANCE_DEPLETED                = 8;
    public static final int BUNDLE_PROVISIONED                     = 9;
    public static final int BUNDLE_BALANCE_TOPPED                  = 10;
    public static final int BUNDLE_BALANCE_DECREASED               = 11;
}
