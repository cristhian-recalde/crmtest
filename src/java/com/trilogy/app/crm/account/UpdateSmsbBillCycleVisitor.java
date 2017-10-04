package com.trilogy.app.crm.account;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.smsb.AppSmsbClientSupport;

/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 9.1
 */
public class UpdateSmsbBillCycleVisitor implements Visitor
{
    private final short billCycleDay_;


    /**
     * @param billCycleDay
     */
    public UpdateSmsbBillCycleVisitor(short billCycleDay)
    {
        this.billCycleDay_ = billCycleDay;
    }


    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
    {
        if (obj instanceof Subscriber)
        {
            Subscriber sub = (Subscriber) obj;
            AppSmsbClientSupport.updateSubscriberProfile(ctx, sub, billCycleDay_);
            new InfoLogMsg(this, "Updated SMSB's bill cycle date for subscription " + sub.getId() + " to " + billCycleDay_, null).log(ctx);
        }
    }
}