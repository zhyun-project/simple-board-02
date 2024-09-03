package kim.zhyun.jwt.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import kim.zhyun.jwt.common.model.ApiResponse;

import java.io.IOException;

public class FilterApiResponseUtil {
    public static void sendMessage(HttpServletResponse response, int httpStatus, boolean reqStatus, String message) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(new ObjectMapper().writeValueAsString(ApiResponse.<Void>builder()
                .status(reqStatus)
                .message(message).build()));
    }

    public static <T> void sendMessage(HttpServletResponse response, int httpStatus, boolean reqStatus, String message, T result) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(new ObjectMapper().writeValueAsString(ApiResponse.<T>builder()
                .status(reqStatus)
                .message(message)
                .result(result).build()));
    }
}
