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
package com.trilogy.app.crm.api.rmi.support;

import java.util.HashMap;
import java.util.Map;
import com.trilogy.util.crmapi.wsdl.v2_1.types.serviceandbundle.PricePlanOptionType;
import com.trilogy.util.crmapi.wsdl.v2_1.types.serviceandbundle.PricePlanOptionTypeEnum;

import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.util.Index2D;
import com.trilogy.framework.xhome.context.Context;

/**
 * @author sbanerjee
 *
 */
public class ApiOptionsUpdateResultSupport
{
    private static final Map<Long, Long> API_OPTIION_TYPE_MAP 
        = new HashMap<Long, Long>();
    static
    {
        API_OPTIION_TYPE_MAP.put(
                toLong(PricePlanOptionTypeEnum.SERVICE), 
                    toLong(ChargingConstants.CHARGABLE_ITEM_SERVICE));
        API_OPTIION_TYPE_MAP.put(
                toLong(PricePlanOptionTypeEnum.BUNDLE), 
                    toLong(ChargingConstants.CHARGABLE_ITEM_BUNDLE));
        API_OPTIION_TYPE_MAP.put(
                toLong(PricePlanOptionTypeEnum.AUXILIARY_BUNDLE), 
                    toLong(ChargingConstants.CHARGABLE_ITEM_BUNDLE));
        API_OPTIION_TYPE_MAP.put(
                toLong(PricePlanOptionTypeEnum.AUXILIARY_SERVICE), 
                    toLong(ChargingConstants.CHARGABLE_ITEM_AUX_SERVICE));
        
    }

    private static final ApiOptionResultHolder NULL = new ApiOptionResultHolder();
    
    
    public static Long toChargableItemType(PricePlanOptionTypeEnum ppoE)
    {
        return API_OPTIION_TYPE_MAP.get(toLong(ppoE));
    }
    public static Long toChargableItemType(PricePlanOptionType ppoType)
    {
        return API_OPTIION_TYPE_MAP.get(toLong(ppoType.getValue()));
    }
    
    /**
     * 
     * @param ctx
     * @param resHolder
     */
    public static void installApiResultSetInContext(Context ctx, ApiResultHolder resHolder)
    {
        ctx.put(ApiResultHolder.class, resHolder);
    }
    
    /**
     * 
     * @param ctx
     * @param resHolder
     */
    public static ApiResultHolder unInstallApiResultSetFromContext(Context ctx)
    {
        ApiResultHolder ret = (ApiResultHolder)ctx.get(ApiResultHolder.class);
        ctx.put(ApiResultHolder.class, null);
        return ret;
    }
    
    /**
     * 
     * @param ctx
     * @return
     */
    public static boolean isInstalledApiResultSetInContext(Context ctx)
    {
        return (ctx.get(ApiResultHolder.class) instanceof ApiResultHolder);
    }
    
    /**
     * 
     * @param ctx
     * @return
     * @throws IllegalStateException if the ApiResultSetHolder is not set in context already.
     * The developer is supposed to check for the installed using method {@link #isInstalledApiResultSetInContext(Context)}.
     */
    public static ApiResultHolder getApiResultSetFromContext(Context ctx)
    {
        final Object object = ctx.get(ApiResultHolder.class);
        if(!(object instanceof ApiResultHolder))
            throw new IllegalStateException("Developer Error: ApiResultHolder not installed in the context before using it.");
        
        return (ApiResultHolder)object;
    }
    
    /**
     * 
     * @param ctx
     * @param optionId
     * @return
     */
    public static ApiOptionResultHolder getPriceplanOptionUpdateApiResultsHolder(Context ctx, long optionId, int chargableItemType)
    {
        if(!isInstalledApiResultSetInContext(ctx))
            return NULL;
        
        ApiOptionResultHolder priceplanOptionUpdateApiResultsHolder = 
            getApiResultSetFromContext(ctx).getPriceplanOptionUpdateApiResultsHolder(optionId, chargableItemType);
        
        return priceplanOptionUpdateApiResultsHolder==null ? 
                    NULL : priceplanOptionUpdateApiResultsHolder;
    }
    
