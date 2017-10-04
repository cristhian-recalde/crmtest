package com.trilogy.app.crm.dispute;



import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.XResultSet;

public class DisputeAmountVisitor implements Visitor 
{

	private static  String query = "select sum(disputedamount) as SUM from dispute where state <> 1 and subscriberid=";
	
	private long totalUnresolvedAmount = 0;
	
	
	public static String getQuery(String subscriberId)
	{
		StringBuilder builder = new StringBuilder(query);
		builder.append("'");
		builder.append(subscriberId);
		builder.append("'");
		
		return builder.toString();
	}
	



	public final long getTotalUnresolvedAmount() {
		return totalUnresolvedAmount;
	}



	public final void setTotalUnresolvedAmount(long totalUnresolvedAmount) {
		this.totalUnresolvedAmount = totalUnresolvedAmount;
	}



	@Override
	public void visit(Context ctx, Object obj) throws AgentException,
			AbortVisitException 
			
	{
		try
		{
		    XResultSet rs = (XResultSet)obj;
		    setTotalUnresolvedAmount(rs.getLong("SUM"));		
		}
		catch(Exception e)
		{
			throw new AgentException(e);
		}
	}

}
