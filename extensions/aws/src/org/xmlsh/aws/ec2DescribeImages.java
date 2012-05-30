package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.aws.util.SafeXMLStreamWriter;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.ProductCode;
import com.amazonaws.services.ec2.model.StateReason;
import com.amazonaws.services.ec2.model.Tag;


public class ec2DescribeImages extends AWSEC2Command {

	


	private boolean bLongListing;




	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("l=long");
		opts.parse(args);

		args = opts.getRemainingArgs();
		

		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		
		
		
		bLongListing = opts.hasOpt("l");
		
		
		try {
			mAmazon = getEC2Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
	

		int ret;
		switch(args.size()){
		case	0:
			ret = describe(null);
			break;
		case	1:
			ret = describe(args);
			break;
			
		default :
				usage();
				return 1;
		}


		
		
		return ret;
		
		
	}


	private int describe(List<XValue> args) throws IOException, XMLStreamException, InvalidArgumentException, SaxonApiException {
		

		OutputPort stdout = this.getStdout();
		mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(mSerializeOpts));
		
		
		startDocument();
		startElement(this.getName());
		
		DescribeImagesRequest  request = new DescribeImagesRequest();
		if( args != null ){
			
			request.setImageIds(Util.toStringList(args));
			
		}
		
		
		
		DescribeImagesResult result = mAmazon.describeImages(request);
		
		
		for( Image image :  result.getImages() )
			writeImage(image);
		
		
		
		
		
		endElement();
		endDocument();
		closeWriter();
		
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		
		return 0;

	}


	

	private void writeImage(Image image) throws XMLStreamException {
		startElement("image");
		attribute("image_id", image.getImageId());
		attribute( "architecture" , image.getArchitecture());
		attribute( "hypervisor" , image.getHypervisor());
		
		
		attribute( "location" , image.getImageLocation());
		attribute( "owner_alias" , image.getImageOwnerAlias());
		attribute( "type" , image.getImageType());
		attribute( "kernel_id" , image.getKernelId());
		attribute( "name" , image.getName());
		attribute( "owner_id" , image.getOwnerId());
		attribute( "platform" , image.getPlatform());
		attribute( "ramdisk_id" , image.getRamdiskId());
		attribute( "root_device_name" , image.getRootDeviceName());
		attribute( "root_device_type" , image.getRootDeviceType());
		
		attribute( "state" , image.getState());
		attribute( "virtualization_type" , image.getVirtualizationType());
		attribute( "public" , image.getPublic() ? "true" : "false");

		List<ProductCode> codes = image.getProductCodes();
		writeProductCodes(codes);
		List<BlockDeviceMapping> deviceMappings = image.getBlockDeviceMappings();
		writeBlockDeviceMappings(deviceMappings);
		StateReason stateReason = image.getStateReason();
		writeStateReason(stateReason);
		
		
		List<Tag> tags = image.getTags();
		writeTags(tags);
		
		
		
		
		
		
		
		endElement();
		
	}


	private void writeStateReason(StateReason stateReason) throws XMLStreamException {
		if( stateReason != null ){
		startElement("state_reason");
		attribute("code",stateReason.getCode());
		attribute("message" , stateReason.getMessage());
		endElement();
		}
		
	}


	public void usage() {
		super.usage("Usage: ec2-describe-images [options] [image-id]");
	}




	

}