/*
 * Created on Dec 15, 2003
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.trilogy.app.crm.bas.tps.pipe;

import com.trilogy.app.crm.bas.tps.TPSRecord;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.ReconciliationStateEnum;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.InfoLogMsg;


/**
 * TPS record duplication checking, if duplicate found, then the processing will be
 * terminated.
 * 
 * @author Larry Xia
 */
public class HummingBirdDuplicationCheckingAgent extends PipelineAgent
{


	public HummingBirdDuplicationCheckingAgent(ContextAgent delegate)
    {
        super(delegate);
    }


    /**
     * @param ctx
     *            A context
     * @exception AgentException
     *                thrown if one of the services fails to initialize
     */
    public void execute(Context ctx) throws AgentException
    {
        TPSRecord tps = (TPSRecord) ctx.get(TPSRecord.class);
        Subscriber subs = (Subscriber) ctx.get(Subscriber.class);
        if (tps.getTransactionNum() == null || tps.getTransactionNum().trim().length() == 0)
        {
            ERLogger.genAccountAdjustmentER(ctx, tps, TPSPipeConstant.FAIL_DUPLICATE_TRANSACTION);
            fail(ctx, this, "Invalid exteranl transaction ID, operation stopped", null,
                    TPSPipeConstant.FAIL_DUPLICATE_TRANSACTION);
        }
        else
        {
            Home home = (Home) ctx.get(TransactionHome.class);
            try
            {
            	AdjustmentType type = (AdjustmentType) ctx.get(AdjustmentType.class);
            	And filter = new And();
            	filter.add(new EQ(TransactionXInfo.EXT_TRANSACTION_ID, tps.getTransactionNum()));
            	
            	//To play safe if the type (or reversal adjustment) is not on the context then the search will be based on the amount
            	if (type != null) 
            	{
            		filter.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, Integer.valueOf(type.getCode())));
            	}
            	else
            	{
            		filter.add(new EQ(TransactionXInfo.AMOUNT, Integer.valueOf(tps.getAmount())));
            	}
            	
                Object obj = home.find(ctx, filter);
                if (obj == null)
                {
                    pass(ctx, this, "no duplicate transaction found");
                }
                else
                {

                    Transaction tr = (Transaction) obj;
                    if (tr.getReconciliationState() == ReconciliationStateEnum.Provisional)
                    {
                        new InfoLogMsg(this, "Reconsiliation successful, subId: " + tr.getSubscriberID()
                                + " | External Transaction Number: " + tr.getExtTransactionId(), null).log(ctx);
                        home.store(ctx, tr);
                        // Set the Reconsiliation ER.
                        ERLogger.generateTPSReconciliationER(ctx, tps, subs);
                        pass(ctx, this, "Reconsiliation successful.");
                    }
                    else
                    {
                        ERLogger.genAccountAdjustmentER(ctx, tps, TPSPipeConstant.FAIL_DUPLICATE_TRANSACTION);
                        fail(ctx, this, "Duplication transaction found, operation stopped", null,
                                TPSPipeConstant.FAIL_DUPLICATE_TRANSACTION);
                    }
                }
            }
            catch (HomeException e)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Transaction home error", e).log(ctx);
                }
                pass(ctx, this, "no duplicate transaction found");
            }
        }
    }

    /**
	 * 
	 */
	private static final long serialVersionUID = -2214897375828044095L;

}