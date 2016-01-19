/*
 * Copyright 2016 logongas.
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
package es.logongas.ix3.web.businessprocess.impl;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.dao.metadata.MetaData;
import es.logongas.ix3.dao.metadata.MetaDataFactory;
import es.logongas.ix3.service.CRUDServiceFactory;
import es.logongas.ix3.web.businessprocess.SchemaBusinessProcess;
import es.logongas.ix3.web.controllers.schema.Schema;
import es.logongas.ix3.web.controllers.schema.SchemaFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author logongas
 */
public class SchemaBusinessProcessImpl implements SchemaBusinessProcess {

    @Autowired
    private MetaDataFactory metaDataFactory;
    @Autowired
    private CRUDServiceFactory crudServiceFactory;

    @Override
    public Schema getSchema(GetSchemaArguments getSchemaArguments) throws BusinessException {
        MetaData metaData = metaDataFactory.getMetaData(getSchemaArguments.entityType);
        if (metaData == null) {
            throw new RuntimeException("No existe la entidad " + getSchemaArguments.entityType);
        }

        Schema schema = (new SchemaFactory()).getSchema(metaData, metaDataFactory, crudServiceFactory, getSchemaArguments.expands, getSchemaArguments.dataSession);

        return schema;
    }

}
