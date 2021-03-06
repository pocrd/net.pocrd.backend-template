package net.pocrd.demo.autotest.test;

import net.pocrd.m.app.client.ApiAccessor;
import net.pocrd.m.app.client.ApiContext;
import net.pocrd.m.app.client.BaseRequest;
import net.pocrd.m.app.client.ServerResponse;
import net.pocrd.m.app.client.api.request.*;
import net.pocrd.m.app.client.api.resp.Api_DEMO_ComplexTestEntity;
import net.pocrd.m.app.client.api.resp.Api_DEMO_DemoEntity;
import net.pocrd.m.app.client.api.resp.Api_DEMO_SimpleTestEntity;
import net.pocrd.m.app.client.api.resp.Api_KeyValueList;
import net.pocrd.m.app.client.util.Base64Util;
import net.pocrd.m.app.client.util.RsaHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by guankaiqiang521 on 2014/9/29.
 */
public class DemoTest {
    private static final String url          = "http://localhost:8080/m.api";
    //    private static final String url          = "http://www.pocrd.net/m.api";
    private static final String deviceId     = "1414807058834";
    private static final String deviceSecret = "581bb3c7f2d09e4d2f07f69706fff13f261f4cfa2038cd2ab7bb46040ca2d568";
    private static final String deviceToken  = "010r8q7HvgHpAqMRYuLX45W+tey5xX9db1sKo3ENCIYaJc4fGpDY4u61zlFfEiIkHtUk7Lx1XDazOleSZjrayFlv3v4WFI1sD+QIfqp5gRvUQvXh4OIWHDBF7OxKFMPyM3l";
    private static final long   userId       = 22L;
    private static final String userToken    = "110f0HUV7XXApEflwNpdBn0Yy9PVHmZ/TSA3TpoXB8+lLHqnwfHeo6BQpQSVSU19QAqHC9tfVQApfAZpJGFoPCOIRVazT7kSE38CGBi0Gs7eY0tPrfVZw10iLnt/N3yifhF";

    private ApiContext  context  = ApiContext.getDefaultContext("1", 123, "1.2.3");
    private ApiAccessor accessor = new ApiAccessor(context, 3000, 30000, "test-agent", url);

    private void initWithDeviceInfo() {
        context.setDeviceInfo(deviceId, deviceSecret, deviceToken);
    }

    private void initWithUserInfo() {
        initWithDeviceInfo();
        context.setUserInfo(userId, userToken, Long.MAX_VALUE);
    }

    @BeforeClass
    public static void init() {
        //        System.setProperty("debug.dubbo.url", "dubbo://localhost:20880/");
        //        System.setProperty("debug.dubbo.version", "LATEST");
    }

    @Test
    public void sayHelloTest() {
        final Demo_SayHello sayHello = new Demo_SayHello("abc");

        accessor.fillApiResponse(sayHello);
        Api_DEMO_DemoEntity resp = sayHello.getResponse();
        Assert.assertEquals(ApiCode.SUCCESS, sayHello.getReturnCode());
        Assert.assertEquals(123, resp.id);
        Assert.assertEquals("abc", resp.name);
    }

    @Test
    public void tryErrorTest() {
        final Demo_TryError tryError = new Demo_TryError("abc");

        accessor.fillApiResponse(tryError);
        Assert.assertEquals(-220, tryError.getReturnCode());
    }

    @Test
    public void testRegistedDevice() {
        initWithDeviceInfo();
        final Demo_TestRegistedDevice regiestedDevice = new Demo_TestRegistedDevice();
        accessor.fillApiResponse(regiestedDevice);
        Assert.assertEquals(ApiCode.SUCCESS, regiestedDevice.getReturnCode());
    }

    @Test
    public void testUserLogin() {
        initWithUserInfo();
        Demo_TestUserLogin userLogin = new Demo_TestUserLogin();
        accessor.fillApiResponse(userLogin);
        Assert.assertEquals(ApiCode.SUCCESS, userLogin.getReturnCode());
    }

