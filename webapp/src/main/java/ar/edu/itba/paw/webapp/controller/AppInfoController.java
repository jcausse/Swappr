package ar.edu.itba.paw.webapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@PropertySource(value = "classpath:git.properties", ignoreResourceNotFound = true)
public class AppInfoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppInfoController.class);

    @Value("${git.commit.id:unknown}")
    private String commitId;

    @Value("${git.build.time:unknown}")
    private String buildTime;

    @Value("${git.branch:unknown}")
    private String branch;

    @Value("${git.tags:unknown}")
    private String tags;

    @Value("${git.build.version:unknown}")
    private String buildVersion;

    @Value("${git.total.commit.count:unknown}")
    private String totalCommitCount;

    @GetMapping(value = "/appinfo", produces = "text/plain;charset=UTF-8")
    @PreAuthorize("permitAll()")
    @ResponseBody
    public String appInfo() {
        LOGGER.debug("Accessing app info");
        return String.join("\n",
                "Build Time: " + buildTime,
                "Build Version: " + buildVersion,
                "Branch: " + branch,
                "Commit: " + commitId,
                "Tags: " + tags,
                "Total Commits: " + totalCommitCount
        );
    }
}
