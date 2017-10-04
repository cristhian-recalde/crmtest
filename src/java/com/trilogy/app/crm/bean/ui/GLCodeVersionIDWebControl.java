package com.trilogy.app.crm.bean.ui;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

public class GLCodeVersionIDWebControl extends com.redknee.app.crm.bean.GLCodeVersionNKeyWebControl{
	
	public GLCodeVersionIDWebControl()
	{
		super();
	}

	public GLCodeVersionIDWebControl(boolean autoPreview)
	{
		super(autoPreview);
	}
	
	@Override
	public Home getHome(Context ctx) {
		// TODO Auto-generated method stub
		Object bean = ctx.get(AbstractWebControl.BEAN);
        if(bean instanceof AdjustmentTypeNVersion)
        {
              String adjCode = ((AdjustmentTypeNVersion)bean).getGLCode(); 
              And and = new And();
              and.add(new EQ(com.redknee.app.crm.bean.GLCodeVersionNXInfo.GL_CODE,adjCode));
              return super.getHome(ctx).where(ctx, and);
        }
        return super.getHome(ctx);
	}
}
