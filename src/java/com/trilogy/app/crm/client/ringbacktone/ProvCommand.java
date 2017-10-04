/**
 * 
 */
package com.trilogy.app.crm.client.ringbacktone;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.bean.ringbacktone.ProvCommandBean;
import com.trilogy.app.crm.bean.ringbacktone.ProvCommandBeanHome;
import com.trilogy.app.crm.bean.ringbacktone.ProvCommandBeanXInfo;
import com.trilogy.app.crm.bean.ringbacktone.ProvCommandParameter;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * @author jli
 * Provisioning Command of Personal Ring Back Tone server.
 */
public class ProvCommand implements Cloneable
{
    /**
     * GenericCommand is name of command used to define a general template.
     * The default value of parameters is defined in this command.
     */
    public static final String GenericCommand       = "Generic Command";
    public static final String RBT_ADDSUB           = "RBT_addSub";
    public static final String RBT_REMOVESUB        = "RBT_removeSub";
    public static final String RBT_SWITCHMSISDN     = "RBT_switchMSISDN";
    public static final String RBT_SUSPENDSUB       = "RBT_suspendSub";
    public static final String RBT_UNSUSPENDSUB     = "RBT_unSuspendSub";
    public static final String RBT_REMOVERBT        = "RBT_removeSub";
    
    public static final int PARAM_EXTERNALID      = 0;
    public static final int PARAM_ACCESSMEDIANAME = 1;
    public static final int PARAM_MSISDN          = 2;
    
    public static final char SEPERATOR            = ':';
    
    /**
     * Refulsh ProvCommand cache so it could be rebuilt using latest configuration.
     */
    public static void clear()
    {
        commands__.clear();
    }
    
    /**
     * Retrieve default value of command parameter from GenericCommand
     * @param param
     * @return
     */
    public static String getDefaultValue(String param)
    {
        ProvCommand command = createCommand(ContextLocator.locate(), GenericCommand);
        return command==null?null:command.getValue(param); 
    }
    
    /**
     * Instantiate a ProvCommand instance using command name and template defined in context (ProvCommandBeanHome).
     * @param context
     * @param name
     * @return
     */
    public static ProvCommand createCommand(Context context, String name)
    {
        ProvCommand command = commands__.get(name);
        if (command == null)
        {
            ProvCommandBean bean = null;
            Home home = (Home) context.get(ProvCommandBeanHome.class);
            if (home != null)
            {
                try
                {
                    bean = (ProvCommandBean) home.find(context, new EQ(ProvCommandBeanXInfo.NAME, name));
                }
                catch (HomeException e)
                {
                    LogSupport.minor(context, ProvCommand.class.getName(), e.getMessage(), e);
                }
            }
            
            if (bean != null)
            {
                command = new ProvCommand(bean);
                commands__.put(name, command);
            }
        }
        
        if (command != null)
        {
            try
            {
                command = (ProvCommand) command.clone();
            }
            catch (CloneNotSupportedException e)
            {
            }
        }
        
        return command;
    }
    
    //TODO: Use ProvCommand to construct a ProvCommand.
    public ProvCommand()
    {
    }
    
    public ProvCommand(ProvCommandBean bean)
    {
        this.name = bean.getName();
        
        if (bean.getParamList()!= null)
        {
            values_ = new String[bean.getParamList().size()];
            for (int i=0; i<bean.getParamList().size(); i++)
            {
                ProvCommandParameter param = (ProvCommandParameter)bean.getParamList().get(i);
                values_[i] = param.getDefaultValue();
                indexMap_.put(param.getName(), i);
                paramMap_.put(i, param);
            }
        }
    }
    
    /**
     * Set parameter value using its index.
     */
    public void setIndexValue(int index, String value) throws InvalidCommandException
    {
        if (values_!= null && index >=0 && index<values_.length)
        {
            validate(index, value);
            values_[index] = value;
            return;
        }
        
        throw new InvalidCommandException("Command is empty!");
    }
    
    /**
     * Set parameter value using its name
     * @param name
     * @param value
     * @throws InvalidCommandException
     */
    public void setNameValue(String name, String value) throws InvalidCommandException
    {
        Integer index = indexMap_.get(name);
        if (index != null)
        {
            setIndexValue(index, value);
        }
        else
        {
            throw new InvalidCommandException("Invalid Parameter:{"+name+"-"+value+"}");
        }
    }
    
    /**
     * Retrieve parameter value using its name
     * @param name
     * @return
     */
    public String getValue(String name)
    {
        Integer index = indexMap_.get(name);
        if (index != null && values_!=null && index>=0 && index<values_.length)
        {
            return values_[index];
        }
        return null;
    }
    
    public String toString()
    {
        //TODO:
        StringBuilder builder = new StringBuilder(name);
        for (int i=0; i<values_.length; i++)
        {
            builder.append(":"+values_[i]);
        }
        
        return builder.toString();
    }

    /**
     * Setter of a common parameter ExternalID
     * @param externalID
     * @throws InvalidCommandException
     */
    public void setExternalID(String externalID) throws InvalidCommandException
    {
        setIndexValue(PARAM_EXTERNALID, externalID);
    }
    
    /**
     * Setter of a common parameter MediaName
     * @param accessMediaName
     * @throws InvalidCommandException
     */
    public void setAccessMediaName(String accessMediaName) throws InvalidCommandException
    {
        setIndexValue(PARAM_ACCESSMEDIANAME, accessMediaName);
    }
    
    /**
     * Setter of a common parameter Msisdn
     * @param msisdn
     * @throws InvalidCommandException
     */
    public void setMsisdn(String msisdn) throws InvalidCommandException
    {
        setIndexValue(PARAM_MSISDN, msisdn);
    }
    
    /**
     * Validate if value is correct based on rules defined in command template.
     * @param index
     * @param value
     * @return
     * @throws InvalidCommandException
     */
    protected void validate(int index, String value) throws InvalidCommandException
    {
        ProvCommandParameter param = paramMap_.get(index);
        if (param != null)
        {
            if (value == null && !param.isOptional()) 
            {
                throw new InvalidCommandException("Parameter "+param.getName()+" at index "+index+" is empty!");
            }
            else if (value != null && (value.length()< param.getMinLen()))
            {
                throw new InvalidCommandException("Parameter "+param.getName()+" at index "+index+" is too short!");
            }
            else if (value != null && (value.length()> param.getMaxLen()))
            {
                throw new InvalidCommandException("Parameter "+param.getName()+" at index "+index+" is too long!");
            }
        }
    }
    
    /**
     * Clone method will make a new copy of values but shared the same instance of indexMap and paramList.
     */
    protected Object clone() throws CloneNotSupportedException
    {
        ProvCommand command = (ProvCommand) super.clone();
        if (values_!=null) command.values_ = values_.clone();
        
        return command;
    }
    
    private String               name      = null;
    private Map<String, Integer> indexMap_ = new HashMap<String,Integer>();
    private Map<Integer, ProvCommandParameter> paramMap_ = new HashMap<Integer,ProvCommandParameter>();
    private String[]             values_   = new String[]{};
    
    private static final Map<String, ProvCommand> commands__ = new HashMap<String, ProvCommand>();
}
