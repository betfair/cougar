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

package com.betfair.cougar.util;

import com.betfair.cougar.util.configuration.PropertyConfigurer;
import org.springframework.core.io.Resource;

import javax.management.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

public class KeyStoreManagement implements DynamicMBean {

    private KeyStore keyStore;
    private SortedMap<String, X509Certificate> certificates = new TreeMap<String, X509Certificate>();
    private SortedMap<String, X509Certificate[]> certificateChains = new TreeMap<String, X509Certificate[]>();
    private Map<String, ValueResolver> attributeValues = new HashMap<String, ValueResolver>();

    private static String STRING = "java.lang.String";
    private final Resource source;
    private final String type;

    public static KeyStoreManagement getKeyStoreManagement(String type, Resource source, String pass) throws Exception {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        KeyStore store = KeyStore.getInstance(type);
        if (source == null || source.getFilename().equals(PropertyConfigurer.NO_DEFAULT)) {
            return null;
        }
        InputStream is = source.getInputStream();
        try {
            store.load(is, pass.toCharArray());
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return new KeyStoreManagement(store, source, type);
    }

    private KeyStoreManagement(KeyStore keyStore, Resource source, String type) throws KeyStoreException {
        this.keyStore = keyStore;
        this.source = source;
        this.type = type;
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();

            if (keyStore.isCertificateEntry(alias)) {
                addCertificate(alias);
            } else {
                addCertificateChain(alias);
            }
        }
    }

    private void addCertificateChain(String alias) throws KeyStoreException {
        Certificate[] certChain = keyStore.getCertificateChain(alias);
        if (certChain == null) {
            return;
        }
        X509Certificate[] newChain = new X509Certificate[certChain.length];
        for (int i = 0; i < certChain.length; i++) {
            if (!(certChain[i] instanceof X509Certificate)) {
                throw new IllegalArgumentException("Only support X509 certificates: " + certChain[i]);
            }
            newChain[i] = (X509Certificate)certChain[i];
        }
        certificateChains.put(alias, newChain);
    }

    private void addCertificate(String alias) throws KeyStoreException {
        Certificate cert = keyStore.getCertificate(alias);
        if (!(cert instanceof X509Certificate)) {
            throw new IllegalArgumentException("Only support X509 certificates: " + cert);
        }
        certificates.put(alias, (X509Certificate)cert);
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        throw new UnsupportedOperationException("No write attributes exist");
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        throw new UnsupportedOperationException("No write attributes exist");
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        throw new UnsupportedOperationException("Unsupported operation: " + actionName);
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return new MBeanInfo(getClass().getName(),
                "KeyStore Management Info",
                getAttributes(),
                new MBeanConstructorInfo[0],
                new MBeanOperationInfo[0],
                new MBeanNotificationInfo[0]);
    }

    @Override
    public Object getAttribute(String attribute) {
        ValueResolver vr = attributeValues.get(attribute);
        if (vr == null) {
            throw new IllegalArgumentException("Unsupported attribute: " + attribute);
        }
        return vr.resolve();
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        AttributeList ret = new AttributeList();
        for (String s : attributes) {
            ret.add(new Attribute(s, getAttribute(s)));
        }
        return ret;
    }

    private void addComplexAttribute(List<MBeanAttributeInfo> ret, String name, String type, String description, ValueResolver vr) {
        MBeanAttributeInfo attr = new MBeanAttributeInfo(
                name,
                type,
                description,
                true,
                false,
                false);
        ret.add(attr);
        attributeValues.put(name, vr);
    }


    private void addCertificateAttributes(List<MBeanAttributeInfo> ret, final X509Certificate certificate, String prefix) {
        addComplexAttribute(ret, prefix + ".SubjectDN", STRING, "", new ValueResolver() {
            @Override
            public Object resolve() {
                return certificate.getSubjectDN().getName();
            }
        });
        addComplexAttribute(ret, prefix + ".StartDate", STRING, "", new ValueResolver() {
            @Override
            public Object resolve() {
                return certificate.getNotBefore();
            }
        });
        addComplexAttribute(ret, prefix + ".ExpiryDate", STRING, "", new ValueResolver() {
            @Override
            public Object resolve() {
                return certificate.getNotAfter();
            }
        });
        addComplexAttribute(ret, prefix + ".IssuerDN", STRING, "", new ValueResolver() {
            @Override
            public Object resolve() {
                return certificate.getIssuerDN().getName();
            }
        });
        addComplexAttribute(ret, prefix + ".SignatureAlgorithm", STRING, "", new ValueResolver() {
            @Override
            public Object resolve() {
                return certificate.getSigAlgName();
            }
        });

    }

    private MBeanAttributeInfo[] getAttributes() {
        List<MBeanAttributeInfo> ret = new ArrayList<MBeanAttributeInfo>();
        // source info first..
//        ret.add(new MBeanAttributeInfo());
        addComplexAttribute(ret, "KeyStore.Source", STRING, "", new ValueResolver() {
            @Override
            public Object resolve() {
                return source.getFilename();
            }
        });
        addComplexAttribute(ret, "KeyStore.Type", STRING, "", new ValueResolver() {
            @Override
            public Object resolve() {
                return type;
            }
        });

        // individual certs first
        for (String alias : certificates.keySet()) {
            X509Certificate certificate = certificates.get(alias);
            String prefix = "Certificate." + alias;
            addCertificateAttributes(ret, certificate, prefix);
        }

        // chains next
        for (String alias : certificateChains.keySet()) {
            int index = 0;
            for (X509Certificate certificate : certificateChains.get(alias)) {
                String prefix = "CertificateChain." + alias + ".Certificate[" + index + "]";
                addCertificateAttributes(ret, certificate, prefix);
                index++;
            }
        }

        return ret.toArray(new MBeanAttributeInfo[ret.size()]);
    }

    public static interface ValueResolver {
        Object resolve();
    }
}
