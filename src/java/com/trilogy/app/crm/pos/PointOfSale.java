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
package com.trilogy.app.crm.pos;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;

/**
 * Point of Sale related contants and static methods.
 * @author ali
 */
public class PointOfSale 
{

    /** License Manager key for enabling Point of Sale. **/ 
    public final static String POINT_OF_SALE_LICENSE_KEY = "Point of Sale";
    
    /**    Default file name of the POS External Agents extract file **/
    protected final static String POS_EXTERNAL_AGENTS_FILE      = "ExternalAgents.DAT";
    /**    Default file name of the POS Cashier extract file **/
    protected final static String POS_CASHIER_FILE              = "Cashier.DAT";
    /**    Default file name of the POS Conciliation extract file **/
    protected final static String POS_CONCILIATION_FILE         = "Conciliation.DAT";
    /**    Default file name of the POS Payment Exception extract file **/
    protected final static String POS_PAYMENT_EXCEPTION_FILE    = "POSException.DAT";
    /**    Default file name of the IVR extract file **/
    protected final static String POS_IVR_FILE                  = "ivrtemp2.DAT";
    
    /**  Point of Sale Account Accumulator Update: How many accounts were visited */
    public static final String OM_POS_ACCOUNTACCUMULATOR_VISIT          = "POS_AccountAccumulator_Visit";
    /**  Point of Sale Account Accumulator Update: How many accounts were visited */
    public static final String OM_POS_MSISDNACCUMULATOR_VISIT          = "POS_SubscriberAccumulator_Visit";
    /**  Point of Sale Account Accumulator Update: How many Account Accumulator records were updated */
    public static final String OM_POS_ACCOUNTACCUMULATOR_UPDATE_ATTEMPT = "POS_AccountAccumulator_Update_Attempt";
    /**  Point of Sale Account Accumulator Update: as above with failure*/
    public static final String OM_POS_ACCOUNTACCUMULATOR_UPDATE_FAILURE = "POS_AccountAccumulator_Update_Failure";
    /**  Point of Sale Account Accumulator Update: as above with failure*/
    public static final String OM_POS_MSISDNCCUMULATOR_UPDATE_FAILURE = "POS_SubscriberAccumulator_Update_Failure";
    /**  Point of Sale Account Accumulator Update: as above with success*/
    public static final String OM_POS_ACCOUNTACCUMULATOR_UPDATE_SUCCESS = "POS_AccountAccumulator_Update_Success";
    /**  Point of Sale External Agents Record Update: Attempt to store External Agents record */
    public static final String OM_POS_EXTERNALAGENTS_RECORD_ATTEMPT = "POS_ExternalAgents_Record_Attempt";
    /**  Point of Sale External Agents Record Update: as above with failure */
    public static final String OM_POS_EXTERNALAGENTS_RECORD_FAILURE = "POS_ExternalAgents_Record_Failure";
    /**  Point of Sale External Agents Record Update: as above with success*/
    public static final String OM_POS_EXTERNALAGENTS_RECORD_SUCCESS = "POS_ExternalAgents_Record_Success";
    /**  Point of Sale Cashier Record Update: Attempt to store Cashier record */
    public static final String OM_POS_CASHIER_RECORD_ATTEMPT = "POS_Cashier_Record_Attempt";
    /**  Point of Sale Cashier Record Update: as above with failure */
    public static final String OM_POS_CASHIER_RECORD_FAILURE = "POS_Cashier_Record_Failure";
    /**  Point of Sale Cashier Record Update: as above with success */
    public static final String OM_POS_CASHIER_RECORD_SUCCESS = "POS_Cashier_Record_Success";
    /**  Point of Sale Conciliation Record Update: Attempt to store Conciliation record */
    public static final String OM_POS_CONCILIATION_RECORD_ATTEMPT = "POS_Conciliation_Record_Attempt";
    /**  Point of Sale Conciliation Record Update: as above with failure */
    public static final String OM_POS_CONCILIATION_RECORD_FAILURE = "POS_Conciliation_Record_Failure";
    /**  Point of Sale Conciliation Record Update: as above with success */
    public static final String OM_POS_CONCILIATION_RECORD_SUCCESS = "POS_Conciliation_Record_Success";
    /**  Point of Sale Payment Exception Record Update: Attempt to store Payment Exception record */
    public static final String OM_POS_PAYMENTEXCEPTION_RECORD_ATTEMPT = "POS_PaymentException_Record_Attempt";
    /**  Point of Sale Payment Exception Record Update: as above with failure */
    public static final String OM_POS_PAYMENTEXCEPTION_RECORD_FAILURE = "POS_PaymentException_Record_Failure";
    /**  Point of Sale Payment Exception Record Update: as above with success */
    public static final String OM_POS_PAYMENTEXCEPTION_RECORD_SUCCESS = "POS_PaymentException_Record_Success";
    /**  Point of Sale IVR Record Update: Attempt to store IVR record */
    public static final String OM_POS_IVR_RECORD_ATTEMPT = "POS_IVR_Record_Attempt";
    /**  Point of Sale IVR Record Update: as above with failure */
    public static final String OM_POS_IVR_RECORD_FAILURE = "POS_IVR_Record_Failure";
    /**  Point of Sale IVR Record Update: as above with success */
    public static final String OM_POS_IVR_RECORD_SUCCESS = "POS_IVR_Record_Success";
    
