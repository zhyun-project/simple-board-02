package kim.zhyun.serveruser.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class DateTimeUtil {
    
    public static DateTimePeriodDto dateTimeCalculate(LocalDateTime targetDateTime) {
        return DateTimePeriodDto.calculatePeriod(LocalDateTime.now(), targetDateTime);
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
                    ChronoUnit.DAYS.between(withdrawalDate, nowDate),
                    ChronoUnit.HOURS.between(withdrawalTime, nowTime),
                    ChronoUnit.MINUTES.between(withdrawalTime, nowTime),
                    ChronoUnit.SECONDS.between(withdrawalTime, nowTime));
        }
    }
}
