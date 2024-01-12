package kim.zhyun.jwt.util;

import java.time.temporal.ChronoUnit;

import static java.time.temporal.ChronoUnit.*;

public class TimeUnitUtil {
    
    /**
     * TimeUnit 반환
     */
    public static ChronoUnit timeUnitFrom(String unitString) {
        return unitString.equalsIgnoreCase("d") ? DAYS
                : unitString.equalsIgnoreCase("h") ? HOURS
                : unitString.equalsIgnoreCase("m") ? MINUTES
                : SECONDS;
    }

}
