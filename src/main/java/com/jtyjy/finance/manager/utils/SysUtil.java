package com.jtyjy.finance.manager.utils;
import java.util.Date;


public class SysUtil {
	private static long currentId = 0L;
	private static byte serverId0;
	private static byte serverId1;
	private static Object lock = new Object();
	public static final byte[] digits = new byte[]{48, 49, 50, 51, 52, 53, 54,
			55, 56, 57, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78,
			79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90};

	public static String getId() {
		long id;
		synchronized (lock) {
			if (currentId == 0L) {
				currentId = (new Date()).getTime() * 10000L;
				String serverId = "00";
				serverId0 = (byte) serverId.charAt(0);
				serverId1 = (byte) serverId.charAt(1);
			}
			id = (long) (currentId++);
		}

		return numToString(id);
	}

	private static String numToString(long num) {
		byte[] buf = new byte[13];
		byte charPos = 12;
		buf[0] = serverId0;

		long val;
		for (buf[1] = serverId1; (val = num / 36L) > 0L; num = val) {
			buf[charPos--] = digits[(byte) ((int) (num % 36L))];
		}

		buf[charPos] = digits[(byte) ((int) num)];
		return new String(buf);
	}

}
