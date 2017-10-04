/*
 * Created on Jul 8, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.trilogy.app.crm.sequenceId;

import com.trilogy.app.crm.bean.IdentifierSequence;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xlog.log.EntryLogMsg;

/**
 * @author dzhang
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class RollOverNotifier extends ContextAwareSupport implements RollOverNotofiable
{
	
	public RollOverNotifier(Context ctx)
	{
		setContext(ctx);
	}
	
	public void notify(IdentifierSequence sequence)
	{
		new EntryLogMsg(10949,this,"AppCrmIdentifierSequence","",new String[]{sequence.getIdentifier()},null).log(getContext());
	}

}
