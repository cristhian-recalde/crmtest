package com.trilogy.app.crm.bean.paymentgatewayintegration;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CreditCardToken;
import com.trilogy.app.crm.bean.CreditCardTokenXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberCltc;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TaxableTopUp;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.bundle.exception.BundleDoesNotExistsException;
import com.trilogy.app.crm.cltc.SubCltcOperationCode;
import com.trilogy.app.crm.paymentgatewayintegration.PaymentGatewaySupport;
import com.trilogy.app.crm.paymentgatewayintegration.PaymentGatewaySupportHelper;
import com.trilogy.app.crm.subscriber.charge.support.BundleChargingSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.app.crm.taxation.LocalTaxAdapter;
import com.trilogy.product.s2100.oasis.param.Parameter;

/**
 * @author chandrachud.ingale
 * @since
 */
public class AutoTopUpTriggerAgentHome extends HomeProxy
{

    private static final long serialVersionUID = 2334719867113058588L;


    /**
 * 
 */
    public AutoTopUpTriggerAgentHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * {@inheritDoc}
     */
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        final Subscriber newSubscriber = (Subscriber) obj;
        
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Checking for ATU for subscriber : " + newSubscriber.getId() + " having MSISDN : " + newSubscriber.getMsisdn()).log(ctx);
        }

        /**
         * This check is only for PREPAID subscribers
         */
        if (!(SubscriberStateEnum.ACTIVE.equals(newSubscriber.getState()) || SubscriberStateEnum.EXPIRED
                .equals(newSubscriber.getState())))
            
        
        if (    (SubscriberTypeEnum.POSTPAID.equals(newSubscriber.getSubscriberType())) ||  
                ((SubscriberTypeEnum.PREPAID.equals(newSubscriber.getSubscriberType()) && 
                        !(SubscriberStateEnum.ACTIVE.equals(newSubscriber.getState()) || SubscriberStateEnum.EXPIRED.equals(newSubscriber.getState()))
                ))
           )
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "ATU not applicable for subscriber : " + newSubscriber.getId() + " having MSISDN : " + newSubscriber.getMsisdn()).log(ctx);
            }    
        	return super.store(ctx, obj);
        }
        
        if(newSubscriber.getAtuAmount() <= 0)
        {
        	if (LogSupport.isDebugEnabled(ctx))
            {
        		new DebugLogMsg(this, "Invalid autoTopupAmount: "+ newSubscriber.getAtuAmount() + " for subscriber : " + newSubscriber.getId()).log(ctx);
            }
            return super.store(ctx, obj);
        }
        
        if(newSubscriber.getAtuBalanceThreshold() < 0)
        {
        	if (LogSupport.isDebugEnabled(ctx))
            {
        		new DebugLogMsg(this, "No balance threshold based ATU is register for the subscriber " + newSubscriber.getId()).log(ctx);
            }
            return super.store(ctx, obj);
        }

        SubscriberCltc subscriberCltc = (SubscriberCltc) ctx.get(Common.ER_447_SUBSCRIBER_CLCT);
        CRMSpid spid = SpidSupport.getCRMSpid(ctx, newSubscriber.getSpid());
        
        if (!(newSubscriber.isClctChange() && (subscriberCltc.getThresholdTags().indexOf(spid.getCcAtuIndicator()) != -1)))
        {
        	if (LogSupport.isDebugEnabled(ctx))
            {
        		new DebugLogMsg(this, "Not processing AutoTopupTrigger as CLTC not reached or spid ATU indicator not matched for subscriber : " + newSubscriber.getId()).log(ctx);
            }
        	return super.store(ctx, obj);
        }

        if(newSubscriber.getAtuCCTokenId() == AbstractSubscriber.DEFAULT_ATUCCTOKENID)
        {
            new MajorLogMsg(this, "Not processing AutoTopupTrigger as CC token not defined for subscriber : " + newSubscriber.getId()).log(ctx);
            return super.store(ctx, obj);
        }
        
        boolean recurring = false;

        Map<Short, Parameter> outParams = new HashMap<Short, Parameter>();
        try
        {
            CreditCardToken token = HomeSupportHelper.get(ctx).findBean(ctx, CreditCardToken.class,
                    new EQ(CreditCardTokenXInfo.ID, newSubscriber.getAtuCCTokenId()));

            if(token == null)
            {
                String msg = "Invalid subscriber CreditCardToken state, no token available for subscriber : " + newSubscriber.getId();
                new MajorLogMsg(this, msg).log(ctx);
                throw new AutoTopupFailedException(msg);
            }
            
            TaxableTopUp topUp = LocalTaxAdapter.getTotalTopUp(ctx, newSubscriber, newSubscriber.getAtuAmount());

            int result = PaymentGatewaySupportHelper.get(ctx).chargePaymentGateway(ctx, topUp.getPGChargeableAmount(),
                    topUp.getTaxAmount(), newSubscriber, recurring, token.getMaskedCreditCardNumber(),
                    token.getValue(), outParams);

            if (result != PaymentGatewaySupport.DEFAULT_SUCCESS)
            {
                throw new AutoTopupFailedException(new PaymentGatewayException(
                        "PaymentGateway charging failed for msisdn:" + newSubscriber.getMsisdn()
                                + ". An account note will be added with details of error shortly.", result));
            }

            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.info(ctx, this,
                        "Payment Gateway successfully charged for subscriber:" + newSubscriber.getId() + " and amount:"
                                + topUp.getPGChargeableAmount());
            }

            AdjustmentType adjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,
                    AdjustmentTypeEnum.ThresholdCreditCardTopUp);

            TransactionSupport.createTransaction(ctx, newSubscriber, topUp.getSubscriberBalanceCrAmount(),
                    adjustmentType);
            
            //-- Re purchase one time re purchasable bundles. It should have different home to call re purchase bunlde functionality.
            if(subscriberCltc.getOperation() == SubCltcOperationCode.BUNDLE_BALANCE_DECREASED)
            {
            	if(LogSupport.isDebugEnabled(ctx))
            	{
            		LogSupport.debug(ctx, this, "ER-447 is having operation BUNDLE_BALANCE_DECREASED code : " +SubCltcOperationCode.BUNDLE_BALANCE_DECREASED +
            				"hence initiating re purchase one time bundle");
            	}
            	int repurchaseResult = repurchaseOneTimeBundles(ctx, newSubscriber, spid);
            	if(repurchaseResult == 0)
            	{
            		if(LogSupport.isDebugEnabled(ctx))
                	{
            			LogSupport.debug(ctx, this,"Re purchase bundle success");
                	}
            	}
            }
            
            return super.store(ctx, obj);
        }
        catch (HomeException e)
        {
            throw new AutoTopupFailedException(e);
        }
        catch (PaymentGatewayException e)
        {
            throw new AutoTopupFailedException(e);
        }
    }
    
    /**
     * 
     * @param ctx
     * @param subscriber
     * @param spid
     * @return
     * @throws HomeException
     */
    public static int repurchaseOneTimeBundles(Context ctx, Subscriber subscriber, CRMSpid spid) throws HomeException
    {
    	int result = 0;
    	// -- PricePlan Bundle re purchase
        final Map<Long, BundleFee> bundles = SubscriberBundleSupport.getPricePlanBundles(ctx, subscriber);
        
        for (BundleFee fee : bundles.values())
        {
            try
            {
                BundleProfile profile = fee.getBundleProfile(ctx, subscriber.getSpid());
                if(profile.isRepurchasable())
                {
                    result = BundleChargingSupport.invokeRepurchaseBundles(ctx, subscriber, profile, fee);
                }
                
            }
            catch (BundleDoesNotExistsException e)
            {
            	LogSupport.minor(ctx, AutoTopUpTriggerAgentHome.class, "Bundle does not exist: " + e.getMessage(), e);
            }
            catch (Exception e)
            {
            	LogSupport.minor(ctx, AutoTopUpTriggerAgentHome.class, "Bundle does not exist: " + e.getMessage(), e);
            }
        }
        
        // -- Auxiliary Bundle re purchase
        final Map<Long, BundleFee> auxBundles = SubscriberBundleSupport.getAvailableAuxiliaryBundlesForSubscriber(ctx, spid.getId(), subscriber.getSubscriberType(), subscriber);
        for (BundleFee fee : auxBundles.values())
        {
            try
            {
            	BundleProfile profile = fee.getBundleProfile(ctx, subscriber.getSpid());
            	if(profile.isRepurchasable())
            	{
            		result = BundleChargingSupport.invokeRepurchaseBundles(ctx, subscriber, profile, fee);
            	}
            } 
            catch (BundleDoesNotExistsException e)
            {
            	LogSupport.minor(ctx, AutoTopUpTriggerAgentHome.class, "Bundle does not exist: " + e.getMessage(), e);
            }
            catch (Exception e)
            {
            	LogSupport.minor(ctx, AutoTopUpTriggerAgentHome.class, "Bundle does not exist: " + e.getMessage(), e);
            }
        }
        return result;
    }
} 
