/**
 * 
 */
package org.jahia.modules.serversettings.async.action;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.plexus.util.StringUtils;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.modules.serversettings.async.AsyncImportSiteProcessManager;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;

/**
 * @author bdjiba
 *
 */
public class KeepSessionAliveAction extends Action{

  @Override
  public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext,
      Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters,
      URLResolver urlResolver) throws Exception {
    // durty solution to keep session
    String procID = req.getParameter("cp");
    // TODO
    if(StringUtils.isNotBlank(procID)) {
      
      String lastStep = "0";
      if(AsyncImportSiteProcessManager.isProcessReady(procID)) {
        lastStep = AsyncImportSiteProcessManager.getProcessLastStep(procID);
      }
      JSONObject result = new JSONObject();
      result.put("sts", lastStep);
      return new ActionResult(HttpServletResponse.SC_OK, null, result);
    }
    // checking all pending processes
    String allProcReqParam = req.getParameter("ca");
    if(StringUtils.isNotBlank(allProcReqParam) && "a".equals(allProcReqParam)) {
      List<String> runningImportList = AsyncImportSiteProcessManager.getPendingImport();
      if(!runningImportList.isEmpty()) {
        JSONObject result = new JSONObject();
        result.put("sts", StringUtils.join(runningImportList.toArray(new String[0]), ","));
        return new ActionResult(HttpServletResponse.SC_OK, null, result);
      }
    }
    return ActionResult.OK;
  }

}
