package kr.co.aim.greenframe.logger.appender;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;

public class SampleAppender extends AppenderSkeleton
{

	@Override
	public boolean requiresLayout()
	{
		return true;
	}

	@Override
	public void activateOptions()
	{
		// Connection init
	}

	@Override
	protected void append(LoggingEvent event)
	{
		if (this.layout == null)
		{
			errorHandler.error("No layout for appender " + name, null, ErrorCode.MISSING_LAYOUT);
			return;
		}

		String message = this.layout.format(event);

	}

	@Override
	public void close()
	{
		// disconnect
	}
}
