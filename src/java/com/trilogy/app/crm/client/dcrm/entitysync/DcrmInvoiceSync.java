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
package com.trilogy.app.crm.client.dcrm.entitysync;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeChangeEvent;
import com.trilogy.framework.xhome.home.HomeChangeListener;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xhome.home.NotifyingHomeItem;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.InvoiceHome;
import com.trilogy.app.crm.client.dcrm.DcrmSupport;
import com.trilogy.app.crm.client.dcrm.DcrmSync;

import com.trilogy.dynamics.crm.crmservice._2006.query.ArrayOfAnyType;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionExpression;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionOperator;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.BusinessEntity;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.CrmDateTime;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.CrmMoney;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Key;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Rkn_accountinvoice;
import com.trilogy.dynamics.crm.crmservice.types.Guid;


/**
 * Provides a method to update DCRM with Invoice information.
 *
 * @author gary.anderson@redknee.com
 */
public class DcrmInvoiceSync
    implements HomeChangeListener, DcrmSync
{
    /**
     * The DCRM name of the entity.
     */
    private static final String ENTITY_NAME = "rkn_accountinvoice";


    /**
     * Useful for identifying this DcrmSync.
     */
    public static final String KEY = "DcrmInvoiceSync";


    /**
     * The pipeline key used to get the pipeline from the operating context.
     */
    private static final Class<InvoiceHome> PIPELINE_KEY = InvoiceHome.class;


    /**
     * {@inheritDoc}
     */
    public void install(final Context context)
    {
        DcrmSyncSupport.addHomeChangeListner(context, PIPELINE_KEY, this);
    }


    /**
     * {@inheritDoc}
     */
    public void uninstall(final Context context)
    {
        DcrmSyncSupport.removeHomeChangeListner(context, PIPELINE_KEY, this);
    }


    /**
     * {@inheritDoc}
     */
    public void updateAll(final Context context)
    {
        final Visitor updateVisitor = new Visitor()
        {
            private static final long serialVersionUID = 1L;


            public void visit(final Context ctx, final Object obj)
                throws AbortVisitException
            {
                updateInvoice(context, (Invoice)obj);
            }
        };

        final Home home = (Home)context.get(PIPELINE_KEY);

        try
        {
            home.forEach(context, updateVisitor);
        }
        catch (final HomeException exception)
        {
            new MajorLogMsg(this, "Failure during update of all beans.", exception).log(context);
        }
    }


    /**
     * {@inheritDoc}
     */
    public String getEntityName()
    {
        return ENTITY_NAME;
    }


    /**
     * {@inheritDoc}
     */
    public Guid[] getDcrmGuids(final Context context, final BusinessEntity[] businessEntities)
    {
        final Guid[] guids = new Guid[businessEntities.length];

        for (int index = 0; index < businessEntities.length; ++index)
        {
            guids[index] = ((Rkn_accountinvoice)businessEntities[index]).getRkn_accountinvoiceid();
        }

        return guids;
    }


    /**
     * {@inheritDoc}
     */
    public void homeChange(final HomeChangeEvent evt)
    {
        final Context context = evt.getContext();
        if (!DcrmSupport.isEnabled(context))
        {
            return;
        }

        final Invoice invoice;

        if (evt.getSource() instanceof Invoice)
        {
            invoice = (Invoice)evt.getSource();
        }
        else if (evt.getSource() instanceof NotifyingHomeItem)
        {
            final NotifyingHomeItem source = (NotifyingHomeItem)evt.getSource();
            invoice = (Invoice)source.getNewObject();
        }
        else
        {
            new MinorLogMsg(this, "Unexpected HomeChangeEvent source: " + evt.getSource(), null).log(context);
            return;
        }

        if (evt.getOperation() == HomeOperationEnum.STORE)
        {
            updateInvoice(context, invoice);
        }
        else if (evt.getOperation() == HomeOperationEnum.CREATE)
        {
            createInvoice(context, invoice);
        }
        else if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Unsupported Home Operation " + evt.getOperation() + " for " + invoice, null).log(context);
        }
    }


    /**
     * Gets the DCRM GUID of the given bean.
     *
     * @param context The operating context.
     * @param invoice The bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final Invoice invoice)
    {
        final String invoiceID = invoice.getInvoiceId();
        return getDcrmGuid(context, invoiceID);
    }


    /**
     * Gets the DCRM GUID for the bean of the given ID.
     *
     * @param context The operating context.
     * @param invoiceID The ID of the bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final String invoiceID)
    {
        final ConditionExpression condition = new ConditionExpression();
        condition.setAttributeName("rkn_invoiceidentifier");
        condition.setOperator(ConditionOperator.Equal);
        condition.setValues(new ArrayOfAnyType());
        condition.getValues().addValue(invoiceID);

        final ConditionExpression[] conditions = new ConditionExpression[]
        {
            condition,
        };

        final Guid primaryGuid = DcrmSyncSupport.getGuid(context, "rkn_accountinvoiceid", conditions, this);

        return primaryGuid;
    }


    /**
     * Creates a new bean in DCRM.
     *
     * @param context The operating context.
     * @param invoice The bean to create in DCRM.
     */
    private void createInvoice(final Context context, final Invoice invoice)
    {
        final Rkn_accountinvoice dcrmInvoice = convert(context, invoice);

        final Guid response = DcrmSyncSupport.create(context, dcrmInvoice);

        if (response != null && LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Invoice Created: " + response.getGuid(), null).log(context);
        }
        else
        {
            new MajorLogMsg(this, "Failed to create Invoice " + invoice.getInvoiceId(), null).log(context);
        }
    }


    /**
     * Updates the bean in DCRM, creating it if necessary.
     *
     * @param context The operating context.
     * @param invoice The bean to update in DCRM.
     */
    private void updateInvoice(final Context context, final Invoice invoice)
    {
        final Rkn_accountinvoice dcrmInvoice = convert(context, invoice);
        final Guid dcrmGuid = getDcrmGuid(context, invoice);

        if (dcrmGuid != null)
        {
            final Key key = new Key();
            key.setGuid(dcrmGuid.getGuid());
            dcrmInvoice.setRkn_accountinvoiceid(key);

            DcrmSyncSupport.update(context, dcrmInvoice);
        }
        else
        {
            new MajorLogMsg(this, "Failed to look-up corresponding Invoice: " + invoice.getInvoiceId(), null).log(context);
            createInvoice(context, invoice);
        }
    }


    /**
     * Converts the bean to an instance of a DCRM entity.
     *
     * @param context The operating context.
     * @param invoice The bean to convert.
     * @return An instance of a DCRM entity.
     */
    private Rkn_accountinvoice convert(final Context context, final Invoice invoice)
    {
        final Rkn_accountinvoice dcrmInvoice = new Rkn_accountinvoice();

        // Set the primary key.
        {
            dcrmInvoice.setRkn_invoiceidentifier(invoice.getInvoiceId());
        }

        // Set the account reference.
        {
            //TODO
        }

        // Set the current amount.
        {
            CrmMoney param = DcrmSyncSupport.adaptToCrmMoney(context, invoice.getCurrentAmount());
            dcrmInvoice.setRkn_currentamount(param);
        }

        // Set the current tax amount.
        {
            CrmMoney param = DcrmSyncSupport.adaptToCrmMoney(context, invoice.getCurrentTaxAmount());
            dcrmInvoice.setRkn_currenttaxamount(param );
        }

        // Set the discount amount.
        {
            CrmMoney param = DcrmSyncSupport.adaptToCrmMoney(context, invoice.getDiscountAmount());
            dcrmInvoice.setRkn_discountamount(param);
        }

        // Set the due date.
        {
            CrmDateTime param = DcrmSyncSupport.adaptToCrmDateTime(context, invoice.getDueDate());
            dcrmInvoice.setRkn_duedate(param);
        }

        // Set the generated date.
        {
            CrmDateTime param = DcrmSyncSupport.adaptToCrmDateTime(context, invoice.getGeneratedDate());
            dcrmInvoice.setRkn_generateddate(param);
        }

        // Set the invoice date.
        {
            CrmDateTime param = DcrmSyncSupport.adaptToCrmDateTime(context, invoice.getInvoiceDate());
            dcrmInvoice.setRkn_invoicedate(param);
        }

        // Set the payment amount.
        {
            CrmMoney param = DcrmSyncSupport.adaptToCrmMoney(context, invoice.getPaymentAmount());
            dcrmInvoice.setRkn_paymentamount(param);
        }

        // Set the previous balance amount.
        {
            CrmMoney param = DcrmSyncSupport.adaptToCrmMoney(context, invoice.getPreviousBalance());
            dcrmInvoice.setRkn_previousbalanceamount(param);
        }

        // Set the total amount.
        {
            CrmMoney param = DcrmSyncSupport.adaptToCrmMoney(context, invoice.getTotalAmount());
            dcrmInvoice.setRkn_totalamount(param);
        }


        return dcrmInvoice;
    }
}
