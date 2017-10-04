/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.dunning.visitor;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;

/**
 * Abstract class responsible to process accounts during DunningPolicy Assignment.
 *
 * @author Sapan Modi
 * 
 */
public abstract class AbstractAccountDunningPolicyAssignement implements Visitor{
	
	  /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    /**
     *  Default constructor
     */
    public AbstractAccountDunningPolicyAssignement()
    {
    	
    }
    
    /**
     * Returns the process name.
     *
     * @return
     */
    public String getProcessName(){
    	return "Account Dunning Policy Assignment";
    }
    
    abstract public void visit(Context ctx,Object obj) throws AgentException, AbortVisitException;
   
   


}
