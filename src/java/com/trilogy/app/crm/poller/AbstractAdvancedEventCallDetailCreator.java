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

package com.trilogy.app.crm.poller;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.CallType;
import com.trilogy.app.crm.bean.CallTypeHome;
import com.trilogy.app.crm.bean.CallTypeID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallTypeEnum;
import com.trilogy.app.crm.bean.calldetail.RateUnitEnum;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.report.ReportUtilities;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * Generic call detail creator for Advanced Event Rating ER (511).
 *
 * @author cindy.wong@redknee.com
 * @since 24-Jun-08
 */
public abstract class AbstractAdvancedEventCallDetailCreator implements CallDetailCreator
{

    /**
     * Index of MSISDN charged.
     */
    protected static final int INDEX_MSISDN = 3;

    /**
     * Index of SGSN.
     */
    protected static final int INDEX_SGSN = 5;

    /**
     * Index of SCP ID.
     */
    protected static final int INDEX_SCP_ID = 13;

    /**
     * Index of transaction date.
     */
    protected static final int INDEX_TRANSACTION_DATE = 14;

    /**
     * Index of transaction time.
     */
    protected static final int INDEX_TRANSACTION_TIME = 15;

    /**
     * Index of protocol type.
     */
    protected static final int INDEX_PROTOCOL_TYPE = 16;

    /**
     * Index of charge.
     */
    protected static final int INDEX_CHARGE = 20;

    /**
     * Index of calling party location.
     */
    protected static final int INDEX_CALLING_PARTY_LOCATION = 5;

    /**
     * Index of destination MSISDN.
     */
    protected static final int INDEX_DESTINATION_MSISDN = 24;
    
    protected static final int INDEX_CALL_ID = 25;


    /**
     * Determines whether the SCP ID is to be processed.
     *
     * @param context
     *            The operating context.
     * @param scpId
     *            SCPI ID.
     * @return WHether the SCP ID is to be processed.
     */
    protected abstract boolean processScpId(Context context, int scpId);


    /**
     * Returns a debug string for the provided ER parameters.
     *
     * @param params
     *            Tokenized ER.
     * @return The debug string for the provided ER.
     */
    protected abstract String getDebugParamsString(List<String> params);


    /**
     * Returns the subscriber type mapped to the SCP ID.
     *
     * @param scpId
     *            SCP ID.
     * @return The subscriber type mapped to the SCP ID.
     */
    protected abstract SubscriberTypeEnum getEquivalentSubscriberType(int scpId);


    /**
     * Returns the appropriate call type mapped to the provided protocol in the ER.
     *
     * @param protocol
     *            Protocol type.
     * @return The appropriate call type.
     */
    protected CallTypeEnum getCallType(final String protocol)
    {
        return CallTypeEnum.ADVANCED_EVENT;
    }


    /**
     * {@inheritDoc}
     */
    public CallDetail createCallDetail(final Context ctx, final ProcessorInfo info, final List<String> params)
        throws ParseException, HomeException, AgentException
    {
        final List<CallDetail> results = createCallDetails(ctx, info, params);
        if (results.isEmpty())
        {
            return null;
        }
        return results.get(0);
    }


    /**
     * {@inheritDoc}
     */
    public List<CallDetail> createCallDetails(final Context ctx, final ProcessorInfo info, final List<String> params)
        throws ParseException, HomeException, AgentException
    {
        final List<CallDetail> list = new ArrayList<CallDetail>();

        try
        {
            CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',', info.getErid(),
                this);
        }
        catch (final FilterOutException e)
        {
            return list;
        }

