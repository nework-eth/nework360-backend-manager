package com.pmcaff.nework.manager.controller;

import com.pmcaff.nework.core.common.CommonResult;
import com.pmcaff.nework.core.common.Constants;
import com.pmcaff.nework.core.common.Page;
import com.pmcaff.nework.core.controller.BaseController;
import com.pmcaff.nework.core.domain.District;
import com.pmcaff.nework.core.service.DistrictService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/district")
public class DistrictController extends BaseController {

    @Resource
    DistrictService districtService;
    @Resource
    OptLogService logService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "listDistrictByParam", method = RequestMethod.GET)
    public JSONObject listDistrictByParam(@RequestParam(required = false) String name,
                                          @RequestParam(required = false) String chinese,
                                          @RequestParam(required = false) Long parentId,
                                          @RequestParam(required = false) String level,
                                          @RequestParam(required = false) Boolean isUsed,
                                          @RequestParam(required = false) Boolean isDeleted,
                                          @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                          @RequestParam(required = false, defaultValue = "0") Integer start,
                                          @RequestParam(required = false, defaultValue = "20") Integer limit) {
        JSONObject retJson = new JSONObject();
        Map<String, Object> param = new HashedMap();
        param.put("name", name);
        param.put("chinese", chinese);
        param.put("parent", parentId);
        param.put("level", level);
        param.put("is_used", isUsed);
        param.put("is_deleted", isDeleted);
        param.put(Constants.PAGE_NUMBER, pageNum);
        param.put(Constants.PAGE_START, start);
        int totalCount = districtService.listDistrictByParam(param).size();
        Page page = assemblePage(pageNum, start, limit, totalCount);
        if (limit == -1) {
            param.put(Constants.PAGE_LIMIT, totalCount);
        } else {
            param.put(Constants.PAGE_LIMIT, limit);
        }
        try {
            List<District> districts = districtService.listDistrictByParam(param);
            retJson = assembleSuccResponseWithPage(retJson, JsonUtils.parseObject(districts), page);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "listDistsByTree", method = RequestMethod.GET)
    public JSONObject listDistsByTree() {
        JSONObject retJson = new JSONObject();
        Map<String, Object> param = new HashedMap();
        try {
            param.put("level", "n");
            param.put("is_used", Boolean.TRUE);
            param.put("is_deleted", Boolean.FALSE);
            List<District> nations = districtService.listDistrictByParam(param);
            Map<String, Map<String, List<District>>> nationMap = new HashMap<>();
            for (District nation : nations) {
                param.put("level", "p");
                param.put("parent", nation.getDistrictId());
                param.put("is_used", Boolean.TRUE);
                param.put("is_deleted", Boolean.FALSE);
                List<District> provinces = districtService.listDistrictByParam(param);
                Map<String, List<District>> provinceMap = new HashMap();
                for (District province : provinces) {
                    param.put("level", "c");
                    param.put("parent", province.getDistrictId());
                    param.put("is_used", Boolean.TRUE);
                    param.put("is_deleted", Boolean.FALSE);
                    List<District> cities = districtService.listDistrictByParam(param);
                    provinceMap.put(province.getChinese(), cities);
                }
                nationMap.put(nation.getChinese(), provinceMap);
            }
            retJson = assembleSuccessResponse(retJson, JsonUtils.fromObject(nationMap));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "getDistrictById", method = RequestMethod.GET)
    public JSONObject getDistrictById(@RequestParam Long districtId) {
        JSONObject retJson = new JSONObject();
        try {
            District district = districtService.getByDistrictId(districtId);
            retJson = assembleSuccessResponse(retJson, JsonUtils.fromObject(district));
        } catch (Exception e) {
            logger.error(" error happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "getParentByCityId", method = RequestMethod.GET)
    public JSONObject getParentByCityId(@RequestParam Long cityId) {
        JSONObject retJson = new JSONObject();
        try {
            JSONObject dist = districtService.getParentByCityId(cityId);
            retJson = assembleSuccessResponse(retJson, dist);
        } catch (Exception e) {
            logger.error(" error happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "addDistrict", method = RequestMethod.POST)
    public JSONObject addDistrict(District district) {
        JSONObject retJson = new JSONObject();
        try {
            Subject subject = SecurityUtils.getSubject();
            if (!subject.isPermitted("district:add")) {
                return assembleFailResponse(retJson, 500, "no permission", "");
            }
            district.setDistrictId(districtService.getMaxDistId() + 1);
            CommonResult result = districtService.insert(district);
            if (result.isSuccess()) {
                logService.write(ManagerConstants.MODULE_DISTRICT, district.getId().toString(),
                        ManagerConstants.OPT_TYPE_ADD, JsonUtils.fromObject(district).toString());
            }
            retJson = assembleResponse(retJson, result.getStatus(), result.getMsg(), "");
        } catch (Exception e) {
            logger.error("error happens : {}", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "updateDistrict", method = RequestMethod.POST)
    public JSONObject updateDistrict(District district) {
        JSONObject retJson = new JSONObject();
        try {
            Subject subject = SecurityUtils.getSubject();
            if (!subject.isPermitted("district:update")) {
                return assembleFailResponse(retJson, 500, "no permission", "");
            }
            CommonResult result = districtService.update(district);
            if (result.isSuccess()) {
                logService.write(ManagerConstants.MODULE_DISTRICT, district.getDistrictId().toString(),
                        ManagerConstants.OPT_TYPE_UPDATE, JsonUtils.fromObject(district).toString());
            }
            retJson = assembleResponse(retJson, result.getStatus(), result.getMsg(), "");
        } catch (Exception e) {
            logger.error("error happens : {}", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "deleteDistrict", method = RequestMethod.GET)
    public JSONObject deleteDistrict(@RequestParam Long districtId) {
        JSONObject retJson = new JSONObject();
        try {
            Subject subject = SecurityUtils.getSubject();
            if (!subject.isPermitted("district:delete")) {
                return assembleFailResponse(retJson, 500, "no permission", "");
            }
            District district = districtService.getByDistrictId(districtId);
            CommonResult result = districtService.deleteByDistrictId(districtId);
            if (result.isSuccess()) {
                logService.write(ManagerConstants.MODULE_DISTRICT, district.getId().toString(),
                        ManagerConstants.OPT_TYPE_DELETE, JsonUtils.fromObject(district).toString());
            }
            retJson = assembleResponse(retJson, result.getStatus(), result.getMsg(), "");
        } catch (Exception e) {
            logger.error(" delete user happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }
}
