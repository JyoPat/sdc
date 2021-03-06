/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.be.tosca.utils;

import static org.openecomp.sdc.be.components.utils.InterfaceOperationUtils.isOperationInputMappedToComponentProperty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.tosca.model.ToscaInterfaceDefinition;
import org.openecomp.sdc.be.tosca.model.ToscaInterfaceNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaLifecycleOperationDefinition;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;


public class InterfacesOperationsToscaUtil {

    private static final String DERIVED_FROM_STANDARD_INTERFACE = "tosca.interfaces.node.lifecycle.Standard";
    private static final String OPERATIONS_KEY = "operations";

    private static final String DEFAULT = "default";
    private static final String DEFAULT_HAS_UNDERSCORE = "_default";
    private static final String DOT = ".";
    private static final String DEFAULTP = "defaultp";

    static final String SELF = "SELF";
    static final String GET_PROPERTY = "get_property";
    static final String GET_OPERATION_OUTPUT = "get_operation_output";

    private InterfacesOperationsToscaUtil() {
    }

    /**
     * Creates the interface_types element.
     *
     * @param component to work on
     * @return the added element
     */
    public static Map<String, Object> addInterfaceTypeElement(Component component, List<String> allInterfaceTypes) {
        if (component instanceof Product) {
            return null;
        }
        final Map<String, InterfaceDefinition> interfaces = component.getInterfaces();
        if (MapUtils.isEmpty(interfaces)) {
            return null;
        }

        Map<String, Object> toscaInterfaceTypes = new HashMap<>();
        for (InterfaceDefinition interfaceDefinition : interfaces.values()) {
            boolean isInterfaceTypeExistInGlobalType =
                    allInterfaceTypes.stream().anyMatch(type -> type.equalsIgnoreCase(interfaceDefinition.getType()));
            if (!isInterfaceTypeExistInGlobalType) {
                ToscaInterfaceNodeType toscaInterfaceType = new ToscaInterfaceNodeType();
                toscaInterfaceType.setDerived_from(DERIVED_FROM_STANDARD_INTERFACE);

                final Map<String, OperationDataDefinition> operations = interfaceDefinition.getOperations();
                Map<String, Object> toscaOperations = new HashMap<>();

                for (Map.Entry<String, OperationDataDefinition> operationEntry : operations.entrySet()) {
                    toscaOperations.put(operationEntry.getValue().getName(), null);
                }
                toscaInterfaceType.setOperations(toscaOperations);
                Map<String, Object> interfacesAsMap = getObjectAsMap(toscaInterfaceType);
                Map<String, Object> operationsMap = (Map<String, Object>) interfacesAsMap.remove(OPERATIONS_KEY);
                interfacesAsMap.putAll(operationsMap);

                toscaInterfaceTypes.put(interfaceDefinition.getType(), interfacesAsMap);
            }
        }
        return MapUtils.isNotEmpty(toscaInterfaceTypes) ? toscaInterfaceTypes : null;
    }

    /**
     * Adds the 'interfaces' element to the node type provided.
     *
     * @param component to work on
     * @param nodeType  to which the interfaces element will be added
     */
    public static void addInterfaceDefinitionElement(Component component, ToscaNodeType nodeType,
                                                     Map<String, DataTypeDefinition> dataTypes,
                                                     boolean isAssociatedResourceComponent) {
        if (component instanceof Product) {
            return;
        }
        final Map<String, InterfaceDefinition> interfaces = component.getInterfaces();
        if (MapUtils.isEmpty(interfaces)) {
            return;
        }
        Map<String, Object> toscaInterfaceDefinitions = getInterfacesMap(component, dataTypes,
                isAssociatedResourceComponent);
        if (MapUtils.isNotEmpty(toscaInterfaceDefinitions)) {
            nodeType.setInterfaces(toscaInterfaceDefinitions);
        }
    }

    private static Map<String, Object> getInterfacesMap(Component component,
                                                        Map<String, DataTypeDefinition> dataTypes,
                                                        boolean isAssociatedResourceComponent) {
        return getInterfacesMap(component, component.getInterfaces(), dataTypes, isAssociatedResourceComponent, false);
    }

