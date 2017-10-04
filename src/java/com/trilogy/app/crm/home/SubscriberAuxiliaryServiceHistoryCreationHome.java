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
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * Adding history entries for subscriber auxiliary services (only for provisioning and unprovisioning)
 * @author ksivasubramaniam
 *
 */
public class SubscriberAuxiliaryServiceHistoryCreationHome extends HomeProxy
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public SubscriberAuxiliaryServiceHistoryCreationHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    public Object create(Context ctx, final Object obj) throws HomeException
    {
        final SubscriberAuxiliaryService subAuxService = (SubscriberAuxiliaryService) super.create(ctx, obj);
        SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, subAuxService.getSubscriberIdentifier(),
                HistoryEventTypeEnum.PROVISION, ChargedItemTypeEnum.AUXSERVICE, subAuxService,
                ServiceStateEnum.PROVISIONED);
        return subAuxService;
    }


    public void remove(Context ctx, final Object obj) throws HomeException
    {
        final SubscriberAuxiliaryService subAuxService = (SubscriberAuxiliaryService) obj;
        super.remove(ctx, obj);
        SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, subAuxService.getSubscriberIdentifier(),
                HistoryEventTypeEnum.UNPROVISION, ChargedItemTypeEnum.AUXSERVICE, subAuxService,
                ServiceStateEnum.UNPROVISIONED);
    }
} 
