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

package org.openecomp.sdc.be.model.tosca.constraints;

import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;

import javax.validation.constraints.NotNull;

public class EqualConstraint extends AbstractPropertyConstraint {

    @NotNull
    private String equal;

    // @JsonIgnore
    private Object typed;

    public EqualConstraint(String equal) {
        super();
        this.equal = equal;
    }

    @Override
    public void initialize(ToscaType propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
        if (propertyType.isValidValue(equal)) {
            typed = propertyType.convert(equal);
        } else {
            throw new ConstraintValueDoNotMatchPropertyTypeException("equal constraint has invalid value <" + equal
                    + "> property type is <" + propertyType.toString() + ">");
        }
    }

    @Override
    public void validate(Object propertyValue) throws ConstraintViolationException {
        if (propertyValue == null) {
            if (typed != null) {
                fail(null);
            }
        } else if (typed == null) {
            fail(propertyValue);
        } else if (!typed.equals(propertyValue)) {
            fail(propertyValue);
        }
    }

    private void fail(Object propertyValue) throws ConstraintViolationException {
        throw new ConstraintViolationException("Equal constraint violation, the reference is <" + equal
                + "> but the value to compare is <" + propertyValue + ">");
    }
}
