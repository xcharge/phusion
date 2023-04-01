var bridge = require("phusion/JavaBridge");

exports._runTransaction = function(strTransaction) {
    var trx = new bridge.Transaction(strTransaction);
    var step = trx.getCurrentStep();

    switch (step) {
        case "before": onBefore(trx); break;
        case "after": onAfter(trx); break;
        case "exception": onException(trx); break;
    }

    return trx.toString();
};

function onBefore(trx) {
    var msg = trx.getMessage();
    var config = trx.getIntegrationConfig();

    // 检查是否满足优惠条件：充电时长

    var minDuration = _getConfig(config, "minDurationInMinutes", msg.stationId, 1);
    if (msg.duration < minDuration) {
        msg = {
            code: "413",
            desc: "充电时间过短，无法优惠"
        };

        trx.setMessage(msg);
        trx.moveToEnd();
        return;
    }

    // 检查是否满足优惠条件：车牌号或用户信息

    if (! msg.carNo && ! (msg.userId && msg.userType) && ! msg.mobile) {
        msg = {
            code: "414",
            desc: "无车牌号或用户信息，无法优惠"
        };

        trx.setMessage(msg);
        trx.moveToEnd();
        return;
    }

    // 检查是否多次优惠

    var hoursToRediscount = _getConfig(config, "hoursToRediscount", msg.stationId, 0);
    if (hoursToRediscount) {
        var user = (msg.carNo ? msg.carNo : "") + (msg.userId ? msg.userId : "") + (msg.mobile ? msg.mobile : "")

        var storage = trx.getContext().getEngine().getKVStorageForIntegration();
        if (storage.doesExist("IssuedCoupon"+user)) {
            msg = {
                code: "415",
                desc: hoursToRediscount+"小时内只能享受一次优惠"
            };

            trx.setMessage(msg);
            trx.moveToEnd();
            return;
        }

        trx.setProperty("user", user);
        trx.setProperty("hoursToRediscount", ""+hoursToRediscount);
    }

    // 生成优惠信息

    var discountType = _getConfig(config, "discountType", msg.stationId, 1);

    var result = {
        requestId: msg.chargingId,
        parkId: msg.stationId,
        carNo: msg.carNo,
        mobile: msg.mobile,
        userId: msg.userId,
        userType: msg.userType,
        type: discountType
    };

    if (discountType != 9) { // 9 是全免，不需要优惠数值
        var discountValues = _getConfig(config, "discountValues", msg.stationId, null);
        if (discountValues) {
            // 按阶梯充电量（从高到低）设置优惠数值

            var power = msg.powerCharged;

            for (var i=0; i<discountValues.length; i++) {
                var v = discountValues[i];
                if (power >= v.minPower) {
                    result.value = v.value;
                    break;
                }
            }
        }

        // 如果没有匹配到任何阶梯，则使用默认优惠数值
        if (! result.value) {
            result.value = _getConfig(config, "discountValue", msg.stationId, 0);

            if (! result.value) {
                msg = {
                    code: "416",
                    desc: "未匹配到优惠信息"
                };

                trx.setMessage(msg);
                trx.moveToEnd();
                return;
            }
        }
    }

    // 设置优惠有效期

    var hoursToExpire = _getConfig(config, "hoursToExpire", msg.stationId, 0);
    if (hoursToExpire) result.expireTime = _timestampToStr19(0, hoursToExpire*60*60*1000);

    // 加载附加信息

    if (config.extraInfoSource) {
        var extraInfo = {};

        _getExtraInfo(extraInfo, config.extraInfoSource.config, config);
        _getExtraInfo(extraInfo, config.extraInfoSource.msg, msg);
        _getExtraInfo(extraInfo, config.extraInfoSource.properties, null, trx);

        result.extraInfo = extraInfo;
    }

    trx.setMessage(result);
}

function onAfter(trx) {
    var msg = trx.getMessage();
    var config = trx.getIntegrationConfig();

    var hoursToRediscount = trx.getProperty("hoursToRediscount");
    if (hoursToRediscount && msg.data && msg.data.discountId) {
        var user = trx.getProperty("user");
        var storage = trx.getContext().getEngine().getKVStorageForIntegration();

        storage.put("IssuedCoupon"+user, msg.data.discountId, parseInt(hoursToRediscount) * 60 * 60 * 1000)
    }

    trx.moveToEnd();
}

function onException(trx) {
    var msg = {
        code: "500",
        desc: "系统发生错误：" + trx.getProperty("exception")
    };

    trx.setMessage(msg);
    trx.moveToEnd();
}

function _getExtraInfo(extraInfo, fields, source, trx) {
    if (fields && fields.length > 0) {
        fields.forEach(function(field) {
            extraInfo[field] = source ? source[field] : trx.getProperty(field);
        });
    }
}

function _getConfig(config, field, station, defaultValue) {
    if (! config) return defaultValue;

    var result;

    if (config.specialStations) {
        if (config.specialStations.hasOwnProperty(station))
            result = config.specialStations[station][field];
    }

    if (result === undefined) result = config[field];
    if (result === undefined) result = defaultValue;

    if (config.extraInfoSource) {
        var fields = config.extraInfoSource.config;
        if (fields && fields.length > 0) {
            for (var i=0; i<fields.length; i++) {
                if (fields[i] == field) {
                    // 如果该参数要被传递给下游，则不必在 JavaScript 中处理该参数
                    result = undefined;
                    break;
                }
            }
        }
    }

    return result;
}

function _timestampToStr19(timestamp, shift) {
    shift = shift || 0;
    var date;

    if (timestamp) date = new Date(timestamp+shift);
    else date = new Date((new Date()).getTime()+shift);

    var parts = [
        date.getFullYear(), "-",
        _f(date.getMonth()+1), "-",
        _f(date.getDate()), " ",
        _f(date.getHours()), ":",
        _f(date.getMinutes()), ":",
        _f(date.getSeconds())
    ];
    return parts.join('');
}

function _f(n) { return n>9 ? ""+n : "0"+n }