    /**
     * 
     * @param ctx
     * @param optionId
     * @return
     */
    public final static ApiErrorSource getApiErrorSource(Context ctx, long optionId, int chargableItemType)
    {
        ApiOptionResultHolder h = getPriceplanOptionUpdateApiResultsHolder(ctx, optionId, chargableItemType);
        return h.getApiErrorSource();
    }
    
    
    
    /**
     * 
     * @param ctx
     * @param optionId
     * @return
     */
    public final static String getApiErrorSourceName(Context ctx, long optionId, int chargableItemType)
    {
        return getApiErrorSource(ctx, optionId, chargableItemType).name();
    }
    
    /**
     * 
     * @param ctx
     * @param optionId
     * @return
     */
    public final static int getApiInternalErrorCode(Context ctx, long optionId, int chargableItemType)
    {
        ApiOptionResultHolder h = getPriceplanOptionUpdateApiResultsHolder(ctx, optionId, chargableItemType);
        return h.getApiInternalErrorCode();
    }
    
    /**
     * 
     * @param ctx
     * @param optionId
     * @return
     */
    public final static int getApiErrorCode(Context ctx, long optionId, int chargableItemType)
    {
        return getPriceplanOptionUpdateApiResultsHolder(ctx, optionId, chargableItemType).getOverallResultCode();
    }
    
    /**
     * 
     * @param ctx
     * @param optionId
     * @return
     */
    public final static String getApiErrorMessage(Context ctx, long optionId, int chargableItemType)
    {
        return getPriceplanOptionUpdateApiResultsHolder(ctx, optionId, chargableItemType).getErrorMessage();
    }
    
    /*
     * Setter Methods
     */
    
    /**
     * 
     */
    public final static void setUrcsErrorCode(Context ctx, long optionId, int chargableItemType, int rc)
    {
        getPriceplanOptionUpdateApiResultsHolder(ctx, optionId, chargableItemType).setUrcsResultCode(rc);
    }
    
    /**
     * 
     * @param ctx
     * @param optionId
     * @param rc
     */
    public final static void setOCGErrorCode(Context ctx, long optionId, int chargableItemType, int rc)
    {
        getPriceplanOptionUpdateApiResultsHolder(ctx, optionId, chargableItemType).setOcgResultCode(rc);
    }
    
    /**
     * 
     * @param ctx
     * @param optionId
     * @param rc
     */
    public final static void setApiErrorCode(Context ctx, long optionId, int chargableItemType, int rc)
    {
        getPriceplanOptionUpdateApiResultsHolder(ctx, optionId, chargableItemType).setOverallResultCode(rc);
    }
    
    /**
     * 
     * @param ctx
     * @param optionId
     * @param msg
     */
    public final static void setApiErrorMessage(Context ctx, long optionId, int chargableItemType, String msg)
    {
        getPriceplanOptionUpdateApiResultsHolder(ctx, optionId, chargableItemType).setErrorMessage(msg);
    }
    
    /*
     * Helper Methods
     */
    public static Index2D<Long> toIndex2D(long optionId, int chargableItemType)
    {
        return new Index2D<Long>(toLong(optionId), toLong(chargableItemType));
    }
    
    public static Index2D<Long> toIndex2D(long optionId, Long chargableItemType)
    {
        return new Index2D<Long>(toLong(optionId), chargableItemType);
    }
    
    /*
     * Private methods
     */
    private static Long toLong(PricePlanOptionTypeEnum ppoE)
    {
        return Long.valueOf(ppoE.getValue().getValue());
    }
    
    private static Long toLong(int val)
    {
        return Long.valueOf(val);
    }
    
    private static Long toLong(long val)
    {
        return Long.valueOf(val);
    }
}