package syb.wewhisky.bean

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.*

/**
 * Created by SunYiBo on 19/02/2019.
 * @user SunYiBo
 * @since 1.0-SNAPSHOT
 */
@ApiModel("用户")
data class User(
    @ApiModelProperty(value = "id")
    val id: Int = -1,
    @ApiModelProperty(value = "用户名")
    val name: String = "",
    @ApiModelProperty(value = "签名")
    val sign: String? = null,
    @ApiModelProperty(value = "好友数")
    val friend: Int = 0,
    @ApiModelProperty(value = "回帖数")
    val reply: Int = 0,
    @ApiModelProperty(value = "主题数")
    val post: Int = 0,
    @ApiModelProperty(value = "分享数")
    val share: Int = 0,
    @ApiModelProperty(value = "真实姓名")
    val real: String? = null,
    @ApiModelProperty(value = "性别")
    val sex: Int = -1,
    @ApiModelProperty(value = "生日")
    val birth: String? = null,
    @ApiModelProperty(value = "居住地")
    val address: String? = null,
    @ApiModelProperty(value = "用户组")
    val level: Int = -1,
    @ApiModelProperty(value = "在线时间")
    val hour: Int = 0,
    @ApiModelProperty(value = "注册时间")
    val register: Date? = null,
    @ApiModelProperty(value = "最后访问")
    val last: Date? = null,
    @ApiModelProperty(value = "积分")
    val credit: Int = 0,
    @ApiModelProperty(value = "积点")
    val point: Int = 0,
    @ApiModelProperty(value = "麦粒")
    val malt: Int = 0,
    @ApiModelProperty(value = "贡献")
    val contribution: Int = 0
)
