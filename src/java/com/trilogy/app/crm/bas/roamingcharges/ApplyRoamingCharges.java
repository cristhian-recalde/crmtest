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

package com.trilogy.app.crm.bas.roamingcharges;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.omg.CORBA.LongHolder;

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.util.time.Time;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.ERLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CallType;
import com.trilogy.app.crm.bean.CallTypeHome;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.calldetail.BillingCategoryEnum;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallDetailHome;
import com.trilogy.app.crm.bean.calldetail.CallTypeEnum;
import com.trilogy.app.crm.bean.calldetail.RateUnitEnum;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.client.AppOcgClient;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.app.crm.support.MultiDbSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.product.s2100.ErrorCode;


/**
 * Description of the Class
 */
public class ApplyRoamingCharges implements ContextAware
{

    /**
     * Gets the context attribute of the ApplyRoamingCharges object
     * 
     *@return The context value
     */
    public Context getContext()
    {
        return ctx_;
    }


    /**
     * Sets the context attribute of the ApplyRoamingCharges object
     * 
     *@param arg0 The new context value
     */
    public void setContext(final Context arg0)
    {
        ctx_ = arg0;
    }


    /**
     * Constructor for the ApplyRoamingCharges object
     * 
     *@param ctx Description of the Parameter
     */
    public ApplyRoamingCharges(final Context ctx)
    {
        setContext(ctx);
        roamingFileFilter = new RoamingFileFilter(getContext());
    }


    /**
     * Gets the exceptionLines attribute of the ApplyRoamingCharges class
     * 
     *@param t Description of the Parameter
     *@param n Description of the Parameter
     *@return The exceptionLines value
     */
    public static String getExceptionLines(final Throwable t, int n)
    {
        final StringBuilder buf = new StringBuilder();
        buf.append("\n");
        final StackTraceElement[] ste = t.getStackTrace();

        if (ste != null && ste.length > 0)
        {
            if (ste.length < n)
            {
                n = ste.length;
            }
            for (int i = 0; i < n; i++)
            {
                buf.append(ste[i].toString()).append("\n");
            }
        }
        else
        {
            buf.append("\n");
        }
        return buf.toString();
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Description of the Method
     */
    public void execute()
    {
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this, "ApplyRoamingCharges::execute() BEGIN", null).log(getContext());
        }

