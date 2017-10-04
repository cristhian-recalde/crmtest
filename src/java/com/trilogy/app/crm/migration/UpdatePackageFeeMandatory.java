package com.trilogy.app.crm.migration;

import java.util.Iterator;
import java.util.Map;

import com.trilogy.app.crm.bas.directDebit.EnhancedParallVisitor;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionXDBHome;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.ParallelVisitor;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
/***
 * This class is for migration work, for TT6040632941, to update all 
 * package Fees in price plan version to "mandatory"
 * @author jchen
 *
 */
public class UpdatePackageFeeMandatory  
{
	/**
	 * 
	 * @param context
	 * @param updateDb, if false, it will not doing db update, only reports
	 * @throws HomeException
	 */
	public void update(Context context, final boolean updateDb) throws HomeException
	{
		final Home home = (Home)context.get(PricePlanVersionHome.class);
		final Home xdbHome = new PricePlanVersionXDBHome(context, "PRICEPLANVERSION");
        EnhancedParallVisitor pv = null;
        try{
        pv =new EnhancedParallVisitor(5,
				new Visitor()
				{
					/**
					 * 
					 * update each price plan version object
					 */
					public void visit(Context context, Object obj) throws AgentException
					{
						Context subCtx = context.createSubContext();
						boolean needsSave = false;
						
						PricePlanVersion ppv = (PricePlanVersion)obj;
						ServicePackageVersion spv = ppv.getServicePackageVersion();
						if (spv != null)
						{
							Map fees = spv.getPackageFees();
							if (fees != null)
							{
								Iterator iter = fees.entrySet().iterator();
								while (iter.hasNext())
								{
									Map.Entry entry = (Map.Entry) iter.next();
									Object key = entry.getKey();
									ServicePackageFee pf = (ServicePackageFee) entry.getValue();
									if (!pf.isMandatory())
									{
										pf.setMandatory(true);
										fees.put(key, pf);
										
										needsSave = true;
									}
								}
								if (needsSave)
								{
									spv.setPackageFees(fees);
									Exception exp = null;
									if (updateDb)
									{
										try
										{
											xdbHome.store(subCtx, ppv);
										}
										catch(HomeException e)
										{
											exp = e;
										}
									}
									print(subCtx, "" + (updateDb? "Updating" : "Checking") + " PackgeFee price plan=" + ppv.getId() + ",version=" + ppv.getVersion(), exp);
								}
							}
						}
					}
				});
				
				home.forEach(context,pv);
    }
    finally
    {
	        	 try
	             {
	        		pv.shutdown(EnhancedParallVisitor.TIME_OUT_FOR_SHUTTING_DOWN);
	             }
	             catch (final Exception e)
	             {
	                 LogSupport.major(context, this, "Exception caught during wait for completion of all UpdatePackageFeeMandatory Threads", e);
	             }
    }
	}
	
	static void print(Context ctx, String msg, Exception e)
	{
		System.out.println(msg  + ", err=" + e);
		new InfoLogMsg("UpdatePackageFeeMandatory", msg, e).log(ctx);
	}
	
	/**
	 * 
	 * @param ctx
	 */
	public void execute(Context ctx)
	{
		try
		{
			print(ctx, "Before update", null);
			update(ctx, false);
			print(ctx, "Updating.....", null);
			update(ctx, true);	
			print(ctx, "Upate done, recheck", null);
			update(ctx, false); 
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

//new com.redknee.app.crm.migration.UpdatePackageFeeMandatory().execute(ctx);
