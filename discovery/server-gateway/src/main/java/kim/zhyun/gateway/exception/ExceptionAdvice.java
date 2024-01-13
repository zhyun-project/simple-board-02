package kim.zhyun.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.yaml.snakeyaml.util.UriEncoder;
import reactor.core.publisher.Mono;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Flux.just;

@Order(-1)
@Component
public class ExceptionAdvice implements ErrorWebExceptionHandler {
    
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable e) {
        ServerHttpResponse response = exchange.getResponse();
        
        if (response.isCommitted()) {
            return Mono.error(e);
        }
        
        response.getHeaders().setContentType(APPLICATION_JSON);
        
        if (e instanceof ResponseStatusException)
            response.setStatusCode(((ResponseStatusException) e).getStatusCode());
        
        Object exceptionResponse;
        String[] exceptionMessage = { e.getMessage() };
        
        String[] splitMessage = e.getMessage().split(" ");
        String requestPath = UriEncoder.decode(splitMessage[splitMessage.length - 1].split("\\.")[0]);
        
        exceptionResponse = new Object() {
            public final boolean status = false;
            public final String message = splitMessage[0].contains("404") ? "🚨 정의되지 않은 path 입니다. --> /%s".formatted(requestPath)
                    : splitMessage[0].startsWith("4") ? "🚨 server exception ! request path --> /%s".formatted(requestPath)
                    : splitMessage[0].startsWith("5") ? "🚨 client exception ! request path --> /%s".formatted(requestPath)
                    : e.getMessage();
        };
        
        String error = "Gateway Error";
        try {
            error = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(exceptionResponse);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
        
        byte[] bytes = error.getBytes(UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(just(buffer));
    }
    
}