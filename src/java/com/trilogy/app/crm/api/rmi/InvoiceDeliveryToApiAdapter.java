/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.api.rmi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.trilogy.app.crm.invoice.delivery.InvoiceDeliveryOption;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_2.types.invoice.DeliveryTypeEnum;


/**
 * Adapts Invoice object to API objects.
 * 
 * @author victor.stratan@redknee.com
 */
public class InvoiceDeliveryToApiAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptInvoiceToApi((InvoiceDeliveryOption) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static com.redknee.util.crmapi.wsdl.v2_2.types.invoice.InvoiceDeliveryOption adaptInvoiceToApi(
            final InvoiceDeliveryOption invoice)
    {
        final com.redknee.util.crmapi.wsdl.v2_2.types.invoice.InvoiceDeliveryOption apiInvoiceDelivery;
        apiInvoiceDelivery = new com.redknee.util.crmapi.wsdl.v2_2.types.invoice.InvoiceDeliveryOption();

        Set<com.redknee.app.crm.invoice.delivery.DeliveryTypeEnum> deliveryTypes = invoice.getDeliveryType();

        Collection<com.redknee.util.crmapi.wsdl.v2_2.types.invoice.DeliveryType> apiDeliveryTypes = new ArrayList<com.redknee.util.crmapi.wsdl.v2_2.types.invoice.DeliveryType>();

        Iterator<com.redknee.app.crm.invoice.delivery.DeliveryTypeEnum> iter = deliveryTypes.iterator();
        int i = 0;
        while (iter.hasNext())
        {
            com.redknee.app.crm.invoice.delivery.DeliveryTypeEnum type = iter.next();
            apiDeliveryTypes.add(DeliveryTypeEnum.valueOf(type.getIndex()));
            i++;
        }
        com.redknee.util.crmapi.wsdl.v2_2.types.invoice.DeliveryType[] apiDeliveryTypesArray = 
            new com.redknee.util.crmapi.wsdl.v2_2.types.invoice.DeliveryType[] {};

        apiInvoiceDelivery.setDeliveryType(apiDeliveryTypes.toArray(apiDeliveryTypesArray));
        apiInvoiceDelivery.setIdentifier(invoice.getId());
        apiInvoiceDelivery.setName(invoice.getDisplayName());
        apiInvoiceDelivery.setNonResponsibleDefault(invoice.getNonResponsibleDefault());
        apiInvoiceDelivery.setSubfolderName(invoice.getSubFolderName());
        return apiInvoiceDelivery;
    }
}
