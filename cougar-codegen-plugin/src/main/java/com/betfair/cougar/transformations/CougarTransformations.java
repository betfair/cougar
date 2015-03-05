
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

package com.betfair.cougar.transformations;

import com.betfair.cougar.transformations.manglers.CommonTypesMangler;
import com.betfair.cougar.transformations.manglers.ResponseToSimpleResponseMangler;
import com.betfair.cougar.transformations.manglers.SimpleTypeMangler;
import com.betfair.cougar.transformations.validators.DataTypeValidator;
import com.betfair.cougar.transformations.validators.ExceptionValidator;
import com.betfair.cougar.transformations.validators.MapsValidator;
import com.betfair.cougar.transformations.validators.NameClashValidator;
import com.betfair.cougar.transformations.validators.OperationValidator;
import com.betfair.cougar.transformations.validators.ParameterNameValidator;
import com.betfair.cougar.transformations.validators.RequestParameterValidator;
import com.betfair.cougar.transformations.validators.UnknownDataTypeValidator;
import com.betfair.cougar.transformations.validators.ValidValuesValidator;
import com.betfair.cougar.codegen.DocumentMangler;
import com.betfair.cougar.codegen.NodeExcluder;
import com.betfair.cougar.codegen.Transformation;
import static com.betfair.cougar.codegen.Transformation.OutputDomain;
import com.betfair.cougar.codegen.Transformations;
import com.betfair.cougar.codegen.Validator;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CougarTransformations implements Transformations{
    protected List<Transformation> transformations = new ArrayList<Transformation>();
    protected List<Validator> validations = new ArrayList<Validator>();
    protected List<DocumentMangler> manglers = new ArrayList<DocumentMangler>();

    private static final NodeExcluder VoidResponseExcluder = new NodeExcluder() {
        public boolean exclude(XPath xp, Node node) {
            try {
                final XPathExpression expr = xp.compile("parameters/simpleResponse/@type");
                final String result = (String)expr.evaluate(node, XPathConstants.STRING);
                return result.equals("void");
            } catch (XPathExpressionException ex) {
                throw new RuntimeException(ex);
            }
        }
    };

    public CougarTransformations() {
        this(false);
    }

    public CougarTransformations(boolean legacyExceptionModeValidation) {
        super();
        Transformation[] definitions = new Transformation[] {
                //directory name should not  contain / at the end
                new Transformation("interface.ftl", "/interface", "${package}/${majorVersion}", "${name}Service.java", false, false, OutputDomain.Server),
                new Transformation("asyncServiceInterface.ftl", "/interface", "${package}/${majorVersion}", "${name}AsyncService.java", false, false, OutputDomain.Server),
                new Transformation("serviceDefinition.ftl", "/interface", "${package}/${majorVersion}", "${name}ServiceDefinition.java", false, false, OutputDomain.Client_and_Server),
                new Transformation("serviceExecutableResolver.ftl", "/interface", "${package}/${majorVersion}", "${name}ServiceExecutableResolver.java", false, false, OutputDomain.Server),
                new Transformation("syncServiceExecutableResolver.ftl", "/interface", "${package}/${majorVersion}", "${name}SyncServiceExecutableResolver.java", false, false, OutputDomain.Server),
                new Transformation("client.ftl", "/interface", "${package}/${majorVersion}", "${name}Client.java", false, false, OutputDomain.Client_and_Server),
                new Transformation("client-impl.ftl", "/interface", "${package}/${majorVersion}", "${name}ClientImpl.java", false, false, OutputDomain.Client_and_Server),
                new Transformation("clientExecutableResolver.ftl", "/interface", "${package}/${majorVersion}", "${name}ClientExecutableResolver.java", false, false, OutputDomain.Client),

                new Transformation("clientFactory.ftl", "/interface", "${package}/${majorVersion}", "${name}ClientFactory.java", false, false, OutputDomain.Client),
                new Transformation("clientSyncServiceImpl.ftl", "/interface", "${package}/${majorVersion}", "${name}SyncClientImpl.java", false, false, OutputDomain.Client_and_Server),

                new Transformation("clientSyncServiceInterface.ftl", "/interface", "${package}/${majorVersion}", "${name}SyncClient.java", false, false, OutputDomain.Client_and_Server),
                new Transformation("dataType.ftl", "/interface/dataType", "${package}/${majorVersion}/to", "${name}.java", true, false, OutputDomain.Client_and_Server),
                new Transformation("dataTypeBuilder.ftl", "/interface/dataType", "${package}/${majorVersion}/to", "${name}Builder.java", true, false, OutputDomain.Client_and_Server),
                new Transformation("dataTypeDelegate.ftl", "/interface/dataType", "${package}/${majorVersion}/to", "${name}Delegate.java", false, false, OutputDomain.Client_and_Server),
                new Transformation("events/event.ftl", "/interface/event", "${package}/${majorVersion}/events", "${name}.java", true, false, OutputDomain.Client_and_Server),
                new Transformation("events/jmsServiceBindingDescriptor.ftl", "/interface", "${package}/${majorVersion}/events", "${name}JMSServiceBindingDescriptor.java", true, false, OutputDomain.Client_and_Server),

                (legacyExceptionModeValidation ?
                    new Transformation("exceptionLegacy.ftl", "/interface/exceptionType", "${package}/${majorVersion}/exception", "${name}.java", false, false, OutputDomain.Client_and_Server) :
                    new Transformation("exception.ftl", "/interface/exceptionType", "${package}/${majorVersion}/exception", "${name}.java", false, false, OutputDomain.Client_and_Server)
                ),

                new Transformation("exceptionFactory.ftl", "/interface", "${package}/${majorVersion}/exception", "${name}ExceptionFactory.java",false, false, OutputDomain.Client),
                new Transformation("enum.ftl", "//parameter/validValues", "${package}/${majorVersion}/enumerations", "${name}Enum.java",true, true, OutputDomain.Client_and_Server),
                new Transformation("wrappedValueEnum.ftl", "//simpleResponse/validValues", "${package}/${majorVersion}/enumerations", "${name}WrappedValueEnum.java",true, false, OutputDomain.Client_and_Server),
                new Transformation("wrappedValueEnum.ftl", "//response/validValues", "${package}/${majorVersion}/enumerations", "${name}WrappedValueEnum.java",true, false, OutputDomain.Client_and_Server),
                new Transformation("simpleTypeEnum.ftl", "/interface/simpleType/validValues", "${package}/${majorVersion}/enumerations", "${name}.java",true, false, OutputDomain.Client_and_Server),
                new Transformation("jsonrpc/jsonRpcServiceBindingDescriptor.ftl", "/interface", "${package}/${majorVersion}/jsonrpc", "${name}JsonRpcServiceBindingDescriptor.java", false, false, OutputDomain.Client_and_Server),
                new Transformation("rescript/requestDataType.ftl", "/interface/operation", "${package}/${majorVersion}/rescript", "${name}Request.java", true, false, OutputDomain.Client_and_Server),
                new Transformation("rescript/responseDataType.ftl", "/interface/operation", "${package}/${majorVersion}/rescript", "${name}Response.java", true, false, OutputDomain.Client_and_Server, VoidResponseExcluder),
                new Transformation("rescript/rescriptServiceBindingDescriptor.ftl", "/interface", "${package}/${majorVersion}/rescript", "${name}RescriptServiceBindingDescriptor.java", false, false, OutputDomain.Client_and_Server),
                new Transformation("SOAP/soapServiceBindingDescriptor.ftl", "/interface", "${package}/${majorVersion}/soap", "${name}SoapServiceBindingDescriptor.java", false, false, OutputDomain.Client_and_Server),
                new Transformation("socket/socketServiceBindingDescriptor.ftl", "/interface", "${package}/${majorVersion}/socket", "${name}SocketServiceBindingDescriptor.java", false, false, OutputDomain.Client_and_Server),

                new Transformation("connectedDataType.ftl", "/interface/dataType", "interface:/interface", "${package}/${majorVersion}/co", "${name}CO.java", false, false, OutputDomain.Client_and_Server, null),
                new Transformation("serverConnectedDataType.ftl", "/interface/dataType", "interface:/interface", "${package}/${majorVersion}/co/server", "${name}ServerCO.java", false, false, OutputDomain.Server, null),
                new Transformation("clientConnectedDataType.ftl", "/interface/dataType", "interface:/interface", "${package}/${majorVersion}/co/client", "${name}ClientCO.java", false, false, OutputDomain.Client, null)
        };
        transformations.addAll(Arrays.asList(definitions));


        validations.add(new DataTypeValidator());
        validations.add(new OperationValidator());
        validations.add(new ExceptionValidator(legacyExceptionModeValidation));
        validations.add(new ValidValuesValidator());
        validations.add(new RequestParameterValidator());
        validations.add(new NameClashValidator());
        validations.add(new MapsValidator());
        validations.add(new UnknownDataTypeValidator());
        validations.add(new ParameterNameValidator());

        manglers.add(new CommonTypesMangler());
        manglers.add(new SimpleTypeMangler());
        manglers.add(new ResponseToSimpleResponseMangler());
    }

    @Override
    public List<Transformation> getTransformations() {
        return transformations;
    }

	@Override
	public List<Validator> getPreValidations() {
		return validations;
	}

	@Override
	public List<DocumentMangler> getManglers() {
		return manglers;
	}

}
