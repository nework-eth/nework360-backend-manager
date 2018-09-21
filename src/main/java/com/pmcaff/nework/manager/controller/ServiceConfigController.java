package com.pmcaff.nework.manager.controller;

import com.pmcaff.nework.core.controller.BaseController;
import com.pmcaff.nework.core.domain.ServiceConfig;
import com.pmcaff.nework.core.service.ServiceConfigService;
import com.pmcaff.nework.core.utils.JsonUtils;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author skd
 * @CREATE 18/6/30
 */
@RestController
@RequestMapping(value = "/service/config")
public class ServiceConfigController extends BaseController {

    @Resource
    ServiceConfigService serviceConfigService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public JSONObject get(@RequestParam Long serviceTypeId) {
        JSONObject retJson = new JSONObject();
        try {
            ServiceConfig service = serviceConfigService.selectByPrimaryKey(serviceTypeId);
            if (service == null) {
                retJson.put("code", 404);
                retJson.put("desc", "not found");
                return retJson;

            }
            retJson = assembleSuccessResponse(retJson, JsonUtils.fromObject(service));
        } catch (Exception e) {
            logger.error(" error happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "/insert", method = RequestMethod.GET)
    public JSONObject insert(@RequestParam Long serviceTypeId,
                             @RequestParam Integer culeCount,
                             @RequestParam(required = false) Integer closeDay) {
        JSONObject retJson = new JSONObject();
        try {
            ServiceConfig service = new ServiceConfig();
            service.setCloseDay(closeDay);
            service.setCuleCount(culeCount);
            service.setServiceTypeId(serviceTypeId);
            serviceConfigService.insertSelective(service);
            retJson = assembleSuccessResponse(retJson);
        } catch (Exception e) {
            logger.error(" error happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "/update", method = RequestMethod.GET)
    public JSONObject update(@RequestParam Long serviceTypeId,
                             @RequestParam Integer culeCount,
                             @RequestParam(required = false) Integer closeDay) {
        JSONObject retJson = new JSONObject();
        try {
            ServiceConfig service = new ServiceConfig();
            service.setServiceTypeId(serviceTypeId);
            service.setCloseDay(closeDay);
            service.setCuleCount(culeCount);
            if (serviceConfigService.selectByPrimaryKey(serviceTypeId) == null) {
                serviceConfigService.insertSelective(service);
            } else {
                serviceConfigService.updateByServiceTypeId(service);
            }
            retJson = assembleSuccessResponse(retJson);
        } catch (Exception e) {
            logger.error(" error happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }
}
