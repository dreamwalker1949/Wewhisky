package syb.wewhisky

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.apache.commons.beanutils.BeanUtils
import java.util.*

/**
 * Created by SunYiBo on 19/02/2019.
 * @user SunYiBo
 * @since 1.0-SNAPSHOT
 */
@ApiModel("帖子")
data class Post(
    @ApiModelProperty(value = "标题")
    var title: String = "",
    @ApiModelProperty(value = "板块")
    var type: Int = 1,
    @ApiModelProperty(value = "帖子id")
    var id: Int = -1,
    @ApiModelProperty(value = "采集时间")
    var collect: Date = Date()
) : PostBase() {

  constructor(reply: Reply, title: String, type: Int) : this(title, type) {
    BeanUtils.copyProperties(this, reply)
  }

}
