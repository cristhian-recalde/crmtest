/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.calculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Value calculator for Multi Sim provisioning
 * 
 * @author kumaran.sivasubramaniam@redknee.com
 * @since 9.5	
 */
public class MultiSimListValueCalculator extends AbstractMultiSimListValueCalculator 
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Object> getDependentContextKeys(Context ctx) {
		final List keyCollection = new ArrayList();
		keyCollection.add(Subscriber.class);
		keyCollection.add(SubscriberAuxiliaryService.class);
		keyCollection.add(com.redknee.app.crm.bean.core.AuxiliaryService.class);
		return keyCollection;
	}

	private boolean shouldUpdate(final Context ctx) {
		SubscriberAuxiliaryService subAuxService = (SubscriberAuxiliaryService) ctx
				.get(SubscriberAuxiliaryService.class);
		if (subAuxService != null) {
			AuxiliaryService auxService = (com.redknee.app.crm.bean.core.AuxiliaryService) ctx
					.get(com.redknee.app.crm.bean.core.AuxiliaryService.class);

			if (auxService == null
					&& subAuxService.getAuxiliaryServiceIdentifier() == auxService
							.getID() ) {
				try {
					auxService = subAuxService.getAuxiliaryService(ctx);
				} catch (HomeException ex) {
					return false;
				}
			}
			if (auxService.getType() == AuxiliaryServiceTypeEnum.MultiSIM)
				{
				return false;
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValueAdvanced(Context ctx) 
	{
		StringBuffer value = new StringBuffer();
		Subscriber subscriber = null;
		subscriber = (Subscriber) ctx.get(Subscriber.class);
		List<SubscriberAuxiliaryService> auxServices = null;

		if (shouldUpdate(ctx)) {
			if (subscriber != null) {
				auxServices = subscriber.getAuxiliaryServices(ctx);
				if (auxServices != null) {
					for (SubscriberAuxiliaryService auxService : auxServices) {
						if (auxService.isProvisioned()) {
							try {
								AuxiliaryService service = auxService
										.getAuxiliaryService(ctx);
								if (service.getType() == AuxiliaryServiceTypeEnum.MultiSIM)
								{
									StringBuffer buf = new StringBuffer();
									String msisdn = auxService
											.getMultiSimMsisdn();
									String imsi = auxService.getMultiSimImsi();
									String profileNum = auxService
											.getMultiSimPackage();
									if (msisdn != null && !msisdn.isEmpty()) {
										buf.append(msisdn);
									}
									else
									{
										continue;
									}
									buf.append('%');
									if (imsi != null && !imsi.isEmpty()) {
										buf.append(imsi);
									}
									else
									{
										continue;
									}
									buf.append('%');
									if (profileNum != null) {
										buf.append(profileNum);
									}
									if (buf.length() > 0) {
										value.append(buf.toString());
										value.append(',');
									}
								}
							} catch (HomeException homeEx) {
								new MinorLogMsg(
										this,
										" Unable to find auxiliary service "
												+ auxService
														.getAuxiliaryServiceIdentifier(),
										homeEx).log(ctx);
							}
						}
					}
				}
			} else {
				new MinorLogMsg(this,
						" Subscriber is not available in context", null)
						.log(ctx);
			}
		}
		return value.toString();
	}


}
