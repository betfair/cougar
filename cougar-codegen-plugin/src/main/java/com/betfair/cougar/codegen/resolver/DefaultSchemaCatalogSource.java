package com.betfair.cougar.codegen.resolver;

import com.betfair.cougar.codegen.FileUtil;
import org.apache.maven.plugin.logging.Log;

import java.io.File;

/**
 *
 */
public class DefaultSchemaCatalogSource implements SchemaCatalogSource {

    protected String[] getSchemas() {
        return new String[] {
                "bsidl/v4_0/common.xsd",
                "bsidl/v4_0/dataType.xsd",
                "bsidl/v4_0/eventType.xsd",
                "bsidl/v4_0/exceptionType.xsd",
                "bsidl/v4_0/Interface.xsd",
                "w3/datatypes.dtd",
                "w3/xml.xsd",
                "w3/XMLSchema.dtd"
        };
    }

    protected String getCatalogFileName() {
        return "catalog.xml";
    }

    @Override
    public File getCatalog(File destDir, Log log) {

        // schemas
        for (String s : getSchemas()) {
            log.debug("Copying "+s+" to "+destDir);
            FileUtil.resourceToFile(s, new File(destDir, s), getClass());
        }

        // catalog (which works using relative links)
        File catalogFile = new File(destDir, getCatalogFileName());
        log.debug("Copying "+getCatalogFileName()+" to "+destDir);
        FileUtil.resourceToFile(getCatalogFileName(), catalogFile, getClass());

        return catalogFile;
    }
}
