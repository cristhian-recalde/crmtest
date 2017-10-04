/*
 * Created on April 11, 2006
 * 
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFCDRED
 * BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR
 * ITS DERIVATIVES.
 */

package com.trilogy.app.crm.provision.corba.api.ecareservices;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.app.crm.pos.AccountAccumulator;
import com.trilogy.app.crm.provision.corba.api.ecareservices.billingmgmt.BillingParamID;
import com.trilogy.app.crm.provision.corba.api.ecareservices.billingmgmt.BillingParameter;
import com.trilogy.app.crm.provision.corba.api.ecareservices.error.ServiceError;
import com.trilogy.app.crm.provision.corba.api.ecareservices.exception.InvoiceNotFoundException;

/**
 * To adapt the account and invoice to Array of Billing Parameters.
 * 
 * @author kso
 *
 */

public class BillingAdapter extends ParamAdapter
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
    public BillingParameter[] adapt(final Context ctx, 
    		final Account acct, 
    		final Invoice invoice, 
    		AccountAccumulator accumulator, 
    		final BillingParamID[] reqSet) throws InvoiceNotFoundException
    {
        List result = new ArrayList();
        for (int i = 0; i < reqSet.length; i++)
        {
            if ( BillingParamID.INVOICE_AMOUNT.equals(reqSet[i]))
            {
                if ( invoice == null )
                {                   
                    throw new InvoiceNotFoundException();
                }
                result.add( new BillingParameter(BillingParamID.INVOICE_AMOUNT, adaptParamValue(invoice.getTotalAmount())));
            }
            else if ( invoice != null && BillingParamID.INVOICE_DATE.equals(reqSet[i]))
            {
                if ( invoice == null )
                {                   
                    throw new InvoiceNotFoundException();
                }
                result.add( new BillingParameter(BillingParamID.INVOICE_DATE, adaptParamValue(invoice.getInvoiceDate())));                
            }
            else if ( BillingParamID.CURRENT_BALANCE.equals(reqSet[i]))
            {
                long amountOwing = accumulator.getBalance();
                result.add( new BillingParameter(BillingParamID.CURRENT_BALANCE, adaptParamValue(amountOwing)));                
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
