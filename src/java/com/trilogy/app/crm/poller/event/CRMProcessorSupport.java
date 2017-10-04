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
package com.trilogy.app.crm.poller.event;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.support.StringSeperator;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.ERFilterPair;
import com.trilogy.app.crm.bean.ERFilterPairXInfo;
import com.trilogy.app.crm.bean.ErPollerConfig;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.PollerProcessorPackage;
import com.trilogy.app.crm.poller.Constants;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CollectionSupportHelper;


/**
 * Support class for ER processing.
 *
 * @author aaron.gourley@redknee.com.
 * @since 7.5
 */
public final class CRMProcessorSupport
{

    /**
     * Date-time format string.
     */
    private static final String DATE_FORMAT_STRING = "yyyy/MM/dd HH:mm:ss";

    /**
     * Date format string.
     */
    private static final String DATE_ONLY_FORMAT_STRING = "yyyy/MM/dd";


    /**
     * Creates a new <code>CRMProcessorSupport</code> instance. This method is made
     * private to prevent instantiation of utility class.
     */
    private CRMProcessorSupport()
    {
        // empty
    }


    /**
     * Returns the boolean value represented by the string.
     *
     * @param str
     *            String to be parsed.
     * @param defaultValue
     *            Default value to be used if the provided string is empty.
     * @return Boolean value represented by the string.
     */
    public static boolean getBoolean(final String str, final boolean defaultValue)
    {
        if (str == null)
        {
            return defaultValue;
        }

        final String in = str.toLowerCase();

        boolean result = false;
        if ("y".equals(in) || "t".equals(in))
        {
            result = true;
        }

        return result;
    }


    /**
     * Returns the integer value represented by the string.
     *
     * @param ctx
     *            The operating context.
     * @param str
     *            String to be parsed.
     * @param defaultValue
     *            Default value to be used if the provided string is empty.
     * @return Integer value represented by the string.
     */
    public static int getInt(final Context ctx, final String str, final int defaultValue)
    {
        int val = defaultValue;
        try
        {
            /*
             * TT# 5101525411 handling the case of str="", returning _default val on
             * str="")
             */
            if (str.equals(""))
            {
                return val;
            }
            val = Integer.parseInt(str);
        }
        catch (final NumberFormatException e)
        {
            new MinorLogMsg(CRMProcessorSupport.class.getName(), e.getMessage(), e).log(ctx);
        }

        return val;
    }


    public static long getLong(final Context ctx, String str, long _default)
    {
        long ret = _default;
        
        try
        {
            if(str==null || str.trim().length()==0)
            {
                return ret;
            }
            else
            {
                ret = Long.parseLong(str);
            }
        }
        catch (NumberFormatException e)
        {
            LogSupport.minor(ctx, CRMProcessor.class.getName(), e.getMessage(), e);
        }
        
        return ret;
    }
    
    /**
     * Returns the double value represented by the string.
     *
     * @param ctx
     *            The operating context.
     * @param str
     *            String to be parsed.
     * @param defaultValue
     *            Default value to be used if the provided string is empty.
     * @return Double value represented by the string.
     */
    public static double getDouble(final Context ctx, final String str, final double defaultValue)
    {
        double val = defaultValue;
        try
        {
            val = Double.parseDouble(str);
        }
        catch (final NumberFormatException e)
        {
            new MinorLogMsg(CRMProcessorSupport.class.getName(), e.getMessage(), e).log(ctx);
        }

        return val;
    }


