package kim.zhyun.serverarticle;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class ServerArticleApplicationTest {
    
    @Test
    void main_test() {
        assertThat("article hi!").isNotBlank();
    }
    
}
