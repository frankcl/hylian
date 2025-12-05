package xin.manong.hylian.server.wechat;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import xin.manong.weapon.base.util.CommonUtil;

/**
 * 用户审核通知
 *
 * @author frankcl
 * @date 2025-05-29 21:17:17
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoticeNewUser extends WechatNotice {

    @JsonProperty("name1")
    @JSONField(name = "name1")
    public String nickName;
    @JsonProperty("date5")
    @JSONField(name = "date5")
    public String registerTime;
    @JsonProperty("thing6")
    @JSONField(name = "thing6")
    public String status;
//    @JsonProperty("thing9")
//    @JSONField(name = "thing9")
//    public String channel;

    public NoticeNewUser() {
    }

    public NoticeNewUser(String nickName, String channel) {
        this.nickName = nickName;
//        this.channel = channel;
        this.status = "新用户注册";
        this.registerTime = CommonUtil.timeToString(System.currentTimeMillis(), null);
    }
}
