package ikoda.mlserver;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.stereotype.Component;

import ikoda.utils.MultiplePropertiesSingleton;

@Component
public class CustomServerConfig implements EmbeddedServletContainerCustomizer
{




        @Override
        public void customize(ConfigurableEmbeddedServletContainer container) {
        	
        	try
        	{
        	
        	Integer port=Integer.valueOf(MultiplePropertiesSingleton.getInstance().getProperties("aml.properties").get("port").toString());

            container.setPort(port);

        	}
        	catch(Exception e)
        	{
        		System.out.println("aml.properties not found or invalid port");
        		System.out.println(e);
        		System.out.println("Setting port to 9999");
        		container.setPort(9999);
        		
        	}

        }
}
