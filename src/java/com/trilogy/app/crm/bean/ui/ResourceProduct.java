package com.trilogy.app.crm.bean.ui;


import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;

public class ResourceProduct extends AbstractResourceProduct {

	private static final long serialVersionUID = 1L;
	
	private transient Context ctx_;
	
	//public static String ROOT_PERMISSION = "app.crm.service";
	
	@Override
	public String getRootPermission() {
		return null;
	}
	
	@Override
	public Context getContext() {
		if(ctx_==null)
			ctx_ = ContextLocator.locate();
		return ctx_;
	}
	

	@Override
	public void setContext(Context context) {
		ctx_ = context;

	}

	/*@Override
	public String getPermission() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPermission(String permission) {
		// TODO Auto-generated method stub

	}

	*/

	/*@Override
	public int getSpid() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSpid(int spid) {
		// TODO Auto-generated method stub

	}*/

}
