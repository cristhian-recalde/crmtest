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

import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.urcs.promotion.Counter;
import com.trilogy.app.urcs.promotion.CounterProfile;
import com.trilogy.app.urcs.promotion.Promotion;
import com.trilogy.app.urcs.promotion.PromotionStatus;
import com.trilogy.app.urcs.promotion.PromotionUnit;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.PromotionCounter;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.PromotionCounterProfile;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.PromotionCounterProfileUnitType;


/**
 * Adapts PromotionStatus object to API objects.
 * 
 * @author kumaran.sivasubramaniam@redknee.com
 */
public class PromotionStatusToApiAdapter implements Adapter
{

    public PromotionStatusToApiAdapter(Context ctx, int spid, PromotionStatus[] promotionStatus)
    {
        map_ = AuxiliaryServiceSupport.getPromotionAuxiliaryServicesMap(ctx, spid, promotionStatus);
    }


    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptPromotionStatusToApi((PromotionStatus) obj, map_);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static com.redknee.util.crmapi.wsdl.v2_2.types.subscription.PromotionStatus adaptPromotionStatusToApi(
            final PromotionStatus promotionStatus, Map<Long, AuxiliaryService> auxServices) throws HomeException
    {
        final com.redknee.util.crmapi.wsdl.v2_2.types.subscription.PromotionStatus apiStatus;
        apiStatus = new com.redknee.util.crmapi.wsdl.v2_2.types.subscription.PromotionStatus();
        final Promotion promotion = promotionStatus.promotion;
        final Counter[] counters = promotionStatus.counters;
        final CounterProfile[] counterProfiles = promotion.counters;
        final AuxiliaryService auxSrv = auxServices.get(Long.valueOf(promotion.optionTag));
        if (auxSrv!=null)
        {
            apiStatus.setAuxiliaryServiceID(auxSrv.getIdentifier());
            apiStatus.setAuxiliaryServiceName(auxSrv.getName());
        }
        apiStatus.setName(promotion.name);
        apiStatus.setDescription(promotion.description);
        apiStatus.setIdentifier(promotion.promotionId);
        PromotionCounterProfile[] promotionCounterProfile = new PromotionCounterProfile[counterProfiles.length];
        
        for (int i = 0; i < counterProfiles.length; i++)
        {
            CounterProfile counterProfile = counterProfiles[i];
            PromotionCounterProfile profile = new PromotionCounterProfile();
            profile.setIdentifier(counterProfile.profileId);
            profile.setName(counterProfile.name);
            profile.setUnit(convertPromotionUnitToApiUnitType(counterProfile.unit));
            profile.setPromotionID(counterProfile.promotionId);
            promotionCounterProfile[i] = profile;
        }
        apiStatus.setCounterProfiles(promotionCounterProfile);
        PromotionCounter[] apiCounters = new PromotionCounter[counters.length];
        for (int i = 0; i < counters.length; i++)
        {
            Counter crmCounter = counters[i];
            PromotionCounter counter = new PromotionCounter();
            counter.setIdentifier(crmCounter.counterId);
            counter.setProfileID(promotion.promotionId);
            counter.setValue(crmCounter.value);
            apiCounters[i]= counter;
        }
        apiStatus.setCounters(apiCounters);
        return apiStatus;
    }


    private static PromotionCounterProfileUnitType convertPromotionUnitToApiUnitType(PromotionUnit unit)
            throws HomeException
    {
        if (PromotionUnit._CURRENCY == unit.value())
        {
            return PromotionCounterProfileUnitType.value3;
        }
        else if (PromotionUnit._EVENTS == unit.value())
        {
            return PromotionCounterProfileUnitType.value4;
        }
        else if (PromotionUnit._KB == unit.value())
        {
            return PromotionCounterProfileUnitType.value2;
        }
        else if (PromotionUnit._NUMBER == unit.value())
        {
            return PromotionCounterProfileUnitType.value5;
        }
        else if (PromotionUnit._SECONDS == unit.value())
        {
            return PromotionCounterProfileUnitType.value1;
        }
        throw new HomeException(" Cannot find promotion unit type " + unit.value());
    }

    Map<Long, AuxiliaryService> map_ = null;
}
