package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Service.DeployLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: operations-management-suite-v3
 * @author: hanch
 * @create: 2018-09-06 18:24
 **/

@RestController
@RequestMapping(value = "/deploylogs")
public class DeployLogController {

    private final DeployLogService deployLogService;

    @Autowired
    public DeployLogController(DeployLogService deployLogService) {
        this.deployLogService = deployLogService;
    }
}
