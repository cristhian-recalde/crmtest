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

import java.math.BigDecimal;

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

import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.bean.TaxAuthorityHome;
import com.trilogy.app.crm.client.dcrm.DcrmSupport;
import com.trilogy.app.crm.client.dcrm.DcrmSync;

import com.trilogy.dynamics.crm.crmservice._2006.query.ArrayOfAnyType;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionExpression;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionOperator;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.BusinessEntity;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.CrmDecimal;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.CrmNumber;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Key;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Lookup;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Rkn_taxauthority;
import com.trilogy.dynamics.crm.crmservice.types.Guid;


/**
 * Provides a method to update DCRM with Tax Authority information.
 *
 * @author gary.anderson@redknee.com
 */
public class DcrmTaxAuthoritySync
    implements HomeChangeListener, DcrmSync
{
    /**
     * The DCRM name of the entity.
     */
    private static final String ENTITY_NAME = "rkn_taxauthority";


    /**
     * Useful for identifying this DcrmSync.
     */
    public static final String KEY = "DcrmTaxAuthoritySync";


    /**
     * The pipeline key used to get the pipeline from the operating context.
     */
    private static final Class<TaxAuthorityHome> PIPELINE_KEY = TaxAuthorityHome.class;


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
                updateTaxAuthority(context, (TaxAuthority)obj);
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
            guids[index] = ((Rkn_taxauthority)businessEntities[index]).getRkn_taxauthorityid();
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

        final TaxAuthority taxAuthority;

        if (evt.getSource() instanceof TaxAuthority)
        {
            taxAuthority = (TaxAuthority)evt.getSource();
        }
        else if (evt.getSource() instanceof NotifyingHomeItem)
        {
            final NotifyingHomeItem source = (NotifyingHomeItem)evt.getSource();
            taxAuthority = (TaxAuthority)source.getNewObject();
        }
        else
        {
            new MinorLogMsg(this, "Unexpected HomeChangeEvent source: " + evt.getSource(), null).log(context);
            return;
        }

        if (evt.getOperation() == HomeOperationEnum.STORE)
        {
            updateTaxAuthority(context, taxAuthority);
        }
        else if (evt.getOperation() == HomeOperationEnum.CREATE)
        {
            createTaxAuthority(context, taxAuthority);
        }
        else if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Unsupported Home Operation " + evt.getOperation() + " for " + taxAuthority, null).log(context);
        }
    }


    /**
     * Gets the DCRM GUID of the given bean.
     *
     * @param context The operating context.
     * @param taxAuthority The bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final TaxAuthority taxAuthority)
    {
        final int taxAuthorityID = taxAuthority.getTaxId();
        return getDcrmGuid(context, taxAuthorityID);
    }


    /**
     * Gets the DCRM GUID for the bean of the given ID.
     *
     * @param context The operating context.
     * @param taxAuthorityID The ID of the bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final int taxAuthorityID)
    {
        final ConditionExpression condition = new ConditionExpression();
        condition.setAttributeName("rkn_identifier");
        condition.setOperator(ConditionOperator.Equal);
        condition.setValues(new ArrayOfAnyType());
        condition.getValues().addValue(taxAuthorityID);

        final ConditionExpression[] conditions = new ConditionExpression[]
        {
            condition,
        };

        final Guid primaryGuid = DcrmSyncSupport.getGuid(context, "rkn_taxauthorityid", conditions, this);

        return primaryGuid;
    }


    /**
     * Creates a new bean in DCRM.
     *
     * @param context The operating context.
     * @param taxAuthority The bean to create in DCRM.
     */
    private void createTaxAuthority(final Context context, final TaxAuthority taxAuthority)
    {
        final Rkn_taxauthority dcrmTaxAuthority = convert(context, taxAuthority);

        final Guid response = DcrmSyncSupport.create(context, dcrmTaxAuthority);

        if (response != null && LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Tax Authority Created: " + response.getGuid(), null).log(context);
        }
        else
        {
            new MajorLogMsg(this, "Failed to create Tax Authority " + taxAuthority.getTaxId(), null).log(context);
        }
    }


    /**
     * Updates the bean in DCRM, creating it if necessary.
     *
     * @param context The operating context.
     * @param taxAuthority The bean to update in DCRM.
     */
    private void updateTaxAuthority(final Context context, final TaxAuthority taxAuthority)
    {
        final Rkn_taxauthority dcrmTaxAuthority = convert(context, taxAuthority);
        final Guid dcrmGuid = getDcrmGuid(context, taxAuthority);

        if (dcrmGuid != null)
        {
            final Key key = new Key();
            key.setGuid(dcrmGuid.getGuid());
            dcrmTaxAuthority.setRkn_taxauthorityid(key);

            DcrmSyncSupport.update(context, dcrmTaxAuthority);
        }
        else
        {
            new MajorLogMsg(this, "Failed to look-up corresponding Tax Authority: " + taxAuthority.getTaxId(), null).log(context);
            createTaxAuthority(context, taxAuthority);
        }
    }


    /**
     * Converts the bean to an instance of a DCRM entity.
     *
     * @param context The operating context.
     * @param taxAuthority The bean to convert.
     * @return An instance of a DCRM entity.
     */
    private Rkn_taxauthority convert(final Context context, final TaxAuthority taxAuthority)
    {
        final Rkn_taxauthority dcrmTaxAuthority = new Rkn_taxauthority();

        // Set the primary key.
        {
            final CrmNumber identifier = new CrmNumber();
            identifier.set_int(taxAuthority.getTaxId());
            dcrmTaxAuthority.setRkn_identifier(Integer.toString(taxAuthority.getTaxId()));
        }

        // Set the name.
        {
            dcrmTaxAuthority.setRkn_name(taxAuthority.getTaxAuthName());
        }

        // Set the tax rate.
        {
            final CrmDecimal rate = new CrmDecimal();
            rate.setDecimal(BigDecimal.valueOf(taxAuthority.getTaxRate()));
            dcrmTaxAuthority.setRkn_taxrate(rate);
        }

        // Set the invoice short code.
        {
            dcrmTaxAuthority.setRkn_invoiceshortcode(taxAuthority.getInvoiceShortCode());
        }

        // Set the invoice sub-total description.
        {
            dcrmTaxAuthority.setRkn_invoicesubtotaldescription(taxAuthority.getInvoiceDescription());
        }

        // Set the SPID.
        {
            final Lookup spid = DcrmServiceProviderSync.getDcrmLookup(context, taxAuthority.getSpid());
            dcrmTaxAuthority.setRkn_serviceproviderid(spid);
        }

        return dcrmTaxAuthority;
    }
}
