package foodcards.adapter.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.regex.Matcher;
import static foodcards.adapter.utils.Constants.TIME_PATTERN;
import static foodcards.adapter.utils.Constants.DEFAULT_SLEEP_TIME;

@Slf4j
@Component
public class TimeParserUtil {
    public long parseToMillis(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return parseToMillis(DEFAULT_SLEEP_TIME);
        }

        long total = 0;
        Matcher matcher = TIME_PATTERN.matcher(timeStr);

        while (matcher.find()) {
            long value = Long.parseUnsignedLong(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();

            switch (unit) {
                case "hour": case "h": total += value * 3600000; break;
                case "min": case "m": total += value * 60000; break;
                case "sec": case "s": total += value * 1000; break;
            }
        }
        return total > 0 ? total : parseToMillis(DEFAULT_SLEEP_TIME);
    }
}