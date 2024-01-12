package kim.zhyun.serveruser.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {
    
    @Value("${spring.mail.protocol}")               private String protocol;
    @Value("${spring.mail.host}")                   private String host;
    @Value("${spring.mail.port}")                   private int port;
    @Value("${spring.mail.username}")               private String id;
    @Value("${spring.mail.password}")               private String password;
    @Value("${spring.mail.default-encoding}")       private String encoding;
    
    @Value("${spring.mail.properties.debug}")                       private String debug;
    @Value("${spring.mail.properties.mail.smtp.auth}")              private String auth;
    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")   private String starttls;
    @Value("${spring.mail.properties.mail.smtp.ssl.enable}")        private String sslEnable;
    @Value("${spring.mail.properties.mail.smtp.ssl.trust}")         private String sslTrust;
    
    @Bean
    public JavaMailSender javaMailService() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setUsername(id);
        sender.setPassword(password);
        sender.setPort(port);
        sender.setJavaMailProperties(getMailProperties());
        sender.setDefaultEncoding(encoding);
        return sender;
    }
    
    private Properties getMailProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", protocol);
        properties.setProperty("mail.smtp.auth", auth);
        properties.setProperty("mail.smtp.starttls.enable", starttls);
        properties.setProperty("mail.debug", debug);
        properties.setProperty("mail.smtp.ssl.trust", sslTrust);
        properties.setProperty("mail.smtp.ssl.enable", sslEnable);
        return properties;
    }

}
