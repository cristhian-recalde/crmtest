package com.trilogy.app.crm.bean.ui;

import java.util.Collection;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * 
 * @author AChatterjee
 * 
 * This adapter removes all the SubGLCode versions when a particular SubGLCode is removed. 
 *
 */
public class RemoveSubGLCodeVersionsHome extends HomeProxy {
	
	public RemoveSubGLCodeVersionsHome(Context ctx, Home home) {
		super(ctx, home);
	}

	private static final long serialVersionUID = 1L;
	
	@Override
	public void remove(Context ctx, Object obj) throws HomeException, HomeInternalException {
		
		SubGLCodeN subGLCode = (SubGLCodeN) obj;
		String id = subGLCode.getId();
		Home home = (Home) ctx.get(SubGLCodeVersionNHome.class);
		final EQ filter = new EQ(SubGLCodeVersionNXInfo.ID, id);
		Collection<SubGLCodeVersionN> subGLCodeVersions  = home.select(ctx, filter);
		for(SubGLCodeVersionN subGLCodeVersion : subGLCodeVersions){
			home.remove(ctx, subGLCodeVersion);
			//home.removeAll(subGLCodeVersions);
		}
		super.remove(ctx, obj);
	}

}
