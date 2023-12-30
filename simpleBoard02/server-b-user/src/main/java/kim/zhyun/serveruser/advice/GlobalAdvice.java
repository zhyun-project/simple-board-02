package kim.zhyun.serveruser.advice;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import kim.zhyun.serveruser.data.response.ApiResponse;
import kim.zhyun.serveruser.data.response.ValidExceptionResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

import static kim.zhyun.serveruser.data.message.ExceptionMessage.REQUIRED_REQUEST_BODY;
import static kim.zhyun.serveruser.data.message.ExceptionMessage.VALID_EXCEPTION;

@RestControllerAdvice
public class GlobalAdvice extends ResponseEntityExceptionHandler {
    
    /**
     * custom Exception
     */
    @ExceptionHandler({
            MailAuthException.class,
            SignUpException.class})
    public ResponseEntity<Object> mailException(RuntimeException e) {
        return ResponseEntity
                .badRequest().body(ApiResponse.<List<ValidExceptionResponse>>builder()
                        .status(false)
                        .message(e.getMessage()).build());
    }
    
    /**
     * @Validate Exception
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> constraintViolationException(ConstraintViolationException e) {
        List<ValidExceptionResponse> errorList = new ArrayList<>();
        
        e.getConstraintViolations()
                .forEach(error -> {
                    List<Path.Node> list = StreamSupport
                            .stream(error.getPropertyPath().spliterator(), false)
                            .toList();
                    
                    String field = list.get(list.size()-1).getName();
                    String message = error.getMessage();
                    
                    errorList.add(ValidExceptionResponse.builder()
                            .field(field)
                            .message(message).build());
                });
        
        Collections.sort(errorList);
        
        return ResponseEntity
                .badRequest().body(ApiResponse.<List<ValidExceptionResponse>>builder()
                        .status(false)
                        .message(VALID_EXCEPTION)
                        .result(errorList).build());
    }
    
    /**
     * @Valid Exception
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<ValidExceptionResponse> list = new ArrayList<>();
        
        ex.getBindingResult()
                .getAllErrors()
                .forEach(objectError -> {
                    FieldError field = (FieldError) objectError;
                    String message = objectError.getDefaultMessage();
                    
                    list.add(ValidExceptionResponse.builder()
                            .field(field.getField())
                            .message(message).build());
                });
        
        Collections.sort(list);
        
        return ResponseEntity
                .badRequest().body(ApiResponse.<List<ValidExceptionResponse>>builder()
                        .status(false)
                        .message(VALID_EXCEPTION)
                        .result(list).build());
    }
    
    /**
     * @RequestBody is null
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        return ResponseEntity
                .badRequest().body(ApiResponse.<List<ValidExceptionResponse>>builder()
                        .status(false)
                        .message(REQUIRED_REQUEST_BODY).build());
    }
    
}
