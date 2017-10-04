package com.trilogy.app.crm.bas.tps.pipe;

import java.util.Date;

import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.Visitor;

public class LastMsisdnHolderVisitor implements Visitor
{
	   public void visit(final Context context, final Object obj)
	   {
	       final MsisdnMgmtHistory number = (MsisdnMgmtHistory) obj;
	       if ( lastTimestamp == null)
	       {
	    	   this.lastTimestamp = number.getTimestamp(); 
	       } else if ( this.lastTimestamp.before(number.getTimestamp()))
	       {
	    	   this.lastTimestamp = number.getTimestamp(); 
	       }
	   }
	   
	   
	   public Date getLastTimestamp() 
	   {
		return lastTimestamp;
	   }


	Date lastTimestamp; 
}