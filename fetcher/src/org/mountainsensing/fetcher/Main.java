package org.mountainsensing.fetcher;

import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mountainsensing.fetcher.operations.*;

/**
 *
 */
public class Main {
    
    private static final Map<String, Operation> operations = new HashMap<>();
    static {
        operations.put("get-sample", new SampleOperation.Get());
        operations.put("del-sample", new SampleOperation.Delete());
        operations.put("fetch-sample", new SampleOperation.Fetch());
                
        operations.put("get-config", new ConfigOperation.Get());
        operations.put("set-config", new ConfigOperation.Set());
        
        operations.put("get-date", new DateOperation.Get());
        operations.put("set-date", new DateOperation.Set());
    }
    
    private static final String PROTOCOL = "coap://";

    private static Options options;

    public static void main(String[] args) {
        Logger californiumLogger = Logger.getLogger("org.eclipse.californium");
        californiumLogger.setLevel(Level.WARNING);
        
        options = new Options();
        
	    JCommander parser = new JCommander(options);
        
        for (String opName : operations.keySet()) {
            parser.addCommand(opName, operations.get(opName));
        }
         
        parser.parse(args);

        if (options.shouldShowHelp()) {
            parser.usage();
            return;
        }
        
        Operation operation = operations.get(parser.getParsedCommand());
        for (String node : operation.getNodes()) {
            URI uri ;
            try {
                uri = new URI(PROTOCOL + "[" + options.getPrefix() + node + "]" + "/" + operation.getRessource() + "/");
            } catch (URISyntaxException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }

            for (int i = 0; i < options.getRetries(); i++) {
                try {
                    operation.processNode(uri, options.getTimeout());
                    break;
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
        
		/*if (args.length > 0) {
		    System.out.println("Attempting to get samples from node " + args[0]);

            //SensorConfig config = SensorConfig.newBuilder().setInterval(154).setHasADC1(false).setHasADC2(false).setHasRain(false).build();
            
			CoapClient client = new CoapClient();
            //ByteArrayOutputStream out = new ByteArrayOutputStream();
            //config.writeDelimitedTo(out);
            
			//CoapResponse response = client.post(out.toByteArray(), MediaTypeRegistry.APPLICATION_OCTET_STREAM);

			//if (response != null && response.isSuccess()) {
            //    System.out.println("Posted config!");
            //}
            
            CoapResponse response;
            
            while (true) {
                client.setURI(PREFIX + args[0] + SUFFIX);
                
                response = client.get();
                
			    if (response != null && response.isSuccess()) {
                    Sample sample = Sample.parseDelimitedFrom(new ByteArrayInputStream(response.getPayload()));
                    //config = SensorConfig.parseDelimitedFrom(new ByteArrayInputStream(response.getPayload()));
                    
				    System.out.println();

                    //System.out.println(config);
                
                    System.out.println(sample);

                    client.setURI(PREFIX + args[0] + SUFFIX + "/" + Integer.toString(sample.getId()));
                    response = client.delete();

                    if (response.isSuccess()) {
                        System.out.println("Succesfully deleted Sample " + Integer.toString(sample.getId()));
                    }
                }
            }
		}*/
}