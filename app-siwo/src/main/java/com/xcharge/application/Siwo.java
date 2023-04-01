package com.xcharge.application;

import cloud.phusion.Context;
import cloud.phusion.DataObject;
import cloud.phusion.Engine;
import cloud.phusion.ScheduledTask;
import cloud.phusion.application.HttpBaseApplication;
import cloud.phusion.application.InboundEndpoint;
import cloud.phusion.application.OutboundEndpoint;
import cloud.phusion.protocol.http.HttpMethod;
import cloud.phusion.protocol.http.HttpRequest;
import cloud.phusion.protocol.http.HttpResponse;
import cloud.phusion.storage.FileStorage;
import cloud.phusion.storage.KVStorage;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.impl.SiwoService;
import com.xcharge.application.util.Transformer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 思沃（万向智控）
 * Protocol: ParkingDiscount (input extraInfo:hoursToRediscount)
 */
public class Siwo extends HttpBaseApplication implements ScheduledTask {
    private final static String _position = Siwo.class.getName();
    private final static String COUPONKEY_PREFIX = "Coupon-";
    private SiwoService service = null;

    // parkId -> keyPath
    private Map<String, String> parks = new ConcurrentHashMap<>();
    private long intervalRefreshToken = 0;

    @Override
    protected void onInit(JSONObject config, Context ctx) throws Exception {
        service = new SiwoService(
                getId(),
                config.getString("serviceUrl"),
                config.getIntValue("intervalTokenExpire")
        );

        intervalRefreshToken = Math.round(config.getIntValue("intervalTokenExpire") * 0.9);
    }

    @Override
    public void onStart(Context ctx) throws Exception {
        String taskId = getId() + "TokenRefreshingTask";
        ctx.getEngine().scheduleTask(taskId, this, null, intervalRefreshToken, 0, true, ctx);
    }

    @Override
    public void onStop(Context ctx) throws Exception {
        String taskId = getId() + "TokenRefreshingTask";
        ctx.getEngine().removeScheduledTask(taskId, ctx);
    }

    public void addPark(String park, String keyPath) {
        parks.put(park, keyPath);
    }

    public void removePark(String park) {
        parks.remove(park);
    }

    @Override
    public void run(String taskId, Context ctx) {
        Set<String> parkIds = parks.keySet();

        for (String parkId : parkIds) {
            try {
                service.retrieveToken(parkId, parks.get(parkId), true, ctx);
            } catch (Exception ex) {
                ctx.logError(_position, "Failed to refresh token", "parkId="+parkId, ex);
            }
        }
    }

    @InboundEndpoint(address="/certificate/{filename}", connectionKeyInReqeust="NONE")
    public void uploadCertificate(HttpRequest request, HttpResponse response, String[] integrationIds,
                            String connectionId, Context ctx) throws Exception {

        response.setStatusCode(200);
        response.setHeader("Content-Type", "text/plain");

        if (request.getMethod().equals(HttpMethod.POST)) {
            String path = request.getParameter("filename");
            if (path!=null && path.length()>0) {
                if (request.hasFiles()) {
                    Set<String> fileNames = request.getFileNames();
                    FileStorage storage = ctx.getEngine().getFileStorageForApplication(getId());
                    path = "/certificates/"+path+".pem";

                    for (String f : fileNames) {
                        storage.saveToFile(path, request.getFileContent(f));
                        break; // 忽略后面的其它文件
                    }

                    response.setBody(new DataObject("Done"));
                    return;
                }
            }
        }

        response.setBody(new DataObject("Failed"));
    }

