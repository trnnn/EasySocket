package easysocket.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtil {
	private static final Logger logger = LoggerFactory
			.getLogger(StringUtil.class);
	// static String[] array = { "'", ";", "--", "|", ":", "+", "-", "=", "\"",
	// "/",
	// "select", "update", "delete", "insert", "shutdown", "exec",
	// "drop", "declear", "%20" };
	static String[] array = { "select", "update", "delete", "insert",
			"shutdown", "exec", "drop", "declear", "%20" };
	static final Charset defaultCharset = Charset.forName("UTF-8");

	public static String getString(ByteBuffer bb) {
		final short length = bb.getShort();
		if (length == 0) {
			return "";
		}
		byte[] str = new byte[length];
		bb.get(str, 0, length);
		return new String(str, defaultCharset);
	}

	public static void putString(ByteBuffer bb, String str) {
		if (str == null || str.equals("")) {
			bb.putShort((short) 0);
			return;
		}
		short size = (short) str.getBytes(defaultCharset).length;
		bb.putShort(size);
		if (size > 0)
			bb.put(str.getBytes(defaultCharset));
	}

	public static boolean isDenyString(String text) {
		if (null == text) {
			return true;
		}

		text = text.toLowerCase();
		for (String str : array) {
			if (text.contains(str)) {
				return true;
			}
		}
		return false;
	}

	public static String removeWord(List<String> removeWord, String text) {
		if (null == text) {
			return null;
		}

		for (String word : removeWord) {
			text = text.replaceAll(word, "");
		}
		return text;
	}

	public static String removeChar(String msg) {
		msg = msg.replace("(", "");
		msg = msg.replace(")", "");
		msg = msg.replace("<", "");
		msg = msg.replace(">", "");
		msg = msg.replace("&", "");
		msg = msg.replace("\"", "");
		msg = msg.replace("'", "");
		msg = msg.replace(";", "");
		msg = msg.replace("--", "");
		msg = msg.replace("|", "");
		msg = msg.replace(":", "");
		msg = msg.replace("+", "");
		msg = msg.replace("-", "");
		msg = msg.replace("=", "");
		msg = msg.replace("/", "");
		return msg;
	}

	public static boolean isValidUserName(final int maxLength, String name) {
		if ((null == name) || (0 >= name.length())) {
			String msg = String
					.format("StringUtil.isValidUserName, name is null");
			logger.error(msg);
			return false;
		}

		if (maxLength < name.length()) {
			String msg = String
					.format("StringUtil.isValidUserName, name is max length over, maxLength:%d, name:[%s], length:%d",
							maxLength, name, name.length());
			logger.error(msg);
			return false;
		}

		String patternStr = String.format("[a-zA-Z0-9]{1,%d}", maxLength);
		Pattern p = Pattern.compile(patternStr);
		Matcher m = p.matcher(name);
		if (m.matches()) {
			return true;
		}

		String msg = String.format(
				"StringUtil.isValidUserName, name is Not Valid, name:[%s]",
				name);
		logger.trace(msg);
		return false;
	}

	public static boolean isValidGuildName(final int maxLength, String name) {
		if ((null == name) || (0 >= name.length())) {
			String msg = "StringUtil.isValidGuildName, name is null";
			logger.error(msg);
			return false;
		}

		if (maxLength < name.length()) {
			String msg = String
					.format("StringUtil.isValidGuildName, name is max length over, maxLength:%d, name:%s, length:%d",
							maxLength, name, name.length());
			logger.error(msg);
			return false;
		}

		String patternStr = String.format("[a-zA-Z ]{1,%d}", maxLength);
		Pattern p = Pattern.compile(patternStr);
		Matcher m = p.matcher(name);
		if (m.matches()) {
			return true;
		}

		String msg = String
				.format("StringUtil.isValidGuildName, name is Not Valid, name:%s",
						name);
		logger.error(msg);
		return false;
	}

	public static String urlencode(String str) {
		try {
			return URLEncoder.encode(str, "utf-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("StringUtil.urlencode, msg:{}, str:{}",
					e.getMessage(), str);
			return null;
		} catch (Exception e) {
			logger.error("StringUtil.urlencode, msg:{}, str:{}",
					e.getMessage(), str);
			return null;
		}
	}

	public static String urldecode(String str) {
		try {
			return URLDecoder.decode(str, "utf-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("StringUtil.urlencode, msg:{}, str:{}",
					e.getMessage(), str);
			return null;
		} catch (Exception e) {
			logger.error("StringUtil.urlencode, msg:{}, str:{}",
					e.getMessage(), str);
			return null;
		}
	}

	public static boolean isValidNumber(String str) {
		if ((null == str) || (0 >= str.length())) {
			String msg = String
					.format("StringUtil.isValidNumber, name is null");
			logger.error(msg);
			return false;
		}

		String patternStr = String.format("[0-9]{1,%d}", str.length());
		Pattern p = Pattern.compile(patternStr);
		Matcher m = p.matcher(str);
		if (m.matches()) {
			return true;
		}

		String msg = String.format(
				"StringUtil.isValidNumber, name is Not Valid, str:%s", str);
		logger.trace(msg);
		return false;
	}

	public static int getDataSize(String str) {
		int sum = 2;
		sum += ((str == null || str.equals("")) ? 0 : str.length());
		return sum;
	}
}
