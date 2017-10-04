/*
 * Copyright (c) 1999-2003, REDKNEE. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of REDKNEE.
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with REDKNEE.
 *
 * REDKNEE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT. REDKNEE SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR
 * ITS DERIVATIVES.
 */
package com.trilogy.app.crm.poller.event;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.config.IPCGPollerConfig;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * This class implements some basic functions aided in processing the ERs parsed from the
 * IPCGW ER files.
 *
 * @author jimmy.ng@redknee.com
 */
public abstract class IPCGWProcessor extends CRMProcessor
{

    /**
     * Creates a new IPCGWProcessor.
     */
    public IPCGWProcessor()
    {
        super();
    }


    /**
     * This method formats the given list of parameters into a string and returns the
     * string for debugging purpose.
     *
     * @param _params
     *            The given list of paramters.
     * @return String The returning parameter list in String format.
     */
    public static String getDebugParams(final List _params)
    {
        final Iterator iParams = _params.iterator();
        int index = 0;

        final StringBuilder buf = new StringBuilder();
        while (iParams.hasNext())
        {
            buf.append(index);
            buf.append("[");
            buf.append(CRMProcessorSupport.getField(_params, index));
            buf.append("] ");

            iParams.next();
            index++;
        }

        return buf.toString();
    }


    /**
     * This method determines whether an ER should be processed or not.
     *
     * @param scpId
     *            Indicate the subscriber type that is used for checking whether the ER
     *            should be processed or not.
     * @return boolean True if the ER should be processed; False otherwise.
     */
    public static boolean isERToBeProcessed(final Context ctx, final int scpId)
    {
        final IPCGPollerConfig config = (IPCGPollerConfig) ctx.get(IPCGPollerConfig.class);
        if (config == null)
        {
            new MajorLogMsg(IPCGWProcessor.class.getName(), "Could not find IPCGPollerConfig in context", null)
                .log(ctx);
            return false;
        }
        else if (!config.isEnableFilter())
        {
            return true;
        }
        else
        {
            return scpId == config.getErFilter();
        }
    }


    /**
     * Return the equivalent subscriber type for the given SCP ID.
     *
     * @param scpId
     *            Represents the subscriber type in the ER.
     * @return SubscriberTypeEnum SubscriberTypeEnum.POSTPAID for the Postpaid subscriber;
     *         SubscriberTypeEnum.PREPAID otherwise.
     */
    public static SubscriberTypeEnum equivalentSubscriberType(final int scpId)
    {
        if (scpId == 0)
        {
            return SubscriberTypeEnum.POSTPAID;
        }
        return SubscriberTypeEnum.PREPAID;
    }


    /**
     * This method determines whether the given Charged MSISDN has a matching subscriber
     * profile with a valid state.
     *
     * @param chargedMsisdn
     *            The given Charged MSISDN.
     * @param scpId
     *            Indicate the subscriber type that is used for checking whether the ER
     *            should be processed or not.
     * @return int 1 if the Charged MSISDN does not have a matching subscriber profile; 2
     *         if the Charged MSISDN has a matching subscriber profile with an invalid
     *         state; 0 otherwise.
     */
    public int isChargedMsisdnOkay(final Context ctx, final String chargedMsisdn, final Date transDate)
    {
        boolean failedToLookupSubscriber = false;
        Subscriber subscriber = null;
        try
        {
            subscriber = SubscriberSupport.lookupSubscriberForMSISDNLimited(ctx, chargedMsisdn, transDate);
        }
        catch (final HomeException e)
        {
            failedToLookupSubscriber = true;
        }

        if (failedToLookupSubscriber || subscriber == null)
        {
            return 1;
        }

        return 0;
    }
}
