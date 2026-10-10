package foodcards.adapter.utils;

import org.springframework.beans.factory.annotation.Value;

import java.util.regex.Pattern;

public class Constants {

    // Паттерн для парсинга времени (поддерживает "5 sec", "10 min", "1 hour")
    public static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)\\s*(hour|min|sec|h|m|s)", Pattern.CASE_INSENSITIVE);


    public static final String DEFAULT_SLEEP_TIME = "5 sec";

    // Статусы бизнес-логики
    public static final String STATUS_WAIT = "WAIT";
    public static final String STATUS_PROGRESS = "PROGRESS";
    public static final String STATUS_SENT_TO_KAFKA = "SENT_TO_KAFKA";
    public static final String STATUS_PROCESSED = "PROCESSED";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_ERROR = "ERROR";


    // Направления сообщений
    public static final String DIR_OUT = "OUT";
    public static final String DIR_IN = "IN";

    // Системные константы
    public static final String SYSTEM_ID = "GRU";
    public static final String BATCH_ERROR = "Batch Error";
}
