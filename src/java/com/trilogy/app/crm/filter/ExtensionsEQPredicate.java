package com.trilogy.app.crm.filter;

import java.sql.SQLException;
import java.util.Collection;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;

import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionAware;


public class ExtensionsEQPredicate implements Predicate, XStatement
{
    
    public ExtensionsEQPredicate(Class extensionClass, PropertyInfo extensionProperty, Object value)
    {
        this.predicate_ = new EQ(extensionProperty, value);
        this.extensionClass_ = extensionClass;
    }
    
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        if (obj instanceof ExtensionAware)
        {
            ExtensionAware extAwareObj = (ExtensionAware) obj;
            Collection<Extension> extensions = extAwareObj.getExtensions();
            if (extensions != null)
            {
                for (Extension extension : extensions)
                {
                    if (extension!=null
                            && extension.getClass().equals(extensionClass_)
                            && predicate_.f(ctx, extension))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    } 
    
    protected Predicate predicate_;
    protected Class extensionClass_;
    
	/**
	 * {@inheritDoc}
	 */
	public String createStatement(Context context) 
	{
		return ((XStatement)True.instance()).createStatement(context);
	}

	/**
	 * {@inheritDoc}
	 */
	public void set(Context context, XPreparedStatement xpreparedstatement)
			throws SQLException 
	{
	}
}
