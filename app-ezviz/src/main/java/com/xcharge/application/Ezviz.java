package com.xcharge.application;

import cloud.phusion.Context;
import cloud.phusion.DataObject;
import cloud.phusion.Engine;
import cloud.phusion.application.HttpBaseApplication;
import cloud.phusion.application.OutboundEndpoint;
import cloud.phusion.protocol.http.HttpClient;
import cloud.phusion.protocol.http.HttpResponse;
import cloud.phusion.storage.FileStorage;
import cloud.phusion.storage.KVStorage;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

/**
 * 萤石云
 * https://open.ys7.com/
 *
 * Application config:
 *          {
 *              "serviceUrl": "https://open.ys7.com/api/lapp"
 *          }
 *
 * Connection config:
 *          {
 *              "appKey": String,
 *              "appSecret": String
 *          }
 *
 * Outbound endpoint: capturePic
 *      摄像头抓拍
 *
 *      Input - Message from Phusion Engine to the application
 *          {
 *              "deviceSerial": String, // 设备序列号，英文字母大写
 *              "quality": Integer, // 视频清晰度。见下
 *              "dataType": Integer // 返回图片的数据类型。见下
 *          }
 *
 *          视频清晰度：
 *              0：流畅
 *              1：高清 (720P)
 *              2：FCIF
 *              3：1080P
 *              4：400w
 *
 *          图片数据类型：
 *              0：图片网址。返回萤石云的原始图片网址，该网址将在 24 小时后失效
 *              1：BASE64 编码的图片数据
 *              2：图片文件地址。返回该应用内的公开图片文件地址
 *
 *      Output - Message from the application to Phusion Engine
 *          {
 *              "code": Integer, // 200 为成功
 *              "msg": String, // 处理结果说明
 *              "data": {
 *                  "url": String, // 图片的原始网址
 *                  "image": String // JPG 图片
 *              }
 *          }
 *
 * Outbound endpoint: vehicleRecognition
 *      识别图片中的车辆
 *
 *      Input - Message from Phusion Engine to the application
 *          {
 *              "dataType": Integer, // 图片数据类型
 *              "image": String // JPG 图片，见下
 *          }
 *
 *          图片规格：800px * 600px 以上（宽 <= 3900px，高 <= 3100px），大小不超过 2MB
 *
 *      Output - Message from the application to Phusion Engine
 *          {
 *              "code": Integer, // 200 为成功
 *              "msg": String, // 处理结果说明
 *              "data": [{
 *                  "plateNumber": String, // 车牌号
 *                  "rect": {"x":Float, "y":Float, "width":Float, "height":Float}, // 车辆在图片中的位置，左上角为原点，单位为 px
 *                  "vehicleColor": {"code":Integer, "val":String, "des":String}, // 车辆颜色
 *                  "vehicleType": {"code":Integer, "val":String, "des":String}, // 车辆种类
 *                  "vehicleModel": {"logo":String, "sublogo":String, "model":String} // 车辆型号
 *              }]
 *          }
 */
public class Ezviz extends HttpBaseApplication {
    private final String _position = Ezviz.class.getName();
    private final int CAMERA_CHANNEL = 1;

    @Override
    public void onConnect(String connectionId, JSONObject config, Context ctx) throws Exception {
        _retriveToken(connectionId, ctx);
    }

