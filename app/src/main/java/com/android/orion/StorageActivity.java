package com.android.orion;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Xml;

import com.android.orion.database.IndexComponent;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;
import com.android.orion.utility.Utility;

public class StorageActivity extends DatabaseActivity {

	static final String XML_DIR_NAME = Constants.APP_NAME;
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
	static final int MESSAGE_SAVE_TO_FILE = 1;
	static final int MESSAGE_LOAD_FROM_FILE = 2;

	static final int REQUEST_CODE_READ = 42;
	static final int REQUEST_CODE_WRITE = 43;

	Uri mUri = null;

	Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case MESSAGE_REFRESH:
				mOrionService.download();
				break;

			case MESSAGE_SAVE_TO_FILE:
				new Thread(new Runnable() {
					@Override
					public void run() {
						saveToFile();
					}
				}).start();
				break;

			case MESSAGE_LOAD_FROM_FILE:
				new Thread(new Runnable() {
					@Override
					public void run() {
						loadFromFile();
					}
				}).start();
				break;

			default:
				break;
			}
		}
	};

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

	void onMessageRefresh() {
		mOrionService.download();
	}

	void performLoadFromFile() {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");

		startActivityForResult(intent, REQUEST_CODE_READ);
	}

	void performSaveToFile() {
		String fileNameString = Constants.FAVORITE
				+ Utility.getCurrentDateString() + Constants.FAVORITE_FILE_EXT;

		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("xml/plain");
		intent.putExtra(Intent.EXTRA_TITLE, fileNameString);

		startActivityForResult(intent, REQUEST_CODE_WRITE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
			Uri uri = data != null ? data.getData() : null;

			if (uri == null) {
				return;
			}

			if (requestCode == REQUEST_CODE_READ) {
				mUri = uri;
				mHandler.sendEmptyMessage(MESSAGE_LOAD_FROM_FILE);
			} else if (requestCode == REQUEST_CODE_WRITE) {
				mUri = uri;
				mHandler.sendEmptyMessage(MESSAGE_SAVE_TO_FILE);
			}
		}
	}

	void closeQuietly(AutoCloseable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void saveToFile() {
		final ContentResolver cr = getContentResolver();

		OutputStream os = null;
		try {
			os = cr.openOutputStream(mUri);
			saveToXml(os);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeQuietly(os);
		}
	}

	void loadFromFile() {
		final ContentResolver cr = getContentResolver();

		InputStream is = null;
		try {
			is = cr.openInputStream(mUri);
			loadFromXml(is);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeQuietly(is);
		}

		mHandler.sendEmptyMessage(MESSAGE_REFRESH);
	}

	int loadFromXml(InputStream inputStream) {
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
		ArrayList<Stock> stockList = new ArrayList<Stock>();
		ArrayList<IndexComponent> indexComponentArrayList = new ArrayList<>();
		ArrayList<StockDeal> stockDealArrayList = new ArrayList<>();
		ContentValues[] contentValues = null;
		int parseType = XML_PARSE_TYPE_NONE;

		if (mStockDatabaseManager == null) {
			return count;
		}

		mStockDatabaseManager.deleteIndexComponent();
		mStockDatabaseManager.deleteStockDeal();

		try {
			eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tagName = parser.getName();
                        if (XML_TAG_STOCK.equals(tagName)) {
							parseType = XML_PARSE_TYPE_STOCK;

							stock = new Stock();

							indexComponentArrayList.clear();
							stockDealArrayList.clear();
						} else if (XML_TAG_STOCK_DEAL.equals(tagName)) {
							parseType = XML_PARSE_TYPE_STOCK_DEAL;

							stockDeal = new StockDeal();
							stockDeal.setSE(stock.getSE());
							stockDeal.setCode(stock.getCode());
							stockDeal.setName(stock.getName());
                        } else if (XML_TAG_INDEX_COMPONENT.equals(tagName)) {
							parseType = XML_PARSE_TYPE_INDEX_COMPONENT;

							indexComponent = new IndexComponent();
							indexComponent.setIndexSE(stock.getSE());
							indexComponent.setIndexCode(stock.getCode());
							indexComponent.setIndexName(stock.getName());
                        } else if (parseType == XML_PARSE_TYPE_STOCK) {
                            if (DatabaseContract.COLUMN_CLASSES.equals(tagName)) {
                                stock.setClasses(parser.nextText());
                            } else if (DatabaseContract.COLUMN_SE.equals(tagName)) {
                                stock.setSE(parser.nextText());
                            } else if (DatabaseContract.COLUMN_CODE.equals(tagName)) {
                                stock.setCode(parser.nextText());
                            } else if (DatabaseContract.COLUMN_NAME.equals(tagName)) {
                                stock.setName(parser.nextText());
                            } else if (DatabaseContract.COLUMN_FLAG.equals(tagName)) {
                                stock.setFlag(Integer.valueOf(parser.nextText()));
							} else if (DatabaseContract.COLUMN_OPERATE.equals(tagName)) {
								stock.setOperate(parser.nextText());
                            } else if (DatabaseContract.COLUMN_NATURAL_THRESHOLD.equals(tagName)) {
                                stock.setNaturalThreshold(Double.valueOf(parser.nextText()));
                            }
                        } else if (parseType == XML_PARSE_TYPE_STOCK_DEAL) {
                            if (DatabaseContract.COLUMN_BUY.equals(tagName)) {
                                stockDeal.setBuy(Double.valueOf(parser.nextText()));
							} else if (DatabaseContract.COLUMN_SELL.equals(tagName)) {
								stockDeal.setSell(Double.valueOf(parser.nextText()));
                            } else if (DatabaseContract.COLUMN_VOLUME.equals(tagName)) {
                                stockDeal.setVolume(Long.valueOf(parser.nextText()));
							} else if (DatabaseContract.COLUMN_ACCOUNT.equals(tagName)) {
								stockDeal.setAccount(parser.nextText());
							} else if (DatabaseContract.COLUMN_ACTION.equals(tagName)) {
								stockDeal.setAction(parser.nextText());
                            } else if (DatabaseContract.COLUMN_CREATED.equals(tagName)) {
                                stockDeal.setCreated(parser.nextText());
                            } else if (DatabaseContract.COLUMN_MODIFIED.equals(tagName)) {
                                stockDeal.setModified(parser.nextText());
                            }
						} else if (parseType == XML_PARSE_TYPE_INDEX_COMPONENT) {
							if (DatabaseContract.COLUMN_SE.equals(tagName)) {
								indexComponent.setSE(parser.nextText());
							} else if (DatabaseContract.COLUMN_CODE.equals(tagName)) {
								indexComponent.setCode(parser.nextText());
							} else if (DatabaseContract.COLUMN_NAME.equals(tagName)) {
								indexComponent.setName(parser.nextText());
							}
                        }
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (XML_TAG_STOCK.equals(tagName)) {
						parseType = XML_PARSE_TYPE_NONE;

						mStockDatabaseManager.getStock(stock);
						if (!mStockDatabaseManager.isStockExist(stock)) {
							stock.setCreated(now);
							stock.setModified(now);
							mStockDatabaseManager.insertStock(stock);
						} else {
							stock.setModified(now);
							mStockDatabaseManager.updateStock(stock,
									stock.getContentValues());
						}
						stockList.add(stock);

						if (stockDealArrayList.size() > 0) {
							contentValues = new ContentValues[stockDealArrayList.size()];

							for (int i = 0; i < stockDealArrayList.size(); i++) {
								stockDeal = stockDealArrayList.get(i);
								contentValues[i] = stockDeal.getContentValues();
							}

							mStockDatabaseManager.bulkInsertStockDeal(contentValues);
						}

						if (indexComponentArrayList.size() > 0) {
							contentValues = new ContentValues[indexComponentArrayList.size()];

							for (int i = 0; i < indexComponentArrayList.size(); i++) {
								indexComponent = indexComponentArrayList.get(i);
								contentValues[i] = indexComponent.getContentValues();
							}

							mStockDatabaseManager.bulkInsertIndexComponent(contentValues);
						}
					} else if (XML_TAG_STOCK_DEAL.equals(tagName)) {
						parseType = XML_PARSE_TYPE_NONE;

						stockDealArrayList.add(stockDeal);
					} else if (XML_TAG_INDEX_COMPONENT.equals(tagName)) {
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
				mStockDatabaseManager.updateStockDeal(stock2);
				mStockDatabaseManager.updateStock(stock2,
						stock2.getContentValuesForEdit());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return count;
	}

	int saveToXml(OutputStream outputStream) {
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

		ArrayList<Stock> stockList = new ArrayList<Stock>();
		IndexComponent indexComponent = new IndexComponent();
		StockDeal stockDeal = new StockDeal();

		Cursor cursor = null;
		String selection = "";

		if (mStockDatabaseManager == null) {
			return count;
		}

		try {
			cursor = mStockDatabaseManager.queryStock(selection, null, null);
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
			mStockDatabaseManager.closeCursor(cursor);
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
				xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_NATURAL_THRESHOLD,
						String.valueOf(stock.getNaturalThreshold()));
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				selection = DatabaseContract.COLUMN_SE + " = " + "\'"
						+ stock.getSE() + "\'" + " AND "
						+ DatabaseContract.COLUMN_CODE + " = " + "\'"
						+ stock.getCode() + "\'";

				cursor = mStockDatabaseManager.queryStockDeal(selection, null,
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
				mStockDatabaseManager.closeCursor(cursor);
			}

			try {
				selection = DatabaseContract.COLUMN_INDEX_CODE + " = " + "\'"
						+ stock.getCode() + "\'";

				cursor = mStockDatabaseManager.queryIndexComponent(selection, null,
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
				mStockDatabaseManager.closeCursor(cursor);
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
			xmlSerializer.text(text);
			xmlSerializer.endTag(null, tag);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
