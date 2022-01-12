package kr.co.aim.greenframe.logger;

import java.net.URL;

import org.apache.log4j.helpers.FileWatchdog;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.xml.DOMConfigurator;



public class Configurator extends DOMConfigurator 
{
	@Override
	public void doConfigure(final URL url, LoggerRepository repository)
	{
		try
		{
			configureAndWatch( url.getFile(), Long.parseLong( System.getProperty( "log4j.watchDelay" )));
		}
		catch ( Exception ex )
		{
			configureAndWatch( url.getFile(), FileWatchdog.DEFAULT_DELAY );
		}
	}
}
