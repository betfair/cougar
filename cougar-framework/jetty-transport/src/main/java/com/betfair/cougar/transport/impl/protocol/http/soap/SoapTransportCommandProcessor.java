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

package com.betfair.cougar.transport.impl.protocol.http.soap;

import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.api.security.IdentityToken;
import com.betfair.cougar.core.api.OperationBindingDescriptor;
import com.betfair.cougar.core.api.ServiceBindingDescriptor;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.ev.OperationDefinition;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.ev.TimeConstraints;
import com.betfair.cougar.core.api.exception.*;
import com.betfair.cougar.core.api.fault.CougarFault;
import com.betfair.cougar.core.api.fault.FaultController;
import com.betfair.cougar.core.api.fault.FaultDetail;
import com.betfair.cougar.core.api.tracing.Tracer;
import com.betfair.cougar.core.api.transcription.*;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.marshalling.impl.databinding.xml.SchemaValidationFailureParser;
import com.betfair.cougar.transport.api.CommandResolver;
import com.betfair.cougar.transport.api.ExecutionCommand;
import com.betfair.cougar.transport.api.TransportCommand.CommandStatus;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.transport.api.protocol.http.soap.SoapIdentityTokenResolver;
import com.betfair.cougar.transport.api.protocol.http.soap.SoapOperationBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.soap.SoapServiceBindingDescriptor;
import com.betfair.cougar.transport.impl.protocol.http.AbstractTerminateableHttpCommandProcessor;
import com.betfair.cougar.util.stream.ByteCountingInputStream;
import com.betfair.cougar.util.stream.ByteCountingOutputStream;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.traverse.OMChildrenNamespaceIterator;
import org.apache.axiom.soap.*;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StreamUtils;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.*;

/**
 * TransportCommandProcessor for the SOAP protocol.
 * Responsible for resolving the operation and arguments from the command,
 * and for writing the result or exception from the operation to the response.
 */
@ManagedResource
public class SoapTransportCommandProcessor extends AbstractTerminateableHttpCommandProcessor<OMElement> {
    private static final String SECURITY_PREFIX = "sec";
    private static final String SECURITY_NAMESPACE = "http://www.betfair.com/security/";
    private static final String SECURITY_CREDENTIALS = "Credentials";

    private static final Logger LOGGER = LoggerFactory.getLogger(SoapTransportCommandProcessor.class);

    private Map<String, SoapOperationBinding> bindings = new HashMap<String, SoapOperationBinding>();

    private boolean schemaValidationEnabled;
    private SchemaValidationFailureParser schemaValidationFailureParser;

    public SoapTransportCommandProcessor(DehydratedExecutionContextResolution contextResolution, String requestTimeoutHeader, SchemaValidationFailureParser schemaValidationFailureParser) {
        super(Protocol.SOAP, contextResolution, requestTimeoutHeader);
        setName("SoapTransportCommandProcessor");
        this.schemaValidationFailureParser = schemaValidationFailureParser;
    }

    @ManagedAttribute
    public boolean isSchemaValidationEnabled() {
        return schemaValidationEnabled;
    }

    @ManagedAttribute
    public void setSchemaValidationEnabled(boolean schemaValidationEnabled) {
        this.schemaValidationEnabled = schemaValidationEnabled;
    }

