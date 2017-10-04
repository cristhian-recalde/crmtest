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

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.bean.GroupScreeningTemplate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.product.bundle.manager.provision.v5_0.screeningTemplate.ScreeningTemplate;
import com.trilogy.product.bundle.manager.provision.v5_0.screeningTemplate.ServiceLevelUsage;

/**
 * 
 * @author ankit.nagpal@redknee.com
 * @since 9.1
 */
public class GroupScreeningTemplateToApiAdapter implements Adapter {
	public static String LOG_CLASS = GroupScreeningTemplateToApiAdapter.class
			.getName();

	@Override
	public Object adapt(Context ctx, Object obj) throws HomeException {
		return null;
	}

	@Override
	public Object unAdapt(Context ctx, Object obj) throws HomeException {
		return null;
	}

	public static ScreeningTemplate adaptScreeningTemplateRequestToScreeningTemplate(
			Context ctx, GroupScreeningTemplate request) {
		ScreeningTemplate screeningTemplate = new ScreeningTemplate();
		int flag = 1;
		screeningTemplate.active = request.getActive();
		screeningTemplate.description = request.getDescription();
		screeningTemplate.name = request.getName();
		screeningTemplate.spid = request.getSpid();
		screeningTemplate.templateId = request.getIdentifier();
		if (request.getAbsolute()) {
			flag = 0;
		}
		screeningTemplate.typeOfUsageLeveLVal = flag;

		return screeningTemplate;
	}

	public static ServiceLevelUsage[] adaptScreeningTemplateToServiceLevelUsage(
			Context ctx, GroupScreeningTemplate request) {
		
		List<ServiceLevelUsage> list = new ArrayList<ServiceLevelUsage>();
		ServiceLevelUsage serviceLevelUsageVoice = new ServiceLevelUsage();
		serviceLevelUsageVoice.eventTypeEnumIndex = 0;
		serviceLevelUsageVoice.Value = request.getVoice();
		list.add(serviceLevelUsageVoice);
		ServiceLevelUsage serviceLevelUsageData = new ServiceLevelUsage();
		serviceLevelUsageData.eventTypeEnumIndex = 1;
		serviceLevelUsageData.Value = request.getData();
		list.add(serviceLevelUsageData);
		ServiceLevelUsage serviceLevelUsageEvent = new ServiceLevelUsage();
		serviceLevelUsageEvent.eventTypeEnumIndex = 2;
		serviceLevelUsageEvent.Value = request.getEvent();
		list.add(serviceLevelUsageEvent);
		return list.toArray(new ServiceLevelUsage[0]);
	}

}
