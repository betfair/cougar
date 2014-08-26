/*
 * Copyright 2014, The Sporting Exchange Limited
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

package com.betfair.cougar.core.api.fault;

import com.betfair.cougar.api.fault.FaultCode;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Fault implements CougarFault {


	private final FaultCode faultCode;
	private final String errorCode;
	private final FaultDetail faultDetail;

	public Fault(FaultCode faultCode, final String errorCode, String detail, final Throwable exception) {
		this.errorCode = errorCode;
		this.faultCode = faultCode;

		if (detail != null || exception != null) {
			faultDetail = new FaultDetail(detail, exception);
		}
		else {
			faultDetail = null;
		}
	}

	public Fault(FaultCode faultCode, String errorCode, String detail) {
		this(faultCode, errorCode, detail, null);
	}

	public Fault(FaultCode faultCode, String errorCode) {
		this(faultCode, errorCode, null, null);
	}

	public Fault(FaultCode faultCode) {
		this(faultCode, null);
	}

	public Fault() {
		this(FaultCode.Server);
	}

	@Override
	public String getErrorCode() {
		return errorCode;
	}
	@Override
	public FaultCode getFaultCode() {
		return faultCode;
	}
	@Override
	public FaultDetail getDetail() {
		return faultDetail;
	}

    public boolean equals(Object o) {
        boolean equal = false;
        if (o instanceof Fault) {
            Fault theOther = (Fault)o;

            equal = new EqualsBuilder()
                        .append(getErrorCode(), theOther.getErrorCode())
                        .append(getFaultCode(), theOther.getFaultCode())
                        .append(getDetail(), theOther.getDetail())
                        .isEquals();
        }
        return equal;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getErrorCode())
                .append(getFaultCode())
                .append(getDetail())
                .hashCode();
    }
}
