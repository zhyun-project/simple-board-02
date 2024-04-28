package kim.zhyun.serveruser.repository;

import kim.zhyun.serveruser.domain.member.repository.UserRepository;
import kim.zhyun.serveruser.domain.signup.repository.RoleRepository;
import kim.zhyun.serveruser.domain.signup.repository.SessionUserRepository;
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
    
    private <T> void print(Iterable<T> data) {
        data.forEach(item -> log.info("\t\t{}", item));
    }
    
    private <T> void print(List<T> data) {
        data.forEach(item -> log.info("\t\t{}", item));
    }
    
    @AfterEach
    @BeforeEach
    public void printLog() {
        log.info("");
        log.info("💁 All Data Logging ------------------------------------------------------------------------------------------------------------------------------------------------------------┐");
        
        if (repository instanceof RoleRepository) {
            print(((RoleRepository) repository).findAll());
        }
        else if (repository instanceof UserRepository) {
            print(((UserRepository) repository).findAll());
        }
        else if (repository instanceof SessionUserRepository) {
            print(((SessionUserRepository) repository).findAll());
            ((SessionUserRepository) repository).deleteAll();
        }
        
        log.info("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------┘");
        log.info("");
    }
    
}
