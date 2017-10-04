// INSPECTED: 24/09/2003 GEA
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.BitSet;
import java.util.Iterator;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xhome.xenum.EnumCollection;
import com.trilogy.framework.xhome.webcontrol.CheckBoxWebControl;
import com.trilogy.framework.xhome.webcontrol.PrimitiveWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * WebControl for use with a BitSet. You can link the BitSet with an Enum,
 * basically the index in the Enum specifies which bits in the BitSet are used.
 * For display, the List of true bits is displayed, as a list of the descriptions
 * in the Enum. Edit view prints a table of the bits / descriptions as checkboxes.
 **/
public class DescribedBitSetWebControl extends PrimitiveWebControl
{

    /** This is the enum linked to the BitSet. */
    protected final EnumCollection enum_;

    /** Checkbox that is used in edit mode to display the editable bits. */
    private static WebControl checkbox_ = CheckBoxWebControl.instance();

    /** Constructor for the EnumWebControl object
     * @param  _collection Linked enum. Contains the descriptions of the bits
     * the bits that are active. */
    public DescribedBitSetWebControl(EnumCollection _collection)
    {
       // REVIEW(cleanup): The super() call can be removed, it is implicit. GEA
        super();

        enum_=_collection;
    }

    /**
     * @see com.redknee.framework.xhome.webcontrol.OutputWebControl#toWeb(com.redknee.framework.xhome.context.Context, java.io.PrintWriter, java.lang.String, java.lang.Object)
     */
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        int mode = ctx.getInt("MODE", DISPLAY_MODE);
        BitSet bs=(BitSet) obj;

        // generic hidden. just marks that this element is in.
        out.println("<INPUT type=\"hidden\" name=\""+name+"\" value=\"permissions\"/>");

        switch (mode)
        {
            case EDIT_MODE :
            case CREATE_MODE :
                printEditable(ctx, out, name, bs);
                break;

            case DISPLAY_MODE :
            default :
                printViewable(out, bs);
                break;
        }
    }

    /** Prints the Bitset of the webpage for viewing. It will print only the true bits
     * as a comma-delimited list of their descriptions.
     * @param out The web page
     * @param bs The BitSet to be printed
     */
    private void printViewable(PrintWriter out, BitSet bs)
    {
        boolean bFirst=true;
        for (Iterator i = enum_.iterator(); i.hasNext();)
        {
            Enum e = (Enum) i.next();
        
            if(bs.get(e.getIndex()))
            {
                if(!bFirst)
                {
                    out.println(",");
                }
                else
                {
                    bFirst=false;
                }
                
                out.println(e.getDescription());
            }
        }
    }

    /**
     * Prints the property on the web page as a part of the form. Every bit gets a checkbox
     * @param ctx The context
     * @param out The web page
     * @param name The name of the Property
     * @param bs The bitset that we print
     */
    private void printEditable(Context ctx, PrintWriter out, String name, BitSet bs)
    {
        out.println("<TABLE border=\"0\">");
        
        for (Iterator i = enum_.iterator(); i.hasNext();)
        {
            Enum e = (Enum) i.next();
            out.println("<TR><TD>");
            out.println(e.getDescription());
            out.println("</TD><TD>");
            checkbox_.toWeb(ctx, out, name + SEPERATOR + e.getIndex(), Boolean.valueOf(bs.get(e.getIndex())));
            out.println("</TD></TR>");
        }
        
        out.println("</TABLE>");
    }

    /**
     * @see com.redknee.framework.xhome.webcontrol.InputWebControl#fromWeb(com.redknee.framework.xhome.context.Context, javax.servlet.ServletRequest, java.lang.String)
     */
    public Object fromWeb(Context ctx, ServletRequest req, String name) throws NullPointerException
    {
        BitSet bs=new BitSet();

        if (req.getParameter(name) == null)
        {
            throw new NullPointerException("Null Enum Value");
        }
        
		for (Iterator i = enum_.iterator(); i.hasNext();)
		{
		    Enum e = (Enum) i.next();

		    String value=req.getParameter(name + SEPERATOR + e.getIndex());
		    if(value!=null && value.length()>0)
		    {
		        bs.set(e.getIndex());
		    }
		}
        
        return bs;
    }
}
