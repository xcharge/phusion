package com.xcharge.application;

import cloud.phusion.Context;
import cloud.phusion.DataObject;
import cloud.phusion.Engine;
import cloud.phusion.application.HttpBaseApplication;
import cloud.phusion.application.InboundEndpoint;
import cloud.phusion.application.OutboundEndpoint;
import cloud.phusion.integration.Integration;
import cloud.phusion.protocol.http.HttpMethod;
import cloud.phusion.protocol.http.HttpRequest;
import cloud.phusion.protocol.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.util.DBTool;
import com.xcharge.application.util.Signature;
import com.xcharge.application.util.Transformer;

/**
 * 智充充电服务
 * Protocol: EVChargingNotification, EVChargingInfoQuery
 *
 * 1、通知接收路径: /xcharge/notification/{client}
 * 2、可以把接收到充电信息存储到数据库供查询之用。
 */
public class XCharge extends HttpBaseApplication {
    private final static String _position = XCharge.class.getName();

    @InboundEndpoint(address="/notification/{client}", connectionKeyInConfig="client", connectionKeyInReqeust="client")
    public void chargingNotification(HttpRequest request, HttpResponse response, String[] integrationIds,
                                         String connectionId, Context ctx) throws Exception {

        response.setStatusCode(200);
        response.setHeader("Content-Type", "application/json; charset=UTF-8");

        if (! request.getMethod().equals(HttpMethod.POST)) {
            response.setBody(new DataObject("{\"code\": \"200\", \"desc\": \"请求格式错误\"}"));
            return;
        }

        if (integrationIds==null || integrationIds.length==0) {
            response.setBody(new DataObject("{\"code\": \"900\", \"desc\": \"尚无可用集成流程\"}"));
        }
        else {
            JSONObject msg;
            try {
                msg = request.getBody().getJSONObject();
            } catch (Exception ex) {
                ctx.logError(_position, "Can not parse the request body", ex);
                response.setBody(new DataObject("{\"code\": \"200\", \"desc\": \"请求格式错误\"}"));
                return;
            }

            // 验证签名

            JSONObject connConfig = getConnectionConfig(connectionId);
            JSONObject appConfig = getApplicationConfig();
            String secretKey = connConfig.getString("secretKey");
            long maxGap = appConfig.getLongValue("maxSignTimeGap", 30000L);

            if (Signature.verifySignature(msg, secretKey, maxGap)) { // 签名验证成功
                // 把输入消息转换为 EVChargingNotification 要求的 output 格式
                DataObject objMsg = new DataObject( Transformer.translateMessage(msg) );

                // 把充电信息记录到数据库
                if (appConfig.getBooleanValue("storeChargeInfo", false))
                    DBTool.storeChargingInfo(getId(), connConfig, objMsg.getJSONObject(), ctx);

                // 将消息发送给各个集成流程

                Engine engine = ctx.getEngine();
                boolean hasResult = false;

                for (int i = 0; i < integrationIds.length; i++) {
                    Integration it = engine.getIntegration(integrationIds[i]);
                    DataObject result = it.execute(objMsg, ctx);

                    // 通常只有一个流程会被执行，所以暂不考虑执行结果互相覆盖的问题
                    if (result != null) {
                        // XCharge 接口与 EVChargingNotification 的 input 格式相同，无须转换格式，直接发出
                        response.setBody(result);
                        hasResult = true;
                    }
                }

                if (! hasResult) {
                    response.setBody(new DataObject("{\"code\": \"901\", \"desc\": \"未匹配到集成流程\"}"));
                }
            }
            else {
                response.setBody(new DataObject(
                        "{\"code\": \"100\", \"desc\": \"签名错误（没有签名，或签名未通过验证，或签名已超时失效，暂只支持 MD5）\"}"
                ));
            }
        }
    }

    @OutboundEndpoint
    public DataObject queryChargingInfo(DataObject msg, String integrationId, String connectionId, Context ctx) throws Exception {
        JSONObject appConfig = getApplicationConfig();
        if (! appConfig.getBooleanValue("storeChargeInfo", false)) // 没有保存充电信息
            return new DataObject("{\"error\":{\"code\":\"410\",\"desc\":\"尚未开启保存充电信息功能\"}}");

        JSONObject query;
        try {
            query = msg.getJSONObject();
        } catch (Exception ex) {
            ctx.logError(_position, "Can not parse the incoming message", ex);
            return new DataObject("{\"error\":{\"code\":\"400\",\"desc\":\"请求格式错误\"}}");
        }

        return DBTool.queryChargingInfo(getId(), query, ctx);
    }

}
