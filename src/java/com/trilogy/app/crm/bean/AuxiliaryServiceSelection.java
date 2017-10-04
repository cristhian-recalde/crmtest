package com.trilogy.app.crm.bean;

import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;


public class AuxiliaryServiceSelection extends
		AbstractAuxiliaryServiceSelection  
implements HiddenOnDemandable, ReadOnlyOnDemandable
{

	
	public Collection getHiddenFields()
	{
		return hiddenFields; 
	}
	
	public Collection getReadOnlyFields()
	{
		return readOnlyFields;
	}
	
	
    public AuxiliaryService getAuxiliarService( Context ctx)
    {
        
    	if (service == null)
    	{	
    		Home auxSvcHome = (Home)ctx.get(AuxiliaryServiceHome.class);

    		try
    		{
    			service = (AuxiliaryService)auxSvcHome.find(ctx, Long.valueOf(this.getSelectionIdentifier()));
    		}
    		catch(HomeException he)
    		{
    			new MajorLogMsg(this,
            		"Can not find the auxiliary service with servcie id:"+ this.getSelectionIdentifier(), null).log(ctx);
    		}
    	}
    		
        return service;
    }


	
	Collection hiddenFields = new ArrayList();
	Collection readOnlyFields = new ArrayList(); 
	
	AuxiliaryService service; 
	
}