        // Return right away if this ER should NOT be processed.
        final int scpId = CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, INDEX_SCP_ID), 100);
        if (!processScpId(ctx, scpId))
        {
            return list;
        }

        // Print all the parameters for debugging purpose.
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, getDebugParamsString(params));
        }

        // Charged MSISDN.
        final String msisdn = CRMProcessorSupport.getField(params, INDEX_MSISDN);
        String chargedMsisdn = "";
        try
        {
            chargedMsisdn = CRMProcessorSupport.getMsisdn(msisdn);
        }
        catch (final ParseException e)
        {
            final String formattedMsg = MessageFormat.format("Could not parse Msisdn \"{0}\".", msisdn);
            new MajorLogMsg(this, formattedMsg, null).log(ctx);

            throw new HomeException(formattedMsg);
        }

        // SGSN.
        final String sgsn = CRMProcessorSupport.getField(params, INDEX_SGSN, "");

        // Transaction Date/Time.
        final String transactionDateTime = CRMProcessorSupport.getField(params, INDEX_TRANSACTION_DATE) + " "
            + CRMProcessorSupport.getField(params, INDEX_TRANSACTION_TIME);
        Date transactionDate = null;
        try
        {
            transactionDate = CRMProcessorSupport.getDate(transactionDateTime);
        }
        catch (final ParseException e)
        {
            final String formattedMsg = MessageFormat.format("Could not parse Transaction Date \"{0}\".",
                transactionDateTime);
            new MajorLogMsg(this, formattedMsg, null).log(ctx);

            throw new HomeException(formattedMsg);
        }

        final Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, chargedMsisdn, transactionDate);
        if (sub == null)
        {
            if (SafetyUtil.safeEquals(getEquivalentSubscriberType(scpId), SubscriberTypeEnum.POSTPAID))
            {
                /*
                 * Generate alarms for Postpaid ERs whose MSISDN has no matching
                 * subscriber profile.
                 */
                new EntryLogMsg(11061, this, "", "", new String[]
                {
                    chargedMsisdn,
                }, null).log(ctx);
                throw new AgentException("Postpaid ERs whose MSISDN has no matching subscriber profile");
            }

            // Ignore Prepaid ERs whose MSISDN has no matching subscriber profile.
            return list;
        }

        // Obtain APN or Calling party location
        final String callingPartyLocation = CRMProcessorSupport.getField(params, INDEX_CALLING_PARTY_LOCATION);

        // Event Charge.
        final long charge = CRMProcessorSupport.getLong(ctx, CRMProcessorSupport.getField(params, INDEX_CHARGE), -1);

        /*
         * TT8061600042: Add support for call categorization.
         */
        final String protocol = CRMProcessorSupport.getField(params, INDEX_PROTOCOL_TYPE);
        
        // TT9011900003: Map Transaction ID to Call ID which is part of the unique index.
        String callId = "";
        if (params.size()>INDEX_CALL_ID)
        {
            callId = CRMProcessorSupport.getField(params, INDEX_CALL_ID);
        }

        /*
         * TT8061600042: Add destination MSISDN.
         */
        String destinationMsisdn = CRMProcessorSupport.getField(params, INDEX_DESTINATION_MSISDN);
        if (destinationMsisdn != null && destinationMsisdn.length() > 0)
        {
            try
            {
                destinationMsisdn = CRMProcessorSupport.getMsisdn(destinationMsisdn);
            }
            catch (final ParseException exception)
            {
                LogSupport.info(ctx, this, "Cannot parse MSISDN " + destinationMsisdn, exception);
            }
            /*
             * Adding calling_party_location value to the call detail record so that call
             * categorization separate different access point. Fix TT#7050648269
             */
            final CallDetail cd = createCallDetailRecord(ctx, chargedMsisdn, transactionDate, getCallType(protocol),
                charge, sub, callingPartyLocation, destinationMsisdn, sgsn, callId);
            list.add(cd);
        }
        else
        {
            /*
             * Adding calling_party_location value to the call detail record so that call
             * categorization separate different access point. Fix TT#7050648269
             */
            final CallDetail cd = createCallDetailRecord(ctx, chargedMsisdn, transactionDate, getCallType(protocol),
                charge, sub, callingPartyLocation, "", sgsn, callId);
            list.add(cd);
        }

        return list;
    }


    /**
     * Creates a call detail based on the provided information.
     *
     * @param ctx
     *            The operating context.
     * @param chargedMSISDN
     *            The MSISDN being charged.
     * @param transactionDate
     *            Transaction date.
     * @param callType
     *            Call type.
     * @param charge
     *            Charge of the call detail.
     * @param sub
     *            Subscriber to be charged.
     * @param callPartyLocation
     *            Location of the calling party.
     * @param destinationMsisdn
     *            Destination MSISDN (optional).
     * @param sgsn
     *            SGSN.
     * @return The created call detail.
     * @throws HomeException
     *             Thrown if there are problems looking up any values.
     */
    private CallDetail createCallDetailRecord(final Context ctx, final String chargedMSISDN,
        final Date transactionDate, final CallTypeEnum callType, final long charge, final Subscriber sub,
        final String callPartyLocation, final String destinationMsisdn, final String sgsn, String callId) throws HomeException
    {
        final Account acct = (Account) ReportUtilities.findByPrimaryKey(ctx, AccountHome.class, sub.getBAN());

        final CallTypeID callTypeId = new CallTypeID(callType.getIndex(), acct.getSpid());
        final CallType ct = (CallType) ReportUtilities.findByPrimaryKey(ctx, CallTypeHome.class, callTypeId);
        if (ct == null)
        {
            final String formattedMsg = MessageFormat.format(
                "Could not find CallType entry for type id \"{0}\" and service provider \"{1}\".",
                    String.valueOf(callType.getIndex()), String.valueOf(acct.getSpid()));

            new MajorLogMsg(this, formattedMsg, null).log(ctx);

            throw new HomeException(formattedMsg);
        }

        final CallDetail cd = new CallDetail();
        cd.setBAN(sub.getBAN());
        cd.setSubscriberID(sub.getId());
        cd.setTranDate(transactionDate);
        cd.setCallType(callType);
        cd.setPostedDate(new Date());
        cd.setChargedMSISDN(chargedMSISDN);
        // One DOWNLOAD for each ER
        cd.setDataUsage(1);
        // DZ: IPCG has changed
        cd.setCharge(charge);
        cd.setSpid(acct.getSpid());
        cd.setTaxAuthority1(acct.getTaxAuthority());
        cd.setGLCode(ct.getGLCode());
        cd.setCallingPartyLocation(callPartyLocation);
        cd.setDestMSISDN(destinationMsisdn);
        cd.setVariableRateUnit(RateUnitEnum.EVENT);
        cd.setCallID(callId);

        if (sgsn != null && sgsn.length() != 0)
        {
            cd.setBillingOption("SGSN:" + sgsn);
        }

        return cd;
    }


    /**
     * Create a new instance of <code>AbstractAdvancedEventCallDetailCreator</code>.
     */
    public AbstractAdvancedEventCallDetailCreator()
    {
        super();
    }

}