    /** Buffer of Blanks used for padding fixed length fields: 100 blanks */
    public static final char[] blankBuff = 
        "                                                                                                              ".toCharArray();
    
    /** @return true iff Point of Sale is enabled (with the License Manager). **/
    public static boolean isEnabled(Context ctx)
    {
        LicenseMgr lMgr = (LicenseMgr) ctx.get(LicenseMgr.class);
        
        return lMgr.isLicensed(ctx, POINT_OF_SALE_LICENSE_KEY);
    }
    
    /**
     * Returns the given StringBuilder value with length set to given maxWidth.  
     * If given StringBuilder's length is shorter than maxWidth, then pad with blanks.
     * Else truncate StringBuilder at maxWidth. 
     * @param value
     * @param maxWidth
     * @return
     */
    public static String padFieldWithBlanks(String value, int maxWidth)
    {
        /** Buffer used to manipulate string padding */
        StringBuilder paddedBuff = new StringBuilder(100);
        paddedBuff.setLength(0);
        if (value==null)
        {
            return paddedBuff.append(blankBuff, 0, maxWidth).toString(); 
        }
        else if (value.length() < maxWidth)
        {
            return paddedBuff.append(value).append(blankBuff, 0, maxWidth - value.length()).toString(); 
        }
        else if (value.length() > maxWidth)
        {
            return value.substring(0, maxWidth);
        }
        return value;
    }
    
    public static String padSpidFieldWithBlanks(String value, int maxWidth)
    {
        Context ctx = ContextLocator.locate();
        if (ctx!=null && LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.STUPID_STORK_HACK_POS_FILE_ONE_CHARACTER))
        {
            return padFieldWithBlanks(value, 1);
        }
        else
        {
            return padFieldWithBlanks(value, maxWidth);
        }
    }

    /**
     * Truncates the given string to length if exceeds length.
     * @param value
     * @param maxWidth
     * @param fromLeft true then the trimming happens from the left, false the trimming is on the right of the string.
     * @return
     */
    public static String trimLength(String value, int maxWidth, boolean fromLeft)
    {
        if (value.length() > maxWidth)
        {
            if (fromLeft)
            {
                value = value.substring(value.length() - maxWidth);
            }
            else
            {
                value = value.substring(0, maxWidth);
            }
        }
        return value;
    }
    
    /**
     * The date format used for specifying the "current date" in parameter 1.
     * This format is currently consistent with other CronAgents.
     */
    protected static final String DATE_FORMAT_STRING = "yyyyMMdd";

    public static final String NON_RESPONSIBLE_ACCOUNT_PROCESSING = "NON_RESPONSIBLE_ACCOUNT_PROCESSING";
}
