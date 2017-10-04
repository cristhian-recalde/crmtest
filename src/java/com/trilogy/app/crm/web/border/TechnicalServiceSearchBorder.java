package com.trilogy.app.crm.web.border;


import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.app.crm.bean.ui.TechnicalServiceSearchXInfo;
import com.trilogy.app.crm.bean.ui.TechnicalServiceSearchWebControl;
//import com.trilogy.app.crm.bean.ui.ServiceSearchXInfo;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateXInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.SelectSearchAgent;
import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;


public class TechnicalServiceSearchBorder extends SearchBorder{

	public TechnicalServiceSearchBorder(Context ctx ) {
		super(ctx, TechnicalServiceTemplate.class, new TechnicalServiceSearchWebControl());

		addAgent( new SelectSearchAgent(TechnicalServiceTemplateXInfo.ID, TechnicalServiceSearchXInfo.ID,false).addIgnore(Long.valueOf(-1)));
		
		SelectSearchAgent spidAgent = new SelectSearchAgent(TechnicalServiceTemplateXInfo.SPID, TechnicalServiceSearchXInfo.SPID,false);
		spidAgent.addIgnore(Integer.valueOf(-1));
		addAgent(new WildcardSelectSearchAgent(TechnicalServiceTemplateXInfo.NAME,TechnicalServiceSearchXInfo.NAME,true));
		
	}

}
