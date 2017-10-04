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
package com.trilogy.app.crm.client.smsb;


import static com.redknee.app.crm.client.CorbaClientTrapIdDef.*;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.AbstractCrmClient;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.smsb.dataserver.smsbcorba.subsProfile7;
import com.trilogy.app.smsb.dataserver.smsbcorba.subsProfile7Holder;
import com.trilogy.app.smsb.dataserver.smsbcorba.svc_provision7;
import com.trilogy.app.smsb.dataserver.smsbcorba.svc_provision9;

/**
 * Support clustered corba client
 * Refactored reusable elements to an abstract class AbstractCrmClient<T>
 * @author rchen
 * @since June 24, 2009 
 */
public class AppSmsbClient extends AbstractCrmClient<svc_provision9>
{
    private static final String SERVICE_NAME = "AppSmsbClient";
    private static final String SERVICE_DESCRIPTION = "CORBA client for SMSB services";
    private static final Class<svc_provision9> CORBA_CLIENT_KEY = svc_provision9.class;
    
    public static final int SUCCESS = 0;
    public static final int NO_ERROR = 0;
    public static final int RECORD_NOT_FOUND = 201;
    public static final int SUBSCRIBER_NOT_FOUND = 201;
    public static final int SQL_ERROR = 202;
    public static final int INTERNAL_ERROR = 203;

    public static final String SIMPLE_DATE_FORMAT_STRING = "yyyyMMdd";

    private static final String PM_MODULE = AppSmsbClient.class.getName();

    public AppSmsbClient(final Context ctx)
    {
        super(ctx, SERVICE_NAME, SERVICE_DESCRIPTION, CORBA_CLIENT_KEY, SMSB_PROV_SVC_DOWN, SMSB_PROV_SVC_UP);
    }


    protected svc_provision7 getService7()
    {
        return getService();
    }


