package kim.zhyun.serverarticle.domain;

import kim.zhyun.jwt.filter.JwtFilter;
import kim.zhyun.serverarticle.config.TestSecurityConfig;
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
        controllers = ArticleServerApiController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        JwtFilter.class
                }
        )
)
class ArticleServerApiControllerTest {

    @Autowired
    MockMvc mvc;


    @Test
    void runningTest() throws Exception {
        mvc.perform(get("/"))
                .andExpect(redirectedUrl("http://localhost:8080/api/article/swagger-ui/index.html"))
                .andDo(print());
    }
}