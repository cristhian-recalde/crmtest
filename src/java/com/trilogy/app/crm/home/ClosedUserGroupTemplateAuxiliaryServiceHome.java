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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceStateEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.CUGTypeEnum;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplate;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xhome.elang.*;
import com.trilogy.app.crm.extension.auxiliaryservice.CallingGroupAuxSvcExtensionXInfo;


/**
 * This class provides functionality for creating/removing/updating auxiliary services
 * associated with the corresponding Closed User Group template.
 *
 * @author jimmy.ng@redknee.com
 * @auth ltse
 */
public class ClosedUserGroupTemplateAuxiliaryServiceHome
extends HomeProxy
{
	/**
	 * Creates a new ClosedUserGroupAuxiliaryServiceCreationHome.
	 *
	 * @param ctx The operating context.
	 * @param delegate The home to delegate to.
	 */
	public ClosedUserGroupTemplateAuxiliaryServiceHome(final Context ctx, final Home delegate)
	{
		super(ctx, delegate);
	}


	/**
	 * INHERIT
	 */
	public Object create(Context ctx, Object obj)
			throws HomeException
			{
		final ClosedUserGroupTemplate cugTemplate = (ClosedUserGroupTemplate) super.create(ctx,obj);

		try
		{ 
			AuxiliaryService service;
			try
			{
				service = (AuxiliaryService)XBeans.instantiate(AuxiliaryService.class, ctx);
			}
			catch(Throwable t)
			{
				service = new com.redknee.app.crm.bean.core.AuxiliaryService();
				new MinorLogMsg(this, "XBeans can't isntantiate", t).log(ctx);
			}
			service.setName(cugTemplate.getName());
			service.setSpid(cugTemplate.getSpid());
			service.setType(AuxiliaryServiceTypeEnum.CallingGroup);

			Collection<Extension> extensions = new ArrayList<Extension>();

			CallingGroupAuxSvcExtension extension = new CallingGroupAuxSvcExtension();
			extension.setAuxiliaryServiceId(service.getID());
			extension.setSpid(service.getSpid());
			extension.setCallingGroupIdentifier(cugTemplate.getID());
			extension.setCallingGroupType(CallingGroupTypeEnum.CUG);
			extensions.add(extension);

			service.setChargingModeType(ServicePeriodEnum.MONTHLY);
			service.setSmartSuspension(cugTemplate.getSmartSuspension());
			service.setActivationFee(cugTemplate.getActivationFee());
			service.setAdjustmentTypeDescription("CUG " + cugTemplate.getName());
			service.setGLCode(cugTemplate.getGlCode());
			service.setInvoiceDescription("CUG " + cugTemplate.getName());
			service.setTaxAuthority(cugTemplate.getTaxAuthority());
			service.setSmartSuspension(cugTemplate.getSmartSuspension()); 

			if (cugTemplate.getCugType().equals(CUGTypeEnum.PublicCUG))
			{
				extension.setCallingGroupType(CallingGroupTypeEnum.CUG);
				service.setCharge(cugTemplate.getServiceCharge()); 

			} else 
			{
				extension.setCallingGroupType(CallingGroupTypeEnum.PCUG);                         	
				extension.setServiceChargePrepaid(cugTemplate.getServiceChargePrePaid()); 
				extension.setServiceChargePostpaid(cugTemplate.getServiceChargePostpaid()); 
				extension.setServiceChargeExternal(cugTemplate.getServiceChargeExternal());
				service.setCharge(cugTemplate.getServiceChargePostpaid()); 
			}

			service.setAuxiliaryServiceExtensions(ExtensionSupportHelper.get(ctx).wrapExtensions(ctx, extensions));

			// set the Aux Service Type to ANY as supposed to GSM TT: 6111441545
			service.setTechnology(TechnologyEnum.ANY);

			Home auxSvcHome = (Home) ctx.get(AuxiliaryServiceHome.class);
			if (auxSvcHome == null)
			{
				throw new HomeException("Startup error: no AuxiliaryServiceHome found in context.");
			}
			service = (AuxiliaryService) auxSvcHome.create(ctx,service);
			cugTemplate.setAuxiliaryService(service.getIdentifier());

			return cugTemplate;
		}
		catch (final HomeException e)
		{
			CallingGroupERLogMsg.generateCUGTemplateCreationER(cugTemplate,
					CallingGroupERLogMsg.ERROR_PROVISIONING_AUXILIARY_SERVICE, ctx);

			throw e;
		}
		catch (final Exception e)
		{
			CallingGroupERLogMsg.generateCUGTemplateCreationER(cugTemplate,
					CallingGroupERLogMsg.ERROR_PROVISIONING_AUXILIARY_SERVICE, ctx);

			throw new HomeException(e.getMessage(), e);
		}


			}

	/**
	 * INHERIT
	 */
	public void remove(Context ctx, Object obj)
			throws HomeException
			{
		super.remove(ctx,obj);
		final ClosedUserGroupTemplate cugTemplate = (ClosedUserGroupTemplate) obj;

		try
		{
			final Home auxSvcHomehome = (Home) ctx.get(AuxiliaryServiceHome.class);

			if (auxSvcHomehome == null)
			{
				throw new HomeException("Startup error: no AuxiliaryServiceHome found in context.");
			}


			final AuxiliaryService aux_svc =
					(AuxiliaryService) auxSvcHomehome.find(ctx, Long.valueOf(cugTemplate.getAuxiliaryService()));

			if (aux_svc != null)
			{
				aux_svc.setState(AuxiliaryServiceStateEnum.DEPRECATED); 
				auxSvcHomehome.store(ctx,aux_svc);
			}

		}
		catch (final HomeException e)
		{
			CallingGroupERLogMsg.generateCUGTemplateDeletionER(cugTemplate, 
					CallingGroupERLogMsg.ERROR_PROVISIONING_AUXILIARY_SERVICE, ctx);

			throw e;
		}
		catch (final Exception e)
		{
			CallingGroupERLogMsg.generateCUGTemplateDeletionER(cugTemplate,
					CallingGroupERLogMsg.ERROR_PROVISIONING_AUXILIARY_SERVICE, ctx);

			throw new HomeException(e.getMessage(), e);
		}
			}

	/**
	 * INHERIT
	 */
	public Object store(Context ctx, Object obj)
			throws HomeException
			{
		final ClosedUserGroupTemplate cugTemplate = (ClosedUserGroupTemplate) obj;

		final Home auxSvcHomehome = (Home) ctx.get(AuxiliaryServiceHome.class);

		if (auxSvcHomehome == null)
		{
			throw new HomeException("AuxiliaryServiceHome not found in context.");
		}


		//added to fix the TT#12103045005
		// Fetching the Auxiliary service ID from Extension because Auxiliary Service ID for CUG is saved in the Extensions.
		long auxiliaryServiceID = -1;
		List<CallingGroupAuxSvcExtension> extensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, CallingGroupAuxSvcExtension.class, new EQ(CallingGroupAuxSvcExtensionXInfo.CALLING_GROUP_IDENTIFIER, cugTemplate.getID()));
		for(CallingGroupAuxSvcExtension cugTemplateExtension : extensions)
		{
			auxiliaryServiceID = cugTemplateExtension.getAuxiliaryServiceId();
			break;
		}

		final AuxiliaryService service =
				(AuxiliaryService) auxSvcHomehome.find(ctx, auxiliaryServiceID);

		if (service != null)
		{
			service.setSmartSuspension(cugTemplate.getSmartSuspension()); 
			auxSvcHomehome.store(ctx,service);
		}

		return super.store(ctx,obj);

			}

}
