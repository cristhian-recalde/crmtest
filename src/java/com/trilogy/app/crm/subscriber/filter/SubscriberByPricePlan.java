package com.trilogy.app.crm.subscriber.filter;

import java.sql.SQLException;

import com.trilogy.app.crm.bean.PricePlanVersionID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;

/**
 * @deprecated Use {@link #com.redknee.framework.xhome.elang.EQ}
 */
@Deprecated
public class SubscriberByPricePlan
	implements Predicate,XStatement
{
	private PricePlanVersionID pricePlan;
   
	public SubscriberByPricePlan(PricePlanVersionID pricePlan)
	{
      this.pricePlan = pricePlan;
	}

   
    public boolean f(Context ctx, Object obj)
    {
        Subscriber sub = (Subscriber) obj;
        return getPricePlan().getId() == sub.getPricePlan();
    }


    public String createStatement(Context ctx)
    {
        return "pricePlan = " + getPricePlan().getId();
    }


    /**
     * Set a PreparedStatement with the supplied Object.
     */
    public void set(Context ctx, XPreparedStatement ps) throws SQLException
    {
    }


    /**
     * @return Returns the pricePlan.
     */
	public PricePlanVersionID getPricePlan()
	{
		return pricePlan;
	}
	
	/**
	 * @param pricePlan The pricePlan to set.
	 */
	public void setPricePlan(PricePlanVersionID pricePlan)
	{
		this.pricePlan = pricePlan;
	}
}