    @OutboundEndpoint
    public DataObject requestParkingDiscount(DataObject msg, String integrationId, String connectionId, Context ctx) throws Exception {
        JSONObject objMsg;
        try {
            objMsg = Transformer.translateRequestMessage(msg.getJSONObject());
        } catch (Exception ex) {
            ctx.logError(_position, "Failed to translate the message", ex);
            return new DataObject("{\"code\":\"400\", \"desc\":\""+ex.getMessage()+"\"}");
        }

        JSONObject connConfig = getConnectionConfig(connectionId);
        Engine engine = ctx.getEngine();
        KVStorage storage = engine.getKVStorageForApplication(getId());

        String keyPath = connConfig.getString("keyPath");
        String parkId = objMsg.getString("parkId");
        String tradeNo = objMsg.getString("tradeNo");
        String plate = objMsg.getString("plate");
        int hoursToRediscount = objMsg.getIntValue("hoursToRediscount", 1);
        String parkPlatePaire = parkId + plate.substring(1); // 车牌去掉第一个字符（汉字）

        parks.put(parkId, keyPath);

        /*  【排重处理】

            由于停车系统没有交易排重、车辆在场校验等安全机制，特在此加以处理；
            同时考虑到输错车牌的情况，允许更换车牌重新发券。

            在当前车场的 hoursToRediscount 时间范围内：
            1 若当前充电交易未发过券：
                1.1 若当前车辆未（通过其它充电交易）发过券，则发券
                1.2 若车辆已发券，则不再发券
            2 若交易已发过券：
                2.1 若当前车辆与原车A相同，则不再发券
                2.2 若当前车辆是另一辆车B，则检查A车的券是否已被使用过：
                    2.2.1 未使用过，则删除该券，然后执行 1 的流程
                    2.2.2 使用过则不再发券

            记录充电交易发券情况：key=Coupon-<orderNo>, value=<parkId><plateX>
            记录车辆发券情况：key=Coupon-<parkId><plateX>, value=<plate>:<discountId>

            注：plateX 是车牌去掉第一个字符（汉字）；上述 key 存活的时间为 hoursToRediscount
         */

        boolean toIssueCoupon = false;

        String existParkPlatePair = (String) storage.get(COUPONKEY_PREFIX+tradeNo);

        if (existParkPlatePair == null) {
            if (! storage.doesExist(COUPONKEY_PREFIX+parkPlatePaire)) toIssueCoupon = true;
        }
        else {
            if (! existParkPlatePair.equals(parkPlatePaire)) {
                String existCouponInfo = (String) storage.get(COUPONKEY_PREFIX+existParkPlatePair);

                if (existCouponInfo!=null && existCouponInfo.length()>0) {
                    int pos = existCouponInfo.indexOf(":");
                    String existPlate = existCouponInfo.substring(0,pos);
                    String discountId = existCouponInfo.substring(pos+1);

                    JSONObject coupons = service.queryValidCoupons(existPlate, parkId, keyPath, ctx);

                    if (coupons != null) {
                        if (! coupons.getBooleanValue(discountId,false)) {
                            // 券未被使用过，将其清除
                            service.cancelCoupon(discountId, parkId, keyPath, ctx);

                            // 当前车辆未（通过其它充电交易）发过券，可以发券
                            if (! storage.doesExist(COUPONKEY_PREFIX+parkPlatePaire)) toIssueCoupon = true;
                        }
                    }
                    else toIssueCoupon = true;
                }
                else toIssueCoupon = true;
            }
        }

        if (toIssueCoupon) {
            // 实际发券

            JSONObject result = service.issueCoupon(
                    objMsg.getInteger("discountType"),
                    objMsg.getDouble("discountValue"),
                    objMsg.getString("validDate"),
                    plate, parkId, keyPath, ctx
            );

            if (result!=null && "ok".equals(result.getString("code"))) {
                JSONObject data = result.getJSONObject("data");
                String discountId = data==null ? null : data.getString("discountId");

                if (discountId != null) {
                    long interval = hoursToRediscount * 60 * 60 * 1000;
                    storage.put(COUPONKEY_PREFIX + tradeNo, parkPlatePaire, interval, ctx);
                    storage.put(COUPONKEY_PREFIX + parkPlatePaire, plate + ":" + discountId, interval, ctx);
                }

                return new DataObject(result);
            }
            else {
                if (result == null) return new DataObject("{\"code\":\"510\", \"desc\":\"发券失败\"}");
                else return new DataObject(result);
            }
        }
        else
            return new DataObject("{\"code\":\"511\", \"desc\":\"在"+hoursToRediscount+"小时内不能重复领券\"}");
    }

}
