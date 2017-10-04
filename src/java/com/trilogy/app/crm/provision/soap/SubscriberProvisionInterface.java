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
package com.trilogy.app.crm.provision.soap;

import electric.util.holder.intOut;

/**
 * @author imahalingam
 * @author amit.baid@redknee.com
 */

public interface SubscriberProvisionInterface
{
    //Return codes.
    int SUCCESSFUL = 0;
    int LOGIN_FAILED = 1;
    int MSISDN_DOES_NOT_EXIST = 2;
    int DEACTIVATE_FAILED = 3;

    SubscriberInfo getSub(final String userName, final String password, String msisdn, intOut retCode)
        throws SoapServiceException;

    int deactivateSub(final String userName, final String password, String msisdn) throws SoapServiceException;

    int deleteSub(final String userName, final String password, String msisdn) throws SoapServiceException;
}
