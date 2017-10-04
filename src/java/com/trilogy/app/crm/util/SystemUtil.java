package com.trilogy.app.crm.util;

import com.trilogy.framework.xhome.context.Context;

public class SystemUtil {

	final public static boolean isFromGUI(Context ctx){
		return ctx.has(javax.servlet.http.HttpServletRequest.class);
	}
	
}
