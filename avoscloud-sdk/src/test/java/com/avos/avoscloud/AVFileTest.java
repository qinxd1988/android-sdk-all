package com.avos.avoscloud;

import android.text.TextUtils;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.avos.avoscloud.AVFileDownloader.getAVFileCachePath;

/**
 * Created by wli on 16/10/10.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE, sdk = 21)
public class AVFileTest {

  public static String TEST_FILE_CONTENT = "hello world";

  @Before
  public void initAvos() {
    AVOSCloud.initialize(RuntimeEnvironment.application, TestConfig.TEST_APP_ID, TestConfig.TEST_APP_KEY);
  }

  @Test
  public void testAVFile_data() throws Exception {
    AVFile file = new AVFile(TEST_FILE_CONTENT.getBytes());
    Assert.assertArrayEquals(TEST_FILE_CONTENT.getBytes(), file.getData());
    Assert.assertNull(file.getName());
    Assert.assertNull(file.getFileObject());

    HashMap<String, Object> map = file.getMetaData();
    Assert.assertNotNull(map);
    Assert.assertTrue(map.containsKey("size"));
    Assert.assertTrue((Integer) map.get("size") == TEST_FILE_CONTENT.getBytes().length);
  }

  @Test
  public void testToJSONObject() throws Exception {
    String fileName = "FileUnitTestFiles";
    AVFile avFile = new AVFile(fileName, TEST_FILE_CONTENT.getBytes());
    JSONObject jsonObject = avFile.toJSONObject();
    Assert.assertNotNull(jsonObject);
    Assert.assertEquals(jsonObject.getString("__type"), AVFile.className());
    Assert.assertFalse(jsonObject.has("url"));
    Assert.assertEquals(jsonObject.getString("id"), fileName);
  }

  @Test
  public void testToJSONObject_existAVFile() throws Exception {
    String fileName = "FileUnitTestFiles";
    AVFile avFile = new AVFile(fileName, TEST_FILE_CONTENT.getBytes());
    avFile.save();
    Assert.assertNotNull(avFile.getUrl());
    JSONObject jsonObject = avFile.toJSONObject();
    Assert.assertNotNull(jsonObject);
    Assert.assertEquals(jsonObject.getString("__type"), AVFile.className());
    Assert.assertTrue(!TextUtils.isEmpty(jsonObject.getString("url")));
  }

  @Test
  public void testAVFile() {
    AVFile file = new AVFile();
    Assert.assertNotNull(file);
  }

  @Test
  public void testAVFile_url() {
    HashMap<String, Object> map = new HashMap<>();
    map.put("key", "value");
    AVFile file = new AVFile("fileName", "url", map);
    Assert.assertEquals("url", file.getUrl());

    HashMap<String, Object> metaData = file.getMetaData();
    Assert.assertEquals("external", metaData.get("__source"));
    Assert.assertEquals("value", metaData.get("key"));
  }

  @Test
  public void testAVFile_nameAndData() throws Exception {
    AVFile file = new AVFile("name", TEST_FILE_CONTENT.getBytes());
    Assert.assertArrayEquals(TEST_FILE_CONTENT.getBytes(), file.getData());
    Assert.assertEquals("name", file.getName());
    Assert.assertNull(file.getFileObject());

    HashMap<String, Object> map = file.getMetaData();
    Assert.assertNotNull(map);
    Assert.assertTrue(map.containsKey("size"));
    Assert.assertTrue((Integer) map.get("size") == TEST_FILE_CONTENT.getBytes().length);
  }

  @Test
  public void testSetObjectId() {
    String objectId = "testObjectId";
    AVFile file = new AVFile();
    file.setObjectId(objectId);
    Assert.assertEquals(objectId, file.getObjectId());
  }

  @Test
  public void testAddMetaData() {
    AVFile file = new AVFile();
    file.addMetaData("key", "value");
    Assert.assertEquals(file.getMetaData("key"), "value");
  }

  @Test
  public void testGetMetaData() {
    AVFile file = new AVFile("name", TEST_FILE_CONTENT.getBytes());
    Assert.assertNotNull(file.getMetaData());
  }

  @Test
  public void testGetSize() {
    AVFile file = new AVFile("name", TEST_FILE_CONTENT.getBytes());
    Assert.assertEquals(file.getSize(), TEST_FILE_CONTENT.length());
  }

  @Test
  public void testRemoveMetaData() {
    String key = "key";
    String value = "value";
    AVFile file = new AVFile("name", TEST_FILE_CONTENT.getBytes());
    Assert.assertTrue(!file.getMetaData().containsKey(key));
    file.addMetaData(key, value);
    Assert.assertEquals(file.getMetaData().get(key), value);
    file.removeMetaData(key);
    Assert.assertTrue(!file.getMetaData().containsKey(key));
  }

  @Test
  public void testClearMetaData() {
    AVFile file = new AVFile("name", TEST_FILE_CONTENT.getBytes());
    Assert.assertTrue(!file.getMetaData().isEmpty());
    file.clearMetaData();
    Assert.assertTrue(file.getMetaData().isEmpty());
  }

  @Test
  public void testGetOwnerObjectId() {
    AVFile file = new AVFile("name", TEST_FILE_CONTENT.getBytes());
    Assert.assertTrue(AVUtils.isBlankContent(file.getOwnerObjectId()));
  }

  @Test
  public void testWithObjectIdInBackground() throws Exception {
    final AVFile file = new AVFile("name", TEST_FILE_CONTENT.getBytes());
    file.save();

    final CountDownLatch latch = new CountDownLatch(1);
    AVFile.withObjectIdInBackground(file.getObjectId(), new GetFileCallback<AVFile>() {
      @Override
      public void done(AVFile object, AVException e) {
        Assert.assertEquals(file.getOriginalName(), object.getOriginalName());
        Assert.assertEquals(file.getObjectId(), object.getObjectId());
        Assert.assertEquals(file.getUrl(), object.getUrl());
        Assert.assertNotNull(object.getBucket());
        Assert.assertEquals(file.getMetaData(), object.getMetaData());
        latch.countDown();
      }
    });
  }

  @Test
  public void testParseFileWithObjectId() throws Exception {
    AVFile file = new AVFile("name", TEST_FILE_CONTENT.getBytes());
    file.save();

    AVFile newFile = AVFile.parseFileWithObjectId(file.getObjectId());
    Assert.assertNotNull(newFile);
    Assert.assertNotNull(newFile.getObjectId());
    Assert.assertNotNull(newFile.getOriginalName());
    Assert.assertNotNull(newFile.getBucket());
    Assert.assertNotNull(newFile.getMetaData());
  }

  @Test
  public void testWithObjectId() throws Exception {
    AVFile file = new AVFile("name", TEST_FILE_CONTENT.getBytes());
    file.save();

    AVFile newFile = AVFile.withObjectId(file.getObjectId());
    Assert.assertNotNull(newFile);
    Assert.assertNotNull(newFile.getObjectId());
    Assert.assertNotNull(newFile.getOriginalName());
    Assert.assertNotNull(newFile.getBucket());
    Assert.assertNotNull(newFile.getMetaData());
  }

  @Test
  public void testWithAVObject() throws Exception {
    AVFile file = new AVFile("name", TEST_FILE_CONTENT.getBytes());
    file.save();

    AVQuery<AVObject> query = new AVQuery<AVObject>("_File");
    AVObject object = query.get(file.getObjectId());

    AVFile newFile = AVFile.withAVObject(object);
    Assert.assertEquals(file.getOriginalName(), newFile.getOriginalName());
    Assert.assertEquals(file.getObjectId(), newFile.getObjectId());
    Assert.assertEquals(file.getUrl(), newFile.getUrl());
    Assert.assertNotNull(newFile.getBucket());
    Assert.assertEquals(file.getMetaData(), newFile.getMetaData());
  }

  @Test
  public void testWithAbsoluteLocalPath() throws Exception {
    File file = new File(getAVFileCachePath(), "test");
    AVPersistenceUtils.saveContentToFile(TEST_FILE_CONTENT, file);

    AVFile newFile = AVFile.withAbsoluteLocalPath("name", file.getAbsolutePath());
    Assert.assertNotNull(newFile);
    Assert.assertNotNull(newFile.getOriginalName());
    Assert.assertNotNull(newFile.getMetaData());
    Assert.assertArrayEquals(TEST_FILE_CONTENT.getBytes(), newFile.getData());
  }

  @Test
  public void testWithFile() throws Exception {
    File file = new File(getAVFileCachePath(), "test");
    AVPersistenceUtils.saveContentToFile(TEST_FILE_CONTENT, file);

    AVFile newFile = AVFile.withFile("name", file);
    Assert.assertNotNull(newFile);
    Assert.assertNotNull(newFile.getOriginalName());
    Assert.assertNotNull(newFile.getMetaData());
    Assert.assertArrayEquals(TEST_FILE_CONTENT.getBytes(), newFile.getData());
  }

  @Test
  public void testGetData() throws Exception {
    AVFile file = new AVFile("name", TEST_FILE_CONTENT.getBytes());
    file.save();

    AVQuery<AVObject> query = new AVQuery<AVObject>("_File");
    AVObject object = query.get(file.getObjectId());

    AVFile newFile = AVFile.withAVObject(object);
    byte[] byteData = newFile.getData();
    Assert.assertArrayEquals(TEST_FILE_CONTENT.getBytes(), byteData);
  }

  @Test
  public void testGetDataNonExistentFile() throws Exception {
    AVFile file = new AVFile(null, "http://leancloud.cn/xxxxxxxx", null);
    boolean result = false;
    try {
      file.getData();
    } catch (Exception e) {
      result = true;
    }
    Assert.assertTrue(result);
  }

  @Test
  public void testGetDataInBackground() throws Exception {
    AVFile file = new AVFile("name", TEST_FILE_CONTENT.getBytes());
    file.save();

    AVQuery<AVObject> query = new AVQuery<AVObject>("_File");
    AVObject object = query.get(file.getObjectId());

    AVFile newFile = AVFile.withAVObject(object);
    final CountDownLatch latch = new CountDownLatch(1);
    final StringBuffer content = new StringBuffer();

    newFile.getDataInBackground(new GetDataCallback() {
      @Override
      public void done(byte[] data, AVException e) {
        content.append(new String(data));
        Assert.assertNull(e);
        Assert.assertNotNull(data);
        latch.countDown();
      }
    });
    Assert.assertEquals(TEST_FILE_CONTENT, content.toString());
  }

  @Test
  public void testGetDataInBackgroundWithProgress() throws Exception {
    AVFile file = new AVFile("name", TEST_FILE_CONTENT.getBytes());
    file.save();

    AVQuery<AVObject> query = new AVQuery<AVObject>("_File");
    AVObject object = query.get(file.getObjectId());

    AVFile newFile = AVFile.withAVObject(object);
    final CountDownLatch latch = new CountDownLatch(1);
    final StringBuffer content = new StringBuffer();
    final StringBuffer progressDetail = new StringBuffer();
    newFile.getDataInBackground(new GetDataCallback() {
      @Override
      public void done(byte[] data, AVException e) {
        content.append(new String(data));
        Assert.assertNull(e);
        Assert.assertNotNull(data);
        latch.countDown();
      }
    }, new ProgressCallback() {
      @Override
      public void done(Integer percentDone) {
        progressDetail.append(percentDone + "");
      }
    });
    Assert.assertEquals(TEST_FILE_CONTENT, content.toString());
    Assert.assertTrue(progressDetail.length() > 0);
  }

  @Test
  public void testGetThumbnail() throws Exception {
    AVQuery query = new AVQuery("_File");
    AVObject object = query.getFirst();
    AVFile file = AVFile.parseFileWithAVObject(object);
    Assert.assertNotNull(file);
    Assert.assertNotNull(file.getUrl());
    Assert.assertEquals(file.getUrl() + "?imageView/1/w/200/h/100/q/100/format/png",
      file.getThumbnailUrl(false, 200, 100));
  }

  @Test
  public void testSaveInBackground() {
    AVFile file = new AVFile("name", TEST_FILE_CONTENT.getBytes());
    final CountDownLatch latch = new CountDownLatch(1);
    file.saveInBackground(new SaveCallback() {
      @Override
      public void done(AVException e) {
        Assert.assertNull(e);
        latch.countDown();
      }
    });
  }

  @Test
  public void testDelete() throws Exception {
    AVFile file = new AVFile("name", TEST_FILE_CONTENT.getBytes());
    file.save();

    AVFile cloudFile = AVFile.withObjectId(file.getObjectId());
    Assert.assertNotNull(cloudFile);
    Assert.assertEquals(file.getUrl(), cloudFile.getUrl());

    file.delete();

    AVException exception = null;
    AVFile deletedFile = null;
    try {
      deletedFile = AVFile.withObjectId(file.getObjectId());
    } catch (AVException e) {
      exception = e;
    }
    Assert.assertEquals(exception.code, AVException.OBJECT_NOT_FOUND);
    Assert.assertNull(deletedFile);
  }

  @Test
  public void testDeleteInBackground() throws Exception {
    AVFile file = new AVFile("name", TEST_FILE_CONTENT.getBytes());
    file.save();

    AVFile cloudFile = AVFile.withObjectId(file.getObjectId());
    Assert.assertNotNull(cloudFile);
    Assert.assertEquals(file.getUrl(), cloudFile.getUrl());

    final CountDownLatch latch = new CountDownLatch(1);
    file.deleteInBackground(new DeleteCallback() {
      @Override
      public void done(AVException e) {
        Assert.assertNull(e);
        latch.countDown();
      }
    });

    latch.await(1, TimeUnit.MINUTES);

    AVException exception = null;
    AVFile deletedFile = null;
    try {
      deletedFile = AVFile.withObjectId(file.getObjectId());
    } catch (AVException e) {
      exception = e;
    }
    Assert.assertEquals(exception.code, AVException.OBJECT_NOT_FOUND);
    Assert.assertNull(deletedFile);
  }

  @Test
  public void testAVFileUploadByUrl() throws AVException {
    AVFile file = new AVFile("remoteUrl", "http://leancloud.cn", null);
    file.save();
  }

  @Test
  public void testAVFileWithPointer() throws AVException {
    String fileTable = "FileUnitTest";
    String column = "singleFile";
    AVObject object = new AVObject(fileTable);
    AVFile file = new AVFile("name", TEST_FILE_CONTENT.getBytes());
    object.put(column, file);
    object.save();

    AVOSCloud.setServer(AVOSCloud.SERVER_TYPE.STATS, "http://xxxx");

    Assert.assertNotNull(object.getObjectId());
    Assert.assertNotNull(object.getAVFile(column));
    Assert.assertNotNull(file.getUrl());

    AVObject remoteObject = AVObject.createWithoutData(fileTable, object.getObjectId());
    remoteObject.fetch();
    Assert.assertNotNull(remoteObject.getAVFile(column));
    Assert.assertNotNull(remoteObject.getAVFile(column).getUrl());
    Assert.assertNotNull(remoteObject.getAVFile(column).getObjectId());
  }
}