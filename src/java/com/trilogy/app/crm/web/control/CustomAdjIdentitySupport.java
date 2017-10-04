package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.beans.MissingRequireValueException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XCloneable;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.support.StringSeperator;
import com.trilogy.framework.xhome.support.URLSupport;
import com.trilogy.framework.xhome.visitor.FindVisitor;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.support.WebSupport;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.Error;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.ui.AdjustmentTypeNIdentitySupport;
import com.trilogy.app.crm.bean.ui.AdjustmentTypeN;
import com.trilogy.app.crm.bean.ui.AdjustmentTypeNID;

public class CustomAdjIdentitySupport extends AdjustmentTypeNIdentitySupport
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final IdentitySupport instance__ = new CustomAdjIdentitySupport();
	
	public CustomAdjIdentitySupport()
	{
		super();
	}
	
	public static IdentitySupport instance()
	   {
	      return instance__;
	   }
	
	/*public Object setID(Object bean, Object id)
	{
      LogSupport.debug(ContextLocator.locate(), this, "[setID] RecivecdId:{" + id + "}Recived Bean:{" + bean + "}");
	  
      ((Bank) bean).setBankId(((BankID) id).getBankId());
      
      ((Bank) bean).setSpid(((BankID) id).getSpid());
	      
	   return bean;
	 }*/
	
	public Object fromStringID(String id)
	{
	   LogSupport.debug(ContextLocator.locate(), this, "[fromStringID]Id:{" + id + "}");
	  // StringSeperator tok=new StringSeperator(id,SEPERATOR);
   	   //return new BankID((URLSupport.decode(tok.next())),Integer.parseInt((URLSupport.decode(tok.next()))));
	   StringSeperator tok=new StringSeperator(id,IdentitySupport.SEPERATOR);
	   String name = tok.next();
	   String spid = tok.next();
	   return (Object)(spid + "-" + name);
	}
	
	public String toStringID(Object id)
	{
		//LogSupport.debug(ContextLocator.locate(), this, "[toStringID] RecivecdId:{" + id + "}Class:{" + id.getClass() + "}", new Exception("For Check"));
		LogSupport.debug(ContextLocator.locate(), this, "[toStringID] RecivecdId:{" + id + "}Class:{" + id.getClass() + "}");
		if(id != null && !(id instanceof String))
		{
			StringBuffer buf=new StringBuffer();
			buf.append(URLSupport.encode(String.valueOf(((AdjustmentTypeNID) id).getCode())));
			buf.append(SEPERATOR);
			buf.append(URLSupport.encode(String.valueOf(((AdjustmentTypeNID) id).getSpid())));

			return buf.toString();
		}
		return "";
	 }
	
	public Object ID(Object bean)
	{
		LogSupport.debug(ContextLocator.locate(), this, "[ID] ReciveceId:{" + bean + "} {ClassType:" + bean.getClass() + "}");  
		AdjustmentTypeN adjType = null;
		if(bean instanceof String)
		{
			String beanStr = (String)bean;
			String adjTypeFields[] = beanStr.split("-");
			adjType = new AdjustmentTypeN();
			adjType.setSpid(Integer.parseInt(adjTypeFields[0]));
			adjType.setName(adjTypeFields[1]);
		}
		else
		{
			adjType = (AdjustmentTypeN)bean;
		}
		return new AdjustmentTypeNID(adjType.getCode(), adjType.getSpid());
	}
	
}
