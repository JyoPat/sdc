/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.components.distribution.engine;

import java.util.List;

public interface INotificationData {
	/**
	 * Global Distribution Identifier: UUID generated by ASDC per each distribution activation.<br>
	 * Generated UUID is compliant with RFC 4122.<br>
	 * It is a 128-bit value formatted into blocks of hexadecimal digits separated by a hyphen ("-").<br>
	 * Ex.: AA97B177-9383-4934-8543-0F91A7A02836
	 */
	String getDistributionID();

	/** Logical Service Name. */
	String getServiceName();

	/**
	 * Service Version.<br>
	 * Two dot (".") separated digit blocks.<br>
	 * Ex. : "2.0"
	 */
	String getServiceVersion();

	/**
	 * Global UUID generated by ASDC per each service version. Generated UUID is compliant with RFC 4122.<br>
	 * It is a 128-bit value formatted into blocks of hexadecimal digits separated by a hyphen ("-").<br>
	 * Ex. : AA97B177-9383-4934-8543-0F91A7A02836
	 */
	String getServiceUUID();

	/**
	 * Service description
	 */
	String getServiceDescription();

	/**
	 * ServiceInvariant UUID
	 */
	String getServiceInvariantUUID();

	/** List of the resource instances */
	List<JsonContainerResourceInstance> getResources();

	/** List of the artifacts */
	List<ArtifactInfoImpl> getServiceArtifacts();

	String getWorkloadContext();

	void setDistributionID(String distributionId);

	/** Logical Service Name. */
	void setServiceName(String serviceName);

	/**
	 * Service Version.<br>
	 * Two dot (".") separated digit blocks.<br>
	 * Ex. : "2.0"
	 */
	void setServiceVersion(String serviceVersion);

	/**
	 * Global UUID generated by ASDC per each service version. Generated UUID is compliant with RFC 4122.<br>
	 * It is a 128-bit value formatted into blocks of hexadecimal digits separated by a hyphen ("-").<br>
	 * Ex. : AA97B177-9383-4934-8543-0F91A7A02836
	 */
	void setServiceUUID(String serviceUUID);

	/**
	 * Service Description
	 */
	void setServiceDescription(String serviceDescription);

	/**
	 * ServiceInvariant UUID
	 */
	void setServiceInvariantUUID(String serviceInvariantUuid);

	/** List of the Resource Instances */
	void setResources(List<JsonContainerResourceInstance> resource);

	/** List of the Resource Instances */
	void setServiceArtifacts(List<ArtifactInfoImpl> artifacts);

	void setWorkloadContext(String workloadContext);

}
