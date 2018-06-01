package openwt.interview.coding.challenge.web.error.handlers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import openwt.interview.coding.challenge.web.error.ControllerError;

@RestController
@ControllerAdvice
public class GlobalWebExceptionHandler extends ResponseEntityExceptionHandler {

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		ControllerError ControllerError = new ControllerError("Validation Error",buildNotValidExceptionMsg(ex));
		return new ResponseEntity<Object>(ControllerError, HttpStatus.BAD_REQUEST);
	}
	
	private String buildNotValidExceptionMsg(MethodArgumentNotValidException ex) {
		StringBuilder sb = new StringBuilder();
		sb.append("validation on argument ").append(ex.getParameter().getParameterName()).append(" failed with errors: ");		
		for (FieldError error: ex.getBindingResult().getFieldErrors()) {
			sb.append("[").append("field '"+error.getField()+"' doesnt accept value '"+error.getRejectedValue()+"' ("+error.getDefaultMessage()+")").append("]");
		}
		return sb.toString();
	}


	@ExceptionHandler(Exception.class)
	public final ResponseEntity<ControllerError> handleAllExceptions(Exception ex, WebRequest request) {
		ControllerError ControllerError = new ControllerError(ex.getMessage(), request.getDescription(false));
		return new ResponseEntity<ControllerError>(ControllerError, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
