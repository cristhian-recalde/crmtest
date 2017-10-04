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
package com.trilogy.app.crm.urcs;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.bean.urcs.ServiceOption;
import com.trilogy.app.crm.bean.urcs.ServiceOptionHome;
import com.trilogy.app.crm.bean.urcs.ServiceOptionKeyWebControl;
import com.trilogy.app.crm.bean.urcs.ServiceOptionTransientHome;
import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.app.crm.client.urcs.PromotionManagementClientV2;
import com.trilogy.app.crm.client.urcs.UrcsClientInstall;
import com.trilogy.app.crm.extension.auxiliaryservice.URCSPromotionAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.URCSPromotionAuxSvcExtensionXInfo;
import com.trilogy.app.crm.extension.service.URCSPromotionServiceExtensionXInfo;
import com.trilogy.app.crm.extension.service.URCSPromotionServiceExtension;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.urcs.promotion.v2_0.Promotion;
import com.trilogy.app.urcs.promotion.v2_0.PromotionState;
import com.trilogy.app.urcs.promotion.v2_0.PromotionType;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.util.snippet.log.Logger;

/**
 * @author victor.stratan@redknee.com
 * @author asim.mahmood@redknee.com
 * @since PromotionManagementClientV2 since 8.5
 */
public class CustomServiceOptionKeyWebControl extends ServiceOptionKeyWebControl
{
    public CustomServiceOptionKeyWebControl(final int listSize)
    {
        super(listSize);
    }

    public CustomServiceOptionKeyWebControl(final int listSize, final boolean autoPreview, final Object optionalValue)
    {
        super(listSize, autoPreview, optionalValue);
    }

    @Override
    public void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        final SpidAware bean = (SpidAware) ctx.get(AbstractWebControl.BEAN);
        final PromotionManagementClientV2 client = UrcsClientInstall.getClient(ctx, UrcsClientInstall.PROMOTION_MANAGEMENT_CLIENT_V2_KEY);

        final Context subCtx = ctx.createSubContext();
        final Home home = new ServiceOptionTransientHome(subCtx);

		if (bean.getSpid() != AuxiliaryService.DEFAULT_SPID) {
			Set<Long> usedIdentifiers = new HashSet<Long>();

			if (ctx.getInt("MODE", DISPLAY_MODE) != DISPLAY_MODE
					&& ((ViewModeEnum) ctx.get(name + ".mode",
							ViewModeEnum.READ_WRITE)) != ViewModeEnum.READ_ONLY) {
				try {
					And filter = new And();
					And filter2 = new And();

					if (bean instanceof URCSPromotionAuxSvcExtension) 
					{
						if (bean instanceof URCSPromotionAuxSvcExtension
								&& ((URCSPromotionAuxSvcExtension) bean)
										.getAuxiliaryServiceId() != URCSPromotionAuxSvcExtension.DEFAULT_AUXILIARYSERVICEID) {
							filter.add(new NEQ(
									URCSPromotionAuxSvcExtensionXInfo.AUXILIARY_SERVICE_ID,
									((URCSPromotionAuxSvcExtension) bean)
											.getAuxiliaryServiceId()));
						}

					} 
					else if (bean instanceof URCSPromotionServiceExtension) 
					{
						if (bean instanceof URCSPromotionServiceExtension
								&& ((URCSPromotionServiceExtension) bean)
										.getServiceId() != URCSPromotionServiceExtension.DEFAULT_SERVICEID) {
							filter2.add(new NEQ(
									URCSPromotionServiceExtensionXInfo.SERVICE_ID,
									((URCSPromotionServiceExtension) bean)
											.getServiceId()));
						}

					
					}

					Collection<URCSPromotionAuxSvcExtension> urcsAuxSvcExts = HomeSupportHelper
							.get(ctx).getBeans(ctx,
									URCSPromotionAuxSvcExtension.class,
									filter);

					if (urcsAuxSvcExts != null && urcsAuxSvcExts.size() > 0) 
					{
						for (URCSPromotionAuxSvcExtension extension : urcsAuxSvcExts) 
						{
                            if (extension.getSpid() == bean.getSpid())
                            {
                                usedIdentifiers.add(extension.getServiceOption());
                            }
						}
					}
					Collection<URCSPromotionServiceExtension> urcsPromExts = HomeSupportHelper
							.get(ctx).getBeans(ctx,
									URCSPromotionServiceExtension.class,
									filter2);

					if (urcsPromExts != null && urcsPromExts.size() > 0) 
					{
						for (URCSPromotionServiceExtension extension : urcsPromExts) 
						{
                            if (extension.getSpid() == bean.getSpid())
                            {
                                usedIdentifiers.add(extension.getServiceOption());
                            }
						}
					}
                }
                catch (HomeException e)
                {
                    Logger.minor(ctx, this, "Unable to filter used promotions off", e);
                }
            }

            try
            {
                final Collection<Promotion> list = client.listAllPromotionsForSpid(ctx, bean.getSpid(), PromotionType.PRIVATETYPE);
                for (Promotion promotion : list)
                {
                    if (promotion.state!=PromotionState.ACTIVATED)
                    {
                        continue;
                    }
                    else if (usedIdentifiers.contains(promotion.optionTag))
                    {
                        continue;
                    }
                    
                    final ServiceOption option = new ServiceOption();
                    option.setIdentifier(promotion.optionTag);
                    option.setName(promotion.name);
                    option.setSpid(promotion.spid);

                    try
                    {
                        home.create(ctx, option);
                    }
                    catch (HomeException e)
                    {
                        Logger.minor(ctx, this, "Unable to present ServiceOption from URCS", e);
                    }
                }
            }
            catch (RemoteServiceException e)
            {
                Logger.minor(ctx, this, "Unable to retrieve available promotions on URCS", e);
            }
        }

        subCtx.put(ServiceOptionHome.class, home);
        
        super.toWeb(subCtx, out, name, obj);
    }
}
