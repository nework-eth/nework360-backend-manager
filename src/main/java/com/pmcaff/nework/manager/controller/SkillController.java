package com.pmcaff.nework.manager.controller;

import com.pmcaff.nework.core.controller.BaseController;
import com.pmcaff.nework.core.domain.ServiceType;
import com.pmcaff.nework.core.domain.Skill;
import com.pmcaff.nework.core.domain.SkillTemp;
import com.pmcaff.nework.core.service.ServiceTypeService;
import com.pmcaff.nework.core.service.SkillService;
import com.pmcaff.nework.core.service.SkillTempService;
import com.pmcaff.nework.core.service.UserService;
import com.pmcaff.nework.core.utils.JsonUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/skill")
public class SkillController extends BaseController {

    @Resource
    SkillService skillService;
    @Resource
    UserService userService;
    @Resource
    SkillTempService skillTempService;
    @Resource
    ServiceTypeService serviceTypeService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 根据用户id获取技能
     */
    @RequestMapping(value = "getSkillByUserId", method = RequestMethod.GET)
    public JSONObject getSkillByUserId(@RequestParam Long userId) {
        JSONObject retJson = new JSONObject();
        try {
            List<Skill> skills = skillService.getSkillByUserId(userId);
            JSONObject jsonObject = new JSONObject();
            Map<String, Set<JSONObject>> data = new HashMap<>();
            for (Skill skill : skills) {
                ServiceType service = serviceTypeService.selectByPrimaryKey(skill.getServiceId());
                ServiceType parentService = serviceTypeService.selectByPrimaryKey(service.getParentId());
                JSONObject jo = JsonUtils.fromObject(skill);
                jo.put("firstServiceTypeName", parentService == null ? "" : parentService.getServiceTypeName());
                jo.put("secondServiceTypeName", service == null ? "" : service.getServiceTypeName());
                Set<JSONObject> jsonObjects = data.get(parentService.getServiceTypeName());
                if (jsonObjects == null) {
                    jsonObjects = new HashSet<>();
                    jsonObjects.add(jo);
                } else {
                    jsonObjects.add(jo);
                }
                data.put(parentService.getServiceTypeName(), jsonObjects);
            }
            jsonObject.put("skill", data);

            List<SkillTemp> skillTemps = skillTempService.getSkillByUserId(userId);
            JSONArray ja = new JSONArray();
            for (SkillTemp skillTemp : skillTemps) {
                if ("s".equals(skillTemp.getLevel())) {
                    JSONObject jo = JsonUtils.fromObject(skillTemp);
                    jo.put("secondServiceTypeName", skillTemp.getServiceName());
                    ja.add(jo);
                }
            }
            jsonObject.put("skillTemp", JsonUtils.parseObject(ja));

            retJson = assembleSuccessResponse(retJson, jsonObject);
        } catch (Exception e) {
            logger.error(" error happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }
}
