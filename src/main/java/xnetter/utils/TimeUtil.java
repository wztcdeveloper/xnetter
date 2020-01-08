package xnetter.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具类
 * @author majikang
 * @create 2019-11-05
 */
public final class TimeUtil {

	public static final String FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
	public static final String FORMAT_PATTERN_MILLI = "yyyy-MM-dd HH:mm:ss.SSS";
	private TimeUtil() {
		
	}
	
	public static long now() {
		return LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
	}
	
	public static long nowWithMilli() {
		return LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
	}
	
	public static String nowFormat() {
		return DateTimeFormatter.ofPattern(FORMAT_PATTERN).format(LocalDateTime.now());
	}
	
	public static String nowFormatWithMilli() {
		return DateTimeFormatter.ofPattern(FORMAT_PATTERN_MILLI).format(LocalDateTime.now());
	}
	
	public static String toString(long time) {
		LocalDateTime ldt = LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.of("+8"));
		return DateTimeFormatter.ofPattern(FORMAT_PATTERN).format(ldt);
	}
	
	public static String toStringWithMilli(long time) {
		LocalDateTime ldt = LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.of("+8"));
		return DateTimeFormatter.ofPattern(FORMAT_PATTERN_MILLI).format(ldt);
	}
	
	public static long toLong(String timeStr) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(FORMAT_PATTERN);
	    LocalDateTime time = LocalDateTime.parse(timeStr, dateTimeFormatter);
	    return time.toEpochSecond(ZoneOffset.of("+8"));
	}
	
	public static long toLongWithMilli(String timeStr) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(FORMAT_PATTERN_MILLI);
	    LocalDateTime time = LocalDateTime.parse(timeStr, dateTimeFormatter);
	    return time.toEpochSecond(ZoneOffset.of("+8"));
	}
	
}
