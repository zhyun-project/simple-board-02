package kim.zhyun.serveruser.container;


import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class RedisTestContainer implements BeforeAllCallback {
    
    private static final String REDIS_IMAGE = "redis";
    private static final int REDIS_PORT = 6379;
    
    @Container
    private GenericContainer redis;
    
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        redis = new GenericContainer(DockerImageName.parse(REDIS_IMAGE))
                .withExposedPorts(REDIS_PORT);
        redis.start();
        
        System.setProperty("spring.data.redis.host", redis.getHost());
        System.setProperty("spring.data.redis.port", String.valueOf(redis.getMappedPort(REDIS_PORT)));
    }
    
}

