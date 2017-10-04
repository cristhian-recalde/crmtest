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

/**
 * Interface for recurring recharge counters.
 *
 * @author larry.xia@redknee.com
 */
public interface RechargeVisitorCountable
{

    /**
     * Returns the total number of bundles recharges attempted.
     *
     * @return The total number of bundles recharges attempted.
     */
    int getBundleCount();


    /**
     * Returns the total number of failed bundles recharges.
     *
     * @return The total number of failed bundles recharges.
     */
    int getBundleCountFailed();


    /**
     * Returns the total number of successful bundles recharges.
     *
     * @return The total number of successful bundles recharges.
     */
    int getBundleCountSuccess();


    /**
     * Returns the total number of suspension of bundles due to failed recharges.
     *
     * @return The total number of suspension of bundles due to failed recharges.
     */
    int getBundleCountSuspend();


    /**
     * Returns the total amount of charges attempted.
     *
     * @return The total amount of charges attempted.
     */
    long getChargeAmount();


    /**
     * Returns the total amount of failed charges.
     *
     * @return The total amount of failed charges.
     */
    long getChargeAmountFailed();


    /**
     * Returns the total amount of successful charges.
     *
     * @return The total amount of successful charges.
     */
    long getChargeAmountSuccess();


    /**
     * Returns the total number of suspension of service packages due to failed recharges.
     *
     * @return The total number of suspension of service packages due to failed recharges.
     */
    int getPackageCountSuspend();


    /**
     * Returns the total number of failed service package recharges.
     *
     * @return The total number of failed service package recharges.
     */
    int getPackagesFailedCount();


    /**
     * Returns the total number of service package recharges attempted.
     *
     * @return The total number of service package recharges attempted.
     */
    int getPackagesCount();


    /**
     * Returns the total number of successful service package recharges.
     *
     * @return The total number of successful service package recharges.
     */
    int gePackagesSuccessCount();


    /**
     * Returns the total number of suspension of services due to failed recharges.
     *
     * @return The total number of suspension of services due to failed recharges.
     */
    int getServiceCountSuspend();


    /**
     * Returns the total number of service recharges attempted.
     *
     * @return The total number of service recharges attempted.
     */
    int getServicesCount();


    /**
     * Returns the total number of failed service recharges.
     *
     * @return The total number of failed service recharges.
     */
    int getServicesCountFailed();


    /**
     * Returns the total number of successful service recharges.
     *
     * @return The total number of successful service recharges.
     */
    int getServicesCountSuccess();


    /**
     * Returns the total number of accounts recharges attempted.
     *
     * @return The total number of accounts recharges attempted.
     */
    int getAccountCount();


    /**
     * Returns the total number of accounts whose recharge has failed.
     *
     * @return The total number of accounts whose recharge has failed.
     */
    int getAccountFailCount();


    /**
     * Returns the total number of accounts whose recharge has succeeded.
     *
     * @return The total number of accounts whose recharge has succeeded.
     */
    int getAccountSuccessCount();


    /**
     * Returns the total number of subscribers recharges attempted.
     *
     * @return The total number of subscribers recharges attempted.
     */
    int getSubscriberCount();


    /**
     * Returns the total number of subscribers whose recharge has failed.
     *
     * @return The total number of subscribers whose recharge has failed.
     */
    int getSubscriberFailCount();


    /**
     * Returns the total number of subscribers whose recharge has succeeded.
     *
     * @return The total number of subscribers whose recharge has succeeded.
     */
    int getSubscriberSuccessCount();

}
