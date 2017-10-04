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
package com.trilogy.app.crm.extension.auxiliaryservice.core.custom;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.app.crm.client.urcs.PromotionProvisionClient;
import com.trilogy.app.crm.client.urcs.UrcsClientInstall;
import com.trilogy.app.crm.extension.AssociableExtension;
import com.trilogy.app.crm.extension.ExtensionAssociationException;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * 
 * @author Marcio Marques
 * @since 9.1.2
 *
 */
public class URCSPromotionAuxSvcExtension extends
        com.redknee.app.crm.extension.auxiliaryservice.core.URCSPromotionAuxSvcExtension implements AssociableExtension<SubscriberAuxiliaryService>
{

    @Override
    public void associate(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        provisionPromotion(ctx, subAuxSvc, true);

    }

    @Override
    public void updateAssociation(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
    }

    @Override
    public void dissociate(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        provisionPromotion(ctx, subAuxSvc, false);
    }
    
	public void provisionPromotion(final Context ctx,
			final SubscriberAuxiliaryService subAuxSvc,
			final boolean isProvision) throws ExtensionAssociationException 
	{
		final String subscriberId = subAuxSvc.getSubscriberIdentifier();
		final Subscriber subscriber;
		try 
		{
			subscriber = SubscriberSupport.getSubscriberForAuxiliaryService(
					ctx, subAuxSvc);
		} 
		catch (HomeException e) 
		{
			subAuxSvc.setProvisionActionState(false);
			throw new ExtensionAssociationException(
					ExternalAppEnum.BSS,
					"Unable to retrieve subscription "
							+ subAuxSvc.getSubscriberIdentifier() + ": "
							+ e.getMessage(),
					ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL,
					e, false);
		}

		try 
		{
			provisionUrcsPromotion(ctx, subscriber, getServiceOption(), isProvision);
			subAuxSvc.setProvisionActionState(true);
		} 
		catch (ExtensionAssociationException ex) 
		{
			subAuxSvc.setProvisionActionState(false);
			throw ex;
		}
	}

	public static void provisionUrcsPromotion(final Context ctx,
			final Subscriber sub, long serviceOption, boolean isProvision)
			throws ExtensionAssociationException 
	{

		final PromotionProvisionClient client = UrcsClientInstall.getClient(
				ctx, UrcsClientInstall.PROMOTION_PROVISION_CLIENT_KEY);
		if (client == null) 
		{
			throw new ExtensionAssociationException(ExternalAppEnum.URCS,
					"PromotionProvisionClient is not available.",
					ExternalAppSupport.NO_CONNECTION);
		}
		final long[] valueArray = new long[] { serviceOption };
		final long[] toAdd;
		final long[] toRemove;

		if (isProvision) 
		{
			toAdd = valueArray;
			toRemove = EMPTY_ARRAY;
		} 
		else 
		{
			toAdd = EMPTY_ARRAY;
			toRemove = valueArray;
		}

		try 
		{
			client.setSubscriptionPromotions(ctx, sub, toAdd, toRemove);
		} 
		catch (RemoteServiceException e) 
		{
			throw new ExtensionAssociationException(ExternalAppEnum.URCS,
					"Unable to provision URCS Promotion for  serviceOption "
							+ serviceOption + " to subscriber "
							+ sub.getId() + ": " + e.getMessage(),
					ExternalAppSupport.REMOTE_EXCEPTION, e, false);
		}
	}

   private static final long[] EMPTY_ARRAY = new long[0];

}
