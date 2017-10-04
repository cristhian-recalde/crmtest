/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.home;

import java.text.MessageFormat;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.report.ReportUtilities;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * This class validates some input parameters from Adjustment Bulk Load.
 *
 * @author jimmy.ng@redknee.com
 */
public class AdjustmentValidator
    extends ContextAwareSupport
    implements Validator
{
    /**
     * Creates a new AdjustmentValidator.
     *
     * @param ctx The operating context.
     */
    public AdjustmentValidator(final Context ctx)
    {
        setContext(ctx);
    }

    
    /**
     * INHERIT
     */
    public void validate(Context ctx,Object obj)
        throws IllegalStateException
    {
        final Adjustment adjustment = (Adjustment) obj;
        
        final CompoundIllegalStateException exception = 
        	new CompoundIllegalStateException();

        // If both Account Number and MSISDN are provided, the MSISDN must be
        // for a subscriber in the account provided.
        // If either Account Number or MSISDN is provided, verify if it is valid.
        // If netiher Account Number nor MSISDN is provided, report error.
        final String acctNum = adjustment.getAcctNum().trim();
        final String msisdn = adjustment.getMSISDN().trim();
        if (acctNum.length() == 0 && msisdn.length() == 0)
        {
            // Commenting the code below because this is may not true for de-activated subscribers
            //TT#11102701001

            exception.thrown(new IllegalArgumentException(
                "MSISDN must be provided if no account number is provided"));
        }
        else if (acctNum.length() != 0 && msisdn.length() == 0)
        {
            final Account acct
                = (Account) ReportUtilities.findByPrimaryKey(
                    getContext(),
                    AccountHome.class,
                    acctNum);
            if (acct == null)
            {
                final String formattedMsg = MessageFormat.format(
                    "Account \"{0}\" could not be found",
                    acctNum);
                exception.thrown(new IllegalArgumentException(formattedMsg));
            }
        }
        else if (msisdn.length() != 0  && acctNum.length() == 0)
        {
            try
            {
                Subscriber sub=SubscriberSupport.lookupSubscriberForMSISDN(getContext(),msisdn);
                
                if (sub == null)
                {
                    throw new HomeException("");
                } 
                adjustment.setBAN(sub.getBAN()); 
                
            }
            catch (HomeException e)
            {
                final String formattedMsg = MessageFormat.format(
                    "Subscriber could not be found for MSISDN \"{0}\"",
                    msisdn);
                exception.thrown(new IllegalArgumentException(formattedMsg));
            }
        }
        
        // Only those adjustment types, that are allowed when creating a
        // transaction using the payment/adjustment screen, are allowed here.
        final int adjTypeIndex = adjustment.getAdjustmentType();
        try
        {
            final AdjustmentType adjType
                = AdjustmentTypeSupportHelper.get(getContext()).getAdjustmentType(getContext(), adjTypeIndex);
            
            if (adjType == null )
            {
                throw new HomeException("Invalide adjustment type");
            }
        }
        catch (HomeException e)
        {
            final String formattedMsg = MessageFormat.format(
                "Adjustment Type \"{0}\" is not found",
                String.valueOf(adjTypeIndex));
            exception.thrown(new IllegalArgumentException(formattedMsg));
        }
        
        // If no payment agency is provided, the string "default" is used.
        final String paymentAgency = adjustment.getPaymentAgency().trim();
        if (paymentAgency.length() == 0)
        {
            adjustment.setPaymentAgency("default");
        }
        
        // Location Code must be provided.
        final String locationCode = adjustment.getLocationCode().trim();
        if (locationCode.length() == 0)
        {
            exception.thrown(new IllegalArgumentException("\"Location Code\" is mandatory"));
        }
        
        // Payment Details must be provided.
        final String paymentDetails = adjustment.getPaymentDetails().trim();
        if (paymentDetails.length() == 0)
        {
            exception.thrown(new IllegalArgumentException("\"Payment Details\" is mandatory"));
        }
        
        // The value provided in the Transaction Method value must match the
        // ID of a Tranaction Method defined in the system.
        final Long transMethodId = Long.valueOf(adjustment.getTransactionMethod());
        final TransactionMethod transMethod
            = (TransactionMethod) ReportUtilities.findByPrimaryKey(
                    getContext(),
                    TransactionMethodHome.class,
                    transMethodId);
        if (transMethod == null)
        {
        	final String formattedMsg;
        	if (transMethodId.intValue()==-1)
        	{
        		formattedMsg ="Transaction Method is mendatory";
            }
        	else
        	{
        		formattedMsg = MessageFormat.format(
                "Transaction Method \"{0}\" could not be found",
                transMethodId.toString());
        	}
            exception.thrown(new IllegalArgumentException(formattedMsg));
        }
        
		exception.throwAll();
    }
}