    public int addSubscriber(final String msisdn, final String groupMsisdn, final String imsi, final short spId,
        final short svcId, final short svcGrade, final String ban, final String birthdate, final String gender,
        final String language, final String location, final short billcycledate, final String eqtype,
        final short tzOffset, final short ratePlan, final short recurDate, final short scpId, final short hlrId,
        final boolean enable, final long barringPlan, final String outgoingSmsCount, final String incomingSmsCount)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "addSubscriber()");

        int result = -1;
        final svc_provision7 service = getService7();
        subsProfile7 profile = null;

        if (service != null)
        {
            profile =
                subsProfile(msisdn, groupMsisdn, imsi, spId, svcId, svcGrade, ban, birthdate, gender, language,
                    location, billcycledate, eqtype, tzOffset, ratePlan, recurDate, scpId, hlrId, enable, barringPlan,
                    outgoingSmsCount, incomingSmsCount);
            try
            {
                result = service.addChangeSub7(profile);
            }
            catch (final org.omg.CORBA.COMM_FAILURE commFail)
            {
                result = ExternalAppSupport.COMMUNICATION_FAILURE;
            }
            catch (final Exception e)
            {
                result = ExternalAppSupport.REMOTE_EXCEPTION;
                new MinorLogMsg(this, "Fail to add new subscriber " + msisdn, e).log(getContext());
            }
        }
        else
        {
            // connection not available
            result = ExternalAppSupport.NO_CONNECTION;
        }

        pmLogMsg.log(getContext());

        return result;
    }


    public int addSubscriber(final subsProfile7 profile)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "addSubscriber()");

        int result = -1;
        final svc_provision7 service = getService7();

        if (service != null)
        {
            try
            {
                result = service.addChangeSub7(profile);
            }
            catch (final org.omg.CORBA.COMM_FAILURE commFail)
            {
                result = ExternalAppSupport.COMMUNICATION_FAILURE;
            }
            catch (final Exception e)
            {
                result = ExternalAppSupport.REMOTE_EXCEPTION;
                new MinorLogMsg(this, "Fail to add new subscriber " + profile.msisdn, e).log(getContext());
            }
        }
        else
        {
            // connection not available
            result = ExternalAppSupport.NO_CONNECTION;
        }

        pmLogMsg.log(getContext());

        return result;
    }


    public int updateSubscriber(final String msisdn, final String groupMsisdn, final String imsi, final short spId,
        final short svcId, final short svcGrade, final String ban, final String birthdate, final String gender,
        final String language, final String location, final short billcycledate, final String eqtype,
        final short tzOffset, final short ratePlan, final short recurDate, final short scpId, final short hlrId,
        final boolean enable, final long barringPlan)
    {
        // The blank smsCount at the end informs SMSB that we do not want to
        // update the smsCount.
        final int result =
            updateSubscriber(msisdn, groupMsisdn, imsi, spId, svcId, svcGrade, ban, birthdate, gender, language,
                location, billcycledate, eqtype, tzOffset, ratePlan, recurDate, scpId, hlrId, enable, barringPlan, "",
                "");

        return result;
    }


    public int updateSubscriber(final String msisdn, final String groupMsisdn, final String imsi, final short spId,
        final short svcId, final short svcGrade, final String ban, final String birthdate, final String gender,
        final String language, final String location, final short billcycledate, final String eqtype,
        final short tzOffset, final short ratePlan, final short recurDate, final short scpId, final short hlrId,
        final boolean enable, final long barringPlan, final int outgoingSmsCount, final int incomingSmsCount)
    {
        // never update incoming SMS Count
        final int result =
            updateSubscriber(msisdn, groupMsisdn, imsi, spId, svcId, svcGrade, ban, birthdate, gender, language,
                location, billcycledate, eqtype, tzOffset, ratePlan, recurDate, scpId, hlrId, enable, barringPlan,
                Integer.toString(outgoingSmsCount), "");

        return result;
    }


    private int updateSubscriber(final String msisdn, final String groupMsisdn, final String imsi, final short spId,
        final short svcId, final short svcGrade, final String ban, final String birthdate, final String gender,
        final String language, final String location, final short billcycledate, final String eqtype,
        final short tzOffset, final short ratePlan, final short recurDate, final short scpId, final short hlrId,
        final boolean enable, final long barringPlan, final String outgoingSmsCount, final String incomingSmsCount)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "updateSubscriber()");

        // never update incoming SMS Count
        final subsProfile7 profile =
            subsProfile(msisdn, groupMsisdn, imsi, spId, svcId, svcGrade, ban, birthdate, gender, language, location,
                billcycledate, eqtype, tzOffset, ratePlan, recurDate, scpId, hlrId, enable, barringPlan,
                outgoingSmsCount, "");

        final int result = updateSubscriber(profile);

        pmLogMsg.log(getContext());

        return result;
    }


    public int updateSubscriber(final subsProfile7 profile)
    {
        int result = -1;
        final svc_provision7 service = getService7();

        if (service != null)
        {
            try
            {
                result = service.addChangeSub7(profile);
            }
            catch (final org.omg.CORBA.COMM_FAILURE commFail)
            {
                result = ExternalAppSupport.COMMUNICATION_FAILURE;
            }
            catch (final Exception e)
            {
                result = ExternalAppSupport.REMOTE_EXCEPTION;
                new MinorLogMsg(this, "Fail to change subscriber " + profile.msisdn, null).log(getContext());
            }
        }
        else
        {
            // connection not available
            result = ExternalAppSupport.NO_CONNECTION;
        }

        return result;
    }


    public int enableSubscriber(final String msisdn, final boolean isEnable)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "enableSubscriber()");

        final subsProfile7 profile = getSubsProfile(msisdn);
        final int result;
        if (profile == null)
        {
            result = 201;
        }
        else
        {
            // The blank smsCount is there to keep SMSB from resetting the
            // value.
            profile.enable = isEnable;
            profile.outgoingSmsCount = "";
            profile.incomingSmsCount = "";
            result = updateSubscriber(profile);
        }

        pmLogMsg.log(getContext());

        return result;
    }


    /**
     * New Method added to enable Subscriber on SMSB with Prorated SMS count
     * values.
     *
     * @param msisdn String representing the Subscriber MSISDN
     * @param isEnable boolean representing whether to enable the subcriber
     * @param subscriber Subcriber Object
     * @return int integer value representing the result of the subscriber
     * update operation on SMSB.
     */
    public int enableSubscriberAndProrateSms(final String msisdn, final boolean isEnable, final Subscriber subscriber)
    {
        // added by Weekly Charges port
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "enableSubscriber()");

        final subsProfile7 profile = getSubsProfile(msisdn);
        final int result;

        if (profile == null)
        {
            result = 201;
        }
        else
        {
            // The blank smsCount is there to keep SMSB from resetting the
            // value.

            profile.enable = isEnable;

            profile.outgoingSmsCount = "0";

            profile.incomingSmsCount = "";

            result = updateSubscriber(profile);
        }

        pmLogMsg.log(getContext());

        return result;
    }


    /**
     * Updates the groupMsisdn of the subscriber.
     *
     * @param msisdn The MSISDN by which SMSB identifies the subscriber.
     * @param groupMsisdn The subscriber's new groupMsisdn.
     */
    public int updateGroupMsisdn(final String msisdn, final String groupMsisdn)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "updateGroupMsisdn()");

        final int result;
        try
        {
            final subsProfile7 profile = getSubsProfile(msisdn);

            // Update the groupMsisdn but set the smsCount to blank to prevent
            // it from being reset in SMSB.
            profile.groupMsisdn = groupMsisdn;
            profile.outgoingSmsCount = "";
            profile.incomingSmsCount = "";

            result = updateSubscriber(profile);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }

        return result;
    }


    /**
     * Updates some selected parameters of the subscriber: the groupMsisdn,
     * rateplan, packageid (or imsi), date of birth, sms enable.
     *
     * @param msisdn The MSISDN by which SMSB identifies the subscriber.
     */
    public int updateSelectedParameters(final String msisdn, final String groupMsisdn, final String imsi,
        Short billCycleDay, final Boolean isEnable)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "updateSelectedParameters()");

        final int result;
        try
        {
            final subsProfile7 profile = getSubsProfile(msisdn);

            if (profile == null)
            {
                result = 201;
            }
            else
            {
                // Group Msisdn
                profile.groupMsisdn = groupMsisdn;

                // SMS Rate Plan
                /* As of CRM 8.2, Rate Plan information is in Price Plan and it doesn't need to be updated at the 
                 * Subscription level.  So we will use a placeholder value of 0 for the rate plan.*/
                profile.ratePlan = (short) 0;

                // Package
                profile.imsi = imsi;

                // Bill Cycle Date
                if (billCycleDay != null)
                {
                    profile.billcycledate = billCycleDay;
                }

                // SMS Enabled
                if (isEnable != null)
                {
                    profile.enable = isEnable.booleanValue();
                }

                // The blank smsCount is there to keep SMSB from resetting the
                // value.
                profile.outgoingSmsCount = "";
                profile.incomingSmsCount = "";

                result = updateSubscriber(profile);
            }
        }
        finally
        {
            pmLogMsg.log(getContext());
        }

        return result;
    }


    public int deleteSubscriber(final String msisdn)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "deleteSubscriber()");

        int result = -1;
        final svc_provision7 service = getService7();

        if (service != null)
        {
            try
            {
                result = service.deleteSub7(msisdn);

                if (result == SUBSCRIBER_NOT_FOUND)
                {
                    if (LogSupport.isDebugEnabled(getContext()))
                    {
                        final String message = "Cannot find subscriber " + msisdn + " that is deleted";
                        new DebugLogMsg(this, message, null).log(getContext());
                    }

                    result = 0;
                }
            }
            catch (final org.omg.CORBA.COMM_FAILURE commFail)
            {
                result = ExternalAppSupport.COMMUNICATION_FAILURE;
            }
            catch (final Exception e)
            {
                result = ExternalAppSupport.REMOTE_EXCEPTION;
                new MinorLogMsg(this, "Fail to delete subscriber " + msisdn, null).log(getContext());
            }
        }
        else
        {
            // connection not available
            result = ExternalAppSupport.NO_CONNECTION;
        }

        pmLogMsg.log(getContext());

        return result;
    }


    public subsProfile7 getSubsProfile(final String msisdn)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "getSubsProfile()");

        int result = -1;
        final svc_provision7 service = getService7();
        subsProfile7Holder profileHolder = null;

        if (service != null)
        {
            try
            {
                profileHolder = new subsProfile7Holder();
                result = service.getSub7(msisdn, profileHolder);
            }
            catch (final org.omg.CORBA.COMM_FAILURE commFail)
            {
                result = ExternalAppSupport.COMMUNICATION_FAILURE;
            }
            catch (final Exception e)
            {
                result = ExternalAppSupport.REMOTE_EXCEPTION;
                new MinorLogMsg(this, "Fail to retrieve subscriber " + msisdn, e).log(getContext());
            }
        }

        final subsProfile7 profile;
        if (result == 0 && profileHolder != null)
        {
            profile = profileHolder.value;
        }
        else
        {
            profile = null;
        }

        pmLogMsg.log(getContext());

        return profile;
    }


    public int getSmsSent(final String msisdn)
    {
        if (msisdn == null)
        {
            if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(this, "Cannot find number of smses sent for msisdn==null", null).log(getContext());
            }

            return 0;
        }

        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "getSmsSent()");

        final subsProfile7 profile = getSubsProfile(msisdn);
        int result = 0;

        if (profile != null)
        {
            final String smsCount = profile.outgoingSmsCount;

            if (smsCount != null)
            {
                try
                {
                    result = Integer.parseInt(smsCount);
                }
                catch (final NumberFormatException exception)
                {
                    result = ExternalAppSupport.REMOTE_EXCEPTION;
                    new MajorLogMsg(this, "SMSB returned a smsCount value that is not a number: \"" + smsCount + "\".",
                        exception).log(getContext());
                }
            }
            else
            {
                new MajorLogMsg(this, "SMSB returned a null smsCount value.", null).log(getContext());
            }
        }

        pmLogMsg.log(getContext());

        return result;
    }


    private subsProfile7 subsProfile(final String msisdn, final String groupMsidn, final String imsi, final short spId,
        final short svcId, final short svcGrade, final String ban, final String birthdate, final String gender,
        final String language, final String location, final short billcycledate, final String eqtype,
        final short tzOffset, final short ratePlan, final short recurDate, final short scpId, final short hlrId,
        final boolean enable, final long barringPlan, final String outgoingSmsCount, final String incomingSmsCount)
    {
        final subsProfile7 profile = new subsProfile7();

        profile.msisdn = msisdn;
        profile.groupMsisdn = groupMsidn;
        profile.imsi = imsi;
        profile.spid = spId;
        profile.svcid = svcId;
        profile.svcGrade = svcGrade;
        profile.ban = ban;
        profile.birthdate = birthdate;
        profile.gender = gender;
        profile.language = language;
        profile.location = location;
        profile.billcycledate = billcycledate;
        profile.eqtype = eqtype;
        profile.TzOffset = tzOffset;
        profile.ratePlan = ratePlan;
        profile.recurDate = recurDate;
        profile.scpid = scpId;
        profile.hlrid = hlrId;
        profile.enable = enable;
        profile.barringplan = barringPlan;
        profile.outgoingSmsCount = outgoingSmsCount;
        profile.incomingSmsCount = incomingSmsCount;

        return profile;
    }

}
