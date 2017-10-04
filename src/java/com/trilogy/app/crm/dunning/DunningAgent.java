package com.trilogy.app.crm.dunning;

import java.util.List;

import com.trilogy.framework.xhome.context.ContextAgent;

public interface DunningAgent extends ContextAgent
{
	List<String> getFailedBANs();
}
