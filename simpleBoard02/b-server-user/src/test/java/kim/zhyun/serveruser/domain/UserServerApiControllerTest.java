package kim.zhyun.serveruser.domain;

import kim.zhyun.jwt.filter.JwtFilter;
import kim.zhyun.serveruser.config.TestSecurityConfig;
import kim.zhyun.serveruser.filter.AuthenticationFilter;
import kim.zhyun.serveruser.filter.SessionCheckFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@Import(TestSecurityConfig.class)
@WebMvcTest(
        controllers = UserServerApiController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        AuthenticationFilter.class,
                        JwtFilter.class,
                        SessionCheckFilter.class
                }
        )
)
class UserServerApiControllerTest {

    @Autowired
    MockMvc mvc;


    @Test
    void runningTest() throws Exception {
        mvc.perform(get("/"))
                .andExpect(redirectedUrl("http://localhost:8080/api/user/swagger-ui/index.html"))
                .andDo(print());
    }
}