    @OutboundEndpoint
    public DataObject capturePic(DataObject msg, String integrationId, String connectionId, Context ctx) throws Exception {
        String token = _retriveToken(connectionId, ctx);
        if (token == null) return new DataObject("{\"code\":500, \"msg\":\"获取 Token 失败\"}");

        JSONObject objMsg = msg.getJSONObject();
        String deviceSerial = objMsg.getString("deviceSerial");
        Integer dataType = objMsg.getInteger("dataType");
        Integer quality = objMsg.getInteger("quality");

        String picUrl = null;

        StringBuilder body = new StringBuilder();
        body.append("accessToken=").append(token)
                .append("&deviceSerial=").append(deviceSerial)
                .append("&channelNo=").append(CAMERA_CHANNEL);
        if (quality != null) body.append("&quality=").append(quality);

        HttpClient http = ctx.getEngine().createHttpClient();
        HttpResponse response = http.post(getApplicationConfig().getString("serviceUrl")+"/device/capture")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(body.toString())
                .context(ctx)
                .send();

        if (response.getStatusCode() < 300) {
            JSONObject objBody = response.getBody().getJSONObject();
            if ("200".equals(objBody.getString("code"))) {
                picUrl = objBody.getJSONObject("data").getString("picUrl");
            }
            else
                return new DataObject(String.format("{\"code\":%s, \"msg\":\"%s\"}",
                        objBody.getString("code"),
                        objBody.getString("msg")==null?"":objBody.getString("msg").replaceAll("\\\"","\\\\\"")));
        }
        else
            return new DataObject("{\"code\":500, \"msg\":\"抓拍图像失败\"}");

        switch (dataType) {
            case 0:
                // Return the original picture URL
                return new DataObject("{\"code\":200, \"msg\":\"操作成功\", \"data\":{\"url\":\""+picUrl+"\", \"image\":\""+picUrl+"\"}}");
            case 1:
                // Return BASE64-encoded picture
                String base64Pic = _encodePicToString(picUrl);
                StringBuilder result = new StringBuilder();
                result.append("{\"code\":200, \"msg\":\"操作成功\", \"data\":{")
                        .append("\"url\":\"").append(picUrl).append("\", ")
                        .append("\"image\":\"").append(base64Pic).append("\"")
                        .append("}}");
                return new DataObject(result.toString());
            case 2:
                // Save picture to public file
                String picPath = _savePicToFile(picUrl, deviceSerial, ctx);
                return new DataObject("{\"code\":200, \"msg\":\"操作成功\", \"data\":{\"url\":\""+picUrl+"\", \"image\":\""+picPath+"\"}}");
            default:
                return new DataObject("{\"code\":500, \"msg\":\"不支持该 dataType: "+dataType+"\"}");
        }
    }

    @OutboundEndpoint
    public DataObject vehicleRecognition(DataObject msg, String integrationId, String connectionId, Context ctx) throws Exception {
        String token = _retriveToken(connectionId, ctx);
        if (token == null) return new DataObject("{\"code\":500, \"msg\":\"获取 Token 失败\"}");

        JSONObject objMsg = msg.getJSONObject();
        String image = objMsg.getString("image");
        Integer dataType = objMsg.getInteger("dataType");

        StringBuilder body = new StringBuilder();
        body.append("accessToken=").append(token);

        switch (dataType) {
            case 0:
                // Give picture URL
                body.append("&dataType=").append(dataType)
                    .append("&image=").append(URLEncoder.encode(image, "UTF-8"));
                break;
            case 1:
                // Give BASE64-encoded picture
                body.append("&dataType=").append(dataType)
                        .append("&image=").append(URLEncoder.encode(image, "UTF-8"));
                break;
            case 2:
                // Give BASE64-encoded picture from file
                body.append("&dataType=1&image=")
                        .append( _encodePicFileToString(image, ctx) );
                break;
        }

        HttpClient http = ctx.getEngine().createHttpClient();
        HttpResponse response = http.post(getApplicationConfig().getString("serviceUrl") + "/intelligence/vehicle/analysis/props")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(body.toString())
                .context(ctx)
                .send();

        if (response.getStatusCode() < 300) {
            JSONObject objBody = response.getBody().getJSONObject();
            if ("200".equals(objBody.getString("code")))
                return new DataObject( _composeVehicleRecognitionResult(objBody) );
            else
                return new DataObject(String.format("{\"code\":%s, \"msg\":\"%s\"}",
                        objBody.getString("code"), objBody.getString("msg")));
        }
        else
            return new DataObject("{\"code\":500, \"msg\":\"车辆识别失败\"}");
    }

    private String _encodePicToString(String picUrl) throws Exception {
        byte[] bytes = IOUtils.toByteArray( new URL(picUrl) ); // To be optimized
        return Base64.getEncoder().encodeToString(bytes);
    }

    private String _encodePicFileToString(String picPath, Context ctx) throws Exception {
        FileStorage storage = ctx.getEngine().getFileStorageForApplication(getId());

        try (InputStream in = storage.readFromPublicFile(picPath, ctx)) {
            byte[] bytes = IOUtils.toByteArray( in ); // To be optimized
            return URLEncoder.encode(Base64.getEncoder().encodeToString(bytes), "UTF-8");
        }
    }

