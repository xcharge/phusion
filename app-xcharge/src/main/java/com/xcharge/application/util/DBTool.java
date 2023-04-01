package com.xcharge.application.util;

import cloud.phusion.Context;
import cloud.phusion.DataObject;
import cloud.phusion.Engine;
import cloud.phusion.storage.DBStorage;
import cloud.phusion.storage.Record;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;

public class DBTool {
    public final static String chargeTable = "charges";

    private final static String chargeTableSchema = "\n" +
            "{\n" +
            "    \"name\": \"charges\",\n" +
            "    \"desc\": \"充电信息记录\",\n" +
            "    \"fields\": {\n" +
            "        \"stationOwner\": \"String[50]\",\n" +
            "        \"stationId\": \"String[50]\",\n" +
            "        \"chargingId\": \"String[50]\",\n" +
            "        \"chargingAuxId\": \"String[50]\",\n" +
            "        \"deviceId\": \"String[50]\",\n" +
            "        \"deviceType\": \"String[20]\",\n" +
            "        \"carNo\": \"String[20]\",\n" +
            "        \"mobile\": \"String[20]\",\n" +
            "        \"userId\": \"String[50]\",\n" +
            "        \"userType\": \"String[20]\",\n" +
            "        \"powerCharged\": \"Float\",\n" +
            "        \"duration\": \"Integer\",\n" +
            "        \"startTime\": \"String[20]\",\n" +
            "        \"endTime\": \"String[20]\"\n" +
            "    },\n" +
            "    \"indexes\": [\n" +
            "        {\"field\": \"chargingId\", \"primary\": true},\n" +
            "        {\"field\": \"chargingAuxId\"},\n" +
            "        {\"field\": \"stationId\"},\n" +
            "        {\"field\": \"deviceId\"},\n" +
            "        {\"field\": \"carNo\"},\n" +
            "        {\"field\": \"mobile\"},\n" +
            "        {\"field\": \"userId\"},\n" +
            "        {\"field\": \"startTime\"}\n" +
            "    ]\n" +
            "}";

    public static void main(String[] args) {
        System.out.println(chargeTableSchema);
    }

    public static void prepareChargeTable(String applicationId, Engine engine) throws Exception {
        DBStorage storage = engine.getDBStorageForApplication(applicationId);

        JSONObject schema = JSON.parseObject(chargeTableSchema);
        schema.remove("name");
        schema.remove("desc");

        storage.prepareTable(chargeTable, schema.toJSONString());
    }

    private static final String _queryChargingInfoSelectClause = "";
    private static final String _queryChargingInfoOrderClause = "startTime desc";

    private static void _addWhereCondition(StringBuilder whereClause, ArrayList<Object> params, JSONObject query, String field) {
        _addWhereCondition(whereClause, params, query, field, "=", field);
    }
    private static void _addWhereCondition(StringBuilder whereClause, ArrayList<Object> params, JSONObject query, String fieldDB, String op, String fieldQuery) {
        if (query.containsKey(fieldQuery)) {
            if (whereClause.length() > 0) whereClause.append(" AND ");
            whereClause.append(fieldDB).append(op).append("?");
            params.add(query.get(fieldQuery));
        }
    }

    public static DataObject queryChargingInfo(String applicationId, JSONObject query, Context ctx) throws Exception {
        DBStorage storage = ctx.getEngine().getDBStorageForApplication(applicationId);
        if (query == null) query = new JSONObject();

        StringBuilder whereClause = new StringBuilder();
        ArrayList<Object> params = new ArrayList<>();
        _addWhereCondition(whereClause, params, query, "startTime", ">=", "timeFrom");
        _addWhereCondition(whereClause, params, query, "startTime", "<", "timeTo");
        _addWhereCondition(whereClause, params, query, "chargingId");
        _addWhereCondition(whereClause, params, query, "chargingAuxId");
        _addWhereCondition(whereClause, params, query, "stationId");
        _addWhereCondition(whereClause, params, query, "deviceId");
        _addWhereCondition(whereClause, params, query, "carNo");
        _addWhereCondition(whereClause, params, query, "mobile");
        _addWhereCondition(whereClause, params, query, "userId");

        long from  = query.getLongValue("pageFrom", 0L);
        long length = query.getLongValue("pageLength", 100L);

        Record[] records = storage.queryRecords(
                DBTool.chargeTable, _queryChargingInfoSelectClause,
                whereClause.toString(), params, _queryChargingInfoOrderClause,
                from, length, ctx
        );

        StringBuilder result = new StringBuilder();
        result.append("{").append("\"result\": [");

        if (records!=null && records.length>0) {
            for (int i = 0; i < records.length; i++) {
                Record record = records[i];
                if (i > 0) result.append(",");
                result.append(record.toJSONString());
            }
        }

        result.append("]}");
        return new DataObject(result.toString());
    }

    public static void storeChargingInfo(String applicationId, JSONObject connConfig, JSONObject msg, Context ctx) throws Exception {
        // 检查是否配置了需要保存充电信息

        JSONObject stations = connConfig.getJSONObject("storeChargeInfoForStations");
        if (stations==null || stations.size()==0) return;

        String stationId = msg.getString("stationId");
        if (! stations.getBooleanValue(stationId, false)) return;

        // 保存充电信息

        DBStorage storage = ctx.getEngine().getDBStorageForApplication(applicationId);
        Record record = new Record(msg.toJSONString());

        storage.upsertRecordById(DBTool.chargeTable, "chargingId", record, ctx);
    }

}
