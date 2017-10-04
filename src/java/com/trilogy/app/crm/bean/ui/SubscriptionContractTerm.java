package com.trilogy.app.crm.bean.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeXInfo;
import com.trilogy.app.crm.bean.BillingMessage;
import com.trilogy.app.crm.bean.ContractDescription;
import com.trilogy.app.crm.bean.ContractDescriptionID;
import com.trilogy.app.crm.bean.CreditCategoryBillingMessage;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.billing.message.BillingMessageAdapter;
import com.trilogy.app.crm.billing.message.BillingMessageHomePipelineFactory;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.messages.MessageConfigurationSupport;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


public class SubscriptionContractTerm extends AbstractSubscriptionContractTerm
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public String getContractPolicySummary()
    {
        Context ctx = getContext();
        MessageMgr mmgr = new MessageMgr(ctx, this);
        String result = mmgr.get(
                com.redknee.app.crm.contract.SubscriptionContractTerm.SUBSCRIPTION_CONTRACT_TERM_POLICY_MSG_KEY,
                " Contract {0} has {1}-month(s) with prepayment amount of {2}",
                getMessageValues(ctx));
        super.setContractPolicySummary(result);
        return super.getContractPolicySummary();
    }


    public int getTaxAuthority()
    {
        if (taxAuthority_ == AbstractSubscriptionContractTerm.DEFAULT_TAXAUTHORITY)
        {
            lazyLoad();
        }
        return super.getTaxAuthority();
    }


    protected Object[] getMessageValues(Context ctx)
    {
        final Currency currency = (Currency) ctx.get(Currency.class, Currency.DEFAULT);
        final Object[] values = new Object[3];
        values[0] = this.getName();
        values[1] = this.getContractLength();
        values[2] = currency.formatValue(this.getPrepaymentAmount());
        return values;
    }


    private synchronized void lazyLoad()
    {
        try
        {
            if (this.getContractAdjustmentTypeId() != DEFAULT_CONTRACTADJUSTMENTTYPEID)
            {
                Context ctx = getContext();
                AdjustmentType type = HomeSupportHelper.get(ctx).findBean(ctx, AdjustmentType.class,
                        new EQ(AdjustmentTypeXInfo.CODE, this.getContractAdjustmentTypeId()));
                Map map = type.getAdjustmentSpidInfo();
                AdjustmentInfo info = (AdjustmentInfo) map.get(Integer.valueOf(this.getSpid()));
                this.setAdjustmentGLCode(info.getGLCode());
                this.setTaxAuthority(info.getTaxAuthority());
            }
        }
        catch (Exception ex)
        {
            Context ctx = getContext();
            new MinorLogMsg(this, " Unable to load adjustment " + this.getContractAdjustmentTypeId(), ex).log(ctx);
        }
    }


    private synchronized void lazyLoadPricePlan()
    {
        try
        {
            if (this.getContractAdjustmentTypeId() != DEFAULT_CONTRACTPRICEPLAN)
            {
                Context ctx = getContext();
                PricePlan pp = HomeSupportHelper.get(ctx).findBean(ctx, PricePlan.class,
                        Long.valueOf(this.getContractPricePlan()));
                this.setSubscriberType(pp.getPricePlanType());
                this.setSubscriptionType(pp.getSubscriptionType());
            }
        }
        catch (Exception ex)
        {
            Context ctx = getContext();
            new MinorLogMsg(this, " Unable to load adjustment " + this.getContractAdjustmentTypeId(), ex).log(ctx);
        }
    }


    public String getAdjustmentGLCode()
    {
        if (adjustmentGLCode_ == AbstractSubscriptionContractTerm.DEFAULT_ADJUSTMENTGLCODE)
        {
            lazyLoad();
        }
        return super.getAdjustmentGLCode();
    }


    public long getSubscriptionType()
    {
        if (super.getSubscriptionType() == com.redknee.app.crm.bean.ui.AbstractSubscriptionContractTerm.DEFAULT_SUBSCRIPTIONTYPE)
        {
            lazyLoadPricePlan();
        }
        return super.getSubscriptionType();
    }


    public SubscriberTypeEnum getSubscriberType()
    {
        if (super.getSubscriptionType() == com.redknee.app.crm.bean.ui.AbstractSubscriptionContractTerm.DEFAULT_SUBSCRIPTIONTYPE)
        {
            lazyLoadPricePlan();
        }
        return super.getSubscriberType();
    }



    // from the BillingMessageAware interface
    @Override
    public MessageConfigurationSupport<ContractDescription, ContractDescriptionID> getConfigurationSupport(Context ctx)
    {
        MessageConfigurationSupport<ContractDescription, ContractDescriptionID> support = (MessageConfigurationSupport<ContractDescription, ContractDescriptionID>) ctx
                .get(BillingMessageHomePipelineFactory.getBillingMessageConfigurationKey(ContractDescription.class));
        return support;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Context getContext()
    {
        Context ctx = getContextInternal();
        if (ctx == null)
        {
            ctx = ContextLocator.locate();
        }
        return ctx;
    }


    public Context getContextInternal()
    {
        return context_;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setContext(final Context context)
    {
        context_ = context;
    }

    protected transient Context context_;


    @Override
    public List getBillingMessages()
    {
        synchronized (this)
        {
            if (contractDescriptions_ == null)
            {
                try
                {
                    BillingMessageAdapter.instance().adapt(getContext(), this);
                }
                catch (HomeException e)
                {
                    LogSupport.minor(getContext(), this, "Unable to load Billing Message for Contract [" + this.getId()
                            + "]. Error: " + e.getMessage(), e);
                }
            }
        }

        Collection<BillingMessage> existingRecords =  super.getContractDescriptions();

        ArrayList<ContractDescription> l = new ArrayList<ContractDescription>(existingRecords.size());
        for (BillingMessage record : existingRecords)
        {
            ContractDescription msg = new ContractDescription();
            msg.setActive(record.getActive());
            msg.setIdentifier(record.getIdentifier());
            msg.setLanguage(record.getLanguage());
            msg.setMessage(record.getMessage());
            msg.setSpid(record.getSpid());
            l.add(msg);
        }
        return l;  
    }


    public void setBillingMessages(List billingMessages)
    {
        this.setContractDescriptions(billingMessages);
    }


    public void saveBillingMessages(final Context ctx)
    {
        synchronized (this)
        {
            if (contractDescriptions_ != null)
            {
                LogSupport.debug(ctx, this, "FIX THIS: Wrong call flow. Should not be called for UI bean.",
                        new HomeException(""));
                try
                {
                    BillingMessageAdapter.instance().unAdapt(ctx, this);
                }
                catch (HomeException e)
                {
                    LogSupport.minor(ctx, this, "Unable to save Billing Message for Contract [" + this.getId()
                            + "]. Error: " + e.getMessage(), e);
                }
            }
        }
    }
    
    // the IdentifierAware interface
    public long getIdentifier()
    {
        return (long) getId();
    }

    public void setIdentifier(long ID) throws IllegalArgumentException
    {
        setId((int) ID);
    }
}
