package com.trilogy.app.crm.home.account;

import java.util.Collection;

import com.trilogy.app.crm.bean.AccountReasonCodeMapping;
import com.trilogy.app.crm.bean.AccountReasonCodeMappingHome;
import com.trilogy.app.crm.bean.AccountReasonCodeMappingXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

public class AccountReasonCodeMappingStoreValidator implements Validator{

	@Override
	public void validate(Context ctx, Object obj) throws IllegalStateException
	{

		AccountReasonCodeMapping asrbean = (AccountReasonCodeMapping)obj;
		if(asrbean!=null)
		{
			CompoundIllegalStateException cise = new CompoundIllegalStateException();
			try
			{
				
				Home home = (Home) ctx.get(AccountReasonCodeMappingHome.class);
				And filter = new And();
				String message = "Default Has been selected already for the combination account state"+asrbean.getAccountState()+" & reason code"+ asrbean.getReasonCode() ;
				filter.add(new EQ(AccountReasonCodeMappingXInfo.ID,asrbean.getID()));
				filter.add(new EQ(AccountReasonCodeMappingXInfo.ACCOUNT_STATE,asrbean.getAccountState()));
				filter.add(new EQ(AccountReasonCodeMappingXInfo.REASON_CODE,asrbean.getReasonCode()));
				
			    Collection<AccountReasonCodeMapping> asrCollection = home.select(ctx, filter);
			    if(asrCollection.size()>0)
			    {
			    	for(AccountReasonCodeMapping arcmBean : asrCollection)
			    	{
			    		if(!(arcmBean.getID()==asrbean.getID()))
			    		{
			    			cise.thrown(new HomeException(message));;
			    		}else
			    		{
			    			break;
			    		}
			    		
			    	}
			    	
			    	
			    }
			    
			}catch (HomeException e) {
				
				e.printStackTrace();
			}
		cise.throwAll();
		}
		
	
		
	}

}
