package kim.zhyun.jwt.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import kim.zhyun.jwt.common.model.ApiResponse;
import kim.zhyun.jwt.exception.model.ValidExceptionResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

import static kim.zhyun.jwt.exception.message.CommonExceptionMessage.EXCEPTION_REQUIRED_REQUEST_BODY;
import static kim.zhyun.jwt.exception.message.CommonExceptionMessage.EXCEPTION_VALID_FORMAT;

@RestControllerAdvice(basePackages = {
        "kim.zhyun.jwt",
        "kim.zhyun.serveruser",
        "kim.zhyun.serverarticle"
})
public class GlobalAdvice extends ResponseEntityExceptionHandler {
    
    /**
     * custom Exception
     */
    @ExceptionHandler({
            ApiException.class,
            MailSenderException.class,
            UsernameNotFoundException.class})
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
                        .message(EXCEPTION_VALID_FORMAT)
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
                        .message(EXCEPTION_VALID_FORMAT)
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
                        .message(EXCEPTION_REQUIRED_REQUEST_BODY).build());
    }
    
}
