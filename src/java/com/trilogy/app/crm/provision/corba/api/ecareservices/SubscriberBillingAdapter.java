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

package com.trilogy.app.crm.provision.corba.api.ecareservices;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.pos.SubscriberAccumulator;
import com.trilogy.app.crm.provision.corba.api.ecareservices.billingmgmt.BillingParamID;
import com.trilogy.app.crm.provision.corba.api.ecareservices.billingmgmt.BillingParameter;
import com.trilogy.app.crm.provision.corba.api.ecareservices.error.ServiceError;
import com.trilogy.app.crm.provision.corba.api.ecareservices.exception.InvoiceNotFoundException;
import com.trilogy.app.crm.provision.corba.api.ecareservices.param.ParameterValue;
import com.trilogy.framework.xhome.context.Context;

/**
 * Adapts the output result parameters   
 * @author amedina
 */

public class SubscriberBillingAdapter extends ParamAdapter 
{

    /**
     * @param ctx
     * @param acct  Account
     * @param invoice   
     * @param reqSet Request Parameters
     * @return Array of Billing Parameters
     * @throws InvoiceNotFoundException 
     * @throws ServiceError
     */
    public BillingParameter[] adapt(final Context ctx, final Msisdn acct, final SubscriberAccumulator accumulator, final BillingParamID[] reqSet) throws InvoiceNotFoundException
    {
        List result = new ArrayList();
        for (int i = 0; i < reqSet.length; i++)
        {
            if ( BillingParamID.INVOICE_AMOUNT.equals(reqSet[i]))
            {
                if ( accumulator == null )
                {                   
                    throw new InvoiceNotFoundException();
                }
                result.add( new BillingParameter(BillingParamID.INVOICE_AMOUNT, adaptParamValue(accumulator.getInvoiceAmount())));
            }
            else if ( accumulator != null && BillingParamID.INVOICE_DATE.equals(reqSet[i]))
            {
                if ( accumulator == null )
                {                   
                    throw new InvoiceNotFoundException();
                }
                result.add( new BillingParameter(BillingParamID.INVOICE_DATE, adaptParamValue(accumulator.getInvoiceDueDate())));                
            }
            else if ( BillingParamID.CURRENT_BALANCE.equals(reqSet[i]))
            {
                long amountOwing = accumulator.getBalance();
                result.add( new BillingParameter(BillingParamID.CURRENT_BALANCE, adaptParamValue(amountOwing)));                
            }
            else if ( BillingParamID.ADJUSTMENT.equals(reqSet[i]))
            {
                long amountOwing = accumulator.getAdjustments();
                result.add( new BillingParameter(BillingParamID.ADJUSTMENT, adaptParamValue(amountOwing)));                
            }
            else if ( BillingParamID.DATE_FORMAT.equals(reqSet[i]))
            {
                result.add( new BillingParameter(BillingParamID.DATE_FORMAT, adaptParamValue(DATE_FORMAT)));
            }
            
        }
        return toArray(result);
    }
    
    
    /**
     * To convert the arraylist to array of AccountParameter
     * @param result
     * @return
     */
    
    public BillingParameter[] toArray(List result)
    {
        BillingParameter[] list = new BillingParameter[result.size()];
        
        for (int i = 0; i < result.size(); i ++ )
        {
            list[i] = (BillingParameter) result.get(i);
        }
        return list;
    }

}
