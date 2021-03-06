package openwt.interview.coding.challenge.web.error.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import openwt.interview.coding.challenge.web.error.ControllerError;
import openwt.interview.coding.challenge.web.error.ElementNotFoundException;

@RestController
@ControllerAdvice
public class DefaultResponseEntityExceptionHandler {
	
	@ExceptionHandler(ElementNotFoundException.class)
	public final ResponseEntity<ControllerError> handleContactNotFoundException(ElementNotFoundException ex, WebRequest request) {
	    ControllerError ControllerError = new ControllerError(ex.getMessage(),
	        request.getDescription(false));
	    return new ResponseEntity<ControllerError>(ControllerError, HttpStatus.NOT_FOUND);
	  }
	
	@ExceptionHandler(AccessDeniedException.class)
	public final ResponseEntity<ControllerError> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
	    ControllerError ControllerError = new ControllerError(ex.getMessage(),
	        request.getDescription(false));
	    return new ResponseEntity<ControllerError>(ControllerError, HttpStatus.FORBIDDEN);
	  }
}
