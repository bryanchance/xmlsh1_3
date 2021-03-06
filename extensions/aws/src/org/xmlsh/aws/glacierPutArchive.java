package org.xmlsh.aws;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSGlacierCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;


public class glacierPutArchive	 extends  AWSGlacierCommand {



    /**
     * @param args
     * @throws IOException 
     */
    @Override
    public int run(List<XValue> args) throws Exception {


        Options opts = getOptions();
        parseOptions(opts, args);

        args = opts.getRemainingArgs();

        setSerializeOpts(this.getSerializeOpts(opts));


        if( args.size() <3)
            usage();

        String vault = args.remove(0).toString();
        String desc = args.remove(0).toString();




        try {
            getGlacierClient(opts);
        } catch (UnexpectedException e) {
            usage( e.getLocalizedMessage() );
            return 1;

        }


        int ret = -1;
        ret = put(vault,desc, args );



        return ret;


    }


    private int put(String vault, String desc, List<XValue> files ) throws IOException, XMLStreamException, SaxonApiException, CoreException 
    {

        OutputPort stdout = getStdout();
        mWriter = stdout.asXMLStreamWriter(getSerializeOpts());

        ArchiveTransferManager tm = new ArchiveTransferManager(mAmazon, mCredentials);



        startDocument();
        startElement(getName());

        for( XValue xf : files ){

            File file = mShell.getFile(xf);

            traceCall("ArchiveTransferManager.upload");

            UploadResult result = tm.upload(vault, desc, file);
            startElement("upload");
            attribute("valut", vault);
            attribute("description" , desc );
            attribute("file" , file.getAbsolutePath());
            attribute("archive-id" , result.getArchiveId());
            endElement();

        }



        endElement();
        endDocument();








        closeWriter();
        stdout.writeSequenceTerminator(getSerializeOpts());
        stdout.release();




        return 0;




    }


    @Override
    public void usage() {
        super.usage();
    }





}
