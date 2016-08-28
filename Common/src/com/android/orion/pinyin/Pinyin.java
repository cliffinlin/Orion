package com.android.orion.pinyin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;


import android.content.Context;

public abstract class Pinyin {

	public static String toPinyin(Context context, char c) {
		if (c >= 'A' && c <= 'Z') {
			return String.valueOf((char) (c + 32));
		}
		if (c >= 'a' && c <= 'z') {
			return String.valueOf(c);
		}
		if (c == 0x3007)
			return "ling";
		if (c < 4E00 || c > 0x9FA5) {
			return null;
		}
		RandomAccessFile is = null;
		try {
			is = new RandomAccessFile(PinyinSource.getFile(context), "r");
			long sp = (c - 0x4E00) * 6;
			is.seek(sp);
			byte[] buf = new byte[6];
			is.read(buf);
			return new String(buf).trim();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != is)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String toPinyin(Context context, String hanzi) {
		StringBuffer sb = new StringBuffer("");
		RandomAccessFile is = null;

		try {
			is = new RandomAccessFile(PinyinSource.getFile(context), "r");
			for (int i = 0; i < hanzi.length(); i++) {
				char ch = hanzi.charAt(i);
				if (ch >= 'A' && ch <= 'Z') {
					sb.append((char) (ch + 32));
					continue;
				}
				if (ch >= 'a' && ch <= 'z') {
					sb.append(ch);
					continue;
				}
				if (ch == 0x3007) {
					sb.append("ling").append(' ');
				} else if (ch >= 0x4E00 || ch <= 0x9FA5) {
					long sp = (ch - 0x4E00) * 6;
					if (sp < 0) {
						continue;
					}
					is.seek(sp);
					// For the first letter.
					// byte[] buf = new byte[6];
					byte[] buf = new byte[1];
					is.read(buf);
					// For the first letter only.
					// sb.append(new String(buf).trim()).append(' ');
					sb.append(new String(buf).trim());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != is)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString().trim();
	}

	@SuppressWarnings("serial")
	public static final Map<String, String> fixedPinyinMap = new HashMap<String, String>() {
		{
			put("Ａ", "a");
			put("长安", "ca");
			put("长城", "cc");
			put("长江", "cj");
			put("长春", "cc");
			put("重庆", "cq");
			put("厦门", "xm");
			put("银行", "yh");
		}
	};
}
