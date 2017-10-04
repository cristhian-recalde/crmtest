package com.trilogy.app.crm.subscriber.agent;

import java.util.Arrays;
import java.util.Collection;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.pipe.PipelineAgent;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.subscriber.state.AbstractSubscriberState;
import com.trilogy.app.crm.support.EnumStateSupportHelper;


public class EnforceAccountState extends PipelineAgent
{
    public EnforceAccountState()
    {
        super();
    }

    public EnforceAccountState(ContextAgent delegate)
    {
        super(delegate);
    }

    /**
     * @see com.redknee.framework.xhome.context.ContextAgentProxy#execute(com.redknee.framework.xhome.context.Context)
     */
    @Override
    public void execute(Context ctx) throws AgentException
    {
            Subscriber sub=(Subscriber) require(ctx,this,Subscriber.class);
            
            try
            {
                Account account = (Account) ctx.get(Account.class);
                if (account==null || !account.getBAN().equals(sub.getBAN()))
                {
                    account = sub.getAccount(ctx);
                }
                if (EnumStateSupportHelper.get(ctx).isOneOfStates(account, dunnedStates) && SubscriberStateEnum.ACTIVE.equals(sub.getState()))
                {
                    sub.setState(AbstractSubscriberState.translateAccountState(account));
                }
                
            }
            catch (HomeException e)
            {
                String message = "Unable to verify account state: " + e.getMessage();
                if(LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, message, e).log(ctx);
                }
                
                throw new AgentException(message, e);
            }
        
        pass(ctx,this);
    }
    final static Collection<AccountStateEnum> dunnedStates = Arrays.asList(
            AccountStateEnum.IN_ARREARS,
            AccountStateEnum.NON_PAYMENT_WARN,
            AccountStateEnum.IN_COLLECTION,
            AccountStateEnum.PROMISE_TO_PAY,
            AccountStateEnum.NON_PAYMENT_SUSPENDED);
}