        final File dir = new File(getRoamingDir());
        final String[] filenames = dir.list(roamingFileFilter);
        for (int i = 0; i < filenames.length; i++)
        {
            try
            {

                final File f = new File(dir + File.separator + filenames[i]);
                if (f.isDirectory())
                {
                    continue;
                }

                parseFile(f, filenames[i]);
                moveFile(f, filenames[i]);
            }
            catch (final IOException e)
            {
                final String m = "Unable to read from roaming file: " + dir + File.separator + filenames[i]
                        + ".  Proceed to the next file.";
                new InfoLogMsg(this, m, e).log(getContext());
                continue;
            }
        }

        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this, "ApplyRoamingCharges::execute() END", null).log(getContext());
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Description of the Method
     * 
     *@param f Description of the Parameter
     *@param filename Description of the Parameter
     *@exception IOException Description of the Exception
     */
    private void parseFile(final File f, final String filename) throws IOException
    {
        final String fullPathfilename = f.getAbsolutePath();
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this, "ApplyRoamingCharges::parseFile() BEGIN filename=" + fullPathfilename,
                    null).log(getContext());
        }

        final FileInputStream fin = new FileInputStream(f);
        final BufferedReader brdr = new BufferedReader(new InputStreamReader(fin));
        String line = null;
        while ((line = brdr.readLine()) != null)
        {

            try
            {
                if (line.trim().length() == 0)
                {
                    continue;
                }

                new OMLogMsg(Common.OM_MODULE, Common.OM_ROAMING_CHARGE_ATTEMPT).log(getContext());

                final Map entry = new TreeMap();
                entry.put(Constants.ROAMING_FILENAME, filename);
                try
                {
                    parseEntry(line, entry);
                }
                catch (final Throwable t)
                {
                    String m = "Invalid roaming file entry! \n";
                    m += "Reason: " + t.getMessage() + "\n";
                    m += "StkTrace:" + getExceptionLines(t, 8);
                    new InfoLogMsg(this, m, t).log(getContext());

                    //1. add entry to exception file for inspection and reprocessing
                    addEntry4Inspection(filename, line);

                    //2. invalid roaming entry ER
                    final String msisdn = (String) entry.get(Constants.CHARGED_MSISDN);
                    new ERLogMsg(778, 700, Constants.ER_INVALID_ROAMING_ENTRY_EVENT, 0, new String[]
                    {msisdn, "", line, Constants.RESULT_GEN_ERR}).log(getContext());

                    //3. invalid roaming entry alarm
                    new EntryLogMsg(10504L, this, this.toString(), null, null, t).log(getContext());
                    continue;
                }

                Subscriber subProfile = null;
                try
                {
                    subProfile = getSubscriberProfile(getContext(), entry);
                }
                catch (final HomeException e)
                {
                    String m = "Can not get subscriber profile! \n";
                    m += "Reason: " + e.getMessage() + "\n";
                    m += "StkTrace:" + getExceptionLines(e, 8);
                    new InfoLogMsg(this, m, e).log(getContext());

                    //1. add entry to exception file for inspection and reprocessing
                    addEntry4Inspection(filename, line);

                    //2. invalid roaming entry ER
                    final String msisdn = (String) entry.get(Constants.CHARGED_MSISDN);
                    new ERLogMsg(778, 700, Constants.ER_INVALID_ROAMING_ENTRY_EVENT, 0, new String[]
                    {msisdn, "", line, Constants.RESULT_GEN_ERR}).log(getContext());

                    //3. invalid roaming entry alarm
                    new EntryLogMsg(10504L, this, this.toString(), null, null, null).log(getContext());
                    continue;
                }

                if (this.isCallDetailExist(getContext(), entry))
                {
                    //1. add entry to exception file for inspection and reprocessing
                    addEntry4Inspection(filename, line);

                    //2. invalid roaming entry ER
                    final String msisdn = (String) entry.get(Constants.CHARGED_MSISDN);
                    new ERLogMsg(778, 700, Constants.ER_INVALID_ROAMING_ENTRY_EVENT, 0, new String[]
                    {msisdn, "", line, Constants.RESULT_DUPLICATE_ENTRY}).log(getContext());

                    //3. invalid roaming entry alarm
                    new EntryLogMsg(10504L, this, this.toString(), null, null, null).log(getContext());
                    continue;
                }

                // get GLCode based on CallType (TranType) and SPID
                entry.put(Constants.GLCODE, getGlCode(entry));

                boolean isApplyTransSuccess = false;
                final String[] ocgResult = new String[1]; // DZ: a holder for the return value
                try
                {
                    isApplyTransSuccess = applyTransaction(entry, subProfile, ocgResult);
                }
                catch (final Throwable t)
                {
                    String m = "Can not apply transaction! \n";
                    m += "Reason: " + t.getMessage() + "\n";
                    m += "StkTrace:" + getExceptionLines(t, 8);
                    new InfoLogMsg(this, m, t).log(getContext());

                    isApplyTransSuccess = false;
                }

                if (isApplyTransSuccess)
                {
                    final int tax_amount = getNumber((String) entry.get(Constants.TAX_AMOUNT));
                    if (tax_amount > 0)
                    {
                        try
                        {
                            entry.put(Constants.CHARGE, tax_amount + "");
                            entry.put(Constants.BILLING_CATEGORY, new Short(BillingCategoryEnum.ROAMING_TAX_INDEX));
                            entry.put(Constants.APPLY_TAX_AMOUNT, "YES");
                            entry.put(Constants.TRANSACTION_TYPE, CallTypeEnum.ROAMING_TAX);
                            isApplyTransSuccess = applyTransaction(entry, subProfile, ocgResult);
                        }
                        catch (final Throwable t)
                        {
                            new InfoLogMsg(this, "Unable to apply transaction (roaming tax).", t).log(getContext());
                            isApplyTransSuccess = false;
                        }
                    }
                }

                final String[] erStringArray = getErStringArray(entry, subProfile);
                final int spid = subProfile.getSpid();
                if (isApplyTransSuccess)
                {
                    //1.
                    erStringArray[erStringArray.length - 1] = Constants.RESULT_SUCCESS;
                    //new ERLogMsg(775, 700, Constants.ER_ROAMING_CHARGE_EVENT, spid, erStringArray).log(getContext());

                    //2.
                    new OMLogMsg(Common.OM_MODULE, Common.OM_ROAMING_CHARGE_SUCCESS).log(getContext());
                }
                else
                {

                    //1.
                    erStringArray[erStringArray.length - 1] = Constants.RESULT_GEN_ERR;
                    //new ERLogMsg(775, 700, Constants.ER_ROAMING_CHARGE_EVENT, spid, erStringArray).log(getContext());

                    //2.
                    new OMLogMsg(Common.OM_MODULE, Common.OM_ROAMING_CHARGE_FAIL).log(getContext());
                }
                //DZ  moved down here
                erStringArray[erStringArray.length - 2] = ocgResult[0];
                new ERLogMsg(775, 700, Constants.ER_ROAMING_CHARGE_EVENT, spid, erStringArray).log(getContext());

            }
            catch (final Throwable unexpected)
            {
                new InfoLogMsg(this, getExceptionLines(unexpected, 10), unexpected).log(getContext());
            }
        }

        brdr.close();

        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this, "ApplyRoamingCharges::parseFile() END filename=" + filename, null).log(getContext());
        }
    }


    /**
     * Description of the Method
     * 
     *@param file Description of the Parameter
     *@param filename Description of the Parameter
     */
    private void moveFile(final File file, final String filename)
    {
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this, "ApplyRoamingCharges::moveFile() BEGIN", null).log(getContext());
        }

        File dir = new File(getRoamingArchiveDir());
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        File destFile = new File(dir, filename + ".processed");
        boolean success = file.renameTo(destFile);
        if (!success)
        {
            String m = "Can not move file [" + file.getAbsolutePath() + "] to [" + destFile.getAbsolutePath()
                    + "], attempt to rename file to avoid reprocessing.";
            new MinorLogMsg(this, m, null).log(getContext());
            dir = new File(getRoamingDir());
            destFile = new File(dir, filename + ".processed");
            success = file.renameTo(destFile);
            if (!success)
            {
                m = "Can not rename file [" + file.getAbsolutePath() + "] to " + destFile.getAbsolutePath() + "]";
                new MajorLogMsg(this, m, null).log(getContext());
            }
        }

        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this, "ApplyRoamingCharges::moveFile() END", null).log(getContext());
        }
    }


    /**
     * Gets the erStringArray attribute of the ApplyRoamingCharges object
     * 
     *@param entry Description of the Parameter
     *@param subProfile Description of the Parameter
     *@return The erStringArray value
     */
    private String[] getErStringArray(final Map entry, final Subscriber subProfile)
    {
        final String[] a = new String[8]; // DZ: add one more field for OCG result
        a[0] = (String) entry.get(Constants.CHARGED_MSISDN);
        a[1] = subProfile.getBAN();
        a[2] = (String) entry.get(Constants.TOTAL_CHARGE);
        a[3] = ((CallTypeEnum) entry.get(Constants.TRANSACTION_TYPE)).getDescription();
        a[4] = (String) entry.get(Constants.GLCODE);
        a[5] = (String) entry.get(Constants.ROAMING_FILENAME);
        return a;
    }


    /**
     * Adds a feature to the Entry4Inspection attribute of the ApplyRoamingCharges object
     * 
     *@param origFilename The feature to be added to the Entry4Inspection attribute
     *@param line The feature to be added to the Entry4Inspection attribute
     */
    private void addEntry4Inspection(final String origFilename, final String line)
    {
        RandomAccessFile inspectionFile_ = null;
        File destFile = null;
        try
        {
            final File dir = new File(getRoamingErrDir());
            if (!dir.exists())
            {
                dir.mkdirs();
            }
            destFile = new File(dir, origFilename + ".err");
            inspectionFile_ = new RandomAccessFile(destFile, "rw");
            inspectionFile_.seek(inspectionFile_.length());
            inspectionFile_.writeBytes(line + "\n");
        }
        catch (final Throwable t)
        {
            String m = "Error occur when writing to inspection file=";
            m += (destFile == null) ? "unknown" : destFile.getAbsolutePath();
            new InfoLogMsg(this, m, t).log(getContext());
        }
        finally
        {
            try
            {
                inspectionFile_.close();
            }
            catch (final IOException ignore)
            {
            }
        }
    }


    /**
     * Gets the subscriberProfile attribute of the ApplyRoamingCharges object
     * 
     *@param entry Description of the Parameter
     *@return The subscriberProfile value
     *@exception HomeException Description of the Exception
     */
    private Subscriber getSubscriberProfile(final Context ctx, final Map entry) throws HomeException
    {
        final String imsi = (String) entry.get(Constants.IMSI);

        Subscriber subProfile = SubscriberSupport.lookupSubscriberForIMSI(getContext(), imsi);

        // DUAL IMSI SUPPORT
        // It could happen that the IMSI was listed with the alternate prefix
        // instead of the real one.  In this case we need to lookup the CRMSpid
        // based on it's alternate IMSI Prefix and then replace the alternate
        // prefix with the real one before attempting to lookup the Subscriber
        // again., KGR
        if (subProfile == null)
        {
            final Home spidHome = (Home) getContext().get(CRMSpidHome.class);
            final CRMSpid cspid = (CRMSpid) spidHome.find(ctx, new Predicate()
            {

                public boolean f(final Context _ctx, final Object obj)
                {
                    final CRMSpid spid = (CRMSpid) obj;

                    return spid.getImsiPrefix2().length() > 0 && imsi.startsWith(spid.getImsiPrefix2());
                }
            });

            if (cspid != null)
            {
                // trim off the old (alternate) prefix and add the new (real) one
                final String imsi2 = cspid.getImsiPrefix() + imsi.substring(cspid.getImsiPrefix2().length());

                // Try again with the Prefix replaced
                subProfile = SubscriberSupport.lookupSubscriberForIMSI(getContext(), imsi2);
            }
        }

        if (subProfile != null)
        {
            final String spid = String.valueOf(subProfile.getSpid());
            entry.put(Constants.SPID, spid);
            final int callTypeInt = ((Integer) entry.get(Constants.CALL_TYPE)).intValue();
            entry.put(Constants.CHARGED_MSISDN, subProfile.getMSISDN());
            entry.put(Constants.BAN, subProfile.getBAN());
            if (callTypeInt == Constants.CALL_TYPE_MOC)
            {
                entry.put(Constants.ORIGINATING_MSISDN, subProfile.getMSISDN());
            }
            else if (callTypeInt == Constants.CALL_TYPE_MTC)
            {
                entry.put(Constants.DESTINATION_MSISDN, subProfile.getMSISDN());
            }
        }
        else
        {
            final String m = "Unable to find subscriber profile for IMSI=" + entry.get(Constants.IMSI);

            throw new HomeException(m);
        }
        return subProfile;
    }


    /**
     * Gets the callDetailExist attribute of the ApplyRoamingCharges object
     * 
     *@param entry Description of the Parameter
     *@return The callDetailExist value
     *@exception HomeException Description of the Exception
     */
    private boolean isCallDetailExist(final Context ctx, final Map entry) throws HomeException
    {
        final Home home = (Home) getContext().get(CallDetailHome.class);

        if (home == null)
        {
            final String m = "System Error: CallDetailHome does not exist in context.";
            throw new HomeException(m);
        }

        final String msisdn = (String) entry.get(Constants.CHARGED_MSISDN);
        final String transDate = String.valueOf(((Date) entry.get(Constants.TRANSACTION_DATE)).getTime());
        final String transType = ((CallTypeEnum) entry.get(Constants.TRANSACTION_TYPE)).getIndex() + "";

        final String sqlClause = "CHARGEDMSISDN='" + msisdn + "' and TRANDATE=" + transDate + " and CALLTYPE='"
                + transType + "' ";
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this, "SQLClause=" + sqlClause, null).log(getContext());
        }
        final Object o = home.find(ctx, sqlClause);

        if (o != null)
        {
            return true;
        }

        return false;
    }


    /**
     * Gets the account attribute of the ApplyRoamingCharges object
     * 
     *@param subProfile Description of the Parameter
     *@return The account value
     *@exception HomeException Description of the Exception
     */
    private Account getAccount(final Subscriber subProfile) throws HomeException
    {
        final Home acctHome = (Home) getContext().get(AccountHome.class);
        if (acctHome == null)
        {
            final String m = "System error: AccountHome is not found in context.";
            throw new HomeException(m);
        }
        Account acct = null;
        acct = (Account) acctHome.find(getContext(), subProfile.getBAN());
        if (acct == null)
        {
            final String m = "Account not found for BAN=" + subProfile.getBAN();
            throw new HomeException(m);
        }
        return acct;
    }


    /**
     * Description of the Method
     * 
     *@param line Description of the Parameter
     *@param entry Description of the Parameter
     *@exception ParseException Description of the Exception
     */
    private void parseEntry(final String line, final Map entry) throws ParseException
    {
        final Date transDate;
        {
            final SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            format.setLenient(false);
            try
            {
                transDate = format.parse(line.substring(0, 14));
            }
            catch (final java.text.ParseException exception)
            {
                throw new ParseException("Invalid date entry: " + line.substring(0, 14), exception);
            }
        }

        final CallDetail th = new CallDetail();

        entry.put(Constants.TRANSACTION_DATE_STRING, line.substring(0, 8).trim());
        entry.put(Constants.TRANSACTION_TIME_STRING, line.substring(8, 14).trim());
        entry.put(Constants.TRANSACTION_DATE, transDate);
        entry.put(Constants.TRANSACTION_TIME, transDate);
        th.setTranDate((Date) entry.get(Constants.TRANSACTION_DATE));

        final String callType = line.substring(14, 16).trim();
        final int callTypeInt = Integer.parseInt(callType);
        entry.put(Constants.CALL_TYPE, Integer.valueOf(callTypeInt));

        final String servCode = line.substring(216, 226).trim();
        CallTypeEnum transType;
        short billCat = BillingCategoryEnum.ROAMING_INCOMING_INDEX;

        if (Constants.CALL_TYPE_MOC == callTypeInt)
        {
            if (Constants.SERV_CODE_22.equals(servCode))
            {
                transType = CallTypeEnum.ROAMING_SMS;
            }
            else
            {
                transType = CallTypeEnum.ROAMING_MO;
            }
            billCat = BillingCategoryEnum.ROAMING_OUTGOING_INDEX;
        }
        else if (Constants.CALL_TYPE_MTC == callTypeInt)
        {
            if (Constants.SERV_CODE_21.equals(servCode))
            {
                transType = CallTypeEnum.ROAMING_SMS;
            }
            else
            {
                transType = CallTypeEnum.ROAMING_MT;
            }
            billCat = BillingCategoryEnum.ROAMING_INCOMING_INDEX;
        }
        else if (Constants.CALL_TYPE_SMS == callTypeInt)
        {
            transType = CallTypeEnum.ROAMING_MO;
            billCat = BillingCategoryEnum.ROAMING_OUTGOING_INDEX;
        }
        else
        {
            transType = CallTypeEnum.ROAMING_MT;
            billCat = BillingCategoryEnum.ROAMING_INCOMING_INDEX;
        }
        entry.put(Constants.TRANSACTION_TYPE, transType);
        entry.put(Constants.BILLING_CATEGORY, new Short(billCat));
        th.setBillingCategory(billCat);
        th.setCallType((CallTypeEnum) entry.get(Constants.TRANSACTION_TYPE));

        final Date curDate = new Date();
        final SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_STRING);
        entry.put(Constants.POSTED_DATE_STRING, sdf.format(curDate));
        entry.put(Constants.POSTED_DATE, curDate);
        entry.put(Constants.POSTED_TIME, curDate);
        th.setPostedDate((Date) entry.get(Constants.POSTED_DATE));

        String origMsisdn;

        String destMsisdn;
        if (Constants.CALL_TYPE_MTC == callTypeInt)
        {
            origMsisdn = line.substring(121, 141).trim();
            destMsisdn = line.substring(31, 51).trim();
        }
        else
        {
            origMsisdn = line.substring(31, 51).trim();
            destMsisdn = line.substring(121, 141).trim();
        }

        validateNumber(origMsisdn, true);
        validateNumber(destMsisdn, true);
        entry.put(Constants.ORIGINATING_MSISDN, origMsisdn);
        entry.put(Constants.DESTINATION_MSISDN, destMsisdn);
        th.setOrigMSISDN((String) entry.get(Constants.ORIGINATING_MSISDN));
        th.setDestMSISDN((String) entry.get(Constants.DESTINATION_MSISDN));

        final String imsi = line.substring(51, 71).trim();
        validateNumber(imsi, false);
        entry.put(Constants.IMSI, imsi);

        entry.put(Constants.CALLING_PARTY_LOCATION, line.substring(286, 306).trim());
        th.setCallingPartyLocation((String) entry.get(Constants.CALLING_PARTY_LOCATION));

        final Time duration = new Time();
        validateNumber(line.substring(16, 31).trim(), false);
        duration.set(0, 0, Integer.parseInt(line.substring(16, 31).trim()), 0);
        entry.put(Constants.DURATION, duration);
        th.setDuration((Time) entry.get(Constants.DURATION));

        entry.put(Constants.FLAT_RATE, "0");
        entry.put(Constants.VARIABLE_RATE, "0");
        entry.put(Constants.VARIABLE_RATE_UNIT, RateUnitEnum.SEC);

        final String charge = validateNumber(line.substring(236, 251).trim(), false);
        entry.put(Constants.CHARGE, charge);
        th.setCharge(Long.parseLong((String) entry.get(Constants.CHARGE)));

        entry.put(Constants.USED_MONTHLY_BUCKET_MINUTES, "0");

        entry.put(Constants.RATE_PLAN, "0");
        entry.put(Constants.RATING_RULE, "0");

        entry.put(Constants.MSC_CALL_REFERENCE_ID, line.substring(101, 121).trim());
        th.setCallID((String) entry.get(Constants.MSC_CALL_REFERENCE_ID));

        entry.put(Constants.DISCONNECT_REASON, "0");

        entry.put(Constants.PLMN_CODE, line.substring(402, 427).trim());
        th.setComments((String) entry.get(Constants.PLMN_CODE));

        th.setTransactionSourceId((String) entry.get(Constants.ROAMING_FILENAME));

        validateNumber(line.substring(427, 442).trim(), false);
        entry.put(Constants.TAX_AMOUNT, line.substring(427, 442).trim());

        final long amt = Long.parseLong((String) entry.get(Constants.CHARGE));
        final long tax_amt = Long.parseLong((String) entry.get(Constants.TAX_AMOUNT));
        entry.put(Constants.TOTAL_CHARGE, String.valueOf(amt + tax_amt));
    }


    /**
     * Description of the Method
     * 
     *@param string Description of the Parameter
     *@param allowEmpty Description of the Parameter
     *@return Description of the Return Value
     *@exception ParseException Description of the Exception
     */
    private String validateNumber(final String string, final boolean allowEmpty) throws ParseException
    {
        try
        {
            if (string.length() == 0 && allowEmpty)
            {
                return string;
            }
            final long num = Long.parseLong(string);
            if (num < 0)
            {
                throw new ParseException("Invalid number=" + string);
            }
            return num + "";
        }
        catch (final NumberFormatException e)
        {
            throw new ParseException("Invalid number=" + string);
        }
    }


    /**
     * Description of the Method
     * 
     *@param entry Description of the Parameter
     *@param subProfile Description of the Parameter
     *@param result OCG call return value
     *@return Description of the Return Value
     *@exception TransactionException Description of the Exception
     *@exception HomeException Description of the Exception
     */
    private boolean applyTransaction(final Map entry, final Subscriber subProfile, final String[] ocgRet) // DZ: add one more return val
            throws TransactionException, HomeException
    {
        final AppOcgClient client = (AppOcgClient) getContext().get(AppOcgClient.class);
        if (client == null)
        {
            final String m = "System error: Can not find AppOcgClient in context.";
            throw new TransactionException(m);
        }

        final Account acct = getAccount(subProfile);

        int result = Constants.FAILURE;
        ocgRet[0] = String.valueOf(result);
        final int amount = Integer.parseInt((String) entry.get(Constants.CHARGE));
        int taxamount = Integer.parseInt((String) entry.get(Constants.TAX_AMOUNT));
        final String msisdn = (String) entry.get(Constants.CHARGED_MSISDN);
        final int spid = Integer.parseInt((String) entry.get(Constants.SPID));

        if (amount > 0 && validateTransaction(amount))
        {
            final boolean bBalFlag = false;
            // for insufficient balance, true = fail transaction, false - zero account
            final String erReference = getErReference();
            //+trans.getReceiptNum();

            logOcgDebitDebugMsg(msisdn, amount, acct.getCurrency(), bBalFlag, erReference);

            final SubscriptionType INSubscriptionType = SubscriptionType.getINSubscriptionType(getContext());
            result = client.requestDebit(msisdn, subProfile.getSubscriberType(), amount, acct.getCurrency(), bBalFlag,
                    erReference, INSubscriptionType.getId(), new LongHolder());
            ocgRet[0] = String.valueOf(result);
            logOmAcctAdjust(result);
        }
        else if (amount==0)
        {
            result = ErrorCode.NO_ERROR;
        }
        else if (amount<0)
        {
            final String m = "Transaction amount is invalid.";
            throw new TransactionException(m);
        }
        else
        {
            final String m = "Transaction amount exceeds users group adjustment limit.";
            throw new TransactionException(m);
        }

        String amt = null;
        if (entry.get(Constants.APPLY_TAX_AMOUNT) == null)
        {
            amt = String.valueOf(amount);
            taxamount = 0;
        }
        else
        {
            amt = "0";
        }

        // generate account adjustment ER
        new ERLogMsg(771, 700, "Account Adjustment Event", spid, new String[]
        {
                acct.getBAN(),
                //TPS Location Code
                "",
                //TPS Transaction No.
                "",
                //Payment Type
                "",
                // Adjustment Type
                (entry.get(Constants.APPLY_TAX_AMOUNT) == null) ? "RoamingCharges" : "RoamingChargeTax",
                //payment method
                "",
                //payment details
                "",
                // transaction Date
                ERLogger.formatERDateDayOnly((Date)entry.get(Constants.TRANSACTION_DATE)),
                // void ID
                "",
                //csr input
                "\"\"",
                // payment amount
                amt,
                //tax amount
                "" + taxamount,
                //GL code
                "" + entry.get(Constants.GLCODE),
                // ocg result
                "" + result, "" + result}).log(getContext());

        if (result == ErrorCode.NO_ERROR)
        {
            writeTransactionHistory(entry);
            return true;
        }

        if (LogSupport.isDebugEnabled(getContext()))
        {
            final String m = "Did not write to CallDetail table because OCG result code=" + result;
            if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(this, m, null).log(getContext());
            }
        }
        return false;
    }


    /**
     * Gets the erReference attribute of the ApplyRoamingCharges object
     * 
     *@return The erReference value
     */
    private String getErReference()
    {
        final StringBuilder erRef = new StringBuilder();
        erRef.append("AppCrm-");
        String uniqueIntStr = "";

        try
        {
            IdentifierSequenceSupportHelper.get(getContext()).ensureSequenceExists(getContext(),
                    IdentifierEnum.ROAMING_ID, 1000000000000L,
                    2000000000000L);

            // TODO - 2004-08-23 - Provide a roll-over alarm.  Roll-over is
            // unlikely in the near future based on the default configuration,
            // but on-site reconfiguration could change that.
            final long identifier = IdentifierSequenceSupportHelper.get(getContext()).getNextIdentifier(getContext(),
                    IdentifierEnum.ROAMING_ID, null);

            uniqueIntStr = Long.toString(identifier);
        }
        catch (final Throwable throwable)
        {
            new MinorLogMsg(this, "Failed to get next roaming ER reference identifier.", throwable).log(getContext());

            uniqueInt++;
            if (uniqueInt > 99999999)
            {
                uniqueInt = 0;
            }
            uniqueIntStr = Integer.toString(uniqueInt);
            final int n = 8 - uniqueIntStr.length();
            for (int i = 0; i < n; i++)
            {
                erRef.append('0');
            }
        }
        erRef.append(uniqueIntStr);
        return erRef.toString();
    }


    /**
     * Description of the Method
     * 
     *@param amount Description of the Parameter
     *@return Description of the Return Value
     */
    private boolean validateTransaction(final int amount)
    {
        return (Math.abs(amount) <= Math.abs(this.getAdjustmentForPrincipal(getContext())));
    }


    /**
     * Description of the Method
     * 
     *@param result Description of the Parameter
     */
    private void logOmAcctAdjust(final int result)
    {
        if (result == ErrorCode.NO_ERROR)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_ADJUST_SUCCESS).log(getContext());
        }
        else
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_ADJUST_FAIL).log(getContext());
        }
    }


    /**
     * Description of the Method
     * 
     *@param msisdn Description of the Parameter
     *@param amount Description of the Parameter
     *@param currency Description of the Parameter
     *@param bBalFlag Description of the Parameter
     *@param erReference Description of the Parameter
     */
    private void logOcgDebitDebugMsg(final String msisdn, final int amount, final String currency,
            final boolean bBalFlag, final String erReference)
    {
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this, "debit MSISDN[" + msisdn + "] amount[" + amount + "] currency[" + currency
                    + "] balFlag[" + bBalFlag + "] erReference[" + erReference + "]", null).log(getContext());
        }
    }


    /**
     * Gets the glCode attribute of the ApplyRoamingCharges object
     * 
     *@param entry Description of the Parameter
     *@return The glCode value
     */
    private String getGlCode(final Map entry)
    {
        final Home home = (Home) getContext().get(CallTypeHome.class);
        if (home == null)
        {
            new InfoLogMsg(this, "System error: CallTypeHome.class does not exist.", null).log(getContext());
            return "";
        }

        final short callType = ((CallTypeEnum) entry.get(Constants.TRANSACTION_TYPE)).getIndex();
        final int spid = Integer.parseInt((String) entry.get(Constants.SPID));

        CallType call_type = new CallType();
        try
        {
            call_type.setId(callType);
            call_type.setSpid(spid);
            call_type = (CallType) home.find(getContext(), call_type);
            if (call_type == null)
            {
                new InfoLogMsg(this, "Unable to find CallType=" + callType + ", SPID=" + spid, null).log(getContext());
                return "";
            }
            return call_type.getGLCode();
        }
        catch (final HomeException e)
        {
            new InfoLogMsg(this, "Unable to find CallType=" + callType + ", SPID=" + spid, e).log(getContext());
            return "";
        }
    }


    /**
     * Gets the number attribute of the ApplyRoamingCharges object
     * 
     *@param number Description of the Parameter
     *@return The number value
     */
    private int getNumber(final String number)
    {
        int result = 0;
        final String num = number.trim();
        if (num != null && num.length() != 0)
        {
            try
            {
                result = Integer.parseInt(num);
            }
            catch (final NumberFormatException e)
            {
                new InfoLogMsg(this, "ApplyRoamingChareges:getNumber(): number=" + num, e).log(getContext());
            }
        }

        return result;
    }


    /**
     * Gets the adjustmentForPrincipal attribute of the ApplyRoamingCharges object
     * 
     *@param ctx Description of the Parameter
     *@return The adjustmentForPrincipal value
     */
    private long getAdjustmentForPrincipal(final Context ctx)
    {
        final User user = (User) ctx.get(Principal.class);
        if (user == null)
        {
            /* There is no reason to log this because it is actually expected
             when this isn't run through the GUI. 
             String m = "System Error: Principal does not exist in context.";
             new InfoLogMsg(this, m, null).log(getContext());
             */
            return Common.DEFAULT_ADJUST_LIMIT;
        }

        final String strGroup = user.getGroup();
        if (strGroup == null)
        {
            final String m = "Can not get group using user=" + user.getId();
            new InfoLogMsg(this, m, null).log(getContext());
            return Common.DEFAULT_ADJUST_LIMIT;
        }

        final Home groupHome = (Home) ctx.get("RawGroupHome");
        if (groupHome == null)
        {
            final String m = "System Error: RawGroupHome does not exist in context.";
            new InfoLogMsg(this, m, null).log(getContext());
            return Common.DEFAULT_ADJUST_LIMIT;
        }

        try
        {
            final CRMGroup grp = (CRMGroup) groupHome.find(ctx, strGroup);
            if (grp == null)
            {
                final String m = "Can not get CRMGroup for key=" + strGroup;
                new InfoLogMsg(this, m, null).log(getContext());
                return Common.DEFAULT_ADJUST_LIMIT;
            }
            return grp.getAdjustmentLimit();
        }
        catch (final HomeException e)
        {
            final String m = "Can not find CRMGroup for key=" + strGroup;
            new InfoLogMsg(this, m, e).log(getContext());
            return Common.DEFAULT_ADJUST_LIMIT;
        }
    }


    /**
     * Description of the Method
     * 
     *@param entry Description of the Parameter
     *@exception HomeException Description of the Exception
     */
    private void writeTransactionHistory(final Map entry) throws HomeException
    {
        final Home home = (Home) getContext().get(CallDetailHome.class);
        if (home == null)
        {
            final String m = "System Error: CallDetailHome does not exist in context.";
            throw new HomeException(m);
        }

        final CallDetail th = new CallDetail();
        th.setTranDate((Date) entry.get(Constants.TRANSACTION_DATE));
        th.setCallType((CallTypeEnum) entry.get(Constants.TRANSACTION_TYPE));
        th.setPostedDate((Date) entry.get(Constants.POSTED_DATE));
        th.setChargedMSISDN((String) entry.get(Constants.CHARGED_MSISDN));
        th.setOrigMSISDN((String) entry.get(Constants.ORIGINATING_MSISDN));
        th.setDestMSISDN((String) entry.get(Constants.DESTINATION_MSISDN));
        th.setCallingPartyLocation((String) entry.get(Constants.CALLING_PARTY_LOCATION));
        th.setDuration((Time) entry.get(Constants.DURATION));
        th.setFlatRate(Long.parseLong((String) entry.get(Constants.FLAT_RATE)));
        th.setVariableRate(Long.parseLong((String) entry.get(Constants.VARIABLE_RATE)));
        th.setVariableRateUnit((RateUnitEnum) entry.get(Constants.VARIABLE_RATE_UNIT));
        th.setCharge(Long.parseLong((String) entry.get(Constants.CHARGE)));
        th.setUsedMinutes(Integer.parseInt((String) entry.get(Constants.USED_MONTHLY_BUCKET_MINUTES)));
        th.setSpid(Integer.parseInt((String) entry.get(Constants.SPID)));
        th.setRatePlan((String) entry.get(Constants.RATE_PLAN));
        th.setRatingRule((String) entry.get(Constants.RATING_RULE));
        th.setCallID((String) entry.get(Constants.MSC_CALL_REFERENCE_ID));
        th.setDisconnectReason(Integer.parseInt((String) entry.get(Constants.DISCONNECT_REASON)));
        th.setBillingCategory((Short) entry.get(Constants.BILLING_CATEGORY));
        th.setComments((String) entry.get(Constants.PLMN_CODE));
        th.setBAN((String) entry.get(Constants.BAN));
        th.setGLCode((String) entry.get(Constants.GLCODE));
        th.setTransactionSourceId((String) entry.get(Constants.ROAMING_FILENAME));

        home.create(getContext(), th);
    }


    /**
     * Gets the generalConfig attribute of the ApplyRoamingCharges object
     * 
     *@return The generalConfig value
     */
    private GeneralConfig getGeneralConfig()
    {
        GeneralConfig config = (GeneralConfig) getContext().get(GeneralConfig.class);
        if (config == null)
        {
            new InfoLogMsg(this, "System Error: GeneralConfig does not exist in context, using default values.",
                    null).log(getContext());
            config = new GeneralConfig();
        }
        return config;
    }


    /**
     * Gets the roamingDir attribute of the ApplyRoamingCharges object
     * 
     *@return The roamingDir value
     */
    private String getRoamingDir()
    {
        return getGeneralConfig().getRoamingDir();
    }


    /**
     * Gets the roamingArchiveDir attribute of the ApplyRoamingCharges object
     * 
     *@return The roamingArchiveDir value
     */
    private String getRoamingArchiveDir()
    {
        return getGeneralConfig().getRoamingArchiveDir();
    }


    /**
     * Gets the roamingErrDir attribute of the ApplyRoamingCharges object
     * 
     *@return The roamingErrDir value
     */
    private String getRoamingErrDir()
    {
        return getGeneralConfig().getRoamingErrDir();
    }

    private final RoamingFileFilter roamingFileFilter;

    private Context ctx_;

    private static volatile int uniqueInt = 0;
}
