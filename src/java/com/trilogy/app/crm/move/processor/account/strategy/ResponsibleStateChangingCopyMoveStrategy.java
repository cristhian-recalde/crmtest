package com.trilogy.app.crm.move.processor.account.strategy;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.move.processor.account.ResponsibleAccountMoveProcessor;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.AccountMoveRequestXInfo;
import com.trilogy.app.crm.support.EnumStateSupportHelper;


public class ResponsibleStateChangingCopyMoveStrategy<AMR extends AccountMoveRequest> extends CopyMoveStrategyProxy<AMR>
{
    public ResponsibleStateChangingCopyMoveStrategy(CopyMoveStrategy<AMR> delegate)
    {
        super(delegate);
    }
    
    /**
     * @{inheritDoc}
     */
    @Override
    public void initialize(Context ctx, AMR request)
    {
        
        Account oldAccount = request.getOriginalAccount(ctx);
        Account account = request.getNewAccount(ctx);

        if (account != null && oldAccount != null)
        {
            account.setResponsible(request.getNewResponsible());    

            new DebugLogMsg(this, "Account " + account.getBAN()
                    + " updated with responsible state " + account.getResponsible(), null).log(ctx);

        }
        
         super.initialize(ctx, request);
    }
    
    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, AMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        Account account = request.getOriginalAccount(ctx);

        Account newParentAccount = request.getNewParentAccount(ctx);
    
        if (newParentAccount != null)
        {
            if (!request.getNewResponsible())
            {
                validateNonResponsibleAccount(ctx, request, newParentAccount, cise);
            }
            else
            {
                ResponsibleAccountMoveProcessor.validateResponsibleAccount(ctx, account, request, newParentAccount, cise);
            }
        }
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx, request);
    }
    
    private void validateNonResponsibleAccount(Context ctx, AMR request, Account newParentAccount, CompoundIllegalStateException cise)
    {
        try
        {
            Account newResponsibleParentAccount = newParentAccount.getResponsibleParentAccount(ctx);
            if (newResponsibleParentAccount != null)
            {
                if (!EnumStateSupportHelper.get(ctx).stateEquals(newResponsibleParentAccount, AccountStateEnum.ACTIVE))
                {
                    throw new IllegalPropertyArgumentException(AccountMoveRequestXInfo.NEW_PARENT_BAN,
                            "Responsible parent account for account " + newParentAccount.getBAN()
                            + " is in an invalid state (" + newResponsibleParentAccount.getState()
                            + ").  " + "Not allowed to move account " + request.getExistingBAN()
                            + " under parent account " + request.getNewParentBAN());
                }
            }
            else
            {
                throw new IllegalPropertyArgumentException(AccountMoveRequestXInfo.NEW_PARENT_BAN,
                        "Responsible parent account not found for account " + newParentAccount.getBAN() + ".");
            }
        }
        catch (HomeException e)
        {
            throw new IllegalPropertyArgumentException(AccountMoveRequestXInfo.NEW_PARENT_BAN,
                    "Error retrieving responsible parent account for " + newParentAccount.getBAN() + ".");
        }
    }

    
}
