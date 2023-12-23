package kim.zhyun.serveruser.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

@Slf4j
public class PrintLog <Repository> {
    Repository repository;
    
    public PrintLog(Repository repository) {
        this.repository = repository;
    }
    
    private <T> void print(List<T> data) {
        data.forEach(item -> log.info("\t\t{}", item));
    }
    
    @AfterEach
    @BeforeEach
    public void printLog() {
        log.info("💁 All Data Logging ------------------------------------------------------------------------------------------------------------------------------------------------------------┐");
        
        if (repository instanceof RoleRepository)
            print(((RoleRepository) repository).findAll());
        
        if (repository instanceof UserRepository)
            print(((UserRepository) repository).findAll());
        
        log.info("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------┘");
    }
    
}
