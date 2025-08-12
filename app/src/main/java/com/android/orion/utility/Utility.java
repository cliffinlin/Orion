package com.android.orion.utility;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.android.orion.database.Stock;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class Utility {

	public static final String CALENDAR_DATE_FORMAT = "yyyy-MM-dd";
	public static final String CALENDAR_TIME_FORMAT = "HH:mm:ss";
	public static final String CALENDAR_DATE_TIME_FORMAT = CALENDAR_DATE_FORMAT
			+ " " + CALENDAR_TIME_FORMAT;

	static Logger Log = Logger.getLogger();

	private Utility() {
	}

	public static boolean isNetworkConnected(Context context) {
		boolean result = false;
		ConnectivityManager connectivityManager = null;
		NetworkInfo networkInfo = null;

		if (context == null) {
			return result;
		}

		connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivityManager != null) {
			if (Setting.getDebugWifi()) {
				networkInfo = connectivityManager
						.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

				if (networkInfo != null && networkInfo.isConnected()) {
					return true;
				}
			}

			networkInfo = connectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

			if (networkInfo != null && networkInfo.isConnected()) {
				return true;
			}
		}

		return result;
	}

	public static String getCalendarString(Calendar calendar, String format) {
		String result = "";

		if (calendar == null) {
			return result;
		}

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format,
				Locale.getDefault());

		result = simpleDateFormat.format(calendar.getTime());

		return result;
	}

	public static String getCalendarDateString(Calendar calendar) {
		return getCalendarString(calendar, CALENDAR_DATE_FORMAT);
	}

	public static String getCalendarTimeString(Calendar calendar) {
		return getCalendarString(calendar, CALENDAR_TIME_FORMAT);
	}

	public static String getCalendarDateTimeString(Calendar calendar) {
		return getCalendarString(calendar, CALENDAR_DATE_TIME_FORMAT);
	}

	public static String getCurrentDateString() {
		return getCalendarString(Calendar.getInstance(), CALENDAR_DATE_FORMAT);
	}

	public static String getCurrentTimeString() {
		return getCalendarString(Calendar.getInstance(), CALENDAR_TIME_FORMAT);
	}

	public static String getCurrentDateTimeString() {
		return getCalendarString(Calendar.getInstance(),
				CALENDAR_DATE_TIME_FORMAT);
	}

	public static Calendar getCalendar(String string, String format) {
		Calendar calendar = Calendar.getInstance();

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format,
				Locale.getDefault());

		Date data = null;

		if (TextUtils.isEmpty(string) || string.contains(Stock.STATUS_SUSPENSION)) {
			return calendar;
		}

		try {
			data = simpleDateFormat.parse(string);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (data != null) {
			calendar.setTime(data);
		}

		return calendar;
	}

	public static long getMilliSeconds(String dateTime) {
		long nMilliSeconds = 0;
		Date date = null;

		SimpleDateFormat formatter = new SimpleDateFormat(
				CALENDAR_DATE_TIME_FORMAT, Locale.getDefault());

		try {
			date = formatter.parse(dateTime);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (date != null) {
			nMilliSeconds = date.getTime();
		}

		return nMilliSeconds;
	}

	public static long getMilliSeconds(String data, String time) {
		String dateTime;

		long result = 0;

		if (TextUtils.isEmpty(data)) {
			return result;
		}

		if (TextUtils.isEmpty(time)) {
			time = "00:00:00";
		}

		dateTime = data + " " + time;

		result = getMilliSeconds(dateTime);

		return result;
	}

	public static double getJsonObjectDouble(JSONObject jsonObject, String key) {
		double result = 0;

		try {
			if (jsonObject != null) {
				if (jsonObject.has(key)) {
					result = jsonObject.getDouble(key);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static int getJsonObjectInt(JSONObject jsonObject, String key) {
		int result = 0;

		try {
			if (jsonObject != null) {
				if (jsonObject.has(key)) {
					result = jsonObject.getInt(key);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static String getJsonObjectString(JSONObject jsonObject, String key) {
		String result = "";

		try {
			if (jsonObject != null) {
				if (jsonObject.has(key)) {
					result = jsonObject.getString(key);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static double Round(double v, double n) {
		double p = Math.pow(10, n);
		return (Math.round(v * p)) / p;
	}

	public static double Round2(double v) {
		double p = Math.pow(10, Constant.DOUBLE_FIXED_DECIMAL_2);
		return (Math.round(v * p)) / p;
	}

	public static double Round4(double v) {
		double p = Math.pow(10, Constant.DOUBLE_FIXED_DECIMAL_4);
		return (Math.round(v * p)) / p;
	}

	public static String getFileNameFromContentUri(Context context, Uri uri) {
		if (uri == null) return null;

		Cursor cursor = null;
		String fileName = null;
		try {
			fileName = null;
			String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
			ContentResolver contentResolver = context.getContentResolver();
			cursor = contentResolver.query(uri, projection, null, null, null);

			if (cursor != null) {
				if (cursor.moveToFirst()) {
					int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
					fileName = cursor.getString(columnIndex);
				}
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return fileName;
	}

	public static boolean isUriWritable(Context context, Uri uri) {
		boolean result = false;
		if (uri == null) {
			return result;
		}

		if (!ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
			return result;
		}

		OutputStream outputStream = null;
		try {
			outputStream = context.getContentResolver().openOutputStream(uri);
			if (outputStream != null) {
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeQuietly(outputStream);
		}
		return result;
	}

	public static void closeQuietly(AutoCloseable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean isFileExist(String path) {
		return isFileExist(path, false);
	}

	public static boolean isDirectoryExist(String path) {
		return isFileExist(path, true);
	}

	public static boolean isFileExist(String path, boolean isDirectory) {
		boolean result = false;

		if (TextUtils.isEmpty(path)) {
			return result;
		}

		File file = new File(path);

		if (file != null) {
			if (isDirectory) {
				result = file.exists() && file.isDirectory();
			} else {
				result = file.exists();
			}
		}

		return result;
	}

	public static String getFileExtension(String path) {
		int index = 0;
		String result = "";

		if (TextUtils.isEmpty(path)) {
			return result;
		}

		index = path.lastIndexOf(".");
		if (index == -1) {
			return result;
		}

		result = path.substring(index + 1);

		return result;
	}

	public static long getFileLength(String fileName) {
		long length = 0;
		RandomAccessFile randomAccessFile = null;

		if (TextUtils.isEmpty(fileName)) {
			return length;
		}

		try {
			randomAccessFile = new RandomAccessFile(fileName, "r");
			if (randomAccessFile != null) {
				length = randomAccessFile.length();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (randomAccessFile != null) {
				try {
					randomAccessFile.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return length;
	}

	public static boolean createFile(String path) {
		return createFile(path, false);
	}

	public static boolean createDirectory(String path) {
		return createFile(path, true);
	}

	public static boolean createFile(String path, boolean isDirectory) {
		boolean result = false;

		if (TextUtils.isEmpty(path)) {
			return result;
		}

		File file = new File(path);

		try {
			if (file != null) {
				if (isDirectory) {
					result = file.mkdirs();
				} else {
					result = file.createNewFile();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static boolean deleteFile(String path) {
		return deleteFile(path, false);
	}

	public static boolean deleteDirectory(String path) {
		return deleteFile(path, true);
	}

	public static boolean deleteFile(String path, boolean isDirectory) {
		boolean result = false;

		if (TextUtils.isEmpty(path)) {
			return result;
		}

		File file = new File(path);

		try {
			if (file != null) {
				if (isDirectory) {
					// result = file.mkdirs();
				} else {
					result = file.delete();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static void writeFile(String path, byte[] bytes, boolean append) {
		FileOutputStream fileOutputStream = null;
		BufferedOutputStream bufferedOutputStream = null;

		if (TextUtils.isEmpty(path) || (bytes == null)) {
			return;
		}

		File file = new File(path);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			fileOutputStream = new FileOutputStream(path, append);
			if (fileOutputStream != null) {
				bufferedOutputStream = new BufferedOutputStream(
						fileOutputStream);

				if (bufferedOutputStream != null) {
					bufferedOutputStream.write(bytes);
					bufferedOutputStream.flush();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bufferedOutputStream != null) {
				try {
					bufferedOutputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void writeFile(String fileName, boolean value) {
		String string;

		if (value) {
			string = "1";
		} else {
			string = "0";
		}

		writeFile(fileName, string);
	}

	public static void writeFile(String fileName, String value) {
		writeFile(fileName, value, false);
	}

	public static void writeFile(String fileName, String value, boolean append) {
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;

		if (TextUtils.isEmpty(fileName) || (value == null)) {
			return;
		}

		if (!isFileExist(fileName)) {
			createFile(fileName);
		}

		try {
			fileWriter = new FileWriter(fileName, append);

			if (fileWriter != null) {
				bufferedWriter = new BufferedWriter(fileWriter);

				if (bufferedWriter != null) {
					bufferedWriter.write(value);
					bufferedWriter.flush();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bufferedWriter != null) {
				try {
					bufferedWriter.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void writeFile(String fileName, ArrayList<String> lineList, boolean append) {
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;

		if (TextUtils.isEmpty(fileName) || lineList == null || lineList.size() == 0) {
			return;
		}

		if (!isFileExist(fileName)) {
			createFile(fileName);
		}

		try {
			fileWriter = new FileWriter(fileName, append);

			if (fileWriter != null) {
				bufferedWriter = new BufferedWriter(fileWriter);

				if (bufferedWriter != null) {
					for (int i = 0; i < lineList.size(); i++) {
						bufferedWriter.write(lineList.get(i));
					}
					bufferedWriter.flush();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bufferedWriter != null) {
				try {
					bufferedWriter.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static byte[] readFileByte(String fileName) {
		byte[] bytes = null;
		int length = 0;
		RandomAccessFile randomAccessFile = null;

		if (TextUtils.isEmpty(fileName)) {
			return bytes;
		}

		try {
			randomAccessFile = new RandomAccessFile(fileName, "r");
			if (randomAccessFile != null) {
				length = (int) randomAccessFile.length();
				if (length > 0) {
					bytes = new byte[length];
					randomAccessFile.readFully(bytes);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (randomAccessFile != null) {
				try {
					randomAccessFile.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return bytes;
	}

	public static void readFile(String fileName, ArrayList<String> lineList) {
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		String strLine;

		if (lineList == null) {
			return;
		}

		if (TextUtils.isEmpty(fileName)) {
			return;
		}

		if (!isFileExist(fileName)) {
			return;
		}

		lineList.clear();

		try {
			fileReader = new FileReader(fileName);
			if (fileReader != null) {
				bufferedReader = new BufferedReader(fileReader);
				if (bufferedReader != null) {
					while ((strLine = bufferedReader.readLine()) != null) {
						lineList.add(strLine);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String readFile(String path) {
		ArrayList<String> result = new ArrayList<>();
		readFile(path, result);
		if (result.size() > 0) {
			String value = result.get(0);
			return value;
		}
		return "";
	}

	public static <E extends Enum<E>> boolean isInEnum(String value, Class<E> enumClass) {
		if (enumClass == null) {
			return false;
		}

		for (E e : enumClass.getEnumConstants()) {
			if (TextUtils.equals(e.name(), value)) {
				return true;
			}
		}
		return false;
	}

	public static int indexOfStrings(String value, String[] strings) {
		int result = -1;

		if (value == null) {
			return result;
		}

		if (strings == null || strings.length == 0) {
			return result;
		}

		for (int i = 0; i < strings.length; i++) {
			if (TextUtils.equals(value, strings[i])) {
				result = i;
				break;
			}
		}

		return result;
	}

	public static ArrayList<String> stringToArrayList(String string) {
		if (TextUtils.isEmpty(string)) {
			return null;
		}

		ArrayList<String> result = new ArrayList<>(Arrays.asList(string
				.split(",")));

		return result;
	}

	public static String bytestoAsciiString(byte[] bytes, int offset,
	                                        int datalen) {

		if (bytes == null || bytes.length == 0 || offset < 0
				|| datalen <= 0) {
			return null;
		}
		if ((offset >= bytes.length) || (bytes.length - offset < datalen)) {
			return null;
		}
		String asciiStr = "";
		byte[] data = new byte[datalen];
		System.arraycopy(bytes, offset, data, 0, datalen);
		try {
			asciiStr = new String(data, "ISO8859-1");
		} catch (Exception e) {

		}
		return asciiStr;
	}

	public static byte[] stringToByteArray(String string) {
		byte[] result = null;

		if (TextUtils.isEmpty(string)) {
			return result;
		}

		String[] strings = string.split(" ");

		result = new byte[strings.length];

		for (int i = 0; i < strings.length; i++) {
			result[i] = (byte) Integer.parseInt(strings[i], 16);
		}

		return result;
	}

	public static String charArrayToString(char[] charArray, int offset) {
		String result = "";

		if (charArray == null) {
			return result;
		}

		if (charArray.length < 2) {
			return result;
		}

		for (int i = offset; i < charArray.length - 1; i += 2) {
			result += (char) (((charArray[i] << 8) & 0xff00) + charArray[i + 1]);
		}

		return result;
	}

	public static byte[] charArrayToByteArray(char[] charArray, int offset) {
		byte[] result = null;

		if (charArray == null) {
			return result;
		}

		if (offset > charArray.length - 1) {
			return result;
		}

		result = new byte[charArray.length - offset];

		for (int i = offset; i < charArray.length; i++) {
			result[i - offset] = (byte) charArray[i];
		}

		return result;
	}

	public static double randomDouble(double min, double max) {
		double result = 0;
		Random random = new Random(System.nanoTime());

		result = random.nextDouble() * (max - min) + min;

		return result;
	}

	public static boolean hasFlag(int value, int flag) {
		return (value & flag) == flag;
	}

	public static boolean isPackageInstalled(String packageName, Context context) {
		try {
			context.getPackageManager().getPackageInfo(packageName, 0);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
