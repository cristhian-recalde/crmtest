package com.trilogy.app.crm.bean.ui;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.KeyWebControlOptionalValue;



public class SubGLCodeVersionIDWebControl extends SubGLCodeVersionNKeyWebControl{

	
	public static final KeyWebControlOptionalValue DEFAULT=new KeyWebControlOptionalValue("--", "-1");

	public SubGLCodeVersionIDWebControl()
	{
		super();
	}

	public SubGLCodeVersionIDWebControl(boolean autoPreview)
	{
		super(autoPreview);
	}

	
	@Override
	public Home getHome(Context ctx) {
		// TODO Auto-generated method stub
		Object bean = ctx.get(AbstractWebControl.BEAN);
        if(bean instanceof AdjustmentTypeNVersion)
        {
              String adjCode = ((AdjustmentTypeNVersion)bean).getSubGLCode(); 
              And and = new And();
              and.add(new EQ(SubGLCodeVersionNXInfo.ID,adjCode));
              return super.getHome(ctx).where(ctx, and);
        }
        return super.getHome(ctx);
	}
}
