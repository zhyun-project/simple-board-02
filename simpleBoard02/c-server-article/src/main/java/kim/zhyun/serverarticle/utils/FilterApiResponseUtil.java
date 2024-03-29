package kim.zhyun.serverarticle.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import kim.zhyun.serverarticle.data.response.ApiResponse;

import java.io.IOException;

public class FilterApiResponseUtil {
    public static void sendMessage(HttpServletResponse response, int httpStatus, boolean reqStatus, String message) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(new ObjectMapper().writeValueAsString(ApiResponse.builder()
                .status(reqStatus)
                .message(message).build()));
    }
}
