package com.android.orion.activity;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Xml;
import android.widget.Toast;

import com.android.orion.data.Period;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.IndexComponent;
import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;
import com.android.orion.setting.Constant;
import com.android.orion.utility.Utility;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class StorageActivity extends DatabaseActivity {

	static final String XML_TAG_ROOT = "root";
	static final String XML_TAG_STOCK = "stock";
	static final String XML_TAG_STOCK_DEAL = "stock_deal";
	static final String XML_TAG_INDEX_COMPONENT = "index_component";
	static final String XML_ATTRIBUTE_DATE = "date";

	static final int XML_PARSE_TYPE_NONE = 0;
	static final int XML_PARSE_TYPE_STOCK = 1;
	static final int XML_PARSE_TYPE_STOCK_DEAL = 2;
	static final int XML_PARSE_TYPE_INDEX_COMPONENT = 3;

	static final int MESSAGE_REFRESH = 0;
	static final int MESSAGE_LOAD_FAVORITE = 1;
	static final int MESSAGE_SAVE_FAVORITE = 2;
	static final int MESSAGE_SAVE_TDX_DATA = 3;

	static final int REQUEST_CODE_READ = 40;
	static final int REQUEST_CODE_READ_FAVORITE = 41;
	static final int REQUEST_CODE_READ_TDX_DATA = 42;

	static final int REQUEST_CODE_WRITE = 50;
	static final int REQUEST_CODE_WRITE_FAVORITE = 51;
	static final int REQUEST_CODE_WRITE_TDX_DATA = 52;

	static final int FILE_TYPE_NONE = 0;
	static final int FILE_TYPE_FAVORITE = 1;
	static final int FILE_TYPE_TDX_DATA = 2;

	Uri mUri = null;
	ArrayList<Uri> mUriList = new ArrayList<>();

	Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
				case MESSAGE_REFRESH:
					mStockDataProvider.download();
					break;

				case MESSAGE_LOAD_FAVORITE:
					new Thread(new Runnable() {
						@Override
						public void run() {
							loadFromFile();
						}
					}).start();
					break;

				case MESSAGE_SAVE_FAVORITE:
					new Thread(new Runnable() {
						@Override
						public void run() {
							saveToFile(FILE_TYPE_FAVORITE);
						}
					}).start();
					break;
				case MESSAGE_SAVE_TDX_DATA:
					new Thread(new Runnable() {
						@Override
						public void run() {
							saveToFile(FILE_TYPE_TDX_DATA);
						}
					}).start();
					break;

				default:
					break;
			}
		}
	};

	void performLoadFromFile(int type) {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		int requestCode = REQUEST_CODE_READ;
		if (type == FILE_TYPE_FAVORITE) {
			requestCode = REQUEST_CODE_READ_FAVORITE;
		} else if (type == FILE_TYPE_TDX_DATA) {
			intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
			requestCode = REQUEST_CODE_READ_TDX_DATA;
		}
		startActivityForResult(intent, requestCode);
	}

	void performSaveToFile(int type) {
		String fileNameString = "";
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		int requestCode = REQUEST_CODE_WRITE;
		if (type == FILE_TYPE_FAVORITE) {
			intent.setType("text/xml");
			fileNameString = Constant.FAVORITE
					+ Utility.getCurrentDateString() + Constant.FILE_EXT_XML;
			requestCode = REQUEST_CODE_WRITE_FAVORITE;
		} else if (type == FILE_TYPE_TDX_DATA) {
			fileNameString = mStock.getSE().toUpperCase() + "#" + mStock.getCode() + Constant.FILE_EXT_TEXT;
			intent.setType("text/plain");
			requestCode = REQUEST_CODE_WRITE_TDX_DATA;
		}
		intent.putExtra(Intent.EXTRA_TITLE, fileNameString);
		startActivityForResult(intent, requestCode);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			if (data != null) {
				mUriList.clear();
				if (data.getClipData() != null) {
					ClipData clipData = data.getClipData();
					int itemCount = clipData.getItemCount();
					for (int i = 0; i < itemCount; i++) {
						Uri uri = clipData.getItemAt(i).getUri();
						mUriList.add(uri);
						takePersistableUriPermission(uri);
					}
				} else if (data.getData() != null) {
					Uri uri = data.getData();
					mUriList.add(uri);
					takePersistableUriPermission(uri);
				}
			}

			if (mUriList.size() == 0) {
				return;
			}

			if (requestCode == REQUEST_CODE_READ_FAVORITE) {
				mHandler.sendEmptyMessage(MESSAGE_LOAD_FAVORITE);
			} else if (requestCode == REQUEST_CODE_READ_TDX_DATA) {
				mBackgroundHandler.importTDXDataFile(mUriList);
			} else if (requestCode == REQUEST_CODE_WRITE_FAVORITE) {
				mHandler.sendEmptyMessage(MESSAGE_SAVE_FAVORITE);
			} else if (requestCode == REQUEST_CODE_WRITE_TDX_DATA) {
				mHandler.sendEmptyMessage(MESSAGE_SAVE_TDX_DATA);
			}
		}
	}

	private void takePersistableUriPermission(Uri uri) {
		final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
		getContentResolver().takePersistableUriPermission(uri, takeFlags);
		String msg = Utility.getFileNameFromContentUri(mContext, uri);
		if (Utility.isUriWritable(mContext, uri)) {
			msg += " " + "READ | WRITE";
		} else {
			msg += " " + "READ";
		}
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
		Log.d(msg);
	}

	void saveToFile(int type) {
		final ContentResolver cr = getContentResolver();

		if (mUriList.size() == 0) {
			return;
		}
		mUri = mUriList.get(0);

		OutputStream os = null;
		try {
			os = cr.openOutputStream(mUri);
			if (type == FILE_TYPE_FAVORITE) {
				saveToXmlFile(os);
			} else if (type == FILE_TYPE_TDX_DATA) {
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
				ArrayList<String> contentList = new ArrayList<>();
				mDatabaseManager.getTDXDataContentList(mStock, Period.MIN5, contentList);
				int index = 0;
				if (writer != null) {
					for (String content : contentList) {
						writer.write(content);
						index++;
					}
					writer.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Utility.closeQuietly(os);
		}
	}

	void loadFromFile() {
		final ContentResolver cr = getContentResolver();

		if (mUriList.size() == 0) {
			return;
		}
		mUri = mUriList.get(0);

		InputStream is = null;
		try {
			is = cr.openInputStream(mUri);
			loadFromXmlFile(is);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Utility.closeQuietly(is);
		}

		mHandler.sendEmptyMessage(MESSAGE_REFRESH);
	}

	int loadFromXmlFile(InputStream inputStream) {
		int count = 0;

		try {
			XmlPullParser parser = XmlPullParserFactory.newInstance()
					.newPullParser();
			parser.setInput(inputStream, null);
			count = xmlParse(parser);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return count;
	}

	int xmlParse(XmlPullParser parser) {
		int count = 0;
		int eventType;
		String now = Utility.getCurrentDateTimeString();
		String tagName = "";
		Stock stock = new Stock();
		IndexComponent indexComponent = new IndexComponent();
		StockDeal stockDeal = new StockDeal();
		ArrayList<Stock> stockList = new ArrayList<>();
		ArrayList<IndexComponent> indexComponentArrayList = new ArrayList<>();
		ArrayList<StockDeal> stockDealArrayList = new ArrayList<>();
		ContentValues[] contentValues = null;
		int parseType = XML_PARSE_TYPE_NONE;

		if (mDatabaseManager == null) {
			return count;
		}

		mDatabaseManager.deleteIndexComponent();
		mDatabaseManager.deleteStockDeal();

		try {
			eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
					case XmlPullParser.START_TAG:
						tagName = parser.getName();
						if (TextUtils.equals(tagName, XML_TAG_STOCK)) {
							parseType = XML_PARSE_TYPE_STOCK;

							stock = new Stock();

							indexComponentArrayList.clear();
							stockDealArrayList.clear();
						} else if (TextUtils.equals(tagName, XML_TAG_STOCK_DEAL)) {
							parseType = XML_PARSE_TYPE_STOCK_DEAL;

							stockDeal = new StockDeal();
							stockDeal.setSE(stock.getSE());
							stockDeal.setCode(stock.getCode());
							stockDeal.setName(stock.getName());
						} else if (TextUtils.equals(tagName, XML_TAG_INDEX_COMPONENT)) {
							parseType = XML_PARSE_TYPE_INDEX_COMPONENT;

							indexComponent = new IndexComponent();
							indexComponent.setIndexSE(stock.getSE());
							indexComponent.setIndexCode(stock.getCode());
							indexComponent.setIndexName(stock.getName());
						} else if (parseType == XML_PARSE_TYPE_STOCK) {
							if (TextUtils.equals(tagName, DatabaseContract.COLUMN_CLASSES)) {
								stock.setClasses(parser.nextText());
							} else if (TextUtils.equals(tagName, DatabaseContract.COLUMN_SE)) {
								stock.setSE(parser.nextText());
							} else if (TextUtils.equals(tagName, DatabaseContract.COLUMN_CODE)) {
								stock.setCode(parser.nextText());
							} else if (TextUtils.equals(tagName, DatabaseContract.COLUMN_NAME)) {
								stock.setName(parser.nextText());
							} else if (TextUtils.equals(tagName, DatabaseContract.COLUMN_FLAG)) {
								stock.setFlag(Integer.parseInt(parser.nextText()));
							} else if (TextUtils.equals(tagName, DatabaseContract.COLUMN_OPERATE)) {
								stock.setOperate(parser.nextText());
							}
						} else if (parseType == XML_PARSE_TYPE_STOCK_DEAL) {
							if (TextUtils.equals(tagName, DatabaseContract.COLUMN_BUY)) {
								stockDeal.setBuy(Double.parseDouble(parser.nextText()));
							} else if (TextUtils.equals(tagName, DatabaseContract.COLUMN_SELL)) {
								stockDeal.setSell(Double.parseDouble(parser.nextText()));
							} else if (TextUtils.equals(tagName, DatabaseContract.COLUMN_VOLUME)) {
								stockDeal.setVolume(Long.parseLong(parser.nextText()));
							} else if (TextUtils.equals(tagName, DatabaseContract.COLUMN_ACCOUNT)) {
								stockDeal.setAccount(parser.nextText());
							} else if (TextUtils.equals(tagName, DatabaseContract.COLUMN_ACTION)) {
								stockDeal.setAction(parser.nextText());
							} else if (TextUtils.equals(tagName, DatabaseContract.COLUMN_CREATED)) {
								stockDeal.setCreated(parser.nextText());
							} else if (TextUtils.equals(tagName, DatabaseContract.COLUMN_MODIFIED)) {
								stockDeal.setModified(parser.nextText());
							}
						} else if (parseType == XML_PARSE_TYPE_INDEX_COMPONENT) {
							if (TextUtils.equals(tagName, DatabaseContract.COLUMN_SE)) {
								indexComponent.setSE(parser.nextText());
							} else if (TextUtils.equals(tagName, DatabaseContract.COLUMN_CODE)) {
								indexComponent.setCode(parser.nextText());
							} else if (TextUtils.equals(tagName, DatabaseContract.COLUMN_NAME)) {
								indexComponent.setName(parser.nextText());
							}
						}
						break;
					case XmlPullParser.END_TAG:
						tagName = parser.getName();
						if (TextUtils.equals(tagName, XML_TAG_STOCK)) {
							parseType = XML_PARSE_TYPE_NONE;

							mDatabaseManager.getStock(stock);
							if (!mDatabaseManager.isStockExist(stock)) {
								stock.setCreated(now);
								mDatabaseManager.insertStock(stock);
							} else {
								stock.setModified(now);
								mDatabaseManager.updateStock(stock,
										stock.getContentValues());
							}
							stockList.add(stock);

							if (stockDealArrayList.size() > 0) {
								contentValues = new ContentValues[stockDealArrayList.size()];

								for (int i = 0; i < stockDealArrayList.size(); i++) {
									stockDeal = stockDealArrayList.get(i);
									contentValues[i] = stockDeal.getContentValues();
								}

								mDatabaseManager.bulkInsertStockDeal(contentValues);
							}

							if (indexComponentArrayList.size() > 0) {
								contentValues = new ContentValues[indexComponentArrayList.size()];

								for (int i = 0; i < indexComponentArrayList.size(); i++) {
									indexComponent = indexComponentArrayList.get(i);
									contentValues[i] = indexComponent.getContentValues();
								}

								mDatabaseManager.bulkInsertIndexComponent(contentValues);
							}
						} else if (TextUtils.equals(tagName, XML_TAG_STOCK_DEAL)) {
							parseType = XML_PARSE_TYPE_NONE;

							stockDealArrayList.add(stockDeal);
						} else if (TextUtils.equals(tagName, XML_TAG_INDEX_COMPONENT)) {
							parseType = XML_PARSE_TYPE_NONE;

							indexComponentArrayList.add(indexComponent);
						}
						count++;
						break;
					default:
						break;
				}
				eventType = parser.next();
			}

			for (Stock stock2 : stockList) {
				mDatabaseManager.updateStockDeal(stock2);
				mDatabaseManager.updateStock(stock2,
						stock2.getContentValuesEdit());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return count;
	}

	int saveToXmlFile(OutputStream outputStream) {
		int count = 0;

		XmlSerializer xmlSerializer = Xml.newSerializer();

		try {
			xmlSerializer.setOutput(outputStream, "UTF-8");
			xmlSerializer.setFeature(
					"http://xmlpull.org/v1/doc/features.html#indent-output",
					true);
			xmlSerializer.startDocument(null, true);

			xmlSerializer.startTag("", XML_TAG_ROOT);
			xmlSerializer.attribute("", XML_ATTRIBUTE_DATE,
					Utility.getCurrentDateTimeString());

			count = xmlSerialize(xmlSerializer);

			xmlSerializer.endTag("", XML_TAG_ROOT);
			xmlSerializer.endDocument();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return count;
	}

	int xmlSerialize(XmlSerializer xmlSerializer) {
		int count = 0;

		ArrayList<Stock> stockList = new ArrayList<>();
		IndexComponent indexComponent = new IndexComponent();
		StockDeal stockDeal = new StockDeal();

		Cursor cursor = null;
		String selection = "";

		if (mDatabaseManager == null) {
			return count;
		}

		try {
			cursor = mDatabaseManager.queryStock(selection, null, null);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					Stock stock = new Stock();
					stock.set(cursor);
					stockList.add(stock);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mDatabaseManager.closeCursor(cursor);
		}

		if (stockList.size() == 0) {
			return count;
		}

		for (Stock stock : stockList) {
			try {
				xmlSerializer.startTag(null, XML_TAG_STOCK);
				xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_CLASSES,
						stock.getClasses());
				xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_SE,
						stock.getSE());
				xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_CODE,
						stock.getCode());
				xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_NAME,
						stock.getName());
				xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_FLAG,
						String.valueOf(stock.getFlag()));
				xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_OPERATE,
						stock.getOperate());
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				selection = DatabaseContract.COLUMN_SE + " = " + "'"
						+ stock.getSE() + "'" + " AND "
						+ DatabaseContract.COLUMN_CODE + " = " + "'"
						+ stock.getCode() + "'";

				cursor = mDatabaseManager.queryStockDeal(selection, null,
						null);
				if ((cursor != null) && (cursor.getCount() > 0)) {
					while (cursor.moveToNext()) {
						stockDeal.set(cursor);

						xmlSerializer.startTag(null, XML_TAG_STOCK_DEAL);
						xmlSerialize(xmlSerializer,
								DatabaseContract.COLUMN_BUY,
								String.valueOf(stockDeal.getBuy()));
						xmlSerialize(xmlSerializer,
								DatabaseContract.COLUMN_SELL,
								String.valueOf(stockDeal.getSell()));
						xmlSerialize(xmlSerializer,
								DatabaseContract.COLUMN_VOLUME,
								String.valueOf(stockDeal.getVolume()));
						xmlSerialize(xmlSerializer,
								DatabaseContract.COLUMN_ACCOUNT,
								stockDeal.getAccount());
						xmlSerialize(xmlSerializer,
								DatabaseContract.COLUMN_ACTION,
								stockDeal.getAction());
						xmlSerialize(xmlSerializer,
								DatabaseContract.COLUMN_CREATED,
								stockDeal.getCreated());
						xmlSerialize(xmlSerializer,
								DatabaseContract.COLUMN_MODIFIED,
								stockDeal.getModified());
						xmlSerializer.endTag(null, XML_TAG_STOCK_DEAL);

						count++;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				mDatabaseManager.closeCursor(cursor);
			}

			try {
				selection = DatabaseContract.COLUMN_INDEX_CODE + " = " + "'"
						+ stock.getCode() + "'";

				cursor = mDatabaseManager.queryIndexComponent(selection, null,
						null);
				if ((cursor != null) && (cursor.getCount() > 0)) {
					while (cursor.moveToNext()) {
						indexComponent.set(cursor);

						xmlSerializer.startTag(null, XML_TAG_INDEX_COMPONENT);
						xmlSerialize(xmlSerializer,
								DatabaseContract.COLUMN_SE,
								String.valueOf(indexComponent.getSE()));
						xmlSerialize(xmlSerializer,
								DatabaseContract.COLUMN_CODE,
								String.valueOf(indexComponent.getCode()));
						xmlSerialize(xmlSerializer,
								DatabaseContract.COLUMN_NAME,
								indexComponent.getName());
						xmlSerializer.endTag(null, XML_TAG_INDEX_COMPONENT);

						count++;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				mDatabaseManager.closeCursor(cursor);
			}

			try {
				xmlSerializer.endTag(null, XML_TAG_STOCK);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return count;
	}

	void xmlSerialize(XmlSerializer xmlSerializer, String tag, String text) {
		try {
			xmlSerializer.startTag(null, tag);
			if (TextUtils.isEmpty(text)) {
				xmlSerializer.text("");
			} else {
				xmlSerializer.text(text);
			}
			xmlSerializer.endTag(null, tag);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
