package com.trilogy.app.crm.bas.roamingcharges;

import java.io.File;
import java.io.FilenameFilter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xlog.log.ERLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;

public class RoamingFileFilter implements FilenameFilter, ContextAware
{
	public RoamingFileFilter(Context ctx) {
		setContext(ctx);
	}
	
	public boolean accept(File dir, String name)
	{		
		File f = new File(dir+File.separator+name);
		if (f.isDirectory()){
			return false;
		}
		
		boolean b = false;
		if (name.length()>4) {
			b = name.endsWith(".cdr");
		}

		if (!b) {
			//1. invalid roaming file ER			
			new ERLogMsg( 777, 700, Constants.ER_INVALID_ROAMING_FILE_EVENT, 0, new String[] {name,Constants.RESULT_GEN_ERR}).log(getContext());

			// 2. invalid roaming file alarm
			new EntryLogMsg(10505L,this,this.toString(),null,null,null).log(getContext());
		}
		
		return b;
	}

	public Context getContext()
	{
		return ctx_;
	}

	public void setContext(Context arg0)
	{
		ctx_=arg0;		
	}
	
	private Context ctx_;
}
