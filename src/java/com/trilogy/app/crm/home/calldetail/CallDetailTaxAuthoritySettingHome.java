package com.trilogy.app.crm.home.calldetail;

import com.trilogy.app.crm.bean.calldetail.AbstractCallDetail;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.support.CallDetailSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

public class CallDetailTaxAuthoritySettingHome extends HomeProxy 
{
	
	   /**
	    * Constructor. 
	    * @param ctx the context 
	    * @param delegate the next home in the chain
	    */
	   public CallDetailTaxAuthoritySettingHome(Context ctx, Home delegate)
	   {
	      super(ctx, delegate);
	   }


	/**
	 * @see com.redknee.framework.xhome.home.HomeProxy#create(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	@Override
    public Object create(Context ctx, Object obj) throws HomeException
	{
		CallDetail cdr = (CallDetail) obj;
		
		setTaxAuthorityId(ctx, cdr);
		
		return super.create(ctx, obj);
	}

	
	private void setTaxAuthorityId(Context ctx, CallDetail cdr) 
	throws HomeException
	{
		if (cdr == null)
		{
			LogSupport.info(ctx, this, "Call detail record is null");
			return;
		}
		int spidTaxAuthority = -1; 
		
		if ( CallDetailSupportHelper.get(ctx).isRoamingCDR(ctx, cdr))
		{
			spidTaxAuthority = SpidSupport.getDefaultRoamingTaxAuthority(ctx, cdr.getSpid());			
		} else {
			spidTaxAuthority = SpidSupport.getDefaultTaxAuthority(ctx, cdr.getSpid());
		}
		
		if (spidTaxAuthority >= 0)
		{
		    String msg = "Setting cdr for subscriber " + cdr.getSubscriberID() + " with default value tax authority " + spidTaxAuthority;
		    CallDetailSupportHelper.get(ctx).debugMsg(CallDetailTaxAuthoritySettingHome.class,cdr,msg,ctx);
		    if (cdr.getTaxAuthority1() == AbstractCallDetail.DEFAULT_TAXAUTHORITY1)
		    {
		        cdr.setTaxAuthority1(spidTaxAuthority);
		    }
			cdr.setTaxAuthority2(spidTaxAuthority);
		} else {
			LogSupport.minor(ctx, this, "Can not find default tax authority for subscriber " + cdr.getSubscriberID());
			
		}
		
	}


	/**
	 * 
	 */
	private static final long serialVersionUID = -4301688193461687252L;

}
