package com.pmcaff.nework.manager.controller;

import com.pmcaff.nework.core.common.CommonResult;
import com.pmcaff.nework.core.common.Constants;
import com.pmcaff.nework.core.common.Page;
import com.pmcaff.nework.core.controller.BaseController;
import com.pmcaff.nework.core.domain.ServiceType;
import com.pmcaff.nework.core.domain.VO.ServiceDistVO;
import com.pmcaff.nework.core.domain.VO.ServiceVO;
import com.pmcaff.nework.core.service.ServiceTypeService;
import com.pmcaff.nework.core.utils.JsonUtils;
import com.pmcaff.nework.manager.common.ManagerConstants;
import com.pmcaff.nework.manager.service.OptLogService;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/service")
public class ServiceTypeController extends BaseController {

    @Resource
    ServiceTypeService serviceTypeService;
    @Resource
    OptLogService logService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/listServiceByParam", method = RequestMethod.GET)
    public JSONObject listServiceByParam(@RequestParam(required = false) Long districtId,
                                         @RequestParam(required = false) String serviceName,
                                         @RequestParam(required = false) Long parentId,
                                         @RequestParam(required = false) String level,
                                         @RequestParam(required = false) Boolean isUsed,
                                         @RequestParam(required = false) Boolean isDeleted,
                                         @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                         @RequestParam(required = false, defaultValue = "0") Integer start,
                                         @RequestParam(required = false, defaultValue = "20") Integer limit,
                                         HttpServletRequest request) {
        JSONObject retJson = new JSONObject();
        Map<String, Object> param = new HashedMap();
        param.put("district_id", districtId);
        param.put("service_type_name", serviceName);
        param.put("is_used", isUsed);
        param.put("is_deleted", isDeleted);
        param.put("parent_id", parentId);
        param.put("level", level);
        param.put(Constants.PAGE_NUMBER, pageNum);
        param.put(Constants.PAGE_START, start);
        int totalCount = serviceTypeService.listServiceWithDist(param).size();
        Page page = assemblePage(pageNum, start, limit, totalCount);
        if (limit == -1) {
            param.put(Constants.PAGE_LIMIT, totalCount);
        } else {
            param.put(Constants.PAGE_LIMIT, limit);
        }
        try {
            List<ServiceDistVO> serviceTypes = serviceTypeService.listServiceWithDist(param);
            retJson = assembleSuccResponseWithPage(retJson, JsonUtils.parseObject(serviceTypes), page);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "/listServiceWithClue", method = RequestMethod.GET)
    public JSONObject listServiceWithClue(@RequestParam(required = false) Long districtId,
                                          @RequestParam(required = false) String serviceName,
                                          @RequestParam(required = false) Long parentId,
                                          @RequestParam(required = false) String level,
                                          @RequestParam(required = false) Boolean isUsed,
                                          @RequestParam(required = false) Boolean isDeleted,
                                          @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                          @RequestParam(required = false, defaultValue = "0") Integer start,
                                          @RequestParam(required = false, defaultValue = "20") Integer limit,
                                          HttpServletRequest request) {
        JSONObject retJson = new JSONObject();
        Map<String, Object> param = new HashedMap();
        param.put("district_id", districtId);
        param.put("service_type_name", serviceName);
        param.put("is_used", isUsed);
        param.put("is_deleted", isDeleted);
        param.put("parent_id", parentId);
        param.put("level", level);
        param.put(Constants.PAGE_NUMBER, pageNum);
        param.put(Constants.PAGE_START, start);
        int totalCount = serviceTypeService.listServiceWithDist(param).size();
        Page page = assemblePage(pageNum, start, limit, totalCount);
        if (limit == -1) {
            param.put(Constants.PAGE_LIMIT, totalCount);
        } else {
            param.put(Constants.PAGE_LIMIT, limit);
        }
        try {
            List<ServiceVO> serviceTypes = serviceTypeService.listServiceWithClue(param);
            retJson = assembleSuccResponseWithPage(retJson, JsonUtils.parseObject(serviceTypes), page);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "getServiceById", method = RequestMethod.GET)
    public JSONObject getServiceById(@RequestParam Long serviceTypeId) {
        JSONObject retJson = new JSONObject();
        try {
            ServiceType service = serviceTypeService.selectByPrimaryKey(serviceTypeId);
            retJson = assembleSuccessResponse(retJson, JsonUtils.fromObject(service));
        } catch (Exception e) {
            logger.error(" error happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "addService", method = RequestMethod.POST)
    public JSONObject addService(ServiceType serviceType) {
        JSONObject retJson = new JSONObject();
        try {
            Subject subject = SecurityUtils.getSubject();
            if (!subject.isPermitted("service:add")) {
                return assembleFailResponse(retJson, 500, "no permission", "");
            }
            CommonResult result = serviceTypeService.insert(serviceType);
            if (result.isSuccess()) {
                logService.write(ManagerConstants.MODULE_SERVICE_TYPE, serviceType.getServiceTypeId().toString(),
                        ManagerConstants.OPT_TYPE_ADD, JsonUtils.fromObject(serviceType).toString());
            }
            retJson = assembleResponse(retJson, result.getStatus(), result.getMsg(), "");
        } catch (Exception e) {
            logger.error("error happens : {}", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "updateService", method = RequestMethod.POST)
    public JSONObject updateService(ServiceType serviceType) {
        JSONObject retJson = new JSONObject();
        try {
            Subject subject = SecurityUtils.getSubject();
            if (!subject.isPermitted("service:update")) {
                return assembleFailResponse(retJson, 500, "no permission", "");
            }
            CommonResult result = serviceTypeService.update(serviceType);
            if (result.isSuccess()) {
                logService.write(ManagerConstants.MODULE_SERVICE_TYPE, serviceType.getServiceTypeId().toString(),
                        ManagerConstants.OPT_TYPE_UPDATE, JsonUtils.fromObject(serviceType).toString());
            }
            retJson = assembleResponse(retJson, result.getStatus(), result.getMsg(), "");
        } catch (Exception e) {
            logger.error("error happens : {}", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "deleteService", method = RequestMethod.GET)
    public JSONObject deleteService(@RequestParam Long serviceTypeId) {
        JSONObject retJson = new JSONObject();
        try {
            Subject subject = SecurityUtils.getSubject();
            if (!subject.isPermitted("service:delete")) {
                return assembleFailResponse(retJson, 500, "no permission", "");
            }
            ServiceType service = serviceTypeService.selectByPrimaryKey(serviceTypeId);
            CommonResult result = serviceTypeService.deleteByPrimaryKey(serviceTypeId);
            if (result.isSuccess()) {
                logService.write(ManagerConstants.MODULE_SERVICE_TYPE, service.getServiceTypeId().toString(),
                        ManagerConstants.OPT_TYPE_DELETE, JsonUtils.fromObject(service).toString());
            }
            retJson = assembleResponse(retJson, result.getStatus(), result.getMsg(), "");
        } catch (Exception e) {
            logger.error(" delete user happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }
}
