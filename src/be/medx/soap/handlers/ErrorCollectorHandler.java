package be.medx.soap.handlers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ErrorCollectorHandler implements ErrorHandler {
	private static final Logger LOG;
	public static final String WARNING = "WARN";
	public static final String ERROR = "ERROR";
	public static final String FATAL = "FATAL";
	private XOPValidationHandler xopHandler;
	private List<String> exceptionWarningList;
	private List<String> exceptionErrorList;
	private List<String> exceptionFatalList;

	public ErrorCollectorHandler() {
		this.exceptionWarningList = new ArrayList<String>();
		this.exceptionErrorList = new ArrayList<String>();
		this.exceptionFatalList = new ArrayList<String>();
	}

	public ErrorCollectorHandler(final XOPValidationHandler xopHandler) {
		this.exceptionWarningList = new ArrayList<String>();
		this.exceptionErrorList = new ArrayList<String>();
		this.exceptionFatalList = new ArrayList<String>();
		this.xopHandler = xopHandler;
	}

	@Override
	public void warning(final SAXParseException exception) throws SAXException {
		final String msg = "WARNING " + this.toString(exception);
		this.exceptionWarningList.add(msg);
	}

	@Override
	public void error(final SAXParseException exception) throws SAXException {
		if (this.accept(exception)) {
			final String msg = "ERROR " + this.toString(exception);
			this.exceptionErrorList.add(msg);
		}
	}

	@Override
	public void fatalError(final SAXParseException exception) throws SAXException {
		if (this.accept(exception)) {
			final String msg = "FATAL " + this.toString(exception);
			this.exceptionFatalList.add(msg);
		}
	}

	private String toString(final SAXParseException exception) {
		return exception.getMessage();
	}

	public final List<String> getExceptionList(final String... errorType) {
		final List<String> exceptionList = new ArrayList<String>();
		if (ArrayUtils.contains(errorType, "WARN")) {
			exceptionList.addAll(this.exceptionWarningList);
		}
		if (ArrayUtils.contains(errorType, "ERROR")) {
			exceptionList.addAll(this.exceptionErrorList);
		}
		if (ArrayUtils.contains(errorType, "FATAL")) {
			exceptionList.addAll(this.exceptionFatalList);
		}
		return exceptionList;
	}

	public final boolean hasExceptions(final String... errorType) {
		return (ArrayUtils.contains(errorType, "WARN") && !this.isEmpty(this.exceptionWarningList)) || (ArrayUtils.contains(errorType, "ERROR") && !this.isEmpty(this.exceptionErrorList)) || (ArrayUtils.contains(errorType, "FATAL") && !this.isEmpty(this.exceptionFatalList));
	}

	private boolean accept(final SAXParseException ex) {
		if (this.xopHandler != null && this.xopHandler.isXop()) {
			ErrorCollectorHandler.LOG.debug("XOP element detected, skipping error [" + ex.getMessage() + "]");
			return false;
		}
		return true;
	}

	private boolean isEmpty(final List<?> inputList) {
		return inputList.size() <= 0;
	}

	static {
		LOG = LoggerFactory.getLogger(ErrorCollectorHandler.class);
	}
}
