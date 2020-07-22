/*
 * Copyright 2015 Lorenzo.
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
package es.logongas.ix3.businessprocess.echo;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.dao.NativeDAO;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author logongas
 */
public class EchoBusinessProcessImpl implements EchoBusinessProcess {

    private static final Log log = LogFactory.getLog(EchoBusinessProcessImpl.class);

    @Autowired
    private NativeDAO nativeDAO;

    @Override
    public EchoResult echoDataBase(EchoDataBaseArguments echoDataBaseArguments) {

        List<Object> resultado = nativeDAO.createNativeQuery(echoDataBaseArguments.dataSession, "select now() from dual", (List<Object>) null);
        Date date = (Date) resultado.get(0);

        EchoResult echoResult = new EchoResult(echoDataBaseArguments.id, date);

        return echoResult;
    }

    @Override
    public EchoResult echoNoDataBase(EchoNoDataBaseArguments echoNoDataBaseArguments) {

        Date date = new Date();

        EchoResult echoResult = new EchoResult(date.getTime(), date);

        return echoResult;

    }

    @Override
    final public void setEntityType(Class t) {
        throw new UnsupportedOperationException("No se puede cambiar la entidad");
    }

    @Override
    final public Class getEntityType() {
        return null;
    }

}
