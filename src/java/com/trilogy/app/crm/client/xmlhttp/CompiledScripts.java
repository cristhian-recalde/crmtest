/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.client.xmlhttp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptException;

import com.trilogy.app.crm.util.Objects;
import com.trilogy.framework.core.bean.ScriptLanguageEnum;
import com.trilogy.framework.core.scripting.ScriptExecutor;
import com.trilogy.framework.core.scripting.support.ScriptExecutorFactory;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 *
 * @author gary.anderson@redknee.com
 */
public class CompiledScripts
{
    public CompiledScripts()
    {
        scripts_ = new HashMap<String, CompiledScript>();
    }


    public Transalator getTranslator(final Context context, final XMLTranslationConfiguration config)
    {
        final String translatorScript = config.getTranslatorScript();
        if (translatorScript == null || translatorScript.trim().length() == 0)
        {
            return null;
        }
        CompiledScript compiledScript = scripts_.get(translatorScript);
        if (compiledScript == null || compiledScript.getTranslator() == null)
        {
            compiledScript = compile(context, translatorScript, config);
            scripts_.put(translatorScript, compiledScript);
        }
        return compiledScript.getTranslator();
    }
    
    public TranslatorStatusEnum getStatus(final Context context, final XMLTranslationConfiguration config)
    {
        final String translatorScript = config.getTranslatorScript();

        final TranslatorStatusEnum status;
        
        if (translatorScript == null || translatorScript.trim().length() == 0)
        {
            status = TranslatorStatusEnum.UNCOMPILED;
        }
        else
        {
            CompiledScript compiledScript = scripts_.get(translatorScript);

            if (compiledScript == null)
            {
                status = TranslatorStatusEnum.UNCOMPILED;
            }
            else
            {
                status = compiledScript.getStatus();
            }
        }

        return status;
    }
    

    public String getStatusMessage(final Context context, final XMLTranslationConfiguration config)
    {
        final String translatorScript = config.getTranslatorScript();

        final String statusMessage;
        
        if (translatorScript == null || translatorScript.trim().length() == 0)
        {
            statusMessage = "No script to compile.";
        }
        else
        {
            CompiledScript compiledScript = scripts_.get(translatorScript);

            if (compiledScript == null)
            {
                statusMessage = "Not yet compiled.";
            }
            else
            {
                statusMessage = compiledScript.getStatusMessage();
            }
        }

        return statusMessage;
    }
    

