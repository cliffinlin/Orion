package com.android.orion;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Xml;
import android.widget.Toast;

import com.android.orion.utility.Utility;

public class StorageActivity extends DatabaseActivity {

	static final String XML_DIR_NAME = Constants.APP_NAME;
	static final String XML_TAG_ROOT = "root";
	static final String XML_TAG_ITEM = "item";
	static final String XML_ATTRIBUTE_DATE = "date";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	void loadListFromSD(String xmlFileName) {
		File file = null;
		File folder = null;
		String folderName = "";
		String fileName = "";

		InputStream inputStream = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			try {
				folderName = Environment.getExternalStorageDirectory()
						.getPath() + "/" + XML_DIR_NAME;
				folder = new File(folderName);

				if (!folder.exists()) {
					return;
				}

				if (TextUtils.isEmpty(xmlFileName)) {
					return;
				}

				fileName = folderName + "/" + xmlFileName;
				file = new File(fileName);

				if (!file.exists()) {
					Toast.makeText(this, R.string.no_file_found,
							Toast.LENGTH_LONG).show();
					return;
				} else {
					inputStream = new BufferedInputStream(new FileInputStream(
							file));
					xmlToList(inputStream);
					inputStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Toast.makeText(this, R.string.no_sd_card_found, Toast.LENGTH_LONG)
					.show();
		}
	}

	void xmlToList(InputStream inputStream) {
		try {
			XmlPullParser parser = XmlPullParserFactory.newInstance()
					.newPullParser();
			parser.setInput(inputStream, null);
			xmlParse(parser);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void xmlParse(XmlPullParser parser) {
	}

	void SaveListToSD(String xmlFileName) {
		File file = null;
		File folder = null;
		String folderName = "";
		String fileName = "";

		FileOutputStream fileOutputStream = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			try {
				folderName = Environment.getExternalStorageDirectory()
						.getPath() + "/" + XML_DIR_NAME;
				folder = new File(folderName);

				if (!folder.exists()) {
					folder.mkdir();
				}

				if (TextUtils.isEmpty(xmlFileName)) {
					return;
				}

				fileName = folderName + "/" + xmlFileName;
				file = new File(fileName);

				if (!file.exists()) {
					file.createNewFile();
				}

				fileOutputStream = new FileOutputStream(file, false);
				listToXml(fileOutputStream);
				fileOutputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Toast.makeText(this, R.string.no_sd_card_found, Toast.LENGTH_LONG)
					.show();
		}
	}

	void listToXml(FileOutputStream fileOutputStream) {
		XmlSerializer xmlSerializer = Xml.newSerializer();

		try {
			xmlSerializer.setOutput(fileOutputStream, "UTF-8");
			xmlSerializer.setFeature(
					"http://xmlpull.org/v1/doc/features.html#indent-output",
					true);
			xmlSerializer.startDocument(null, true);

			xmlSerializer.startTag("", XML_TAG_ROOT);
			xmlSerializer.attribute("", XML_ATTRIBUTE_DATE,
					Utility.getCurrentDateTimeString());

			xmlSerialize(xmlSerializer);

			xmlSerializer.endTag("", XML_TAG_ROOT);
			xmlSerializer.endDocument();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void xmlSerialize(XmlSerializer xmlSerializer) {
	}

	void xmlSerialize(XmlSerializer xmlSerializer, String tag, String text) {
		try {
			xmlSerializer.startTag(null, tag);
			xmlSerializer.text(text);
			xmlSerializer.endTag(null, tag);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
