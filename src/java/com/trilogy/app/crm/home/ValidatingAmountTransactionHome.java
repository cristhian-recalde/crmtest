package com.trilogy.app.crm.home;

import java.security.Permission;
import java.security.Principal;

import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.AuthSupport;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;

public class ValidatingAmountTransactionHome
	extends HomeProxy
	implements ContextAware
{
    public final static Permission PAYMENT_REVERSAL_PERMISSION =
        new SimplePermission("payment.reversal");

	public ValidatingAmountTransactionHome(Context ctx, Home delegate)
	{
		super(delegate);
		setContext(ctx);
	}

	@Override
    public Object create(Context ctx, Object obj)
		throws HomeException, HomeInternalException
	{
		Transaction transaction = (Transaction)obj;


		validateAmount(ctx, transaction);

		if ( this.getContext().has( Principal.class)){

            if (CoreTransactionSupportHelper.get(ctx).isPaymentReversal(getContext(), transaction))
            {
                //
                // Only designated agents are permitted to enter Payment Reversal
                // adjustments.
                //
                if (!AuthSupport.hasPermission(ctx, PAYMENT_REVERSAL_PERMISSION))
                {
                    throw new HomeException("No permission to reverse payments.");
                }
                //
                // Only debit (positive) amounts are accepted.
                //
                if (transaction.getAmount() < 0)
                {
                    throw new HomeException("For reversing payments, the amount should be positive to indicate a debit.");
                }
            }
            else if (CoreTransactionSupportHelper.get(ctx).isPayment(getContext(), transaction)
                && !CoreTransactionSupportHelper.get(ctx).isDeposit(getContext(), transaction))
			{
				if (transaction.getAmount() > 0)
				{
                    throw new HomeException("For payments, the amount should be negative to indicate a credit.");
				}
			}
            else if (CoreTransactionSupportHelper.get(ctx).isBalanceTransfer(getContext(), transaction))
            {
                if (transaction.getAmount() > 0)
                {
                    throw new HomeException("For balance transfers, the amount should be negative.");
                }
            }
		}

		return super.create(ctx,transaction);
	}

	/**
	 * Validates and sets the correct amount
	 * @param transaction
	 * @throws HomeException
	 */
	private void validateAmount(Context ctx, Transaction transaction)
	throws HomeException
	{
        AdjustmentType adjType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,transaction.getAdjustmentType());

        if (adjType != null)
        {
            AdjustmentTypeActionEnum action = adjType.getAction();

            if ((action == AdjustmentTypeActionEnum.CREDIT) ||
            	(action == AdjustmentTypeActionEnum.DEBIT))
            {
            	transaction.setAction(action);
            }
         }

		if (transaction.getAction() == AdjustmentTypeActionEnum.CREDIT )
		{
			if (transaction.getAmount() >= 0)
            {
				transaction.setAmount(transaction.getAmount()*-1);
            }
			else
            {
				throw new HomeException("The Amount should be positive");
            }

		}

		if (transaction.getAction() == AdjustmentTypeActionEnum.DEBIT && transaction.getAmount() < 0 )
		{
			throw new HomeException("The Amount should be positive");
		}

	}

	@Override
    public void setContext(Context ctx)
	{
		ctx_ = ctx;
	}

	@Override
    public Context getContext()
	{
		return ctx_;
	}

	private Context ctx_;
}
