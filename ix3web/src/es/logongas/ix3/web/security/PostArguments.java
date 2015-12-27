/*
 * Copyright 2015 logongas.
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
package es.logongas.ix3.web.security;

import es.logongas.ix3.web.controllers.command.CommandResult;
import java.util.Map;

/**
 *
 * @author logongas
 */
public class PostArguments {
        private final Map<String,Object> arguments;
        private final CommandResult commandResult;

        public PostArguments(Map<String, Object> arguments, CommandResult commandResult) {
            this.arguments = arguments;
            this.commandResult = commandResult;
        }

        /**
         * @return the arguments
         */
        public Map<String,Object> getArguments() {
            return arguments;
        }


        /**
         * @return the commandResult
         */
        public CommandResult getCommandResult() {
            return commandResult;
        }
}