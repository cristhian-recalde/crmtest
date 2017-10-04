package com.trilogy.app.crm.api.rmi.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.util.Index2D;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.util.crmapi.wsdl.v2_1.types.serviceandbundle.PricePlanOptionType;

/**
 * Extensible holder to avoid bloating Support-methods with arguments.
 * 
 * Add fields as needed.
 * @author sbanerjee
 *
 */
public final class ApiResultHolder
{
    /**
     * 
     */
    private final Map<Index2D<Long>, ApiOptionResultHolder> priceplanOptionUpdateApiResults = 
        new HashMap<Index2D<Long>, ApiOptionResultHolder>();
    
    /**
     * For Stackable Bundles - For each bundle topped up successfully, the reference to the ChargingHistory record.
     */
    private final Set<Long> bundlesToppedUpSuccessfully = new HashSet<Long>();
    
    /**
     * This represents a collection of temporary state objects.
     */
    private ApiState apiTempState = new ApiState();
    
    private final Set<Long> optionsNotProvisionedDueToInsufficientBalance = new HashSet<Long>();
    
    /**
     * @return the optionsNotProvisionedDueToInsufficientBalance
     */
    public Set<Long> getOptionsNotProvisionedDueToInsufficientBalance()
    {
        return this.optionsNotProvisionedDueToInsufficientBalance;
    }

    /**
     * @return A non-Null Set that contains the id's of bundles that were recharged successfully
     * (will be empty, if none). 
     */
    public Set<Long> getBundlesToppedUpSuccessfully()
    {
        return this.bundlesToppedUpSuccessfully;
    }

    /**
     * @return the apiTempState
     */
    public ApiState getApiTempState()
    {
        return this.apiTempState;
    }

    /**
     * @param apiTempState the apiTempState to set
     */
    public void resetApiTempState()
    {
        this.apiTempState = new ApiState();
    }

    /**
     * 
     * @param ctx
     * @param subscriber
     */
    public void initSubscriberState(Context ctx, Subscriber subscriber)
    {
        this.apiTempState.setCalculatedSubscriberBalanceRemaining(
                subscriber.getBalanceRemaining(ctx));
    }

    /**
     * 
     * @param ctx
     * @return
     */
    public long getCalculatedSubscriberBalanceRemaining(Context ctx)
    {
        return this.apiTempState.getCalculatedSubscriberBalanceRemaining();
    }

    /**
     * 
     * @param ctx
     * @param reduceBy
     */
    public void reduceCalculatedSubscriberBalanceRemainingBy(Context ctx,
            long reduceBy)
    {
        this.apiTempState.reduceCalculatedSubscriberBalanceRemainingBy(reduceBy);
    }

    public void addToNotProvisionedDueToInsufficientBalance(Long optionId)
    {
        this.optionsNotProvisionedDueToInsufficientBalance.add(optionId);
    }

    public void reduceCalculatedSubscriberBalanceRemainingByOnlyIfSufficientBalance(
            Context ctx, long reduceBy)
    {
        if(this.apiTempState.getCalculatedSubscriberBalanceRemaining() >= reduceBy)
            this.apiTempState.reduceCalculatedSubscriberBalanceRemainingBy(reduceBy);
    }
    
    
    /*
     * Helper methods
     */
    
    /**
     * Restricted Access: Use {@link ApiOptionsUpdateResultSupport#getPriceplanOptionUpdateApiResultsHolder(long, int)}
     */
    ApiOptionResultHolder getPriceplanOptionUpdateApiResultsHolder(long optionId, int chargableItemType)
    {
        return this.priceplanOptionUpdateApiResults.get(
                ApiOptionsUpdateResultSupport.toIndex2D(optionId, chargableItemType));
    }
    
    /**
     * 
     * @param optionId
     * @param ppoType
     * @return
     */
    public ApiOptionResultHolder getPriceplanOptionUpdateApiResultsHolder(long optionId, PricePlanOptionType ppoType)
    {
        return this.priceplanOptionUpdateApiResults.get(
                ApiOptionsUpdateResultSupport.toIndex2D(optionId,
                        ApiOptionsUpdateResultSupport.toChargableItemType(ppoType)));
    }
    
    /**
     * 
     * @param optionId
     * @param ppoType
     * @param resH
     */
    public void putPriceplanOptionUpdateApiResultsHolder(long optionId, 
            PricePlanOptionType ppoType, ApiOptionResultHolder resH)
    {
        this.priceplanOptionUpdateApiResults.put(
                ApiOptionsUpdateResultSupport.toIndex2D(optionId, 
                        ApiOptionsUpdateResultSupport.toChargableItemType(ppoType)), 
                resH);
    }

    /**
     * 
     * @param optionId
     */
    public void addToBundlesToppedUpSuccessfully(Long optionId)
    {
        this.bundlesToppedUpSuccessfully.add(optionId);
    }
    
    
}