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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class FaultDetail {
	private Throwable exception;
	private String detailMessage;
    private List<String[]> faultMessages;


	public static final Parameter faultClassNameParam = new Parameter("faultClassName",new ParameterType(String.class, null), false);
    public static final Parameter detailMessageParam = new Parameter("detailMessage",new ParameterType(String.class, null), false);

	public FaultDetail() {
	}

	public FaultDetail(String detailMessage, Throwable exception) {
        this(detailMessage,
            ((exception != null && (exception instanceof CougarApplicationException)) ?
                ((CougarApplicationException)exception).getApplicationFaultMessages() : null));
        this.exception = exception;
	}

    public FaultDetail(String detailMessage, List<String[]> faultMessages) {
        this.detailMessage = detailMessage;
        this.faultMessages = faultMessages;
    }


	public String getStackTrace() {
		if (exception == null) {
			return "";
		}
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		exception.printStackTrace(printWriter);
		return result.toString();
	}

	public String getDetailMessage() {
		return (detailMessage == null) ? "" : detailMessage;
	}

	public List<String[]> getFaultMessages() {
        return faultMessages;
	}

	public String getFaultName() {
		return exception == null || !(exception instanceof CougarApplicationException)
			? null
			: exception.getClass().getSimpleName();
	}

	public String getFaultNamespace() {
		return exception == null || !(exception instanceof CougarApplicationException)
			? null
			: ((CougarApplicationException)exception).getApplicationFaultNamespace();
	}

	public Throwable getCause() {
		return exception;
	}

    public boolean equals(Object o) {
        boolean equal = false;
        if (o instanceof FaultDetail) {
            FaultDetail theOther = (FaultDetail)o;
            equal = new EqualsBuilder()
                    .append(getCause(), theOther.getCause())
                    .append(getFaultNamespace(), theOther.getFaultNamespace())
                    .append(getFaultName(), theOther.getFaultName())
                    .append(getFaultMessages(), theOther.getFaultMessages())
                    .append(getDetailMessage(), theOther.getDetailMessage())
                    .isEquals();
        }
        return equal;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getCause())
                .append(getFaultNamespace())
                .append(getFaultName())
                .append(getFaultMessages())
                .append(getDetailMessage())
                .hashCode();
    }
}
