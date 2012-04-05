package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSSNSCommand;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;


public class snsPublish extends AWSSNSCommand {

	

	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("t=topic:,m=message:,s=subject:");
		opts.parse(args);

		args = opts.getRemainingArgs();
		
		
		String topic = opts.getOptStringRequired("topic");
		String message = opts.getOptStringRequired("message");
		String subject = opts.getOptString("subject", null );
		

		
		mSerializeOpts = this.getSerializeOpts(opts);
		

		
		
		try {
			mAmazon = getSNSClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		int ret;
		
		ret = publish(topic,subject,message);
		
		
		return ret;
		
		
	}


	private int publish(String topic,String subject, String message) throws IOException, XMLStreamException, InvalidArgumentException, SaxonApiException {
		

		PublishRequest request = new PublishRequest().withTopicArn(topic).withMessage(message).withSubject(subject);
		PublishResult result = mAmazon.publish(request);
		
		

		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(mSerializeOpts);
		
		
		startDocument();
		startElement(getName());
		
		startElement("message");
		attribute("id" , result.getMessageId() );
		endElement();

		endElement();
		endDocument();
		closeWriter();
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		
		
		return 0;
		
		
		
		
	}


	

}