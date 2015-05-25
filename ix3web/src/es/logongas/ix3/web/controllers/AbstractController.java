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
package es.logongas.ix3.web.controllers;

import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author logongas
 */
public class AbstractController {

    public void noCache(HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader("Cache-Control", "no-cache");
    }

    public void cache(HttpServletResponse httpServletResponse) {
        cache(httpServletResponse, 60);
    }

    public void cache(HttpServletResponse httpServletResponse, long expireSeconds) {
        httpServletResponse.setHeader("Cache-Control", "private, no-transform, max-age=" + expireSeconds);
    }
}
