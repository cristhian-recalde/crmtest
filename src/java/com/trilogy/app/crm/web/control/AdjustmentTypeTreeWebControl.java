// RESPONSED: 06/10/13 LZOU
// INSPECTED: 19/09/03 MLAM

package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeIdentitySupport;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xhome.webcontrol.tree.TreeWebControl;

/**
 * @author lzou
 * @date   Sep.19, 2003
 *
 * this is a WebControl to display AdjustmentType nicely.  
 */
public class AdjustmentTypeTreeWebControl
	implements WebControl
{
	
	private WebControl delegate;

	public AdjustmentTypeTreeWebControl()
	{
		//REVIEW(readability): parameters can be aligned
	 	delegate = new TreeWebControl(
                AdjustmentTypeIdentitySupport.instance(), // to get each entry's identity
	 		    "Adjustment Type",   // table title
	 	        new OutputWebControl()
	 	        {
	 	    	    public void toWeb(Context ctx, PrintWriter out, String nae, Object obj)
	 	    	    {
	 	    		    AdjustmentType node = ( AdjustmentType ) obj;
	 	    		    out.print( node.getName() );
	 	    	    }
	 	        }
	 	)
        {
            protected String getImage(Context ctx, Object child)
            {
                MessageMgr mmgr = new MessageMgr(ctx, child.getClass());

                AdjustmentType adj = (AdjustmentType) child;
                if(adj.isCategory())
                {
                    return mmgr.get(
                            "TreeFolder",
                            "<img src=\"/images/openFolder.gif\" alt=\"--\" />");
                }
                else
                {
                    return mmgr.get(
                            "ChildNode",
                            "<img src=\"/images/org/javalobby/icons/16x16/Document.gif\" alt=\"--\" />");
                }

            }
        };
	}

	
	public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	{
	    SysFeatureCfg sysCfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
		Collection col = (Collection) obj;
	    Collection newCol = new ArrayList(col);

		String strCats = sysCfg.getAdjTypeCat().trim();
        short[] disableCats = null;
		if(strCats!=null && strCats.length()>0)
		{
			String[] arrCat = strCats.split(RK_SEPARATOR_);
			 disableCats = new short[arrCat.length];
			
			for (int i = 0; i < arrCat.length; i++)
			{
			    String strToken = arrCat[i].trim();
			    if (strToken.length() > 0)
			    {
			        disableCats[i] = Short.parseShort(strToken);
			    }
			}
        }   

		    for (Iterator iter = col.iterator(); iter.hasNext();)
		    {
		        AdjustmentType type = (AdjustmentType) iter.next();
		        
                if(strCats!=null && strCats.length()>0)
                {
		        for (int i = 0; i < disableCats.length; i++)
		        {
		            AdjustmentTypeEnum disabledEnum = AdjustmentTypeEnum.get(disableCats[i]);
		            
		            if (disabledEnum!=null && type.isInCategory(ctx, disabledEnum))
		            {
		                newCol.remove(type);
		            }
		        }
                }
			}
		
	    
		delegate.toWeb(ctx, out, name, newCol);
	}
	
	
	public Object fromWeb(Context ctx, ServletRequest req, String name)
		  throws NullPointerException
	{
		return delegate.fromWeb(ctx, req, name);
	}
	
	
	public void fromWeb(Context ctx, Object obj, ServletRequest req, String name)
	{
		delegate.fromWeb(ctx, obj, req, name);
	}
	

	public WebControl getDelegate()
	{
		return delegate;
	}

	private final static String RK_SEPARATOR_ = ",";
	

}
