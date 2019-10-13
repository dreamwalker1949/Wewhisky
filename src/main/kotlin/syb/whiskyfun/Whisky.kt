package syb.whiskyfun

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * Tasting Note
 * date 2019-10-12
 * @author SunYiBo
 */
@ApiModel("酒评")
data class Whisky(
    @ApiModelProperty(value = "标题")
    var title: String = "",
    @ApiModelProperty(value = "打分")
    var score: String = "",
    @ApiModelProperty(value = "正文")
    var text: String = "",
    @ApiModelProperty(value = "时间")
    var date: String = "",
    @ApiModelProperty(value = "地址")
    var url: String = ""
)