/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdcrests.vsp.rest.services;

import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static org.openecomp.core.utilities.file.FileUtils.getFileExtension;
import static org.openecomp.core.utilities.file.FileUtils.getNetworkPackageName;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.inject.Named;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.onap.config.api.Configuration;
import org.onap.config.api.ConfigurationManager;
import org.openecomp.sdc.common.http.client.api.HttpRequest;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManager;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.UploadFileResponseDto;
import org.openecomp.sdcrests.vsp.rest.VnfPackageRepository;
import org.openecomp.sdcrests.vsp.rest.mapping.MapUploadFileResponseToUploadFileResponseDto;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * The class implements the API interface with VNF Repository (VNFSDK) such as
 * i) Get all the VNF Package Meta-data ii) Download the VNF Package iii) Import
 * VNF package to SDC catalog (Download & validate)
 * 
 * @version Amsterdam release (ONAP 1.0)
 */
@Named
@Service("vnfPackageRepository")
@Scope(value = "prototype")
public class VnfPackageRepositoryImpl implements VnfPackageRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(VnfPackageRepositoryImpl.class);

    private static boolean initFlag = false;

    // Default VNF Repository configuration
    private static final String CONFIG_NAMESPACE = "vnfrepo";

    // Default address for VNF repository docker
    private static final String DEF_DOCKER_COMPOSE_ADDR = "127.0.0.1";

    private static String ipAddress = DEF_DOCKER_COMPOSE_ADDR;

    // Default Download package URI and Get VNF package meta-data URI -
    // configurable
    private static String getVnfPkgUri = "/onapapi/vnfsdk-marketplace/v1/PackageResource/csars";

    private static String downldPkgUri = "/onapapi/vnfsdk-marketplace/v1/PackageResource/csars/%s/files";

    // Default port for VNF Repository
    private static String port = "8702";

    @Override
    public Response getVnfPackages(String vspId, String versionId, String user) throws Exception {

        LOGGER.debug("Get VNF Packages from Repository:{}", vspId);

        // Step 1: Create REST client and configuration and prepare URI
        init();

        // Step 2: Build URI based on the IP address and port allocated
        HttpResponse<String> rsp = HttpRequest.get(getVnfPkgUri);
        if(HttpStatus.SC_OK != rsp.getStatusCode()) {
            LOGGER.error("Failed to query VNF package metadata:uri={}, Response={}", getVnfPkgUri, rsp);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        // Step 3: Send the response to the client
        LOGGER.debug("Response from VNF Repository: {}", rsp.getResponse());

        return Response.ok(rsp.getResponse()).build();
    }

    @Override
    public Response importVnfPackage(String vspId, String versionId, String csarId, String user) throws Exception {

        LOGGER.debug("Import VNF Packages from Repository:{}", csarId);

        // Step 1: Create REST client and configuration and prepare URI
        init();

        // Step 2: Build URI based on the IP address and port allocated
        String uri = String.format(downldPkgUri, csarId);
        HttpResponse<String> rsp = HttpRequest.get(uri);
        if(HttpStatus.SC_OK != rsp.getStatusCode()) {
            LOGGER.error("Failed to download package from VNF Repository:uri={}, Response={}", uri, rsp);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        LOGGER.debug("Response from VNF Repository for download package is success ");

        // Step 3: Import the file to SDC and validate and send the response
        try (InputStream fileStream = new BufferedInputStream(
                new ByteArrayInputStream(rsp.getResponse().getBytes(StandardCharsets.ISO_8859_1)))) {

            String filename = "temp_" + csarId + ".csar";
            OrchestrationTemplateCandidateManager candidateManager =
                    OrchestrationTemplateCandidateManagerFactory.getInstance().createInterface();
            UploadFileResponse uploadFileResponse = candidateManager.upload(vspId, getVersion(vspId, versionId),
                    fileStream, getFileExtension(filename), getNetworkPackageName(filename));

            UploadFileResponseDto uploadFileResponseDto = new MapUploadFileResponseToUploadFileResponseDto()
                    .applyMapping(uploadFileResponse, UploadFileResponseDto.class);

            return Response.ok(uploadFileResponseDto).build();
        } catch(Exception e) {
            // Exception while uploading file

            LOGGER.error("Exception while uploading VNF package received from VNF Repository:", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public Response downloadVnfPackage(String vspId, String versionId, String csarId, String user) throws Exception {

        LOGGER.debug("Download VNF Packages from Repository:csarId={}", csarId);

        // Step 1: Create REST client and configuration and prepare URI
        init();

        // Step 2: Build URI based on the IP address and port allocated
        String uri = String.format(downldPkgUri, csarId);
        HttpResponse<String> rsp = HttpRequest.get(uri);
        if(HttpStatus.SC_OK != rsp.getStatusCode()) {
            LOGGER.error("Failed to download package from VNF Repository:uri={}, Response={}", uri, rsp);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        // Step 3:Send response to the client
        String filename = "temp_" + csarId + ".csar";
        Response.ResponseBuilder response = Response.ok(rsp.getResponse().getBytes(StandardCharsets.ISO_8859_1));
        response.header(CONTENT_DISPOSITION, "attachment; filename=" + filename);

        LOGGER.debug("Response from VNF Repository for download package is success ");

        return response.build();
    }

    private Version getVersion(String vspId, String versionId) {
        // Get list of Versions from the rest call
        VersioningManager versioningManager = VersioningManagerFactory.getInstance().createInterface();

        // Find the corresponding version from versionId
        return versioningManager.list(vspId).stream().filter(ver -> ver.getId() != versionId).findAny()
                .orElse(new Version(versionId));
    }

    private static void setVnfRepoConfig() {

        try {
            // Step 1: Fetch the on-boarding configuration
            Configuration config = ConfigurationManager.lookup();

            String vnfRepoHost = config.getAsString(CONFIG_NAMESPACE, "vnfRepoHost");
            if(null != vnfRepoHost) {
                ipAddress = vnfRepoHost;
            }

            String vnfRepoPort = config.getAsString(CONFIG_NAMESPACE, "vnfRepoPort");
            if(null != vnfRepoPort) {
                port = vnfRepoPort;
            }

            String getVnfUri = config.getAsString(CONFIG_NAMESPACE, "getVnfUri");
            if(null != getVnfUri) {
                getVnfPkgUri = getVnfUri;
            }

            String downloadVnfUri = config.getAsString(CONFIG_NAMESPACE, "downloadVnfUri");
            if(null != downloadVnfUri) {
                downldPkgUri = downloadVnfUri;
            }

        } catch(Exception e) {
            LOGGER.error("Failed to load configuration, Exception caught, using default configuration", e);
        }

        getVnfPkgUri =
                new StringBuilder("http://").append(ipAddress).append(":").append(port).append(getVnfPkgUri).toString();

        downldPkgUri =
                new StringBuilder("http://").append(ipAddress).append(":").append(port).append(downldPkgUri).toString();
    }

    private static synchronized void init() throws Exception {
        if(!initFlag) {
            // Step 1: Initialize configuration
            setVnfRepoConfig();

            initFlag = true;
        }
    }
}
