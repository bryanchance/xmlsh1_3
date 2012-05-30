/**
 * $Id: PipedStream.java 209 2009-04-11 16:32:13Z daldei $
 * $Date: 2009-04-11 12:32:13 -0400 (Sat, 11 Apr 2009) $
 *
 */

package org.xmlsh.util;

import java.io.IOException;

import javanet.staxutils.XMLEventPipe;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XMLEventInputPort;
import org.xmlsh.core.XMLEventOutputPort;
import org.xmlsh.core.XdmStreamInputPort;
import org.xmlsh.core.XdmStreamOutputPort;
import org.xmlsh.core.XdmItemPipe;
import org.xmlsh.sh.shell.SerializeOpts;

public class PipedXDMPort extends PipedPort {
	private		XdmStreamInputPort 	mIn;
	private		XdmStreamOutputPort 	mOut;
	private		XdmItemPipe		mPipe;
	
	
	public PipedXDMPort(SerializeOpts opts)  throws IOException
	{
		mPipe = new XdmItemPipe(1000);
	
		mIn = new XdmStreamInputPort(mPipe.getReadEnd(), opts);
		mOut = new XdmStreamOutputPort(mPipe.getWriteEnd(),opts);
		
	}
	
	public	InputPort	getInput() throws IOException { 
		return mIn;
	}
	
	public OutputPort getOutput() { 
		return mOut;
	}
	
	static public PipedXDMPort[] getPipes(int n, SerializeOpts opts) throws IOException
	{
		if( n == 0 )
			return null;
		PipedXDMPort	streams[] = new PipedXDMPort[n];
		for( int i = 0 ; i < n ; i++ )
			streams[i] = new PipedXDMPort(opts);
		return streams;
		
	}
	
}
//
//
//Copyright (C) 2008,2009,2010,2011,2012 , David A. Lee.
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
