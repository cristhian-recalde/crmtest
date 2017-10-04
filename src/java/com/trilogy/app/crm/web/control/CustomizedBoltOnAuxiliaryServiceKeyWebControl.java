package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceKeyWebControl;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * 
 * @author vijay.gote
 * @see 9.9
 * This calss is reponsible to get the list auxliary service of type BOLT ON.
 */


public class CustomizedBoltOnAuxiliaryServiceKeyWebControl extends AuxiliaryServiceKeyWebControl
{

    public CustomizedBoltOnAuxiliaryServiceKeyWebControl(int listSize, boolean autoPreview, Object optionalValue)
    {
        super(listSize, autoPreview, optionalValue);
    }

    public void toWeb(Context ctx, PrintWriter out, String name, final Object obj)
    {   
        Context subCtx = filterBoltOnAuxServices(ctx);
        super.toWeb(subCtx, out, name, obj);       
    }
    
    @Override
    public Object fromWeb(Context ctx, ServletRequest req, String name)
    {
    	Context subCtx = ctx.createSubContext();
    	return super.fromWeb(subCtx, req, name);    	
    }

 
    public Context filterBoltOnAuxServices(final Context ctx)
    {
        Context subContext = ctx.createSubContext();
        Predicate filter = new EQ(AuxiliaryServiceXInfo.TYPE, AuxiliaryServiceTypeEnum.URCS_Promotion);
        final Home originalHome = (Home) subContext.get(AuxiliaryServiceHome.class);
        final Home newHome = new HomeProxy(subContext, originalHome).where(subContext, filter);
        subContext.put(AuxiliaryServiceHome.class, newHome);            
        return subContext;          
    }

}