    public static Map<String, Object> getInterfacesMap(Component component,
                                                       Map<String, InterfaceDefinition> interfaces,
                                                       Map<String, DataTypeDefinition> dataTypes,
                                                       boolean isAssociatedResourceComponent,
                                                       boolean isServiceProxyInterface) {
        if(MapUtils.isEmpty(interfaces)) {
            return null;
        }

        Map<String, Object> toscaInterfaceDefinitions = new HashMap<>();
        for (InterfaceDefinition interfaceDefinition : interfaces.values()) {
            ToscaInterfaceDefinition toscaInterfaceDefinition = new ToscaInterfaceDefinition();
            final String interfaceType = interfaceDefinition.getType();
            toscaInterfaceDefinition.setType(interfaceType);
            final Map<String, OperationDataDefinition> operations = interfaceDefinition.getOperations();
            Map<String, Object> toscaOperations = new HashMap<>();

            String operationArtifactPath;
            for (Map.Entry<String, OperationDataDefinition> operationEntry : operations.entrySet()) {
                ToscaLifecycleOperationDefinition toscaOperation = new ToscaLifecycleOperationDefinition();
                if (isArtifactPresent(operationEntry)) {
                    operationArtifactPath = OperationArtifactUtil
                            .createOperationArtifactPath(component, operationEntry.getValue(),
                                    isAssociatedResourceComponent);
                    toscaOperation.setImplementation(operationArtifactPath);
                }
                toscaOperation.setDescription(operationEntry.getValue().getDescription());
                fillToscaOperationInputs(operationEntry.getValue(), toscaOperation, component);

                toscaOperations.put(operationEntry.getValue().getName(), toscaOperation);
            }

            toscaInterfaceDefinition.setOperations(toscaOperations);
            Map<String, Object> interfaceDefAsMap = getObjectAsMap(toscaInterfaceDefinition);
            Map<String, Object> operationsMap = (Map<String, Object>) interfaceDefAsMap.remove(OPERATIONS_KEY);
            if (isServiceProxyInterface) {
                handleServiceProxyOperationInputValue(operationsMap, interfaceType);
            } else {
                handleDefaults(operationsMap);
            }
            interfaceDefAsMap.putAll(operationsMap);
            toscaInterfaceDefinitions.put(getLastPartOfName(interfaceType), interfaceDefAsMap);
        }

        return toscaInterfaceDefinitions;
    }

    private static void handleServiceProxyOperationInputValue(Map<String, Object> operationsMap, String parentKey) {
        for (Map.Entry<String, Object> operationEntry : operationsMap.entrySet()) {
            final Object value = operationEntry.getValue();
            final String key = operationEntry.getKey();
            if (value instanceof Map) {
                if ("inputs".equals(parentKey)) {
                    Object defaultValue = getDefaultValue((Map<String, Object>) value);
                    operationsMap.put(key, defaultValue);
                } else {
                    handleServiceProxyOperationInputValue((Map<String, Object>) value, key);
                }
            }
        }
    }

    private static Object getDefaultValue(Map<String, Object> inputValueMap) {
        Object defaultValue = null;
        for (Map.Entry<String, Object> operationEntry : inputValueMap.entrySet()) {
            final Object value = operationEntry.getValue();
            if (value instanceof Map) {
                getDefaultValue((Map<String, Object>) value);
            }
            final String key = operationEntry.getKey();
            if (key.equals(DEFAULTP)) {
                defaultValue = inputValueMap.remove(key);
            }
        }
        return defaultValue;
    }

    /*
     * workaround for : currently "defaultp" is not being converted to "default" by the relevant code in
     * ToscaExportHandler so, any string Map key named "defaultp" will have its named changed to "default"
     * @param operationsMap the map to update
     */
    private static void handleDefaults(Map<String, Object> operationsMap) {
        for (Map.Entry<String, Object> operationEntry : operationsMap.entrySet()) {
            final Object value = operationEntry.getValue();
            if (value instanceof Map) {
                handleDefaults((Map<String, Object>) value);
            }
            final String key = operationEntry.getKey();
            if (key.equals(DEFAULTP)) {
                Object removed = operationsMap.remove(key);
                operationsMap.put(DEFAULT, removed);
            }
        }
    }

    private static String getLastPartOfName(String toscaResourceName) {
        return toscaResourceName.substring(toscaResourceName.lastIndexOf(DOT) + 1);
    }