    @Test
    public void testReirectUrl() {
        final Demo_TestRedirect req = new Demo_TestRedirect("A", "456");
        accessor.fillApiResponse(req);
        String msg = req.getResponse();
        Assert.assertTrue(msg.startsWith("<?xml"));
    }

    public static final Comparator<String> StringComparator = new Comparator<String>() {
        @Override
        public int compare(String s1, String s2) {
            int n1 = s1 == null ? 0 : s1.length();
            int n2 = s2 == null ? 0 : s2.length();
            int mn = n1 < n2 ? n1 : n2;
            for (int i = 0; i < mn; i++) {
                int k = s1.charAt(i) - s2.charAt(i);
                if (k != 0) {
                    return k;
                }
            }
            return n1 - n2;
        }
    };

    @Test
    public void testIntegrated() throws UnsupportedEncodingException {
        Map<String, String> mapping = new HashMap<String, String>();
        //1.设置请求参数
        mapping.put("_mt", "demo.getResByThirdPartyId");//方法名
        mapping.put("_tpid", "1");//tpid即集成第三方的编号（由网关统一分配），爬虫暂且用1
        mapping.put("in", "abcde xxxx");//其他方法入参
        //2.进行签名
        StringBuilder sb = new StringBuilder(128);
        String[] array = mapping.keySet().toArray(new String[mapping.size()]);
        if (array.length > 0) {
            Arrays.sort(array, StringComparator);
            for (String key : array) {
                sb.append(key);
                sb.append("=");
                sb.append(mapping.get(key));
            }
        }
        System.out.println("before sig:" + sb.toString());
        //分配给爬虫的对应公私钥对
        String pub = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCyYhw3zrUeFCmvuu82VAkFIX6NKtQPGdKAWVFYhXR9BwFeELmehdEUwcwoHECkzDN4DArsHegWx1nkv4S1+Yz3YIWc0eO2TQgQISw0moj7seqFiAwxzYko5BApabaXQJfR/veGWakEvJCk+jTrH/R6nv1V+8g71HWqnPKBbEdsyQIDAQAB";
        String pri = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALJiHDfOtR4UKa+67zZUCQUhfo0q1A8Z0oBZUViFdH0HAV4QuZ6F0RTBzCgcQKTMM3gMCuwd6BbHWeS/hLX5jPdghZzR47ZNCBAhLDSaiPux6oWIDDHNiSjkEClptpdAl9H+94ZZqQS8kKT6NOsf9Hqe/VX7yDvUdaqc8oFsR2zJAgMBAAECgYBgjYw6hM8x/bXmoWczX98WAOAv5turZM20nSPTp0C7H9yUnrbp4AKgmpk3qLswuDqvos0Sqslh8vtsPmHF4dJzdfXHBHDec93O/b4QTlKr6tTEPdjwkF/JU3mgMQZsNEUdmVHfNG2owsI+0VEfHMfn09VIgs4SQjSbijIQ7Td6VQJBAPbE5m3Q1dUfuDCHuxQrRCIcH8UWTgDLwqvFtfRiD+/C6jpsrarXHUIuxgiJ1jVq1TiE0X/pNc6oUBWJZNXJow8CQQC5DlYH/R573/r2al1y6sYmgGmneHeEbOffzngzxqU+8GNAIhWN1yC2DOdiMUCmgVP34WG4WcIpWHzAkfUnSRKnAkAzizM6cumHR8XYVTGNZ/AmU8uLBjqqzeTOrlBwSF9dzE/SfkrUKXSSE2UH+YqFw9ffo1aDKjoz/VIk/XrTcPefAkEAmN/a+maEVFlH/WEJKfIBF7Vlks/WDDPbqevrKPqlcEUt+MEvhSl/AGXQkDGX8vVL5K7wB1c/KuDKzlrFZ1raaQJBAOrZPzsHcsOS91fwRyVF37vdtRUS0YTMKnFAKI0254UXKbmzbqOSwKC3hkYcu9jIzWkMk8kB2SMFqh9+xPsTNTc=";
        RsaHelper rsaHelper = new RsaHelper(pub, pri);
        String sig = Base64Util.encodeToString(rsaHelper.sign(sb.toString().getBytes("utf-8")));
        mapping.put("_sig", sig);
        System.out.println("sig:" + sig);
        //3.构造请求
        StringBuilder req = new StringBuilder();
        for (Entry<String, String> entry : mapping.entrySet()) {
            req.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "utf-8")).append("&");//进行url encoding
        }
        System.out.println("http://127.0.0.1:8080/m.api?" + req.toString());
    }

    @Test
    public void testMixer() {
        initWithUserInfo();
        String v = "kkkpppnnnqqq";
        final Demo_SayHello req1 = new Demo_SayHello("abc");
        final Apitest_TestDemoSayHello req2 = new Apitest_TestDemoSayHello("abc");
        final Apitest_TestSimpleTestEntityReturn req3 = new Apitest_TestSimpleTestEntityReturn(v);
        final Mixer_Mix_A_B_C mix = new Mixer_Mix_A_B_C(req1, req2, req3);

        ServerResponse sr = accessor.fillApiResponse(new BaseRequest[] { req1, req2, req3, mix });
        Assert.assertEquals(0, sr.getReturnCode());
        Assert.assertEquals(0, req1.getReturnCode());
        Assert.assertEquals(0, req2.getReturnCode());
        Assert.assertEquals(0, req3.getReturnCode());
        Assert.assertEquals(v, req3.getResponse().strValue);
        Assert.assertEquals(0, mix.getReturnCode());

        Assert.assertEquals(req1.getResponse().serialize().toString(), mix.getResponse().a.serialize().toString());
        Assert.assertEquals(req2.getResponse().serialize().toString(), mix.getResponse().b.serialize().toString());
        Assert.assertEquals(req3.getResponse().serialize().toString(), mix.getResponse().c.serialize().toString());
    }

    @Test
    public void testMockObject() {
        initWithUserInfo();
        final Demo_TestMock testMock = new Demo_TestMock("NAME");
        accessor.fillApiResponse(testMock);
        Api_DEMO_DemoEntity resp = testMock.getResponse();
        Assert.assertEquals(ApiCode.SUCCESS, testMock.getReturnCode());
        Assert.assertEquals(resp.id, 1234567);
        Assert.assertEquals(resp.name, "mock test");
    }

    @Test
    public void testShortCircuit() {
        initWithUserInfo();
        final Demo_TestShortCircuit testMock = new Demo_TestShortCircuit("NAME");
        accessor.fillApiResponse(testMock);
        Api_DEMO_DemoEntity resp = testMock.getResponse();
        Assert.assertEquals(ApiCode.SUCCESS, testMock.getReturnCode());
        Assert.assertEquals(resp.id, 1234567);
        Assert.assertEquals(resp.name, "mock test");
    }

    @Test
    public void testMockService() {
        initWithUserInfo();
        final Demo_TestMockService testMock = new Demo_TestMockService("NAME");
        accessor.fillApiResponse(testMock);
        Api_DEMO_DemoEntity resp = testMock.getResponse();
        Assert.assertEquals(ApiCode.SUCCESS, testMock.getReturnCode());
        Assert.assertEquals(resp.id, 7654321);
        Assert.assertEquals(resp.name, "mock service test NAME");
    }

    @Test
    public void testIgnoreParameterForSecurity() {
        initWithUserInfo();
        final Demo_TestMockService testMock = new Demo_TestMockService("NAME");
        final Demo_TestIgnoreParameterForSecurity testIgnoreParameterForSecurity = new Demo_TestIgnoreParameterForSecurity("hahaha");
        final BaseRequest[] reqs = new BaseRequest[] { testMock, testIgnoreParameterForSecurity };
        accessor.fillApiResponse(reqs);
        Api_DEMO_DemoEntity resp = testMock.getResponse();
        Assert.assertEquals(ApiCode.SUCCESS, testMock.getReturnCode());
        Assert.assertEquals(resp.id, 7654321);
        Assert.assertEquals(resp.name, "mock service test NAME");
    }

    // _mt=r1:r2/r3@1,r2:r4,r3@1:r5,r3@2:r5,r4,r5,r6:r5,r7,r8:r6/r7
    @Test
    public void testDependencies() {
        initWithUserInfo();
        Demo_TestApiInjectionR4 r4 = new Demo_TestApiInjectionR4();
        r4.setName("R4");
        Demo_TestApiInjectionR5 r5 = new Demo_TestApiInjectionR5("R5");
        Demo_TestApiInjectionR6 r6 = Demo_TestApiInjectionR6.createDependencyBuilder().depends(r5).build();
        r6.setName("R6");
        Demo_TestApiInjectionR7 r7 = new Demo_TestApiInjectionR7();
        r7.setName("R7");
        Demo_TestApiInjectionR8 r8 = Demo_TestApiInjectionR8.createDependencyBuilder().depends(r6).depends(r7).build("R8");
        Demo_TestApiInjectionR3 r3_1 = Demo_TestApiInjectionR3.createDependencyBuilder().depends(r5).build("R3_1");
        Demo_TestApiInjectionR3 r3_2 = Demo_TestApiInjectionR3.createDependencyBuilder().depends(r5).build("R3_2");
        Demo_TestApiInjectionR2 r2 = Demo_TestApiInjectionR2.createDependencyBuilder().depends(r4).build();
        r2.setName("R2");
        Demo_TestApiInjectionR1 r1 = Demo_TestApiInjectionR1.createDependencyBuilder().depends(r2).depends(r3_1).build("R1");

        final BaseRequest[] reqs = new BaseRequest[] { r1, r2, r3_1, r3_2, r4, r5, r6, r7, r8 };
        accessor.fillApiResponse(reqs);
        System.out.println("result:" + r1.getResponse().serialize());
        checkResponse(r1.getResponse());
        checkResponse(r2.getResponse());
        checkResponse(r3_1.getResponse());
        checkResponse(r3_2.getResponse());
        checkResponse(r4.getResponse());
        checkResponse(r5.getResponse());
        checkResponse(r6.getResponse());
        checkResponse(r7.getResponse());
        checkResponse(r8.getResponse());
    }

    private void checkResponse(Api_DEMO_ComplexTestEntity e) {
        Assert.assertTrue(e.boolValue);
        Assert.assertTrue(e.byteValue > 0);
        Assert.assertEquals('x', e.charValue);
        Assert.assertTrue(e.doubleValue > 0);
        Assert.assertTrue(e.floatValue > 0);
        Assert.assertTrue(e.shortValue > 0);
        Assert.assertTrue(e.intValue > 0);
        Assert.assertTrue(e.longValue > 0);
        Assert.assertTrue(e.simpleTestEntity != null);
        Assert.assertTrue(e.simpleTestEntity.strValue != null);
        Assert.assertEquals(7, e.simpleTestEntity.intArray.length);
        Assert.assertEquals(2, e.simpleTestEntityList.size());
        Assert.assertEquals(Api_KeyValueList.class, e.dynamicEntity.entity.getClass());
        Assert.assertEquals(2, e.dynamicEntityList.size());
        Assert.assertEquals(Api_DEMO_SimpleTestEntity.class, e.dynamicEntityList.get(0).entity.getClass());
        Assert.assertEquals(Api_KeyValueList.class, e.dynamicEntityList.get(1).entity.getClass());
    }
}
