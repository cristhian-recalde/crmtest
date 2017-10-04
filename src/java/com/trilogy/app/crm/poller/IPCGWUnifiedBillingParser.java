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

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.bean.IPCGData;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.calldetail.CallTypeEnum;
import com.trilogy.app.crm.bean.calldetail.RateUnitEnum;
import com.trilogy.app.crm.config.IPCGPollerConfig;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.poller.event.IPCGWProcessor;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.FrameworkSupportHelper;


/**
 * Extracted class to handle IPCG ER 501 parsing.
 * See Doors /S-5600 IPCG/HLD/Native 8.1.3.1
 *
 * @author cindy.wong@redknee.com
 * @since 2008-07-04
 */
public class IPCGWUnifiedBillingParser
{

    /**
     * Map of protocol type to call type.
     */
    public static final Map<String, CallTypeEnum> PROTOCOL_CALL_TYPE_MAP = new HashMap<String, CallTypeEnum>();

    // static init block
    {
        PROTOCOL_CALL_TYPE_MAP.put("MMS", CallTypeEnum.MMS);
        PROTOCOL_CALL_TYPE_MAP.put("WSP", CallTypeEnum.WAP);
        PROTOCOL_CALL_TYPE_MAP.put("WSP-WTP", CallTypeEnum.WAP);
        PROTOCOL_CALL_TYPE_MAP.put("WTLS", CallTypeEnum.WAP);
        PROTOCOL_CALL_TYPE_MAP.put("WTLS-WTP", CallTypeEnum.WAP);
    }

    /**
     * Comparator for sorting IPCGData by call type and usage type.
     *
     * @author cindy.wong@redknee.com
     * @since 2008-07-04
     */
    static class CallTypeUsageTypeComparator implements Comparator<IPCGData>, Serializable
    {

        /**
         * {@inheritDoc}
         */
        public int compare(final IPCGData o1, final IPCGData o2)
        {
            int result = CalendarSupportHelper.get().getDateWithNoTimeOfDay(o1.getTranDate()).compareTo(
                CalendarSupportHelper.get().getDateWithNoTimeOfDay(o2.getTranDate()));
            if (result == 0)
            {
                result = o1.getCallType().compareTo(o2.getCallType());
                if (result == 0)
                {
                    result = o1.getUnitType().compareTo(o2.getUnitType());
                }
            }
            return result;
        }

    }


    /**
     * Create a new instance of <code>IPCGWUnifiedBillingParser</code>.
     */
    protected IPCGWUnifiedBillingParser()
    {
        // empty
    }


    /**
     * Returns an instance of <code>IPCGWUnifiedBillingParser</code>.
     *
     * @return An instance of <code>IPCGWUnifiedBillingParser</code>.
     */
    public static IPCGWUnifiedBillingParser instance()
    {
        if (instance == null)
        {
            instance = new IPCGWUnifiedBillingParser();
        }
        return instance;
    }


    /**
     * Parses a single IPCG ER 501.
     *
     * @param context
     *            The operating context.
     * @param params
     *            ER parameters.
     * @return A collection of all data usages of different call types in the ER.
     * @throws ParseException
     *             Thrown if there are problems parsing a specific field.
     * @throws AgentException
     *             Thrown if the postpaid MSISDN cannot be found.
     */
    public Collection<IPCGData> processEr501(final Context context, final List<String> params) throws AgentException,
        ParseException
    {
        final PMLogMsg pm = new PMLogMsg(PM_MODULE, "processEr501");

        try
        {
            final List<IPCGData> results = new ArrayList<IPCGData>();

            final IPCGPollerConfig config = (IPCGPollerConfig) context.get(IPCGPollerConfig.class);
            if (config == null)
            {
                LogSupport.major(context, this, "Cannot find IPCG poller configuration in context");
                return results;
            }

            // Return right away if this ER should NOT be processed.
            final int scpId = CRMProcessorSupport.getInt(context, CRMProcessorSupport.getField(params,
                    config.getPosSCPID()), 100);

            if (IPCGWProcessor.isERToBeProcessed(context, scpId))
            {

                // Print all the parameters for debugging purpose.
                if (LogSupport.isDebugEnabled(context))
                {
                    LogSupport.debug(context, this, IPCGWProcessor.getDebugParams(params));
                }

                final IPCGData prototype = new IPCGData();

                prototype.setTranDate(parseDate(config, params));

                // Set the Charged MSISDN for the IPCGData object.

                final String chargedMsisdn = parseMsisdn(context, config, prototype.getTranDate(), scpId, params);

                if (chargedMsisdn != null)
                {
                    prototype.setChargedMSISDN(chargedMsisdn);

                    /*
                     * TT8061600041: Support SDR charges.
                     */
                    final IPCGData data = parseSdrCharge(context, config, prototype, params);
                    if (data != null)
                    {
                        results.add(data);
                    }

                    // Charges for different call types first "Protocol Type"
                    int i = config.getPosHttp() - 1;

                    do
                    {
                        i = parseProtocolSection(context, config, prototype, params, i, results);
                    }
                    while (CRMProcessorSupport.getField(params, i).equals("#"));
                }
            }

            return consolidateResults(results);
        }
        finally
        {
            pm.log(context);
        }
    }


