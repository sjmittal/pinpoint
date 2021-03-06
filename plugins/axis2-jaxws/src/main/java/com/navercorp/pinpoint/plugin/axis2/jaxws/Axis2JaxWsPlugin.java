/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.axis2.jaxws;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

public class Axis2JaxWsPlugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        Axis2JaxWsConfiguration config = new Axis2JaxWsConfiguration(context.getConfig());
        
        addServerInterceptor(config);
    }

    private void addServerInterceptor(final Axis2JaxWsConfiguration config){
        transformTemplate.transform("org.apache.axis2.jaxws.core.controller.impl.AxisInvocationController",  new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                InstrumentMethod handleMethodEditorBuilder = target.getDeclaredMethod("doInvoke", "org.apache.axis2.jaxws.core.MessageContext");
                if (handleMethodEditorBuilder != null) {
                    handleMethodEditorBuilder.addInterceptor("com.navercorp.pinpoint.plugin.axis2.jaxws.interceptor.MessageHandleInterceptor", va(config.getAxis2JaxWsExcludeUrlFilter()));
                    return target.toBytecode();
                }

                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