    @Override
    public void onCougarStart() {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        for (ServiceBindingDescriptor sd : getServiceBindingDescriptors()) {
            SoapServiceBindingDescriptor soapServiceDesc = (SoapServiceBindingDescriptor) sd;
            try {
                // we'll load the schema content and create a Schema object once, as this is threadsafe and so can be reused
                // this should cut down on some memory usage and remove schema parsing from the critical path when validating
                try (InputStream is = soapServiceDesc.getClass().getClassLoader().getResourceAsStream(soapServiceDesc.getSchemaPath())) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    StreamUtils.copy(is, baos);
                    String schemaContent = baos.toString();
                    Schema schema = schemaFactory.newSchema(new StreamSource(new StringReader(schemaContent)));
                    String uriVersionStripped = stripMinorVersionFromUri(soapServiceDesc.getServiceContextPath() + soapServiceDesc.getServiceVersion());
                    for (OperationBindingDescriptor desc : soapServiceDesc.getOperationBindings()) {
                        SoapOperationBindingDescriptor soapOpDesc = (SoapOperationBindingDescriptor) desc;
                        OperationDefinition opDef = getOperationDefinition(soapOpDesc.getOperationKey());
                        String operationName = uriVersionStripped + "/" + soapOpDesc.getRequestName().toLowerCase();
                        bindings.put(operationName,
                                new SoapOperationBinding(opDef, soapOpDesc,
                                        soapServiceDesc, schema));
                    }
                }
            }
            catch (IOException | SAXException e) {
                throw new CougarFrameworkException("Error loading schema", e);
            }
        }
    }

    @Override
    protected CommandResolver<HttpCommand> createCommandResolver(
            final HttpCommand command, final Tracer tracer) {
        String operationName = null;
        ByteCountingInputStream in = null;
        try {
            in = createByteCountingInputStream(command.getRequest().getInputStream());
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            XMLStreamReader parser = factory.createXMLStreamReader(in);
            StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(parser);
            final SOAPHeader header = builder.getSOAPEnvelope().getHeader();
            final OMElement credentialElement = getCredentialElement(header);

            final OMElement requestNode = builder.getSOAPEnvelope().getBody().getFirstElement();

            operationName = requestNode.getLocalName();
            String fullPathToOperationName = stripMinorVersionFromUri(command.getOperationPath()) + "/" + operationName.toLowerCase();
            final SoapOperationBinding binding = bindings.get(fullPathToOperationName);
            if (binding != null) {

                if (schemaValidationEnabled) {
                    Schema schema = binding.getSchema();
                    Validator validator = schema.newValidator();
                    validator.validate(new StAXSource(requestNode.getXMLStreamReader(true)));
                }

                final ByteCountingInputStream finalIn = in;
                return new SingleExecutionCommandResolver<HttpCommand>(tracer) {

                    private DehydratedExecutionContext context;
                    private ExecutionCommand exec;

                    @Override
                    public DehydratedExecutionContext resolveExecutionContext() {
                        if (context == null) {
                            context = SoapTransportCommandProcessor.this.resolveExecutionContext(command, credentialElement);
                        }
                        return context;
                    }

                    @Override
                    public ExecutionCommand resolveExecutionCommand(Tracer tracer) {
                        if (exec == null) {
                            exec = SoapTransportCommandProcessor.this.resolveExecutionCommand(binding, command,
                                    resolveExecutionContext(), requestNode, finalIn, tracer);
                        }
                        return exec;
                    }
                };
            }
        } catch (CougarException e) {
            throw e;
        } catch (SAXException e) {
            if (e.getException() instanceof TransformerException) {
                TransformerException te = (TransformerException) e.getException();
                if (te.getException() instanceof XMLStreamException) {
                    XMLStreamException se = (XMLStreamException) te.getException();
                    if (se.getCause() instanceof SAXParseException) {
                        SAXParseException spe = (SAXParseException) se.getCause();
                        CougarException ce = schemaValidationFailureParser.parse(spe, "soap", false);
                        if (ce != null) {
                            throw ce;
                        }
                    }
                }
            }
            throw CougarMarshallingException.unmarshallingException("soap", e, false);
        } catch (Exception e) {
            throw CougarMarshallingException.unmarshallingException("soap", e, false);
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException ie) {
                throw CougarMarshallingException.unmarshallingException("soap", ie, false);
            }
        }

        throw new CougarValidationException(ServerFaultCode.NoSuchOperation,
                "The SOAP request could not be resolved to an operation");
    }

    private ExecutionCommand resolveExecutionCommand(
            final SoapOperationBinding operationBinding,
            final HttpCommand command, final DehydratedExecutionContext context,
            OMElement requestNode, ByteCountingInputStream in, final Tracer tracer) {
        final Object[] args = readArgs(operationBinding, requestNode);
        final long bytesRead = in.getCount();
        final TimeConstraints realTimeConstraints = DefaultTimeConstraints.rebaseFromNewStartTime(context.getRequestTime(), readRawTimeConstraints(command.getRequest()));
        return new ExecutionCommand() {
            public Object[] getArgs() {
                return args;
            }

            public OperationKey getOperationKey() {
                return operationBinding.getOperationKey();
            }

            @Override
            public TimeConstraints getTimeConstraints() {
                return realTimeConstraints;
            }

            public void onResult(ExecutionResult result) {
                if (command.getStatus() == CommandStatus.InProgress) {
                    try {
                        if (result.getResultType() == ExecutionResult.ResultType.Fault) {
                            command.getResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            writeResponse(command, operationBinding, null, result.getFault(), context, bytesRead);
                        } else if (result.getResultType() == ExecutionResult.ResultType.Success) {
                            writeResponse(command, operationBinding, result.getResult(), null, context, bytesRead);
                        }
                    } finally {
                        command.onComplete();
                    }
                }
            }
        };
    }

    @Override
    protected void writeErrorResponse(HttpCommand command, DehydratedExecutionContext context, CougarException e, boolean traceStarted) {
        incrementErrorsWritten();
        if (command.getStatus() == CommandStatus.InProgress) {
            try {
                // if we have a fault, then for SOAP we must return a 500: http://www.w3.org/TR/2000/NOTE-SOAP-20000508/#_Toc478383529
                command.getResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writeResponse(command, null, null, e, resolveContextForErrorHandling(context, command), 0);
            } finally {
                command.onComplete();
            }
        }
    }

    private Object[] readArgs(SoapOperationBinding operationBinding,
                              OMElement requestNode) {
        final Parameter[] params = operationBinding.getOperationDefinition()
                .getParameters();
        final Object[] args = new Object[params.length];
        EnumUtils.setHardFailureForThisThread(hardFailEnumDeserialisation);
        TranscriptionInput in = new XMLTranscriptionInput(requestNode);
        try {
            for (int i = 0; i < params.length; i++) {
                args[i] = readArg(in.readObject(params[i], false), params[i]);
            }
        } catch (EnumDerialisationException ce) {
            throw CougarMarshallingException.unmarshallingException("soap", ce.getMessage(), ce.getCause(), false);
        } catch (CougarException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CougarFrameworkException("Failed to unmarshall SOAP arguments", e);
        }
        return args;
    }


    private Object readArg(Object arg, Parameter param) {
        // Special handling of enums
        // If an enum is not involved just return the already deserialised object

        Object returnValue = arg;
        // if the top level param is an enum we need to convert it as this is normally left to the Transcribable interface
        if (param.getParameterType().getType() == ParameterType.Type.ENUM) {
            returnValue = toEnum(param.getParameterType(), (String) arg, param.getName(), hardFailEnumDeserialisation);
        }

        // Handle Collection of enums. Only List and Sets supported as of now
        if ((param.getParameterType().getType() == ParameterType.Type.SET || param.getParameterType().getType() == ParameterType.Type.LIST) &&
                param.getParameterType().getComponentTypes()[0].getType() == ParameterType.Type.ENUM && arg != null) {
            Collection result = ((param.getParameterType().getType() == ParameterType.Type.SET) ? new HashSet() : new ArrayList());
            for (String enumTextValue : (Collection<String>) arg) {
                result.add(toEnum(param.getParameterType().getComponentTypes()[0], enumTextValue, param.getName(), hardFailEnumDeserialisation));
            }
            returnValue = result;
        }

        return returnValue;
    }

    // Deserialise enums explicitly
    private Object toEnum(ParameterType parameterType, String enumTextValue, String paramName, boolean hardFailEnumDeserialisation) {
        try {
            return EnumUtils.readEnum(parameterType.getImplementationClass(), enumTextValue, hardFailEnumDeserialisation);
        } catch (Exception e) {
            throw XMLTranscriptionInput.exceptionDuringDeserialisation(parameterType, paramName, e, false);
        }
    }

    private void writeResponse(HttpCommand command, SoapOperationBinding binding, Object result, CougarException error,
                               DehydratedExecutionContext context, long bytesRead) {
        MediaType mediaType = MediaType.TEXT_XML_TYPE;
        ByteCountingOutputStream out = null;
        long bytesWritten = 0;
        boolean logAccess = true;
        try {
            command.getResponse().setContentType(mediaType.toString());
            out = new ByteCountingOutputStream(command.getResponse().getOutputStream());
            SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
            SOAPEnvelope envelope = factory.createSOAPEnvelope();
            SOAPHeader header = factory.createSOAPHeader(envelope);
            writeHeaders(factory, header, command, context);
            SOAPBody body = factory.createSOAPBody(envelope);
            writeError(factory, binding, body, error);
            writeBody(factory, binding, body, result);
            envelope.serialize(out);
            bytesWritten = out.getCount();
        } catch (Exception e) {
            CougarException ce = handleResponseWritingIOException(e, result.getClass());

            if (ce.getResponseCode() == ResponseCode.CantWriteToSocket) {
                // Log in the access log what's happened and end it all.
                error = ce;
            } else if (error == null) {
                // It was a normal response, so write an error instead
                writeErrorResponse(command, context, ce, true);
                logAccess = false; // We're coming back in here, so log the access then.
            } else {
                // Not much to do here - it's already an error and it's failed to send
                LOGGER.warn("Failed to write SOAP error", e);
            }
        } finally {
            closeStream(out);
            if (logAccess) {
                logAccess(command,
                        context, bytesRead,
                        bytesWritten, mediaType,
                        mediaType,
                        error != null ? error.getResponseCode() : ResponseCode.Ok);
                if (context != null) {
                    tracer.end(context.getRequestUUID());
                }
            }
        }
    }

    private void writeHeaders(final SOAPFactory factory, final SOAPHeader header, HttpCommand command, DehydratedExecutionContext context)
            throws Exception {
        final SoapIdentityTokenResolver identityTokenResolver = (SoapIdentityTokenResolver) command.getIdentityTokenResolver();
        if (context != null && context.getIdentity() != null && identityTokenResolver != null) {

            writeIdentity(context.getIdentityTokens(), new IdentityTokenIOAdapter() {
                @Override
                public void rewriteIdentityTokens(List<IdentityToken> identityTokens) {
                    OMElement element = header.addHeaderBlock(SECURITY_CREDENTIALS, factory.createOMNamespace(SECURITY_NAMESPACE, SECURITY_PREFIX));
                    identityTokenResolver.rewrite(identityTokens, element);
                }

                @Override
                public boolean isRewriteSupported() {
                    return identityTokenResolver.isRewriteSupported();
                }
            });
        }
    }

    /**
     * Changes made to this method as part of workaround for DE5417 (bug in isEquals method in Axiom code) - altered lines commented
     */
    private OMElement getCredentialElement(SOAPHeader header) {
        if (header != null) {
            //Iterator it = header.getChildrenWithNamespaceURI(SECURITY_NAMESPACE); // Line commented out
            Iterator it = new WorkAroundOMChildrenNamespaceIterator(header.getFirstOMChild(), SECURITY_NAMESPACE); // Line added
            if (it.hasNext()) {
                OMElement element = (OMElement) it.next();
                if (element.getLocalName().equalsIgnoreCase(SECURITY_CREDENTIALS)) {
                    return element;
                } else {
                    LOGGER.debug("Unexpected security header arrived: {}", element.getLocalName());
                }
            }
        }
        return null;

    }

    private void writeBody(SOAPFactory factory, SoapOperationBinding binding, SOAPBody body, Object result)
            throws Exception {
        if (result != null) {
            OMNamespace ns = factory.createOMNamespace(binding
                    .getServiceBindingDescriptor().getNamespaceURI(), XMLConstants.DEFAULT_NS_PREFIX);
            OMElement resultNode = factory.createOMElement(binding
                    .getBindingDescriptor().getResponseName(), ns);
            TranscriptionOutput out = new XMLTranscriptionOutput(resultNode, ns, factory);
            out.writeObject(result, new Parameter("response", binding.getOperationDefinition()
                    .getReturnType(), true), false);
            body.addChild(resultNode);
        }
    }

    private void writeError(SOAPFactory factory, SoapOperationBinding binding, SOAPBody body, CougarException error) throws Exception {
        if (error != null) {
            SOAPFault soapFault = factory.createSOAPFault(body);
            if (error.getFault() != null) {
                createFaultCode(factory, soapFault, error.getFault());
                createFaultString(factory, soapFault, error.getFault());
                createFaultDetail(factory, soapFault, error.getFault(), binding);
            }
        }
    }

    private void createFaultCode(SOAPFactory factory, SOAPFault soapFault, CougarFault fault) {
        SOAPFaultCode code = factory.createSOAPFaultCode(soapFault);
        code.setText(factory.getNamespace().getPrefix() + ":" + fault.getFaultCode().name());
    }

    private void createFaultString(SOAPFactory factory, SOAPFault soapFault, CougarFault fault) {
        SOAPFaultReason reason = factory.createSOAPFaultReason(soapFault);
        reason.setText(fault.getErrorCode());
    }

    private void createFaultDetail(SOAPFactory factory, SOAPFault soapFault, CougarFault fault, SoapOperationBinding binding) throws Exception {
        SOAPFaultDetail soapFaultDetail = factory.createSOAPFaultDetail(soapFault);
        FaultDetail detail = fault.getDetail();
        if (detail != null) {
            List<String[]> faultMessages = detail.getFaultMessages();

            if (faultMessages != null && faultMessages.size() > 0) {
                OMNamespace ns = factory.createOMNamespace(binding
                        .getServiceBindingDescriptor().getNamespaceURI(), binding
                        .getServiceBindingDescriptor().getNamespacePrefix());


                OMElement faultNode = factory.createOMElement(detail.getFaultName(), ns);
                for (String[] msg : faultMessages) {
                    OMElement messageNode = factory.createOMElement(msg[0], ns);
                    messageNode.setText(msg[1]);
                    faultNode.addChild(messageNode);
                }
                soapFaultDetail.addChild(faultNode);
            }

            if (FaultController.getInstance().isDetailedFaults()) {
                OMElement stackTrace = factory.createOMElement(new QName("trace"));
                stackTrace.setText(detail.getStackTrace());
                soapFaultDetail.addChild(stackTrace);

                OMElement detailedMessage = factory.createOMElement(new QName("message"));
                detailedMessage.setText(detail.getDetailMessage());
                soapFaultDetail.addChild(detailedMessage);
            }

        }
    }

    /**
     * Extending OMChildrenNamespaceIterator in order to override bug in isEquals method as part of workaround for DE5417
     */
    private static class WorkAroundOMChildrenNamespaceIterator extends OMChildrenNamespaceIterator {

        public WorkAroundOMChildrenNamespaceIterator(OMNode currentChild, String uri) {
            super(currentChild, uri);
        }

        /**
         * This version of equals returns true if the local parts match. (Overridden to workaround bug in isEquals method in Axiom code)
         *
         * @param searchQName
         * @param currentQName
         * @return true if equals
         */
        @Override
        public boolean isEqual(QName searchQName, QName currentQName) {
            return searchQName.getNamespaceURI().equals(currentQName.getNamespaceURI());
        }
    }
}