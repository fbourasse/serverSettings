/**
 * 
 */
package org.jahia.modules.serversettings.async.action;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;

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
    return ActionResult.OK;
  }

}
