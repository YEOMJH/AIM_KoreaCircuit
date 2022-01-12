package kr.co.aim.messolution.generic.errorHandler;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.object.ErrorDef;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CustomException extends Exception 
{

	public ErrorDef  errorDef;
	
	private static Log 	log = LogFactory.getLog(CustomException.class);

	public CustomException() {
	}

//	public CustomException(String CustomError, String Cha, String Eng, String Kor, String Loc )
//	{
//		ErrorDef tempErrorDef = GenericServiceProxy.getErrorDefMap().getErrorDef(CustomError);
//		if (StringUtil.equals(CustomError, "CustomError"))
//		{
//			tempErrorDef = new ErrorDef();
//			tempErrorDef.setErrorCode(CustomError);
//			tempErrorDef.setCha_errorMessage(Cha);
//			tempErrorDef.setEng_errorMessage(Eng);
//			tempErrorDef.setKor_errorMessage(Kor);
//			tempErrorDef.setLoc_errorMessage(Loc);		
//			
//			errorDef = new ErrorDef();
//			errorDef.setErrorCode(tempErrorDef.getErrorCode());
//			
//			String korTempMsg = tempErrorDef.getKor_errorMessage();
//			String engTempMsg = tempErrorDef.getEng_errorMessage();
//			String chaTempMsg = tempErrorDef.getCha_errorMessage();
//			String locTempMsg = tempErrorDef.getLoc_errorMessage();
//			
//			errorDef.setKor_errorMessage(korTempMsg);
//			errorDef.setEng_errorMessage(engTempMsg);
//			errorDef.setCha_errorMessage(chaTempMsg);
//			errorDef.setLoc_errorMessage(locTempMsg);
//		}
//		else
//		{
//			if (tempErrorDef != null)
//			{
//				errorDef = new ErrorDef();
//				errorDef.setErrorCode(tempErrorDef.getErrorCode());
//				
//				String korTempMsg = tempErrorDef.getKor_errorMessage();
//				String engTempMsg = tempErrorDef.getEng_errorMessage();
//				String chaTempMsg = tempErrorDef.getCha_errorMessage();
//				String locTempMsg = tempErrorDef.getLoc_errorMessage();
//				
//				errorDef.setKor_errorMessage(korTempMsg);
//				errorDef.setEng_errorMessage(engTempMsg);
//				errorDef.setCha_errorMessage(chaTempMsg);
//				errorDef.setLoc_errorMessage(locTempMsg);
//			}
//			else
//			{
//				tempErrorDef = new ErrorDef();
//				tempErrorDef.setErrorCode("NotCustomError");
//				tempErrorDef.setCha_errorMessage("");
//				tempErrorDef.setEng_errorMessage("");
//				tempErrorDef.setKor_errorMessage("");
//				tempErrorDef.setLoc_errorMessage("");
//				
//				errorDef = new ErrorDef();
//				errorDef.setErrorCode(tempErrorDef.getErrorCode());
//				
//				String korTempMsg = tempErrorDef.getKor_errorMessage();
//				String engTempMsg = tempErrorDef.getEng_errorMessage();
//				String chaTempMsg = tempErrorDef.getCha_errorMessage();
//				String locTempMsg = tempErrorDef.getLoc_errorMessage();
//				
//				errorDef.setKor_errorMessage(korTempMsg);
//				errorDef.setEng_errorMessage(engTempMsg);
//				errorDef.setCha_errorMessage(chaTempMsg);
//				errorDef.setLoc_errorMessage(locTempMsg);
//			}
//		}
//		
//		if(log.isErrorEnabled())
//		{
//			log.error(String.format("[%s]%s", errorDef.getErrorCode(), errorDef.getLoc_errorMessage()));
//		}
//	}
	
	public CustomException(String errorCode, Object... args)
	{
		ErrorDef tempErrorDef = GenericServiceProxy.getErrorDefMap().getErrorDef(errorCode);
		
		if ( tempErrorDef == null )
		{
			
			tempErrorDef = new ErrorDef();
			tempErrorDef.setErrorCode("UndefinedCode");
			tempErrorDef.setCha_errorMessage("");
			tempErrorDef.setEng_errorMessage("");
			tempErrorDef.setKor_errorMessage("");
			tempErrorDef.setLoc_errorMessage("");
		}
		
		errorDef = new ErrorDef();
		{
			//initialize object
			errorDef.setErrorCode(tempErrorDef.getErrorCode());
			errorDef.setCha_errorMessage("");
			errorDef.setEng_errorMessage("");
			errorDef.setKor_errorMessage("");
			errorDef.setLoc_errorMessage("");
		}
		
		String korTempMsg = tempErrorDef.getKor_errorMessage();
		String engTempMsg = tempErrorDef.getEng_errorMessage();
		String chaTempMsg = tempErrorDef.getCha_errorMessage();
		String locTempMsg = tempErrorDef.getLoc_errorMessage();
		
		errorDef.setKor_errorMessage(
				MessageFormat.format(korTempMsg, args));
		errorDef.setEng_errorMessage(
				MessageFormat.format(engTempMsg, args));
		errorDef.setCha_errorMessage(
				MessageFormat.format(chaTempMsg, args));
		errorDef.setLoc_errorMessage(
				MessageFormat.format(locTempMsg, args));
		
		if(log.isErrorEnabled())
		{
			log.error(String.format("[%s]%s", errorDef.getErrorCode(), errorDef.getLoc_errorMessage()));
		}
	}

	public CustomException(String errorCode) {
		
		ErrorDef tempErrorDef = GenericServiceProxy.getErrorDefMap().getErrorDef(errorCode);
		
		if ( tempErrorDef == null )
		{
			
			tempErrorDef = new ErrorDef();
			tempErrorDef.setErrorCode("UndefinedCode");
			tempErrorDef.setCha_errorMessage("");
			tempErrorDef.setEng_errorMessage("");
			tempErrorDef.setKor_errorMessage("");
			tempErrorDef.setLoc_errorMessage("");
		}
		
		errorDef = new ErrorDef();
		errorDef.setErrorCode(tempErrorDef.getErrorCode());
		
		String korTempMsg = tempErrorDef.getKor_errorMessage();
		String engTempMsg = tempErrorDef.getEng_errorMessage();
		String chaTempMsg = tempErrorDef.getCha_errorMessage();
		String locTempMsg = tempErrorDef.getLoc_errorMessage();
		
		errorDef.setKor_errorMessage(korTempMsg);
		errorDef.setEng_errorMessage(engTempMsg);
		errorDef.setCha_errorMessage(chaTempMsg);
		errorDef.setLoc_errorMessage(locTempMsg);
		
		if(log.isErrorEnabled())
		{
			log.error(String.format("[%s]%s", errorDef.getErrorCode(), errorDef.getLoc_errorMessage()));
		}
	}

	public CustomException(Throwable cause) {
		
		if (cause instanceof CustomException)
			return;
		
		errorDef = new ErrorDef();
		errorDef.setErrorCode("UndefinedCode");
		
		if (cause!=null && cause.getStackTrace() != null)
		{
			List<StackTraceElement>traceElementList = new ArrayList<>();
			
			for (StackTraceElement element : cause.getStackTrace())
			{
				if (element.getClassName().contains("kr.co.aim.messolution") || element.getClassName().contains("kr.co.aim.greenframe"))
					traceElementList.add(element);
			}

			String errorMessage = "RuntimeException: [" + (cause == null ? "null]" : cause.toString() + "]");
			String messageBody = traceElementList.size() > 0 ? traceElementList.get(0).toString() : "";
			StringBuilder comment = new StringBuilder(errorMessage).append(" ").append(messageBody);

			errorDef.setKor_errorMessage(comment.toString());
			errorDef.setEng_errorMessage(comment.toString());
			errorDef.setCha_errorMessage(comment.toString());
			errorDef.setLoc_errorMessage(comment.toString());
			
			if(traceElementList.size() > 0)
			this.setStackTrace(traceElementList.toArray(new StackTraceElement[traceElementList.size()]));
		}
		else
		{
			errorDef.setKor_errorMessage("");
			errorDef.setEng_errorMessage("");
			errorDef.setCha_errorMessage("");
			errorDef.setLoc_errorMessage(cause == null ? "":cause.getMessage());
		}
		
		if(log.isErrorEnabled())
		{
			log.error(String.format("[%s]%s", errorDef.getErrorCode(), errorDef.getLoc_errorMessage()));
		}
	}
}
