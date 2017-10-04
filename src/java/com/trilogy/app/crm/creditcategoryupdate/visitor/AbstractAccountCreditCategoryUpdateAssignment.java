package com.trilogy.app.crm.creditcategoryupdate.visitor;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;

public abstract class AbstractAccountCreditCategoryUpdateAssignment  implements Visitor{
	
	  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  /**
   *  Default constructor
   */
  public AbstractAccountCreditCategoryUpdateAssignment()
  {
  	
  }
  
  /**
   * Returns the process name.
   *
   * @return
   */
  public String getProcessName(){
  	return "Account Credit Category Update Assignment";
  }
  
  abstract public void visit(Context ctx,Object obj) throws AgentException, AbortVisitException;
 
 


}
