package com.trilogy.app.crm.home.transaction;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.trilogy.app.crm.bean.AbstractTransaction;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.sequenceId.OnDemandSequenceManager;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Used set unified receipt number for the standard payments
 * 
 * @author ksivasubramaniam
 * 
 */
public class TransactionUnifiedReceiptSettingHome extends HomeProxy
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public TransactionUnifiedReceiptSettingHome(Home home)
    {
        super(home);
    }


    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        Transaction trans = (Transaction) obj;
        if (trans.getUnifiedReceiptID() == AbstractTransaction.DEFAULT_UNIFIEDRECEIPTID)
        {
            if (CoreTransactionSupportHelper.get(ctx).isStandardPayment(ctx, trans))
            {
                Long receiptNum = OnDemandSequenceManager.acquireNextIdentifier(ctx,
                        OnDemandSequenceManager.RECEIPT_SEQUENCE_KEY, OnDemandSequenceManager.SINGLE_BLOCK_SIZE);
                if (receiptNum != null)
                {
                    String[] params =
                        {};
                    MessageMgr mmgr = new MessageMgr(ctx, this);
                    String prefix = mmgr.get(TRANSACTION_PAYMENT_UNIFIED_RECEIPT_ID_PREFIX, "RCT", params);
                    String suffixFormat = mmgr.get(TRANSACTION_PAYMENT_UNIFIED_RECEIPT_ID_SUFFIX, "yy", params);
                    Date curDate = new Date();
                    SimpleDateFormat format = new SimpleDateFormat(suffixFormat);
                    String curDateFormated = format.format(curDate);
        
                    String identifier = prefix + "/" + String.format("%08d", receiptNum.longValue()) + "/" + curDateFormated;
                    trans.setUnifiedReceiptID(identifier);
                    
                }
                else
                {
                    new MinorLogMsg(this, " Unified Receipt sequence is not configured.  Please configure it", null)
                            .log(ctx);
                }
            }
        }
        return super.create(ctx, trans);
    }

    public final static String TRANSACTION_PAYMENT_UNIFIED_RECEIPT_ID_PREFIX = "TransactionPaymentUnifiedReceiptIdPrefix";
    public final static String TRANSACTION_PAYMENT_UNIFIED_RECEIPT_ID_SUFFIX = "TransactionPaymentUnifiedReceiptIdSuffix";
}
