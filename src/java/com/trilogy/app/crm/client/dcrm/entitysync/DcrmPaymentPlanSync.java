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

import com.trilogy.app.crm.bean.payment.PaymentPlan;
import com.trilogy.app.crm.bean.payment.PaymentPlanHome;
import com.trilogy.app.crm.client.dcrm.DcrmSupport;
import com.trilogy.app.crm.client.dcrm.DcrmSync;

import com.trilogy.dynamics.crm.crmservice._2006.query.ArrayOfAnyType;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionExpression;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionOperator;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.BusinessEntity;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.CrmNumber;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Key;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Lookup;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Rkn_paymentplan;
import com.trilogy.dynamics.crm.crmservice.types.Guid;


/**
 * Provides a method to update DCRM with Payment Plan information. A special
 * concern of this sync is that the local primary key is a "long", but DCRM does
 * not support "long". As a result, this sync casts to "int" and uses
 * Integer.MAX_VALUE for any identifier too large to fit in an "int" and issues
 * MAJOR logs.
 *
 * @author gary.anderson@redknee.com
 */
public class DcrmPaymentPlanSync
    implements HomeChangeListener, DcrmSync
{
    /**
     * The DCRM name of the entity.
     */
    private static final String ENTITY_NAME = "rkn_paymentplan";


    /**
     * Useful for identifying this DcrmSync.
     */
    public static final String KEY = "DcrmPaymentPlanSync";


    /**
     * The pipeline key used to get the pipeline from the operating context.
     */
    private static final Class<PaymentPlanHome> PIPELINE_KEY = PaymentPlanHome.class;


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
                updatePaymentPlan(context, (PaymentPlan)obj);
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
            guids[index] = ((Rkn_paymentplan)businessEntities[index]).getRkn_paymentplanid();
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

        final PaymentPlan paymentPlan;

        if (evt.getSource() instanceof PaymentPlan)
        {
            paymentPlan = (PaymentPlan)evt.getSource();
        }
        else if (evt.getSource() instanceof NotifyingHomeItem)
        {
            final NotifyingHomeItem source = (NotifyingHomeItem)evt.getSource();
            paymentPlan = (PaymentPlan)source.getNewObject();
        }
        else
        {
            new MinorLogMsg(this, "Unexpected HomeChangeEvent source: " + evt.getSource(), null).log(context);
            return;
        }

        if (evt.getOperation() == HomeOperationEnum.STORE)
        {
            updatePaymentPlan(context, paymentPlan);
        }
        else if (evt.getOperation() == HomeOperationEnum.CREATE)
        {
            createPaymentPlan(context, paymentPlan);
        }
        else if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Unsupported Home Operation " + evt.getOperation() + " for " + paymentPlan, null).log(context);
        }
    }


    /**
     * Gets the DCRM GUID of the given bean.
     *
     * @param context The operating context.
     * @param paymentPlan The bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final PaymentPlan paymentPlan)
    {
        final long paymentPlanID = paymentPlan.getId();
        return getDcrmGuid(context, paymentPlanID);
    }


    /**
     * Gets the DCRM GUID for the bean of the given ID.
     *
     * @param context The operating context.
     * @param paymentPlanID The ID of the bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final long paymentPlanID)
    {
        final ConditionExpression condition = new ConditionExpression();
        condition.setAttributeName("rkn_identifier");
        condition.setOperator(ConditionOperator.Equal);
        condition.setValues(new ArrayOfAnyType());
        // Note that DCRM does not support "long". See notes in class comments.
        condition.getValues().addValue((int)paymentPlanID);

        final ConditionExpression[] conditions = new ConditionExpression[]
        {
            condition,
        };

        final Guid primaryGuid = DcrmSyncSupport.getGuid(context, "rkn_paymentplanid", conditions, this);

        return primaryGuid;
    }


    /**
     * Creates a new bean in DCRM.
     *
     * @param context The operating context.
     * @param paymentPlan The bean to create in DCRM.
     */
    private void createPaymentPlan(final Context context, final PaymentPlan paymentPlan)
    {
        final Rkn_paymentplan dcrmPaymentPlan = convert(context, paymentPlan);

        final Guid response = DcrmSyncSupport.create(context, dcrmPaymentPlan);

        if (response != null && LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Payment Plan Created: " + response.getGuid(), null).log(context);
        }
        else
        {
            new MajorLogMsg(this, "Failed to create Payment Plan " + paymentPlan.getId(), null).log(context);
        }
    }


    /**
     * Updates the bean in DCRM, creating it if necessary.
     *
     * @param context The operating context.
     * @param paymentPlan The bean to update in DCRM.
     */
    private void updatePaymentPlan(final Context context, final PaymentPlan paymentPlan)
    {
        final Rkn_paymentplan dcrmPaymentPlan = convert(context, paymentPlan);
        final Guid dcrmGuid = getDcrmGuid(context, paymentPlan);

        if (dcrmGuid != null)
        {
            final Key key = new Key();
            key.setGuid(dcrmGuid.getGuid());
            dcrmPaymentPlan.setRkn_paymentplanid(key);

            DcrmSyncSupport.update(context, dcrmPaymentPlan);
        }
        else
        {
            new MajorLogMsg(this, "Failed to look-up corresponding Payment Plan: " + paymentPlan.getId(), null).log(context);
            createPaymentPlan(context, paymentPlan);
        }
    }


    /**
     * Converts the bean to an instance of a DCRM entity.
     *
     * @param context The operating context.
     * @param paymentPlan The bean to convert.
     * @return An instance of a DCRM entity.
     */
    private Rkn_paymentplan convert(final Context context, final PaymentPlan paymentPlan)
    {
        final Rkn_paymentplan dcrmPaymentPlan = new Rkn_paymentplan();

        // Set the primary key.
        {
            final CrmNumber identifier = new CrmNumber();
            // Note that DCRM does not support "long". See notes in class
            // comments.
            identifier.set_int((int)paymentPlan.getId());
            dcrmPaymentPlan.setRkn_identifier(Long.toString(paymentPlan.getId()));
        }

        // Set the name.
        {
            dcrmPaymentPlan.setRkn_name(paymentPlan.getName());
        }

        // Set the description.
        {
            dcrmPaymentPlan.setRkn_description(paymentPlan.getDesc());
        }

        // Set the credit limit decrease.
        {
            final CrmNumber decrease = new CrmNumber();
            // This is a whole-valued percentage, so can safely cast to int.
            decrease.set_int((int)paymentPlan.getCreditLimitDecrease());
            dcrmPaymentPlan.setRkn_creditlimitlowering(decrease);
        }

        // Set the number of payments.
        {
            final CrmNumber payments = new CrmNumber();
            payments.set_int(paymentPlan.getNumOfPayments());
            dcrmPaymentPlan.setRkn_numberofpayments(payments);
        }

        // Set the SPID.
        {
            final Lookup spid = DcrmServiceProviderSync.getDcrmLookup(context, paymentPlan.getSpid());
            dcrmPaymentPlan.setRkn_serviceproviderid(spid);
        }

        return dcrmPaymentPlan;
    }
}
