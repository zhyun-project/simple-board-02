package kim.zhyun.serveruser.utils;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static java.time.temporal.ChronoUnit.*;
import static kim.zhyun.jwt.util.TimeUnitUtil.timeUnitFrom;

@Component
public class DateTimeUtil {
    @Value("${withdrawal.expiration-time}") private static long time;
    @Value("${withdrawal.expiration-time-unit}") private static String unitString;
    
    public DateTimeUtil(@Value("${withdrawal.expiration-time}") long time,
                        @Value("${withdrawal.expiration-time-unit}") String unitString) {
        DateTimeUtil.time = time;
        DateTimeUtil.unitString = unitString;
    }
    
    /**
     * 타겟 시간 이후로 몇일, 몇시간, 몇분, 몇초 지났는지 반환
     */
    public static DateTimePeriodDto dateTimeCalculate(LocalDateTime targetDateTime) {
        return DateTimePeriodDto.calculatePeriod(LocalDateTime.now(), targetDateTime);
    }
    
    /**
     * 지금으로부터 application.yml 설정파일에서 읽어온 시간 전의 LocalDateTime 반환
     */
    public static LocalDateTime beforeDateTime() {
        return LocalDateTime.now().minus(time, timeUnitFrom(unitString));
    }

    
    public record DateTimePeriodDto(
            long days,
            long hours,
            long minutes,
            long seconds
    ) {
        public static DateTimePeriodDto calculatePeriod(LocalDateTime now, LocalDateTime targetDateTime) {
            LocalDate withdrawalDate = targetDateTime.toLocalDate();
            LocalTime withdrawalTime = targetDateTime.toLocalTime();
            LocalDate nowDate = now.toLocalDate();
            LocalTime nowTime = now.toLocalTime();
            
            return new DateTimePeriodDto(
                    DAYS.between(withdrawalDate, nowDate),
                    HOURS.between(withdrawalTime, nowTime),
                    MINUTES.between(withdrawalTime, nowTime),
                    SECONDS.between(withdrawalTime, nowTime));
        }
    }
}
