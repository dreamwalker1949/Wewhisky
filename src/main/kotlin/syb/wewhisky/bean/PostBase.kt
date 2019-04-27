package syb.wewhisky.bean

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.*

/**
 * Created by SunYiBo on 19/02/2019.
 * @user SunYiBo
 * @since 1.0-SNAPSHOT
 */
@ApiModel("帖子基类")
open class PostBase(
    @ApiModelProperty(value = "发布者")
    var user: Int = -1,
    @ApiModelProperty(value = "创建时间")
    var create: Date? = null,
    @ApiModelProperty(value = "内容")
    var text: String = "",
    @ApiModelProperty(value = "最后更新时间")
    var update: Date? = null
)
