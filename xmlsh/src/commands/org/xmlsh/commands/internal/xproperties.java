/**
 * $Id: xpwd.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.commands.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.StringPair;

/*
 * 
 * Manage a Java properties file
*/

public class xproperties extends XCommand
{

	
	public int run(  List<XValue> args )	throws Exception
	{

		

		Options opts = new Options( "in:,inxml:,text,xml,d=delete:+,v=var:+,a=add:+,c=comment:" , args );
		opts.parse();
	
		XValue optIn 		= opts.getOptValue("in");
		XValue optInXml		= opts.getOptValue("inxml");

		

		boolean	 bOutText = opts.hasOpt("text");
		
		

		if( optIn != null && optInXml != null )
			usage("Only one of -in and -inxml allowed");
		
		
		
		
		
		
		String comment = opts.getOptString("c", null);
		
		
		
		
		Properties props = new Properties();
		SerializeOpts serializeOpts = getSerializeOpts();
		if( optInXml != null  )
			props.loadFromXML(getInput(optInXml).asInputStream(serializeOpts));
		else
		if( optIn != null )
			props.load(getInput(optIn).asInputStream(serializeOpts));
		
		
		/*
		 * Delete values as specified
		 */
		if( opts.hasOpt("d"))
			for (XValue d : opts.getOpt("d").getValues())
				props.remove(d.toString());

		List<String> printVars = null ;
		if( opts.hasOpt("v")){
			printVars = new ArrayList<String>();
			for (XValue var : opts.getOpt("v").getValues())
				printVars.add(var.toString() );
		}
		
		
	
		/*
		 * Add values from remaining args
		 */
		List<XValue> xvargs = opts.getRemainingArgs();
		

		if (opts.hasOpt("a")) {
			for (XValue add : opts.getOpt("a").getValues()){
				StringPair pair = new StringPair( add.toString() , '=');
				props.setProperty( pair.getLeft(), pair.getRight() );
			}
		}
		
		
		if( printVars != null )
			writeVars( props, printVars );
		else
		if( ! bOutText)
			writeXML(props, comment);
		else
			writeText(props,comment);
		
		

		
		return 0;
		
	}

	private void writeVars(Properties props, List<String> vars ) throws UnsupportedEncodingException, IOException {
		
		PrintWriter out = getStdout().asPrintWriter(getSerializeOpts());
		for( String var : vars )
			out.println( props.getProperty(var, "") );
	
		out.flush();
		
		
	}

	private void usage(String string) throws UnexpectedException {
		this.printErr(string);
		this.printErr("Usage: xproperites [-in file | -inxml file] [-out file | -outxml file]");
		throw new UnexpectedException(string);
		
	}

	private void writeText(Properties props, String comment) throws IOException {
		props.store(getEnv().getStdout().asOutputStream(), comment);
		
	}


	private void writeXML(Properties props, String comment)
			throws IOException, CoreException, SaxonApiException, XMLStreamException {
		
		SerializeOpts serializeOpts = getSerializeOpts();
		
		/*
		 * Load XML text into a buffer
		 */
		
		ByteArrayOutputStream oss = new ByteArrayOutputStream();
		props.storeToXML( oss , comment , serializeOpts.getEncoding());
		ByteArrayInputStream iss = new ByteArrayInputStream( oss.toByteArray());
		
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLEventReader reader = factory.createXMLEventReader( null , iss);
		XMLEventWriter writer = getStdout().asXMLEventWriter(serializeOpts);
		writer.add(reader);
		reader.close();
		writer.close();

		
	}


}

//
//
//Copyright (C) 2008,2009 , David A. Lee.
//
//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.
//
//The Original Code is: all this file.
//
//The Initial Developer of the Original Code is David A. Lee
//
//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
//Contributor(s): none.
//
