/*******************************************************************************
* Copyright (c) 2021 IBM Corporation.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Bera Sogut
*******************************************************************************/

package io.openliberty.sample.jakarta.jaxrs;

import jakarta.fake.rs.DELETE;
import jakarta.fake.rs.FormParam;

public class MultipleEntityParamsResourceMethod {

	@DELETE
	public void resourceMethodWithTwoEntityParams(String entityParam1, @FormParam(value = "") String nonEntityParam, int entityParam2) {
        
    }
}
