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
public class NoticeUserAudit extends WechatNotice {

    @JsonProperty("thing1")
    @JSONField(name = "thing1")
    public String nickName;
    @JsonProperty("time5")
    @JSONField(name = "time5")
    public String auditTime;
    @JsonProperty("phrase7")
    @JSONField(name = "phrase7")
    public String status;

    public NoticeUserAudit() {}

    public NoticeUserAudit(String nickName, String status) {
        this.nickName = nickName;
        this.status = status;
        this.auditTime = CommonUtil.timeToString(System.currentTimeMillis(), null);
    }
}