    private String _savePicToFile(String picUrl, String deviceSerial, Context ctx) throws Exception {
        URL url = new URL(picUrl);
        FileStorage storage = ctx.getEngine().getFileStorageForApplication(getId());

        SimpleDateFormat df = new SimpleDateFormat("/yyyy/MM/dd/HHmmss");
        String strDate = df.format(new Date());
        String path = "/capture/"+deviceSerial+strDate+".jpg";

        try (InputStream in = url.openStream()) {
            storage.saveToPublicFile(path, in, ctx);
        }

        return path;
    }

    private String _retriveToken(String connectionId, Context ctx) {
        String tokenKey = "Token-" + connectionId;
        Engine engine = ctx.getEngine();
        KVStorage storage;

        // Firstly, try to fetch token from KV storage
        // Before expiring, the token will not be changed, so do not need to refresh

        try {
            storage = engine.getKVStorageForApplication(getId());
            String token = (String) storage.get(tokenKey);
            if (token != null) return token;
        } catch (Exception ex) {
            ctx.logError(_position, "Failed to retrieve token from storage", ex);
            return null;
        }

        // If not in the storage, fetch token from web service

        String url = getApplicationConfig().getString("serviceUrl") + "/token/get";

        JSONObject config = getConnectionConfig(connectionId);
        String appKey = config.getString("appKey");
        String appSecret = config.getString("appSecret");
        String token = null;

        try {
            HttpClient http = engine.createHttpClient();

            HttpResponse response = http.post(url + "?appKey=" + appKey + "&appSecret=" + appSecret)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .context(ctx)
                    .send();

            if (response.getStatusCode() < 300) {
                JSONObject body = response.getBody().getJSONObject();
                if ("200".equals(body.getString("code"))) {
                    JSONObject data = body.getJSONObject("data");
                    token = data.getString("accessToken");
                    long msToExpire = data.getLong("expireTime") - System.currentTimeMillis();

                    storage.put(tokenKey, token, msToExpire, ctx);
                }
                else
                    ctx.logError(_position, "Token not returned properly", "body="+response.getBody().getString());
            }
            else
                ctx.logError(_position, "Token not returned properly", "httpCode="+response.getStatusCode());
        } catch (Exception ex) {
            ctx.logError(_position, "Failed to query token", ex);
        }

        return token;
    }

    private String _composeVehicleRecognitionResult(JSONObject body) {
        StringBuilder result = new StringBuilder();
        result.append("{")
                .append("\"code\":").append(body.getInteger("code")).append(", ")
                .append("\"msg\":\"").append(body.getString("msg")==null?"":body.getString("msg").replaceAll("\\\"","\\\\\"")).append("\", ")
                .append("\"data\":[");

        JSONArray arr = body.getJSONArray("data");
        if (arr!=null && arr.size()>0) {
            for (int i = 0; i < arr.size(); i++) {
                if (i > 0) result.append(", ");
                JSONObject vehicle = arr.getJSONObject(i);

                result.append("{")
                        .append("\"rect\":").append(vehicle.getString("rect"));
                if (vehicle.containsKey("plateNumber") && vehicle.getString("plateNumber")!=null)
                    result.append(", \"plateNumber\":\"").append(vehicle.getString("plateNumber")).append("\"");
                if (vehicle.containsKey("vehicleColor"))
                    result.append(", \"vehicleColor\":").append(vehicle.getString("vehicleColor"));
                if (vehicle.containsKey("vehicleType"))
                    result.append(", \"vehicleType\":").append(vehicle.getString("vehicleType"));

                if (vehicle.containsKey("vehicleLogo")) {
                    result.append(", \"vehicleModel\":{")
                            .append("\"logo\":\"").append(vehicle.getString("vehicleLogo")).append("\"");
                    if (vehicle.containsKey("vehicleSublogo"))
                        result.append(", \"sublogo\":\"").append(vehicle.getString("vehicleSublogo")).append("\"");
                    if (vehicle.containsKey("vehicleModel"))
                        result.append(", \"model\":\"").append(vehicle.getString("vehicleModel")).append("\"");
                    result.append("}");
                }

                result.append("}");
            }
        }

        result.append("]}");
        return result.toString();
    }

}