    private static boolean isArtifactPresent(Map.Entry<String, OperationDataDefinition> operationEntry) {
        final boolean isImplementationPresent = !Objects.isNull(operationEntry.getValue().getImplementation());
        if (isImplementationPresent) {
            return !Objects.isNull(operationEntry.getValue().getImplementation().getArtifactName());
        }
        return false;
    }

    private static void fillToscaOperationInputs(OperationDataDefinition operation,
                                                 ToscaLifecycleOperationDefinition toscaOperation,
                                                 Component component) {
        if (Objects.isNull(operation.getInputs()) || operation.getInputs().isEmpty()) {
            toscaOperation.setInputs(null);
            return;
        }
        Map<String, ToscaProperty> toscaInputs = new HashMap<>();

        for (OperationInputDefinition input : operation.getInputs().getListToscaDataDefinition()) {
            ToscaProperty toscaInput = new ToscaProperty();
            toscaInput.setDescription(input.getDescription());
            String mappedPropertyName;
            if (Objects.nonNull(input.getInputId())) {
                if (isOperationInputMappedToComponentProperty(input, component.getInputs())) {
                    mappedPropertyName = input.getInputId().substring(input.getInputId().indexOf(DOT) + 1);
                    toscaInput.setDefaultp(createMappedInputPropertyDefaultValue(mappedPropertyName));
                } else {
                    mappedPropertyName = input.getInputId();
                    toscaInput.setDefaultp(createMappedOutputDefaultValue(mappedPropertyName));
                }
            }
            toscaInput.setType(input.getType());
            toscaInput.setRequired(input.isRequired());
            toscaInputs.put(input.getName(), toscaInput);
        }

        toscaOperation.setInputs(toscaInputs);
    }

    private static Map<String, List<String>> createMappedInputPropertyDefaultValue(String propertyName) {
        Map<String, List<String>> getPropertyMap = new HashMap<>();
        List<String> values = new ArrayList<>();
        values.add(SELF);
        if (Objects.nonNull(propertyName) && !propertyName.isEmpty()) {
            values.addAll(Arrays.asList(propertyName.split("\\.")));
        }

        getPropertyMap.put(GET_PROPERTY, values);

        return getPropertyMap;
    }

    /**
     * Create the value for operation input mapped to an operation output.
     * @param propertyName the mapped other operation output full name
     * @return input map for tosca
     */
    private static Map<String, List<String>> createMappedOutputDefaultValue(String propertyName) {
        Map<String, List<String>> getOperationOutputMap = new HashMap<>();
        //For operation input mapped to other operation output parameter, the mapped property value
        // should be of the format <interface name>.<operation name>.<output parameter name>
        // Operation name and output param name should not contain "."
        List<String> defaultMappedOperationOutputValue = new ArrayList<>();
        String[] tokens = propertyName.split("\\.");
        if (tokens.length > 2) {
            defaultMappedOperationOutputValue.add(SELF);
            String outputPropertyName = tokens[tokens.length - 1];
            String operationName = tokens[tokens.length - 2];
            String mappedPropertyInterfaceType =
                    propertyName.substring(0, propertyName.indexOf(operationName + '.' + outputPropertyName) - 1);
            String interfaceName =
                    mappedPropertyInterfaceType.substring(mappedPropertyInterfaceType.lastIndexOf('.') + 1);
            defaultMappedOperationOutputValue.addAll(Arrays.asList(interfaceName, operationName, outputPropertyName));
            getOperationOutputMap.put(GET_OPERATION_OUTPUT, defaultMappedOperationOutputValue);
        }
        return getOperationOutputMap;
    }

    private static Map<String, Object> getObjectAsMap(Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (obj instanceof ToscaInterfaceDefinition) {
            //Prevent empty field serialization in interface definition
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
        Map<String, Object> objectAsMap =
                obj instanceof Map ? (Map<String, Object>) obj : objectMapper.convertValue(obj, Map.class);

        if (objectAsMap.containsKey(DEFAULT)) {
            Object defaultValue = objectAsMap.get(DEFAULT);
            objectAsMap.remove(DEFAULT);
            objectAsMap.put(DEFAULT_HAS_UNDERSCORE, defaultValue);
        }
        return objectAsMap;
    }
}