    /**
     * Consolidate a list of IPCGData by call type and usage type.
     *
     * @param results
     *            List of IPCGData to consolidate.
     * @return A list of IPCGData consolidated by call type and usage type. The total
     *         charge of all items of each (call type, usage type) combination in the new
     *         list is the same as the old list.
     */
    public List<IPCGData> consolidateResults(final List<IPCGData> results)
    {
        Collections.sort(results, new CallTypeUsageTypeComparator());

        final List<IPCGData> dataList = new ArrayList<IPCGData>();
        CallTypeEnum callType = null;
        RateUnitEnum unitType = null;
        IPCGData data = null;

        for (final IPCGData current : results)
        {
            if (current.getCallType() != callType || current.getUnitType() != unitType || data == null)
            {
                if (data != null)
                {
                    dataList.add(data);
                }
                data = current;
                callType = current.getCallType();
                unitType = current.getUnitType();
            }
            else
            {
                data.setUsage(data.getUsage() + current.getUsage());
                data.setCharge(data.getCharge() + current.getCharge());
            }
        }

        if (data != null)
        {
            dataList.add(data);
        }
        return dataList;
    }


    /**
     * Returns the absolute position of a field, based on the absolute position of a pivot
     * field and the relative distance from the field.
     *
     * @param pivotPosition
     *            Absolute position of the pivot field.
     * @param relativePosition
     *            Relative distance of the field from the pivot. Only non-negative values
     *            are considered valid distance.
     * @return The absolute position of a field. If either the pivot position or the
     *         relative distance are negative, returns <code>-1</code>.
     */
    private int getAbsoluteIndex(final int pivotPosition, final int relativePosition)
    {
        int result = -1;
        if (pivotPosition >= 0 && relativePosition >= 0)
        {
            result = pivotPosition + relativePosition;
        }
        return result;
    }


    /**
     * Returns the call type based on the protocol.
     *
     * @param protocol
     *            Protocol.
     * @return The call type corresponding to the protocol.
     */
    private CallTypeEnum getCallType(final String protocol)
    {
        CallTypeEnum callType = PROTOCOL_CALL_TYPE_MAP.get(protocol);

        // default to web
        if (callType == null)
        {
            callType = CallTypeEnum.WEB;
        }

        return callType;
    }


    /**
     * Parses the date from the ER.
     *
     * @param config
     *            IPCG ER configuration.
     * @param params
     *            IPCG ER parameters.
     * @return Parsed date.
     * @throws ParseException
     *             Thrown if there are problems parsing the date.
     */
    private Date parseDate(final IPCGPollerConfig config, final List<String> params) throws ParseException
    {
        // Set the Transaction Date/Time for the IPCGData object.
        final String dateTimeStr = CRMProcessorSupport.getField(params, config.getPosTransDate()) + " "
            + CRMProcessorSupport.getField(params, config.getPosTransTime());

        return CRMProcessorSupport.getDate(dateTimeStr);
    }


