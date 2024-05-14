package com.xcharge.application;

import cloud.phusion.Context;
import cloud.phusion.DataObject;
import cloud.phusion.Engine;
import cloud.phusion.application.HttpBaseApplication;
import cloud.phusion.application.InboundEndpoint;
import cloud.phusion.application.OutboundEndpoint;
import cloud.phusion.protocol.http.HttpClient;
import cloud.phusion.protocol.http.HttpMethod;
import cloud.phusion.protocol.http.HttpRequest;
import cloud.phusion.protocol.http.HttpResponse;
import cloud.phusion.storage.FileStorage;
import cloud.phusion.storage.KVStorage;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.util.Transformer;

import java.util.Set;

public class JsLifeV3 extends HttpBaseApplication {
    private final static String _position = JsLifeV3.class.getName();

    @OutboundEndpoint
    public DataObject requestParkingDiscount(DataObject msg, String integrationId, String connectionId, Context ctx) throws Exception {
        String serviceUrl = getApplicationConfig().getString("serviceUrl");
        Engine engine = ctx.getEngine();
        JSONObject connConfig = getConnectionConfig(connectionId);
        KVStorage storage = engine.getKVStorageForApplication(getId());

        String appId = connConfig.getString("appId");
        String cid = connConfig.getString("cid");
        String abilityCode = connConfig.getString("abilityCode");
        String projectCode = connConfig.getString("projectCode");
        String privateKey = connConfig.getString("secret");

        JSONObject objMsg;
        try {
            objMsg = Transformer.translateRequestMessage(msg.getJSONObject(), privateKey, appId, cid, projectCode, abilityCode);
        } catch (Exception ex) {
            ctx.logError(_position, "Failed to translate the message", ex);
            return new DataObject("{\"code\":\"400\", \"desc\":\"" + ex.getMessage() + "\"}");
        }

        HttpClient http = ctx.getEngine().createHttpClient();
        HttpResponse response = http.post(serviceUrl)
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(new DataObject(objMsg))
                .context(ctx)
                .send();

        try {
            JSONObject objResponse = response.getBody().getJSONObject();
            return new DataObject(Transformer.translateResponseMessage(objResponse));
        } catch (Exception ex) {
            ctx.logError(_position, "Can not parse the response: " + response.getBody().getString(), ex);
            return new DataObject("{\"code\":\"500\", \"desc\":\"响应格式错误\"}");
        }
    }

//    @InboundEndpoint(address = "/certificate/{filename}", connectionKeyInReqeust = "NONE")
//    public void uploadCertificate(HttpRequest request, HttpResponse response, String[] integrationIds,
//                                  String connectionId, Context ctx) throws Exception {
//
//        response.setStatusCode(200);
//        response.setHeader("Content-Type", "text/plain");
//
//        if (request.getMethod().equals(HttpMethod.POST)) {
//            String path = request.getParameter("filename");
//            if (path != null && path.length() > 0) {
//                if (request.hasFiles()) {
//                    Set<String> fileNames = request.getFileNames();
//                    FileStorage storage = ctx.getEngine().getFileStorageForApplication(getId());
//                    path = "/certificates/" + path + ".pem";
//
//                    for (String f : fileNames) {
//                        storage.saveToFile(path, request.getFileContent(f));
//                        break; // 忽略后面的其它文件
//                    }
//
//                    response.setBody(new DataObject("Done"));
//                    return;
//                }
//            }
//        }
//
//        response.setBody(new DataObject("Failed"));
//    }
}
