package com.pmcaff.nework.manager.controller;

import com.pmcaff.nework.core.controller.BaseController;
import com.pmcaff.nework.core.domain.TemplateItem;
import com.pmcaff.nework.core.service.TemplateService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/template", method = RequestMethod.POST)
public class TemplateController extends BaseController {
    @Resource
    TemplateService templateService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping("/create")
    @ResponseBody
    public JSONObject createTemplate(@RequestBody String content, @RequestParam String serviceId) {
        JSONObject retJson = new JSONObject();
        if (serviceId == null && serviceId.isEmpty()) {
            return assembleFailResponse(retJson, "serviceId is empty");
        }
        //String tpId = GenerateIDUtil.generateTemplateId();
        int count = templateService.existServiceId(serviceId);
        if (count > 0) {
            return assembleFailResponse(retJson, "serviceId  exist");
        }
        templateService.createTemplate(content);
        return assembleSuccessResponse(retJson);
    }

    @RequestMapping(value = "/get", method = {RequestMethod.GET, RequestMethod.OPTIONS})
    @ResponseBody
    public JSONObject findTemplate(@RequestParam String serviceId) {
        JSONObject retJson = new JSONObject();
        TemplateItem templateItem = new TemplateItem();
        templateItem.setServiceId(serviceId);

        int count = templateService.existServiceId(serviceId);
        if (count == 0) {
            JSONObject dataJson = new JSONObject();
            dataJson.put("exist", false);
            dataJson.put("pages", new JSONArray());
            return assembleSuccessResponse(retJson, dataJson);
        }
        List<TemplateItem> templateItems = templateService.getTemplate(templateItem);
        JSONObject templateJson = templateService.assembleTemplate(templateItems);
        templateJson.put("exist", true);
        return assembleSuccessResponse(retJson, templateJson);
    }

    @RequestMapping(value = "/replace", method = {RequestMethod.POST})
    @ResponseBody
    public JSONObject updateTemplate(@RequestBody String content,
                                     @RequestParam String serviceId) {
        JSONObject retJson = new JSONObject();

        if (serviceId == null || serviceId.isEmpty()) {
            return assembleFailResponse(retJson, "serviceId is empty");
        }

        if (content == null || content.isEmpty()) {
            return assembleFailResponse(retJson, "content is empty");
        }

        try {
            templateService.update(content, serviceId);
            return assembleSuccessResponse(retJson);
        } catch (Exception e) {
            return assembleFailResponse(retJson, "service exception");
        }

    }
}
