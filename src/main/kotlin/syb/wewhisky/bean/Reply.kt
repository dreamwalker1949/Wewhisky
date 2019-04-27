package syb.wewhisky.bean

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * Created by SunYiBo on 19/02/2019.
 * @user SunYiBo
 * @since 1.0-SNAPSHOT
 */
@ApiModel("回复")
data class Reply(
    @ApiModelProperty(value = "回复id")
    var rid: String = "",
    @ApiModelProperty(value = "贴子id")
    var id: Int = -1,
    @ApiModelProperty(value = "回复")
    var reply: String? = null
) : PostBase()
