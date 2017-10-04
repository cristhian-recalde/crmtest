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

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.LoyaltyCard;
import com.trilogy.app.crm.client.urcs.LoyaltyParameters;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.product.bundle.manager.provision.v4_0.loyalty.LoyaltyCardProfile;
import com.trilogy.util.crmapi.wsdl.v3_0.types.loyalty.LoyaltyPointsConversionResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.loyalty.LoyaltyProfileAssociation;
import com.trilogy.util.crmapi.wsdl.v3_0.types.loyalty.LoyaltyProfileAssociationCreateRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.loyalty.LoyaltyProfileAssociationResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.loyalty.LoyaltyProfileBalanceResponse;

/**
 * 
 * @author asim.mahmood@redknee.com
 * @since 9.1
 */
public class LoyaltyToApiAdapter implements Adapter
{
    public static String LOG_CLASS = LoyaltyToApiAdapter.class.getName();
    
    public static Pattern loyaltyCardPattern = Pattern.compile("\\d{3}-\\d{6}");
    
    @Override
    public Object adapt(Context ctx, Object obj) throws HomeException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object unAdapt(Context ctx, Object obj) throws HomeException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public static LoyaltyCard adaptLoyaltyRequestToLoyaltyCard(Context ctx, LoyaltyProfileAssociationCreateRequest request,
            Account account)
    {
        LoyaltyCard card;
        try
        {
            card = (LoyaltyCard)XBeans.instantiate(LoyaltyCard.class, ctx);
        }
        catch (Exception e)
        {
            new InfoLogMsg(LoyaltyToApiAdapter.class, "Unable to instantiate LoyaltyCard using Xbeans, attempting manual...", e).log(ctx);
            card = new LoyaltyCard();
        }
        
        card.setBAN(request.getAccountID());
        card.setProgramID(request.getProgramID());
        
        //Only store the required part if possible
        Matcher lcid = loyaltyCardPattern.matcher(request.getLoyaltyCardID());
        if (lcid.find())
        {
            card.setLoyaltyCardID(lcid.group(0));    
        }
        else
        {
            card.setLoyaltyCardID(request.getLoyaltyCardID());
        }
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, LOG_CLASS, "Request adapted to " + card.toString());
        }
        return card;
    }

    public static LoyaltyCardProfile adaptLoyaltyRequestToLoyaltyCardProfile(Context ctx,
            LoyaltyProfileAssociationCreateRequest request)
    {
        LoyaltyCardProfile profile = new LoyaltyCardProfile();
        
        profile.subscriberId = request.getAccountID();
        profile.programId = request.getProgramID();
        profile.loyaltyCardId = request.getLoyaltyCardID();
        profile.redemptionEnableFlag = request.getRedemptionEnabled() != null ? request.getRedemptionEnabled() : true;
        profile.accumulationEnableFlag = request.getAccumulationEnabled() != null ? request.getAccumulationEnabled() : true;
        profile.expiryDate = request.getExpiryDate() != null ? request.getExpiryDate().getTimeInMillis() : 0;

        return profile;
    }


    /**
     * Returns a response that contains the loyalty card profile association.
     * If the association does not exists, the association is populated with empty parameters 
     * since it cannot be null.
     * 
     * @param ctx
     * @param cardProfile
     * @return
     */
    public static LoyaltyProfileAssociationResponse adaptLoyaltyCardProfileToResponse(Context ctx,
            LoyaltyCardProfile cardProfile)
    {
        LoyaltyProfileAssociationResponse response = new LoyaltyProfileAssociationResponse();
        LoyaltyProfileAssociation card = new LoyaltyProfileAssociation();
        
        if (cardProfile != null)
        {   
            
            card.setAccountID(cardProfile.subscriberId);
            card.setProgramID(cardProfile.programId);
            card.setLoyaltyCardID(cardProfile.loyaltyCardId);
            card.setRedemptionEnabled(cardProfile.redemptionEnableFlag);
            card.setAccumulationEnabled(cardProfile.accumulationEnableFlag);
            card.setIssueDate(CalendarSupportHelper.get(ctx).dateToCalendar(new Date(cardProfile.issueDate)));
            card.setExpiryDate(CalendarSupportHelper.get(ctx).dateToCalendar(new Date(cardProfile.expiryDate)));
        }
        else
        {
            card.setAccountID("");
            card.setProgramID("");
            card.setLoyaltyCardID("");
            card.setRedemptionEnabled(false);
            card.setAccumulationEnabled(false);
            card.setIssueDate(CalendarSupportHelper.get(ctx).dateToCalendar(new Date(0)));
            card.setExpiryDate(CalendarSupportHelper.get(ctx).dateToCalendar(new Date(0)));
        }
       
        response.setAssociation(card);
        return response;
    }

    /**
     * Populates 
     * @param ctx
     * @param params
     * @return
     */
    public static LoyaltyProfileBalanceResponse adaptLoyaltyParametersToResponse(Context ctx,
            LoyaltyParameters params)
    {
        LoyaltyProfileBalanceResponse result = new LoyaltyProfileBalanceResponse();
        
        result.setPoints(params.getAvailablePoints());
        result.setTransactionID(params.getTransactionId());
        
        return result;
    }

    public static LoyaltyPointsConversionResponse adaptLoyaltyCardProfileToConversionResponse(Context ctx,
            LoyaltyParameters params)
    {
        LoyaltyPointsConversionResponse result = new LoyaltyPointsConversionResponse();
        
        result.setAvailablePoints(params.getAvailablePoints());
        result.setCalculatedAmount(params.getCalculatedAmount());
        result.setUsedLoyaltyPoints(params.getCalculatedPoints());
        return result;
    }

    public static LoyaltyProfileBalanceResponse adaptLoyaltyCardProfileToResponse(Context ctx, long points)
    {
        LoyaltyProfileBalanceResponse result = new LoyaltyProfileBalanceResponse();
        
        result.setPoints(points);
        result.setTransactionID("");
        return result;
    }
}
