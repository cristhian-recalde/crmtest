package com.trilogy.app.crm.api.rmi.agent;

import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.visitor.Visitor;

public class ConfigShareTask {

	 
	public Home getHome() {
		return home_;
	}
	public void setHome(Home home) {
		this.home_ = home;
	}
	public Visitor getVisitor() {
		return visitor_;
	}
	public void setVisitor(Visitor visitor) {
		this.visitor_ = visitor;
	}
	public Object getCondition() {
		return condition_;
	}
	public void setCondition(Object condition) {
		this.condition_ = condition;
	}
	private Home home_;
	private Visitor visitor_;
	private Object condition_;

}
