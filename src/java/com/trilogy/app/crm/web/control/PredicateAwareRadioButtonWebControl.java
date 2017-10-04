package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Iterator;
import javax.servlet.ServletRequest;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.webcontrol.RadioButtonWebControl;
import com.trilogy.framework.xhome.xenum.EnumCollection;


public class PredicateAwareRadioButtonWebControl extends RadioButtonWebControl
{

    private Predicate pred_;


    public PredicateAwareRadioButtonWebControl(EnumCollection _enum)
    {
        super(_enum);
    }


    public PredicateAwareRadioButtonWebControl(EnumCollection _enum, boolean autoPreview)
    {
        super(_enum, autoPreview);
    }


    public PredicateAwareRadioButtonWebControl(EnumCollection _enum, String before, String after)
    {
        super(_enum, before, after);
    }


    public PredicateAwareRadioButtonWebControl(EnumCollection _enum, boolean autoPreview, String before, String after)
    {
        super(_enum, autoPreview, before, after);
    }
    
    public PredicateAwareRadioButtonWebControl(EnumCollection _enum, boolean autoPreview, String before, String after, Predicate predicate)
    {
        super(_enum, autoPreview, before, after);
        pred_ = predicate;
    }

    public EnumCollection getEnumCollection(Context ctx)
    {
        return enum_.where(ctx, pred_);
    }


    public Object fromWeb(Context ctx, ServletRequest req, String name) throws NullPointerException
    {
        if (req.getParameter(name) == null)
        {
            throw new NullPointerException("Null Enum Value");
        }
        else
        {
            // returns an Enum object containing the index and description
            // new com.redknee.framework.xlog.log.InfoLogMsg(this, "selection
            // ["+req.getParameter(name)+"] index ["+getEnumCollection(ctx).getByIndex(new
            // Short(req.getParameter(name)).shortValue())+"]" ,null).log(ctx);
            return getEnumCollection(ctx).getByIndex(Short.parseShort(req.getParameter(name)));
        }
    }


    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        int mode = ctx.getInt("MODE", DISPLAY_MODE);
        com.redknee.framework.xhome.xenum.Enum localEnum = (com.redknee.framework.xhome.xenum.Enum) obj;
        MessageMgr mmgr = new MessageMgr(ctx, this);
        switch (mode)
        {
        case EDIT_MODE:
        case CREATE_MODE:
            for (Iterator it = getEnumCollection(ctx).iterator(); it.hasNext();)
            {
                com.redknee.framework.xhome.xenum.Enum e = (com.redknee.framework.xhome.xenum.Enum) it.next();
                out.print(beforeText_);
                out.print("<input type=\"radio\" name=\"");
                out.print(name);
                out.print("\" value=\"");
                out.print(e.getIndex());
                out.print("\"");
                if (isAutoPreview())
                {
                    out.print(" onClick=\"autoPreview('");
                    out.print(WebAgents.getDomain(ctx));
                    out.print("',event);\"");
                }
                if (localEnum.getIndex() == e.getIndex())
                {
                    out.print(" checked=\"checked\"");
                }
                out.print("/>");
                out.print(mmgr.get(e.getDescription(), e.getDescription()));
                out.println(afterText_);
            }
            break;
        case DISPLAY_MODE:
        default:
            for (Iterator it = getEnumCollection(ctx).iterator(); it.hasNext();)
            {
                com.redknee.framework.xhome.xenum.Enum e = (com.redknee.framework.xhome.xenum.Enum) it.next();
                if (localEnum.getIndex() == e.getIndex())
                {
                    out.print(beforeText_);
                    // If there is no label for the enum display the index
                    if (e.getDescription().equals(""))
                        out.print(e.getIndex());
                    else
                        out.print(mmgr.get(e.getDescription(), e.getDescription()));
                    out.println(afterText_);
                }
            }
        }
    }
}