    /**
     * Parses and validates the MSISDN in the ER.
     *
     * @param context
     *            The operating context.
     * @param config
     *            IPCG configuration.
     * @param date
     *            Date of the ER.
     * @param scpId
     *            SCP ID of the ER.
     * @param params
     *            ER parameters.
     * @return Returns the MSISDN if it is valid, <code>null</code> otherwise.
     * @throws AgentException
     *             Thrown if the MSISDN does not exist in CRM.
     * @throws ParseException
     *             Thrown if the MSISDN format is incorrect.
     */
    private String parseMsisdn(final Context context, final IPCGPollerConfig config, final Date date, final int scpId,
        final List<String> params) throws AgentException, ParseException
    {
        final String msisdn = CRMProcessorSupport.getMsisdn(
                CRMProcessorSupport.getField(params, config.getPosMsisdn()));

        final int chargedMsisdnReturnCode = getProcessor().isChargedMsisdnOkay(context, msisdn, date);
        if (chargedMsisdnReturnCode == 1)
        {
            if (IPCGWProcessor.equivalentSubscriberType(scpId) == SubscriberTypeEnum.POSTPAID)
            {
                /*
                 * Generate alarms for Postpaid ERs whose MSISDN has no matching
                 * subscriber profile.
                 */
                new EntryLogMsg(11061, this, "", "", new String[]
                {
                    msisdn,
                }, null).log(context);
                throw new AgentException("Postpaid ERs whose MSISDN has no matching subscriber profile");
            }
            return null;
        }
        return msisdn;
    }


