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
package com.trilogy.app.crm.api.rmi;

import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_2.types.invoice.InvoiceDetailReference;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
//import com.trilogy.util.crmapi.wsdl.v2_2.types.invoice.SubAccountInvoiceReference;

/**
 * Adapts Invoice object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class InvoiceToSubAccountInvoiceApiAdapter implements Adapter
{
	private enum paramName
	{
		invoiceDate,totalAmount;
	}
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptInvoiceToReference((Invoice) obj);
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static InvoiceDetailReference adaptInvoiceToReference(final Invoice invoice)
    {
//        final SubAccountInvoiceReference reference = new SubAccountInvoiceReference();
    	InvoiceDetailReference invoiceDetailRef = new InvoiceDetailReference();
        adaptInvoiceToReference(invoice, invoiceDetailRef);

        return invoiceDetailRef;
    }

    public static InvoiceDetailReference adaptInvoiceToReference(final Invoice invoice, final InvoiceDetailReference reference)
    {
        
//        InvoiceDetailReference invoiceDetailRef = new InvoiceDetailReference();
        reference.setInvoiceID(invoice.getInvoiceId());
//        reference.setParameters(param);
        GenericParameter[] param = new GenericParameter[paramName.values().length];
        
        for (int i=0; i<param.length; i++)
        {
            param[i] = new GenericParameter();
            switch(i)
            {
            case 0:
            	param[i].setName(paramName.invoiceDate.name());
            	param[i].setValue(invoice.getInvoiceDate());
            	break;
            case 1:
            	param[i].setName(paramName.totalAmount.name());
            	param[i].setValue(invoice.getTotalAmount());
            	break;
            
            }
        }
        
        reference.setParameters(param);
        return reference;
    }
}
