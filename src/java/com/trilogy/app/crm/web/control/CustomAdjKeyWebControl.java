package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.ui.AdjustmentTypeN;
import com.trilogy.app.crm.bean.ui.AdjustmentTypeNKeyWebControl;
import com.trilogy.app.crm.bean.ui.AdjustmentTypeNVersion;
import com.trilogy.app.crm.bean.ui.AdjustmentTypeNXInfo;
import com.trilogy.framework.xhome.beans.MissingRequireValueException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XCloneable;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.visitor.FindVisitor;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.support.WebSupport;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.Error;
import com.trilogy.framework.xlog.log.LogSupport;

public class CustomAdjKeyWebControl extends AdjustmentTypeNKeyWebControl
{


	public CustomAdjKeyWebControl() 
	{
		super();
		// TODO Auto-generated constructor stub
	}

	public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
		LogSupport.debug(ctx, this, "[toWeb] RecivecdId:" + name + " Obj:" + obj); 
		int mode = ctx.getInt("MODE", DISPLAY_MODE);
	        Home home = getHome(ctx);
	        if (home == null)
	        {
	            out.print("<font color=\"red\">Developer Error: No Home supplied in Context under key '");
	            out.print(getHomeKey());
	            out.println("'.</font>");
	            return;
	        }
	        if (mode == DISPLAY_MODE)
	        {
	            try
	            {
	                Object input = obj;
	                if (SafetyUtil.safeEquals(obj, "null"))
	                {
	                    input = null;
	                }
	                if (isOptional_ && SafetyUtil.safeEquals(input, getOptionalValue(ctx)))
	                {
	                    out.print(getOptionalLabel(ctx));
	                    return;
	                }
	                Object bean = home.find(ctx, obj);
	                if (bean == null)
	                {
	                    getDelegate().toWeb(ctx, out, name, obj);
	                }
	                else
	                {
	                    String str = getDesc(ctx, bean);
	                    if (str != null && str.length() > 0)
	                    {
	                        if (!str.equals(""))
	                        {
	                            out.print(str);
	                        }
	                    }
	                    else
	                    {
	                        out.print("&nbsp;");
	                    }
	                }
	            }
	            catch (HomeException he)
	            {
	            	LogSupport.debug(ctx, this, "[toWeb] RecivecdId:" + name + " Obj:" + obj,he); 
	            	Error.internalError(ctx, out, "invalid key '" + obj + "'", he);
	            }
	            catch (NullPointerException ne)
	            {
	            	LogSupport.debug(ctx, this, "[toWeb] RecivecdId:" + name + " Obj:" + ne); 
	            	Error.internalError(ctx, out, "invalid key '" + obj + "'", ne);
	            }
	        }
	        // Edit Mode
	        else
	        {
	            try
	            {
	                final IdentitySupport id = getIdentitySupport();
	                boolean found = false;
	                Object first = null;
	                StringBuilder buf = new StringBuilder(512);
	                StringBuilder firstBuf = new StringBuilder(512);
	                StringBuilder currentBuf = buf;
	                int size = 0;
	                for (Iterator i = home.selectAll(ctx).iterator(); i.hasNext();)
	                {
	                    Object bean = i.next();
	                    String key = id.toStringID(id.ID(bean));
	                    if (!(getSelectFilter(ctx).f(ctx, bean) || key.equals(id.toStringID(obj))))
	                        continue;
	                    size++;
	                    if (first == null)
	                    {
	                        first = bean;
	                        currentBuf = firstBuf;
	                    }
	                    else
	                    {
	                        currentBuf = buf;
	                    }
	                    String str = getDesc(ctx, bean);
	                    if (!str.equals(""))
	                    {
	                        currentBuf.append("<option value=\"");
	                        currentBuf.append(key);
	                        currentBuf.append("\"");
	                        if (key.equals(id.toStringID(obj)))
	                        {
	                            currentBuf.append(" selected=\"selected\" ");
	                            found = true;
	                        }
	                        currentBuf.append(">");
	                        currentBuf.append(str);
	                        currentBuf.append("</option>");
	                    }
	                }
	                if (size == 0 && getNoSelectionMsg() != null)
	                {
	                    out.print(new MessageMgr(ctx, this).get(this.getClass().getName() + ".NoSelectionMsg." + name,
	                            getNoSelectionMsg()));
	                }
	                else
	                {
	                    out.print("<select id=\"");
	                    out.print(WebSupport.fieldToId(ctx, name));
	                    out.print("\" name=\"");
	                    out.print(name);
	                    out.print("\" size=\"");
	                    out.print(listSize_);
	                    out.println("\"");
	                    if (autoPreview_)
	                    {
	                        out.print(" onChange=\"autoPreview('");
	                        out.print(WebAgents.getDomain(ctx));
	                        out.print("', event)\"");
	                    }
	                    out.print(">");
	                    if (isOptional_)
	                    {
	                        out.print("<option value=\"");
	                        out.print(getOptionalOutputValue(ctx));
	                        out.print("\">");
	                        out.print(getOptionalLabel(ctx));
	                        out.println("</option>");
	                    }
	                    Object container = ctx.get(AbstractWebControl.BEAN);
	                    PropertyInfo pInfo = (PropertyInfo) ctx.get(AbstractWebControl.PROPERTY);
	                    if ((!found && container != null && pInfo != null)
	                            && ((size == 1 && (!getIsOptional() || getSelectWhenSize1())) || (size > 1 && !getIsOptional())))
	                    {
	                        Object bean = first;
	                        if (bean == null)
	                        {
	                            bean = ((FindVisitor) home.forEach(ctx, new FindVisitor())).getValue();
	                        }
	                        String key = id.toStringID(id.ID(bean));
	                        String str = getDesc(ctx, bean);
	                        if (!str.equals(""))
	                        {
	                            out.print("<option value=\"");
	                            out.print(key);
	                            out.print("\"");
	                            out.print(" selected=\"selected\" ");
	                            out.print(">");
	                            out.print(str);
	                            out.print("</option>");
	                        }
	                        AdjustmentTypeN adjType = (AdjustmentTypeN)bean;
	                        String idStr = adjType.getSpid() + "-" + adjType.getCode();
	                        LogSupport.debug(ctx, this, "[toWeb] CreatedId:" + idStr + "}{Class:" + idStr.getClass() + "}");
	                        //pInfo.set(container, id.ID(idStr));
	                        pInfo.set(container, idStr);
	                        found = true;
	                        if (size > 1)
	                        {
	                            out.print(buf.toString());
	                        }
	                    }
	                    else
	                    {
	                        out.print(firstBuf.toString());
	                        out.print(buf.toString());
	                    }
	                    out.println("</select>");
	                }
	                if (isAllowCustom())
	                {
	                    out.print(" or ");
	                    if (found)
	                    {
	                        getDelegate().toWeb(ctx, out, name + SEPERATOR + "custom", "");
	                    }
	                    else
	                    {
	                        getDelegate().toWeb(ctx, out, name + SEPERATOR + "custom", obj);
	                    }
	                }
	            }
	            catch (HomeException e)
	            {
	            	LogSupport.debug(ctx, this, "[toWeb] RecivecdId:" + name + " Obj:" + obj,e); 
	            	Error.internalError(ctx, out, "no home", e);
	            }
	        }
    }
	
	public Object fromWeb(Context ctx, ServletRequest req, String name) throws NullPointerException
	{
		
		LogSupport.debug(ctx, this, "[fromWeb1] {ServletRequest:" + req + "}{Name:" + name + "}");  
		IdentitySupport id = getIdentitySupport();
        String val = req.getParameter(name);
        String customVal = req.getParameter(name + SEPERATOR + "custom");

        // Check for "custom" value first
        if (isAllowCustom() &&
            customVal != null &&
            customVal.length() > 0)
        {
            return id.fromStringID(customVal);
        }
        else if (val != null)
        {
        	LogSupport.debug(ctx, this, "[fromWeb1] {val:" + val + "}");  
        	// "" denotes the optionalValue
            if (val.equals(getOptionalOutputValue(ctx)))
            {
                Object opt = getOptionalValue(ctx);
                LogSupport.debug(ctx, this, "[fromWeb1:getOptionalValue] {val:" + opt + "}"); 
                // if the optionalValue is a Bean then we need to clone it to prevent
                // it from being shared between beans causing unwated updates
                if (opt instanceof XCloneable)
                {
                    try
                    {
                        opt = ((XCloneable) opt).clone();
                    }
                    catch (CloneNotSupportedException cnse)
                    {
                    }
                }

                // test if optional value is of same type as
                // key web control
                if (id.isKey(opt))
                {
                	LogSupport.debug(ctx, this, "[fromWeb1:isKey] {val:" + opt + "}");  
                	return opt;
                }
                else
                {
                    // this generally occurs when a default selection
                    // such as -- Please Select -- is provided for a
                    // numeric field.
                    PropertyInfo pInfo = (PropertyInfo) ctx.get(AbstractWebControl.PROPERTY);
                    MissingRequireValueException mrvEx = new MissingRequireValueException(pInfo);
                    mrvEx.setMessageText("Selection required");
                    throw mrvEx;
                }
            }
            else
            {
                return id.fromStringID(val);
            }
        }
        else
        {
            throw new NullPointerException();
        }
	}


	public void fromWeb(Context ctx, Object obj, ServletRequest req, String name)
	{
		LogSupport.debug(ctx, this, "[fromWeb2] {ServletRequest:" + req + "}{Name:" + name + "}{Object:" + obj + "}"); 
		getDelegate().fromWeb(ctx, obj, req, name);
	}
	
	public IdentitySupport getIdentitySupport()
	{
	   return CustomAdjIdentitySupport.instance();
	}
	
	
	/*@Override
    public Home getHome(Context ctx)
    {
    	Object bean=ctx.get(AbstractWebControl.BEAN);
    	int spid=0;
    	if(bean instanceof AdjustmentTypeNVersion)
    	{
    		spid = ((AdjustmentTypeNVersion)bean).getSpid();
    		if(spid > 0)
    		{
    	        return super.getHome(ctx).where(ctx,
    	                new And().add(new EQ(AdjustmentTypeNXInfo.SPID, spid)));
    	    }
    	}
    	return super.getHome(ctx);
    }*/
	@Override
    public Home getHome(Context ctx)
    {
       Object bean=ctx.get(AbstractWebControl.BEAN);
       int spid = 0;
       if(bean instanceof AdjustmentTypeNVersion)
       {      
              spid = ((AdjustmentTypeNVersion)bean).getSpid();
                     
       }
       else if(bean instanceof com.redknee.app.crm.bean.UnifiedCatalogSpidConfig)
       {
              spid = ((com.redknee.app.crm.bean.UnifiedCatalogSpidConfig)bean).getSpid();
       }
       
       if(spid > 0)
              {
               return super.getHome(ctx).where(ctx,
                       new And().add(new EQ(AdjustmentTypeNXInfo.SPID, spid)));
           }
       return super.getHome(ctx);
    }

	
	/*public Object fromStringID(String id)
	{
	  StringSeperator tok=new StringSeperator(id,IdentitySupport.SEPERATOR);
	  //return new BankID((URLSupport.decode(tok.next())),Integer.parseInt((URLSupport.decode(tok.next()))));
	  String name = tok.next();
	  String spid = tok.next();
	  return (Object)(spid + "-" + name);
	  
	}*/
	

}
