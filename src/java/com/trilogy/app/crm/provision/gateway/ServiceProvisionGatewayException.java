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
package com.trilogy.app.crm.provision.gateway;

import java.util.List;


/**
 * Indicates that an exception occurred at the CORBA level for the
 * SubscriberProfileProvision interface.
 *
 * @author victor.stratan@redknee.com
 */
public class ServiceProvisionGatewayException extends Exception
{
    /**
     * Creates a new SubscriberProfileProvisionCorbaException.
     *
     * @param resultCode The result code.
     * @param message The exception message.
     */
    public ServiceProvisionGatewayException(final int resultCode, final String message)
    {
        super(message);
        this.resultCode_ = resultCode;
        this.failedServices_ = null;
    }

    /**
     * Creates a new SubscriberProfileProvisionCorbaException.
     *
     * @param resultCode The result code.
     * @param message The exception message.
     */
    public ServiceProvisionGatewayException(final int resultCode, final String message, final List<Long> errorSrvs)
    {
        super(message);
        this.resultCode_ = resultCode;
        this.failedServices_ = errorSrvs;
    }

    /**
     * Creates a new SubscriberProfileProvisionCorbaException.
     *
     * @param resultCode The result code.
     * @param message The exception message.
     * @param cause The linked cause.
     */
    public ServiceProvisionGatewayException(final int resultCode, final String message, final Throwable cause)
    {
        super(message, cause);
        this.resultCode_ = resultCode;
        this.failedServices_ = null;
    }


    public int getResultCode()
    {
        return resultCode_;
    }

    public List<Long> getFailedServices()
    {
        return failedServices_;
    }

    /**
     * SPG Result Code
     */
    private final int resultCode_;

    /**
     * SPG Failed Services
     */
    private final List<Long> failedServices_;

    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 1L;
}
