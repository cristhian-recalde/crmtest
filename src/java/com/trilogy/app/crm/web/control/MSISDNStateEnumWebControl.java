/*
 * Created on Nov 12, 2003
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.GeneralConfigSupport;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.PortingTypeEnum;
import com.trilogy.app.crm.bean.ui.Msisdn;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.Lang;
import com.trilogy.framework.xhome.language.MessageMgrSPI;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.EnumWebControl;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xhome.xenum.EnumCollection;

 
/**
 * @author lzou
 * @date   Nov 12, 2003
 * 
 * WebControl to display Msisdn State in detail ( calculating and showing when 
 * it gets to be available if it is currently in IN_USE state). 
 * 
 **/
public class MSISDNStateEnumWebControl extends EnumWebControl
{
    /** Key to use to lookup DateFormat from MessageMgr. **/
    public final static String     MSG_MGR_KEY      = "Format";
    public final static DateFormat DEFAULT_FORMAT   = new SimpleDateFormat("MMM dd, yyyy");
	
    /**
	 * @param msisdnStateEnum
	 */
	public MSISDNStateEnumWebControl(EnumCollection msisdnStateEnum )
	{
		super(msisdnStateEnum);
	}
	
    /** Default DateFormat to use if not explicitly set in constructor or implicitly in Context. **/
    public DateFormat getDefaultFormat()
    {
        return DEFAULT_FORMAT;      
    }
      
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
       int  mode = ctx.getInt("MODE", DISPLAY_MODE);
       Enum enumeration = (Enum) obj;

       switch (mode)
       {
          case EDIT_MODE:
          case CREATE_MODE:
             out.print("<select name=\"" + name + "\" size=\"" + size_ + "\"");
             if ( autoPreview_ )
             {              
                out.print( " onChange=autoPreview()");
             }
             out.print(">");
            
             for ( Iterator i = getEnumCollection(ctx).iterator() ; i.hasNext() ; )
             {
                Enum e = (Enum) i.next();

                out.print("<option value=\"" + e.getIndex() + "\"");
                if ( e.equals(enumeration) )
                {
                   out.print(" selected=\"selected\"");
                }
                out.println(">" + e.getDescription() + "</option>");
             }
             out.println("</select>");
          break;

          case DISPLAY_MODE:
          default:
             if ( MsisdnStateEnum.HELD.getDescription().equals(enumeration.getDescription()) 
                     && !(GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ctx)))
             {
                  Msisdn bean = (Msisdn)ctx.get(AbstractWebControl.BEAN);
                  
                  int spid = bean.getSpid();
                  
                  Home spHome = ( Home )ctx.get(CRMSpidHome.class);
                  
                  try
                  {
                        CRMSpid idObj = (CRMSpid)spHome.find(ctx, Integer.valueOf(spid));
                  
                        int msisdnHeldDays = bean.isExternal() ? idObj.getExternalMobileNumberHeldDays() : idObj.getMobileNumberHeldDays();
                        
                        Date lastModifiedDay = bean.getLastModified();                        
                        
                        Date availableDate  = new Date(lastModifiedDay.getTime() + msisdnHeldDays * 24L * 60* 60* 1000);
                        if( PortingTypeEnum.NONE == bean.getPortingType())
                        {
                            out.println(enumeration.getDescription() + " until " + getFormat(ctx).format(availableDate));
                        } else
                        {
                            out.println(enumeration.getDescription() + " indefinitely (Ported) ");
                        }
                        
                  }catch(HomeException e)
                  {
                        e.printStackTrace();
                        out.print(enumeration.getDescription());      
                  }
             }
             else
             {
                   out.print(enumeration.getDescription());
             }             
       }
    }
    
    public EnumCollection getEnumCollection(Context ctx)
    {
        Msisdn msisdn = (Msisdn) ctx.get(AbstractWebControl.BEAN);
        // If its not IN_USE, we remove the IN_USE item from the collection
        // so they cannot select it
        if (!msisdn.getState().equals(MsisdnStateEnum.IN_USE))
        {
            List stateList = new ArrayList();
            for (Iterator i = enumc_.iterator(); i.hasNext();)
            {
                Enum e = (Enum) i.next();
                if (!e.equals(MsisdnStateEnum.IN_USE))
                {
                    // ported MSISDNs can't be avaialbe.
                    if (PortingTypeEnum.NONE == msisdn.getPortingType() || MsisdnStateEnum.AVAILABLE != msisdn.getState())
                    {
                        stateList.add(e);
                    }
                    // don't try to optimize the Enum collections by having a static with list without more thought. 
                    // Enum's use Binary Splits, the list order must always adhere to index of enum. 
                    //Better we don't fall in for optimization
                }
                    
            }
            Enum[] tempEnum = new Enum[] {};
            tempEnum = (Enum[]) stateList.toArray(tempEnum);
            return new EnumCollection(tempEnum);
        }
        return enumc_;
    }

    
    private DateFormat getFormat(Context ctx)
    {
        MessageMgrSPI mmgr = (MessageMgrSPI) ctx.get(MessageMgrSPI.class);
    
        String format = mmgr.get(ctx, MSG_MGR_KEY, getClass(), (Lang) ctx.get(Lang.class), null, null);
      
        return ( format == null ) ?
                 getDefaultFormat() : 
                    new SimpleDateFormat(format);
    }
    
    
}
