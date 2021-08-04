package com.webank.wedatasphere.linkis.computation.client.interactive

import com.webank.wedatasphere.linkis.computation.client.AbstractLinkisJobBuilder
import com.webank.wedatasphere.linkis.manager.label.entity.engine.RunType.RunType
import com.webank.wedatasphere.linkis.ujes.client.UJESClient
import com.webank.wedatasphere.linkis.ujes.client.request.JobSubmitAction

/**
  * Created by enjoyyin on 2021/6/1.
  */
class InteractiveJobBuilder private[interactive]()
  extends AbstractLinkisJobBuilder[SubmittableInteractiveJob] {

  def setCode(code: String): this.type = addJobContent("code", code)

  def setRunType(runType: RunType): this.type= addJobContent("runType", runType.toString)

  def setRunTypeStr(runType: String): this.type = addJobContent("runType", runType)

  override protected def createLinkisJob(ujesClient: UJESClient,
                                         jobSubmitAction: JobSubmitAction): SubmittableInteractiveJob = new SubmittableInteractiveJob(ujesClient, jobSubmitAction)

}
