package com.trilogy.app.crm.bas.recharge;


/**
 * @author Abhishek Sathe
 * @since
 */

import com.trilogy.app.crm.agent.CronConstant;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;

/**
 * Abstract class Discount Event Transaction.
 */
public abstract class AbstractDiscountEventTransaction implements Visitor{
    
      /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    /**
     *  Default constructor
     */
    public AbstractDiscountEventTransaction()
    {
        
    }
    
    /**
     * Returns the process name.
     *
     * @return
     */
    public String getProcessName(){
        return CronConstant.FINAL_DISCOUNT_EVENT_AGENT_NAME;
    }
    
    abstract public void visit(Context ctx,Object obj) throws AgentException, AbortVisitException;
}
