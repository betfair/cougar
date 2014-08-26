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

package com.betfair.cougar.codegen.resolver;

import org.apache.maven.plugin.logging.Log;

import java.io.File;

/**
 * Encapsulates our manipulation of schema files for validation.
 * <p>
 * Service definitions point to the BSIDL Interface.xsd which is currently housed at
 * http://www.betfair.com/BSIDL/4.0
 * This (and included) xsd's point to schemas at www.w3.org, causing the validation to contact
 * w3.org on each build, and builds to fail due to network problems.
 * <p>
 * To remove the dependency on having the schema available on a URL, we've chosen to store
 * schemas in the source repo. We could whip up or re-use a classpath-based
 * resolver for validation (it would have to be a {@code LSResourceResolver}), but for now we
 * just dump the schemas into a working directory and reference them from there, via a catalog.
 */
public interface SchemaCatalogSource {
    File getCatalog(File tmpDir, Log log);
}