    /**
     * Parses a single protocol section.
     *
     * @param context
     *            The operating context.
     * @param config
     *            IPCG poller configuration.
     * @param prototype
     *            IPCGData prototype.
     * @param params
     *            ER parameters.
     * @param position
     *            starting position.
     * @param results
     *            Collection of resulting IPCGData. Additional IPCGData are added to this
     *            collection.
     * @return The position of the next protocol section.
     */
    private int parseProtocolSection(final Context context, final IPCGPollerConfig config, final IPCGData prototype,
        final List<String> params, final int position, final Collection<IPCGData> results)
    {
        final PMLogMsg pm = new PMLogMsg(PM_MODULE, "parseProtocolSection");

        try
        {
            int i = position;

            // Move index to "Protocol Type"
            i++;
            final String protocol = CRMProcessorSupport.getField(params, i);
            final CallTypeEnum callType = getCallType(protocol);

            // Move index to "Dest Port Number"
            i++;

            do
            {
                final int totalChargeIndex = getAbsoluteIndex(i, config.getPosCharge());

                /*
                 * TODO [2008-07-04]: support bundle charges
                 */
                final int bundleChargeIndex = getAbsoluteIndex(i, config.getPosBundleCharge());

                final int totalCharge = CRMProcessorSupport.getInt(context,
                        CRMProcessorSupport.getField(params, totalChargeIndex), 0);

                final int bundleCharge = CRMProcessorSupport.getInt(context,
                        CRMProcessorSupport.getField(params, bundleChargeIndex), 0);

                IPCGData data = parseEventCharge(context, config, prototype, params, i);
                if (data != null)
                {
                    results.add(data);
                }

                data = parseDurationCharge(context, config, prototype, params, i, callType);
                if (data != null)
                {
                    results.add(data);
                }

                data = parseVolumeCharge(context, config, prototype, params, i, callType);
                if (data != null)
                {
                    results.add(data);
                }

                if (LogSupport.isDebugEnabled(context))
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("i = ");
                    sb.append(i);
                    sb.append(", protocol = ");
                    sb.append(protocol);
                    sb.append(", call type = ");
                    sb.append(callType);
                    sb.append(", total charge = ");
                    sb.append(totalCharge);
                    sb.append(", bundle charge = ");
                    sb.append(bundleCharge);
                    LogSupport.debug(context, this, sb.toString());
                }

                // Move index to next protocol/URL info.
                i += config.getPosUrlInfo();
            }
            while (CRMProcessorSupport.getField(params, i).equals("|"));
            return i;
        }
        finally
        {
            pm.log(context);
        }
    }


    /**
     * Parses SDR usage.
     *
     * @param context
     *            The operating context.
     * @param config
     *            IPCG poller configuration.
     * @param prototype
     *            Prototype of <code>IPCGData</code> to use.
     * @param params
     *            ER parameters.
     * @return The SDR usage if any; otherwise returns <code>null</code>.
     */
    private IPCGData parseSdrCharge(final Context context, final IPCGPollerConfig config, final IPCGData prototype,
        final List<String> params)
    {
        final int sdrUsage = CRMProcessorSupport.getInt(context,
                CRMProcessorSupport.getField(params, config.getPosSdrUsage()), 0);
        /* TT 8061600041: ER 501 from IPCG gives the value of the "General Charge" field in cents.  
         * The charge values for the protocol charges in the IPCG ER 501 are in 1/10 cents.  
         * Our IPCG Poller solution has hard-coded the conversion from 1/10 cents to cents in the 
         * aggregation step of polling (IPCGCallDetailCreator.createCallDetail(Context, IPCGData, boolean)).
         * To avoid erroneous conversion of the "General Charge" field, we have to save it in the 
         * IPCGData object as if the charge value was 1/10 cents.  Multiply the value from the ER by 10.  */
        final int generalCharge = CRMProcessorSupport.getInt(context,
                CRMProcessorSupport.getField(params, config.getPosGeneralCharge()), 0) * 10;

        if (LogSupport.isDebugEnabled(context))
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("SDR usage = ");
            sb.append(sdrUsage);
            sb.append(", ER general charge (in 1/10 cents)= ");
            sb.append(generalCharge);
            LogSupport.debug(context, this, sb.toString());
        }

        if (sdrUsage > 0)
        {
            final IPCGData usage = (IPCGData) FrameworkSupportHelper.get(context).doClone(prototype);
            usage.setCallType(CallTypeEnum.SDR);
            usage.setUnitType(RateUnitEnum.SEC);
            usage.setUsage(sdrUsage);
            usage.setCharge(generalCharge);
            return usage;
        }

        return null;
    }


    /**
     * Parses and generates an IPCGData for a duration charge.
     *
     * @param context
     *            The operating context.
     * @param config
     *            IPCG poller configuration.
     * @param prototype
     *            IPCGData prototype.
     * @param params
     *            ER parameters.
     * @param protocolPosition
     *            Position of the protocol field.
     * @param callType
     *            Call type of the duration charge.
     * @return The generated IPCGData, or <code>null</code> if there is no duration
     *         traffic.
     */
    private IPCGData parseDurationCharge(final Context context, final IPCGPollerConfig config,
        final IPCGData prototype, final List<String> params, final int protocolPosition, final CallTypeEnum callType)
    {

        final int durationChargeIndex = getAbsoluteIndex(protocolPosition, config.getPosDurationCharge());
        final int durationRatedIndex = getAbsoluteIndex(protocolPosition, config.getPosDurationRated());
        final int durationCharge = CRMProcessorSupport.getInt(context,
                CRMProcessorSupport.getField(params, durationChargeIndex), 0);
        final int durationRated = CRMProcessorSupport.getInt(context,
                CRMProcessorSupport.getField(params, durationRatedIndex), 0);

        if (LogSupport.isDebugEnabled(context))
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("protocol position = ");
            sb.append(protocolPosition);
            sb.append(", call type = ");
            sb.append(callType);
            sb.append(", durationg charge = ");
            sb.append(durationCharge);
            sb.append(", duration rated = ");
            sb.append(durationRated);
            LogSupport.debug(context, this, sb.toString());
        }

        if (durationRated > 0 || durationCharge > 0)
        {
            final IPCGData data = (IPCGData) FrameworkSupportHelper.get(context).doClone(prototype);
            data.setCallType(callType);
            data.setUnitType(RateUnitEnum.SEC);
            data.setUsage(durationRated);
            data.setCharge(durationCharge);
            return data;
        }
        return null;
    }


    /**
     * Parses and generates an IPCGData for an event download.
     *
     * @param context
     *            The operating context.
     * @param config
     *            IPCG poller configuration.
     * @param prototype
     *            IPCGData prototype.
     * @param params
     *            ER parameters.
     * @param protocolPosition
     *            Position of the protocol field.
     * @return The generated IPCGData, or <code>null</code> if there is no event download
     *         traffic.
     */
    private IPCGData parseEventCharge(final Context context, final IPCGPollerConfig config, final IPCGData prototype,
        final List<String> params, final int protocolPosition)
    {
        final int eventChargeIndex = getAbsoluteIndex(protocolPosition, config.getPosEventCharge());
        final int eventCharge = CRMProcessorSupport.getInt(context,
                CRMProcessorSupport.getField(params, eventChargeIndex), 0);

        final int eventRatedIndex = getAbsoluteIndex(protocolPosition, config.getPosEventCounter());
        final int eventRated = CRMProcessorSupport.getInt(context,
                CRMProcessorSupport.getField(params, eventRatedIndex), 0);

        if (LogSupport.isDebugEnabled(context))
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("protocol position = ");
            sb.append(protocolPosition);
            sb.append(", event charge = ");
            sb.append(eventCharge);
            sb.append(", event rated = ");
            sb.append(eventRated);
            LogSupport.debug(context, this, sb.toString());
        }

        if (eventRated > 0 || eventCharge > 0)
        {
            final IPCGData data = (IPCGData) FrameworkSupportHelper.get(context).doClone(prototype);
            data.setCallType(CallTypeEnum.DOWNLOAD);
            data.setUnitType(RateUnitEnum.EVENT);
            data.setUsage(eventRated);
            data.setCharge(eventCharge);
            return data;
        }
        return null;
    }


    /**
     * Parses and generates an IPCGData for a volume charge.
     *
     * @param context
     *            The operating context.
     * @param config
     *            IPCG poller configuration.
     * @param prototype
     *            IPCGData prototype.
     * @param params
     *            ER parameters.
     * @param protocolPosition
     *            Position of the protocol field.
     * @param callType
     *            Call type of the duration charge.
     * @return The generated IPCGData, or <code>null</code> if there is no volume traffic.
     */
    private IPCGData parseVolumeCharge(final Context context, final IPCGPollerConfig config, final IPCGData prototype,
        final List<String> params, final int protocolPosition, final CallTypeEnum callType)
    {

        final int volumeChargeIndex = getAbsoluteIndex(protocolPosition, config.getPosVolumeCharge());
        final int volumeDownIndex = getAbsoluteIndex(protocolPosition, config.getPosVolDown());
        final int volumeUpIndex = getAbsoluteIndex(protocolPosition, config.getPosVolUp());
        final int volumeCharge = CRMProcessorSupport.getInt(context,
                CRMProcessorSupport.getField(params, volumeChargeIndex), 0);
        final int volumeDown = CRMProcessorSupport.getInt(context,
                CRMProcessorSupport.getField(params, volumeDownIndex), 0);
        final int volumeUp = CRMProcessorSupport.getInt(context,
                CRMProcessorSupport.getField(params, volumeUpIndex), 0);
        final int volumeRated = volumeUp + volumeDown;

        if (LogSupport.isDebugEnabled(context))
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("protocol position = ");
            sb.append(protocolPosition);
            sb.append(", call type = ");
            sb.append(callType);
            sb.append(", volume charge = ");
            sb.append(volumeCharge);
            sb.append(", volume down = ");
            sb.append(volumeDown);
            sb.append(", volume up = ");
            sb.append(volumeUp);
            sb.append(", total volume rated = ");
            sb.append(volumeRated);
            LogSupport.debug(context, this, sb.toString());
        }

        if (volumeRated > 0 || volumeCharge > 0)
        {
            final IPCGData data = (IPCGData) FrameworkSupportHelper.get(context).doClone(prototype);
            data.setCallType(callType);
            data.setUnitType(RateUnitEnum.KBYTES);
            data.setUsage(volumeRated);
            data.setCharge(volumeCharge);
            return data;
        }
        return null;
    }


    /**
     * Retrieves the ER processor used.
     *
     * @return The ER processor used.
     */
    public final IPCGWProcessor getProcessor()
    {
        return this.processor_;
    }


    /**
     * Sets the ER processor to use.
     *
     * @param processor
     *            The ER processor to use.
     */
    public final void setProcessor(final IPCGWProcessor processor)
    {
        this.processor_ = processor;
    }

    /**
     * ER processor.
     */
    private IPCGWProcessor processor_ = new IPCGWProcessor()
    {
        // empty
    };

    /**
     * Singleton instance.
     */
    private static IPCGWUnifiedBillingParser instance;

    /**
     * PM module name.
     */
    private static final String PM_MODULE = "IPCGWUnifiedBillingParser";

}
