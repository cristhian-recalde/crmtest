package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.ui.PriceTemplate;
import com.trilogy.app.crm.bean.ui.PriceTemplateXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.SelectSearchAgent;
import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.app.crm.bean.ui.PriceTemplateSearchWebControl;
import com.trilogy.app.crm.bean.ui.PriceTemplateSearchXInfo;

/**
 * 
 * @author AChatterjee
 *
 */
public class PriceTemplateSearchBorder extends SearchBorder {

	public PriceTemplateSearchBorder(Context ctx) {
		super(ctx, PriceTemplate.class, new PriceTemplateSearchWebControl());

		addAgent(new SelectSearchAgent(PriceTemplateXInfo.ID,
				PriceTemplateSearchXInfo.ID, false).addIgnore(Long.valueOf(-1)));

		SelectSearchAgent spidAgent = new SelectSearchAgent(
				PriceTemplateSearchXInfo.SPID, PriceTemplateSearchXInfo.SPID,
				false);
		spidAgent.addIgnore(Integer.valueOf(-1));
		addAgent(new WildcardSelectSearchAgent(PriceTemplateXInfo.NAME,
				PriceTemplateSearchXInfo.NAME, true));

	}

}