    /**
     *
     *
     * @param context
     * @return
     */
    private CompiledScript compile(final Context context, final String translatorScript, XMLTranslationConfiguration config)
    {
        final CompiledScript compiledScript = new CompiledScript();
        compiledScript.setScript(translatorScript);
        final StringWriter statusWriter = new StringWriter();
        final PrintWriter statusPrinter = new PrintWriter(statusWriter);
        Transalator transalator = null;
        if (!config.isTextContent())
        {
            try
            {
                final ScriptLanguageEnum type = config.getScriptType();
                ScriptExecutor executor = ScriptExecutorFactory.create(type);

                final Object result = executor.retrieveObject(context, translatorScript, "");
                if (result instanceof Transalator)
                {
                    transalator = ((Transalator) result);
                    statusPrinter.println("Compiled on " + new Date());
                }
                else
                {
                    statusPrinter.println("Unexpected Compilation returned: " + result);
                }
            }
            catch (ScriptException exception)
            {
                statusPrinter.println("Compilation error: " + exception.getMessage());
                new MajorLogMsg(this, "Compilation error!", exception).log(context);
            }
        }
        else
        {
            /**
             * 
             */
            transalator = new Transalator()
            {

                @Override
                public Objects prepareRequest(Context context, Objects input, Class<?> retunrClass,
                        Class<?>... resultClasses) throws Exception
                {
                    // the script itself is the content
                    return new Objects().putObject(String.class, translatorScript);
                }


                @Override
                public Objects handleResponse(Context context, Objects input, Class<?> retunrClass,
                        Class<?>... resultClasses)
                {
                    // The input is the final content
                    return input;
                }

                
                @Override
                public Collection<Class<?>> getInputTypesHandleReponse()
                {
                    // TODO Auto-generated method stub
                    return NO_CLASSES;
                }


                @Override
                public Collection<Class<?>> getInputTypesPrepareRequest()
                {
                    // TODO Auto-generated method stub
                    return NO_CLASSES;
                }


                @Override
                public Collection<Class<?>> getOutputTypesHandleReponse()
                {
                    // TODO Auto-generated method stub
                    return NO_CLASSES;
                }


                @Override
                public Collection<Class<?>> getOutputTypesPrepareRequest()
                {
                    // TODO Auto-generated method stub
                    return classes;
                }
                @Override
                public String getDescription()
                {
                    // TODO Auto-generated method stub
                    return "Text-Translator that simply consumes the script-text and presents it as the produced content.";
                }
                
                final Set<Class<?>> classes = Collections.unmodifiableSet(new HashSet<Class<?>>(Arrays
                        .asList(String.class)));

            };
            statusPrinter.println("Compiled on " + new Date());
        }
        if (null != transalator)
        {
            compiledScript.setTranslator(transalator);
            compiledScript.setStatus(TranslatorStatusEnum.COMPILED);
            printTranslatorStatus(statusPrinter, transalator);
        }
        else
        {
            compiledScript.setTranslator(null);
            compiledScript.setStatus(TranslatorStatusEnum.ERROR);
        }
        compiledScript.setStatusMessage(statusWriter.getBuffer().toString());
        return compiledScript;
    }
    
    
    private void printTranslatorStatus(PrintWriter statusPrinter, Transalator transalator)
    {

        // write details in the status message
        statusPrinter.println();
        statusPrinter.println(transalator.getDescription());
        statusPrinter.println();
        statusPrinter.println("-Prepares Request-");
        statusPrinter.print("--* Consumes (");
        statusPrinter.print(transalator.getInputTypesPrepareRequest().size());
        statusPrinter.println(") Input Types");
        for (Class<?> type : transalator.getInputTypesPrepareRequest())
        {
            statusPrinter.print("    # ");
            statusPrinter.println(type);
        }
        statusPrinter.print("--* Produces (");
        statusPrinter.print(transalator.getOutputTypesPrepareRequest().size());
        statusPrinter.println(") Output Types");
        for (Class<?> type : transalator.getOutputTypesPrepareRequest())
        {
            statusPrinter.print("----# ");
            statusPrinter.println(type);
        }
        statusPrinter.println();
        statusPrinter.println("-Handles Response-");
        statusPrinter.print("--* Consumes (");
        statusPrinter.print(transalator.getInputTypesHandleReponse().size());
        statusPrinter.println(") Input Types");
        for (Class<?> type : transalator.getInputTypesHandleReponse())
        {
            statusPrinter.print("----# ");
            statusPrinter.println(type);
        }
        statusPrinter.print("--* Produces (");
        statusPrinter.print(transalator.getOutputTypesHandleReponse().size());
        statusPrinter.println(") Output Types");
        for (Class<?> type : transalator.getOutputTypesHandleReponse())
        {
            statusPrinter.print("----# ");
            statusPrinter.println(type);
        }
       
    }


    private Map<String, CompiledScript> scripts_;
 
}


class CompiledScript
{

    /**
     * Gets the script.
     *
     * @return The script.
     */
    public String getScript()
    {
        return script_;
    }
    /**
     * Sets the script.
     *
     * @param script The script.
     */
    public void setScript(String script)
    {
        script_ = script;
    }
    /**
     * Gets the status.
     *
     * @return The status.
     */
    public TranslatorStatusEnum getStatus()
    {
        return status_;
    }
    /**
     * Sets the status.
     *
     * @param status The status.
     */
    public void setStatus(TranslatorStatusEnum status)
    {
        status_ = status;
    }
    /**
     * Gets the statusMessage.
     *
     * @return The statusMessage.
     */
    public String getStatusMessage()
    {
        return statusMessage_;
    }
    /**
     * Sets the statusMessage.
     *
     * @param statusMessage The statusMessage.
     */
    public void setStatusMessage(String statusMessage)
    {
        statusMessage_ = statusMessage;
    }
    /**
     * Gets the translator.
     *
     * @return The translator.
     */
    public Transalator getTranslator()
    {
        return translator_;
    }
    /**
     * Sets the translator.
     *
     * @param translator The translator.
     */
    public void setTranslator(Transalator translator)
    {
        translator_ = translator;
    }
    private String script_;
    private TranslatorStatusEnum status_;
    private String statusMessage_;
    private Transalator translator_;
   
}
