/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.serversettings.async;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * @author bdjiba
 *
 */
public class AsyncImportSiteProcessManager {
    private static final Logger logger = LoggerFactory.getLogger(AsyncImportSiteProcessManager.class);
    
    private AsyncImportSiteProcessManager() {
        // Nothing to do
    }
    
    private static class Holder {
        static final AsyncImportSiteProcessManager INSTANCE = new AsyncImportSiteProcessManager();
    }

    public static AsyncImportSiteProcessManager getInstance() {
        return Holder.INSTANCE;
    }
	
    private static final Map<String, List<String>> SITE_IMPORT_TASK_STEPS_MAP = new ConcurrentHashMap<String, List<String>>();

    private static final Map<String, ListenableFuture<Boolean>> SITE_IMPORT_TASK_MAP = new ConcurrentHashMap<String, ListenableFuture<Boolean>>();

    // TODO: clean steps maps after notification
    public void addSiteImportTask(final String key, final ListenableFuture<Boolean> importTask) {
        
        if(StringUtils.isNotBlank(key) && importTask != null) {
            SITE_IMPORT_TASK_MAP.put(key, importTask);
            final List<String> processSteps = new ArrayList<String>();
            processSteps.add("0");
            SITE_IMPORT_TASK_STEPS_MAP.put(key, processSteps);
            Futures.addCallback(importTask, new FutureCallback<Boolean>() {
                // FIXME: when to clean the steps ??
                @Override
                public void onSuccess(Boolean result) {
                    // finish but check result value // T or F
                    SITE_IMPORT_TASK_MAP.remove(key);
                    SITE_IMPORT_TASK_STEPS_MAP.get(key).add("1");
                    if(result) {
                        logger.info("Successfully end processing import " + key); // 
                    } else {
                        logger.warn("End processing import " + key + " but the process may fail."); //
                    }
                }
                
                @Override
                public void onFailure(Throwable t) {
                    logger.error("Error during import.", t);
                    SITE_IMPORT_TASK_MAP.remove(key);
                    SITE_IMPORT_TASK_STEPS_MAP.get(key).add("-1_" + t.getMessage());
                    // Error during process running
                    logger.warn("Import processing failed " + key);
                }
            });
        }
    }
	
    /**
     * Check if the given proc
     * @param procID
     * @return
     */
    public boolean isProcessAlive(String procID) {
        return SITE_IMPORT_TASK_MAP.containsKey(procID);
    }
    
    /**
     * Returns a flag that says if the given process id is ended.
     * It checks if the process is not alive and its last steps is different to 0
     * @param procID the process id
     * @return true if
     */
    public boolean isProcessReady(String procID) {
        return !isProcessAlive(procID) && SITE_IMPORT_TASK_STEPS_MAP.containsKey(procID) && !"0".equals(getProcessLastStep(procID));
    }
    
    /**
     * Gets all steps of the given process identifier in an array of string.
     * Returns null if the process is not present
     * @param processId the process identifier
     * @return the steps
     */
    public String[] getProcessSteps(String processId) {
        List<String> stepList = SITE_IMPORT_TASK_STEPS_MAP.get(processId);
        if(stepList != null) {
            return stepList.toArray(new String[0]);
        }
        return null;
    }
    
    /**
     * Gets the last step of the given process id.
     * @param processId the process identifier
     * @return the last step if exists
     */
    public String getProcessLastStep(String processId) {
        // 
        List<String> stepList = SITE_IMPORT_TASK_STEPS_MAP.get(processId);
        if(stepList != null) {
            return stepList.get(stepList.size() - 1);
        }
        return null;
    }
    
    public List<String> getPendingImport(){
        List<String> pendingOperationList = new ArrayList<String>();
        for(String entryKey : SITE_IMPORT_TASK_MAP.keySet()) {
            pendingOperationList.add(entryKey);
        }
        return pendingOperationList;
    }
	

}