    /**
     * Returns the date-time represented by the string.
     *
     * @param str
     *            String to be parsed.
     * @return The date-time represented by the string.
     * @throws ParseException
     *             Thrown if there are problems parsing the date.
     */
    public static Date getDate(final String str) throws ParseException
    {
        final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_STRING);
        return dateFormat.parse(str);
    }


    /**
     * Returns the date (without time) represented by the string.
     *
     * @param str
     *            String to be parsed.
     * @return The date represented by the string.
     * @throws ParseException
     *             Thrown if there are problems parsing the date.
     */
    public static Date getDateOnly(final String str) throws ParseException
    {
        final DateFormat dateFormat = new SimpleDateFormat(DATE_ONLY_FORMAT_STRING);
        return dateFormat.parse(str);
    }


    /**
     * Returns the MSISDN represented by the string and validates it is in the accepted
     * MSISDN format.
     *
     * @param msisdn
     *            MSISDN to be parsed.
     * @return The MSISDN represented by the string.
     * @throws ParseException
     *             Thrown if the MSISDN does not conform to the accepted MSISDN format as
     *             defined in {@link Msisdn} bean.
     */
    public static String getMsisdn(final String msisdn) throws ParseException
    {
        if (!Msisdn.MSISDN_PATTERN.matcher(String.valueOf(msisdn)).matches())
        {
            throw new ParseException("invalid MSISDN " + msisdn, 10);
        }
        return msisdn;
    }


    /**
     * Returns the value of field at the provided position.
     *
     * @param params
     *            List of fields.
     * @param index
     *            Index of the field to be retrieved.
     * @return The value of the field at the provided position.
     */
    public static String getField(final List<String> params, final int index)
    {
        if (params == null || params.size() <= index || index < 0)
        {
            return "";
        }

        return params.get(index);
    }


    /**
     * Returns the value of field at the provided position.
     *
     * @param params
     *            List of fields.
     * @param index
     *            Index of the field to be retrieved.
     * @param defaultValue
     *            Default value of the field.
     * @return The value of the field at the provided position.
     */
    public static String getField(final List<String> params, final int index, final String defaultValue)
    {
        if (params == null || params.size() <= index)
        {
            return defaultValue;
        }

        String val = params.get(index);
        if (val == null)
        {
            val = defaultValue;
        }
        else if ( val.length() == 0 )
        {
            val = defaultValue;
        }

        return val;
    }


    /**
     * Returns the timestamp of a raw ER. A utility function for ER testing.
     *
     * @param rawErRecord
     *            Raw ER record.
     * @return the date of the ER.
     * @throws ParseException
     *             Thrown if there are problems parsing the date.
     */
    public static Date getErDate(final String rawErRecord) throws ParseException
    {
        // TODO 2008-12-10 this is a potential performance issue. ER is Pattern tokenized to get 2 fields.
        final String[] erFields = rawErRecord.split(",");
        final String dateTimeStr = erFields[0] + " " + erFields[1];
        return getDate(dateTimeStr);
    }


    /**
     * Returns the starting index of an ER. Starting index starts at the field after ER
     * ID. A utility function for ER testing.
     *
     * @param erId
     *            ER ID.
     * @param rawErRecord
     *            Raw ER.
     * @return The starting index of an ER.
     * @throws ParseException
     *             Thrown if the ER format is incorrect.
     */
    public static int getErStartIndex(final String erId, final String rawErRecord) throws ParseException
    {
        final String delimiter = ",";
        int startIndex = rawErRecord.indexOf(delimiter + erId + delimiter);
        if (startIndex == -1)
        {
            throw new ParseException("not correct er for" + erId + "<" + rawErRecord + ">", 0);
        }
        startIndex = startIndex + 1 + erId.length();
        return startIndex;
    }


    /**
     * Processed a raw ER. A utility function for ER testing.
     *
     * @param context
     *            The operating context.
     * @param erId
     *            ER ID.
     * @param rawErRecord
     *            The raw ER.
     * @param erExecAgent
     *            The ER processing agent.
     * @throws ParseException
     *             Thrown if there are problems parsing the syntax ER.
     * @throws AgentException
     *             Thrown if there are operational problems when processing the ER.
     */
    public static void processEr(final Context context, final String erId, final String rawErRecord,
        final ContextAgent erExecAgent) throws ParseException, AgentException
    {
        final Context ctx = context.createSubContext();
        final Date date = CRMProcessorSupport.getErDate(rawErRecord);
        final int startIndex = CRMProcessorSupport.getErStartIndex(erId, rawErRecord);
        ctx.put(ProcessorInfo.class, new ProcessorInfo(date.getTime(), erId, rawErRecord.toCharArray(), startIndex));

        /**
         * this is some hack if we need to execute this function from beanshell
         */

        final ErPollerConfig config = (ErPollerConfig) ctx.get(ErPollerConfig.class);
        if (config == null)
        {
            ctx.put(ErPollerConfig.class, new ErPollerConfig());
        }
        final PollerProcessorPackage packCfg = (PollerProcessorPackage) ctx.get(PollerProcessorPackage.class);
        if (packCfg == null)
        {
            ctx.put(PollerProcessorPackage.class, new PollerProcessorPackage());
        }

        erExecAgent.execute(ctx);
    }


    /**
     * Converts a char-array ER into a list of string.
     *
     * @param ctx
     *            The operating context.
     * @param params
     *            The list to store the tokenized parameters in output.
     * @param record
     *            ER to be tokenized.
     * @param idx
     *            Start index of the field.
     * @param ch
     *            Character used as string separator.
     * @param erid
     *            ER ID.
     * @param source
     *            Caller of this method.
     * @return The list of tokenized parameters.
     * @throws FilterOutException
     *             Thrown if the ER date is out of range for the allowed date range.
     */
    public static List<String> makeArray(final Context ctx, final List<String> params, final char[] record,
        final int idx, final char ch, final String erid, final Object source) throws FilterOutException
    {
        if (!CRMProcessorSupport.validateErEventDate(ctx, record))
        {
            new MinorLogMsg(source, "This ER date is out of range for the allowed date range for parsing "
                + ". Not parsing this ER " + Arrays.toString(record) + " . Check the ER date ", null).log(ctx);
            throw new FilterOutException("This ER date is out of range for the allowed date range for parsing");
        }

        String str = "";
        int startIndex = idx;
        if (record[startIndex] == ',')
        {
            startIndex++;
        }

        /*
         * new DebugLogMsg(this,"startIndex:" + startIndex + "record.length:" +
         * record.length, null).log(getContext());
         */
        str = new String(record, startIndex, record.length - startIndex);

        // cuts the EOLs if any
        while (str.length() > 0 && (str.endsWith("\n") || str.endsWith("\r")))
        {
            str = str.substring(0, str.length() - 1);
        }

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(CRMProcessorSupport.class.getName(), "Processing:" + str, null).log(ctx);
        }

        final StringSeperator sep = new StringSeperator(str, ch);

        params.clear();

        while (sep.hasNext())
        {
            params.add(sep.next());
        }

        if (CRMProcessorSupport.filterER(ctx, params, erid))
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(source, "Er is filter out:", null).log(ctx);
            }
            throw new FilterOutException("This ER date is filter out by ER filter");
        }

        return params;
    }


    /**
     * Validate the ER's event date depending upon the "Past Valid Days" and "Future Valid
     * Days" field under the respective poller's configuration.
     *
     * @param ctx
     *            The operating context.
     * @param erRecord
     *            The ER record to be validated.
     * @return Returns <code>true</code> if the event date of the ER is valid.
     */
    public static boolean validateErEventDate(final Context ctx, final char[] erRecord)
    {
        final ErPollerConfig config = (ErPollerConfig) ctx.get(ErPollerConfig.class);
        boolean isValid = false;
        Calendar todayMidnight;
        Calendar pastDateMidnight;
        Calendar futureDateMidnight;
        final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_ONLY_FORMAT_STRING);
        final Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
        todayMidnight = new GregorianCalendar();
        todayMidnight.setTime(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(runningDate));

        Date timePast;
        Date timeFuture;
        if (config == null)
        {
            return isValid;
        }

        try
        {
            final int pastValidDays = config.getPastValidDays();
            final int futureValidDays = config.getFutureValidDays();

            pastDateMidnight = (GregorianCalendar) todayMidnight.clone();
            pastDateMidnight.add(Calendar.DAY_OF_MONTH, -pastValidDays);

            futureDateMidnight = (GregorianCalendar) todayMidnight.clone();
            futureDateMidnight.add(Calendar.DAY_OF_MONTH, futureValidDays);

            timePast = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(pastDateMidnight.getTime());
            timeFuture = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(futureDateMidnight.getTime());

            /*
             * only parse the first 25 chars: assume the format
             * 2002/09/27,01:35:27,301,....
             */
            final String dateComponent = new String(erRecord, Constants.ER_DATE_START_POS, Constants.ER_DATE_LENGTH);
            final Date erDate = dateFormat.parse(dateComponent);

            if (erDate.after(timePast) && (erDate.before(timeFuture) || erDate.equals(timeFuture)))
            {
                isValid = true;
            }
            else
            {
                isValid = false;
            }
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(CRMProcessorSupport.class, "Er Event Date(without Time): " + erDate
                    + ",Considered Past Date: " + timePast + ",Considered Future Date: " + timeFuture + ",Valid = "
                    + isValid, null).log(ctx);
            }
        }
        catch (final ParseException pe)
        {
            new MinorLogMsg(CRMProcessorSupport.class,
                "Invalid date format specified in the Er,expected Date format is " + dateFormat, pe).log(ctx);
        }
        return isValid;
    }


    /**
     * Determines whether the ER satisfies the filter.
     *
     * @param ctx
     *            The operating context.
     * @param params
     *            ER as a list of tokenized parameters.
     * @param filterPairs
     *            Collection ER filter pairs.
     * @return Returns <code>true</code> if the ER satisfies one or more of the
     *         patterns, <code>false</code> otherwise.
     */
    public static boolean filterER(final Context ctx, final List<String> params,
        final Collection<ERFilterPair> filterPairs)
    {
        boolean result = false;
        for (final ERFilterPair pair : filterPairs)
        {
            try
            {
                final String erSection = CRMProcessorSupport.getField(params, pair.getPosition());
                final Pattern pattern = Pattern.compile(pair.getFilterPattern(), Pattern.DOTALL);
                final Matcher m = pattern.matcher(erSection);
                if (!m.matches())
                {
                    result = true;
                    break;
                }
            }
            catch (final Throwable t)
            {
                LogSupport.debug(ctx, CRMProcessorSupport.class.getName(), "Error in ER filtering: ", t);
                result = true;
                break;
            }
        }

        return result;
    }


    /**
     * Determines whether the ID of the ER matches the provided ID.
     *
     * @param ctx
     *            The operating context.
     * @param params
     *            ER as a list of tokenized parameters.
     * @param erid
     *            ER ID.
     * @return Returns <code>true</code> if the ID of the ER matches the provided ID.
     */
    @SuppressWarnings("unchecked")
    public static boolean filterER(final Context ctx, final List<String> params, final String erid)
    {
        final PollerProcessorPackage config = (PollerProcessorPackage) ctx.get(PollerProcessorPackage.class);
        final List<ERFilterPair> filterPairs = config.getFilterPair();
        final Collection<ERFilterPair> matches = CollectionSupportHelper.get(ctx).findAll(ctx, filterPairs,
                new EQ(ERFilterPairXInfo.ID, erid));
        return CRMProcessorSupport.filterER(ctx, params, matches);
    }


    /**
     * Validates the number of fields given in an ER
     * @param params
     *              List of fields from ER
     * @param num
     *              The number of fields expected for ER
     */
    public static void validateErFields(final List<String> params, final int num)
    {
        int extraFields = 3; // +3(Date, Time, Event Record ID)
        if ((params.size() + extraFields) < num)
        {
            throw new IllegalArgumentException("Fields are missing from ER. Found " + (params.size() + extraFields)
                    + " but expected at least " + num);
        }
    }
}
