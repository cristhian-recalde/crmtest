package com.trilogy.app.crm.discount;


/**
 * @author harsh.murumkar
 * @since
 */

import com.trilogy.app.crm.agent.CronConstant;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;

/**
 * Abstract class Discount Class Assignment.
 */
public abstract class AbstractDiscountClassAssignement implements Visitor{
    
      /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    /**
     *  Default constructor
     */
    public AbstractDiscountClassAssignement()
    {
        
    }
    
    /**
     * Returns the process name.
     *
     * @return
     */
    public String getProcessName(){
        return CronConstant.DISCOUNT_ASSIGNMENT_TASK_NAME;
    }
    
    abstract public void visit(Context ctx,Object obj) throws AgentException, AbortVisitException;
}
