/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.s9api.SaxonApiException;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.UnknownOption;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Regions;

public abstract class AWSCommand extends XCommand {
	
	Logger mLogger = LogManager.getLogger( this.getClass() );
	
	protected XMLStreamWriter mWriter;
	private OutputPort mResultOut;
	
	protected static final String sCOMMON_OPTS = "region:,endpoint:,client:,config:,accessKey:,secretKey:,rate-retry:,retry-delay:" ;
	protected int rateRetry = 0;
	protected int retryDelay = 10000; // 10 seconds default

    private boolean bSetEndpoint = false ;

	
	protected void setEndpointSet(boolean v) {
        this.bSetEndpoint = v ;
    }

    static {
		
		LogManager.getLogger("httpclient").setLevel(Level.WARN);
		LogManager.getLogger("http.wire").setLevel(Level.WARN);

		LogManager.getLogger("org.apache.http").setLevel(Level.WARN);
		LogManager.getLogger("com.amazonaws").setLevel(Level.WARN);
		
		LogManager.getLogger("org.apache.http.impl.conn").setLevel(Level.WARN);
		LogManager.getLogger("com.amazonaws.auth.profile").setLevel(Level.ERROR);
		LogManager.getLogger("com.amazonaws.auth.profile.internal").setLevel(Level.ERROR);
		
	}
	
	
	
	public AWSCommand() {
		super();
	}
	
	abstract protected Object getClient();

	protected abstract String getCommonOpts() ;
	
	public List<OptionValue> parseOptions(Options options, List<XValue> args) throws UnknownOption, InvalidArgumentException {
        List<OptionValue> opts = options.parse(args);
        parseCommonOptions(options);
        return opts ;
    }

    protected	Options getOptions( String... sopts )	
	{
		return new Options( Options.joinOptions(getCommonOpts() , Options.joinOptions(sopts) )  , SerializeOpts.getOptionDefs());
	}
	
	
	
	protected void closeWriter() throws XMLStreamException, IOException, CoreException, SaxonApiException {
	    if( mWriter != null ){
	      try { 
	        mWriter.flush();
	        mWriter.close();
	        
	      } finally {
	          mWriter = null;
	      }
	    }
	    if( mResultOut != null ) {
	        try {
	           mResultOut.writeSequenceTerminator(getSerializeOpts());
	        } finally {
	           mResultOut = null ;
	        }
	    }
	}


	protected void endDocument() throws XMLStreamException {
		mWriter.writeEndDocument();
	}


	protected void endElement() throws XMLStreamException {
		mWriter.writeEndElement();
	}


	protected void startElement(String localname) throws XMLStreamException {
		mWriter.writeStartElement(localname);
	}


	protected void startDocument() throws XMLStreamException {
		mWriter.writeStartDocument();
	}
	
	protected	  void attribute( String localName , String value ) throws XMLStreamException
	{
		mWriter.writeAttribute(localName, Util.notNull(value));
	}
	
	
	
	protected	  void characters( String value ) throws XMLStreamException
	{
		if( value != null )
			mWriter.writeCharacters(value);
	}


	public void attribute(String localName, boolean flag) throws XMLStreamException {
		attribute( localName , flag ? "true" : "false" );
		
	}




	protected void writeZones(List<String> zones) throws XMLStreamException {
		
		writeStringList("zones" , "zone" , "name" , zones );

	}

	protected void attribute(String name, Integer value) throws XMLStreamException {
		attribute( name , value == null ? "" : String.valueOf(value));
	}

	protected void attribute(String name, int value) throws XMLStreamException {
		attribute( name , String.valueOf(value));
	}


	protected void attribute(String name, Date date) throws XMLStreamException {
		attribute( name ,  date == null ? "" : Util.formatXSDateTime(date));
		
	}


	protected void writeStringList(String listName, String elementName, String attrName, List<String> strings)
			throws XMLStreamException {
		       if( listName != null )
		    	   startElement(listName);
			   for( String string : strings ){
			
				   writeElementAttribute(elementName, attrName, string);
			   }
			   if( listName != null )
				   endElement();
			
			}


	public void writeElementAttribute(String elementName, String attrName, String string)
			throws XMLStreamException {
		startElement( elementName );
		   attribute( attrName  , string );
		   endElement();
	}


	protected void attribute(String name, long n) throws XMLStreamException {
		attribute( name , String.valueOf(n));
		
	}


	protected void setEndpoint(Options opts) throws InvalidArgumentException {
		
		if( opts.hasOpt("endpoint") ){
			setEndpoint(opts.getOptStringRequired("endpoint"));
			bSetEndpoint = true ;
		}
		
		
	}
	protected boolean hasSetEndpoint() {
	    return bSetEndpoint;
	    
	}
	protected void setRegion(Options opts) {
	    if( hasSetEndpoint() )
	        return ;

	    if( opts.hasOpt("region"))
	    	setRegion(opts.getOptString("region",Regions.DEFAULT_REGION.getName()) );
	    else {
	    	String region = mShell.getEnv().getVarString("AWS_REGION");
	    	if( Util.isBlank(region))
	    		region = mShell.getEnv().getVarString("EC2_REGION");  // ec2 command line compatibility
	    	if( !Util.isBlank(region))
	    		setRegion(region);
	    }

	}
	
	public abstract void setEndpoint(String endpoint);
	public abstract void setRegion( String region );


	protected void emptyDocument() throws XMLStreamException {
		startDocument();
		startElement(getName());
		endElement();
		endDocument();
	}


	protected void textElement(String name, String s) throws XMLStreamException {
		startElement(name);
		characters(s);
		endElement();
	}
	
	protected void traceCall( String method )
	{
		Object obj = getClient();
		
		mLogger.info( "AWS Method Call: " + obj.getClass().toString() + "." + method );
	}

	protected void parseCommonOptions(Options opts) throws InvalidArgumentException {
		
		rateRetry = opts.getOptInt("rate-retry", 0);
		retryDelay = opts.getOptInt("retry-delay", 10000);
	}

	protected XValue xpath(XValue xv, String expr)
			throws UnexpectedException {
				return xv.xpath(getShell(), expr);
				
			}
    protected int handleException(AmazonClientException e)  {
       mLogger.error("AWS Exception in " + getName() , e );
       mShell.printErr("AWS Exception in " + getName() , e);
       return -1 ;
       }
         
    protected boolean startResult() throws XMLStreamException, SaxonApiException, IOException, CoreException {
        if( mWriter != null ){
          mLogger.warn(getName() + ": AWS startResult previously called for this request");
          return false ; 
        }
        if( mResultOut != null ){
            mLogger.warn(getName() + ": AWS startResult with result port open - closing");
        }
        mResultOut = this.getStdout();
        
         mWriter = mResultOut.asXMLStreamWriter(getSerializeOpts());
            startDocument();
            startElement(getName());
        return true ;
    }

    protected void endResult() throws XMLStreamException, IOException, CoreException, SaxonApiException {
        if( mWriter == null )
            return ;
        endElement();
    	endDocument();
    	closeWriter();
    	
    }


    

}

//
//
// Copyright (C) 2008-2014    David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
