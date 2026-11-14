/*
 * Tencent is pleased to support the open source community by making polaris-java-agent available.
 *
 * Copyright (C) 2021 Tencent. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package cn.polarismesh.agent.plugin.spring.cloud.interceptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.Test;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.FailFastProblemReporter;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SpringFrameworkCompatibilityTest {

    @Test
    public void injectsBeanDefinitionsUsingSpring7ConfigurationParser() throws Exception {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test",
                Collections.singletonMap("spring.cloud.polaris.enabled", "false")));
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        Class<?> parserClass = Class.forName("org.springframework.context.annotation.ConfigurationClassParser");
        Constructor<?> constructor = parserClass.getDeclaredConstructor(MetadataReaderFactory.class,
                ProblemReporter.class, Environment.class, ResourceLoader.class,
                BeanNameGenerator.class, BeanDefinitionRegistry.class);
        constructor.setAccessible(true);
        Object parser = constructor.newInstance(new CachingMetadataReaderFactory(),
                new FailFastProblemReporter(), environment, new DefaultResourceLoader(),
                AnnotationBeanNameGenerator.INSTANCE, registry);
        Set<BeanDefinitionHolder> candidates = Collections.singleton(new BeanDefinitionHolder(
                new AnnotatedGenericBeanDefinition(TestApplication.class), "testApplication"));

        new ConfigurationParserInterceptor().after(parser, new Object[]{candidates}, null, null);

        assertTrue(registry.containsBeanDefinition("metadataAutoConfiguration"));
        assertTrue(registry.containsBeanDefinition("applicationContextAwareUtils"));
        assertTrue(registry.containsBeanDefinition("polarisAsyncPropertiesAutoConfiguration"));
        assertEquals("com.tencent.cloud.common.metadata.config.MetadataAutoConfiguration",
                registry.getBeanDefinition("metadataAutoConfiguration").getBeanClassName());
        Method configurationClasses = parserClass.getDeclaredMethod("getConfigurationClasses");
        configurationClasses.setAccessible(true);
        Set<String> parsedClassNames = new HashSet<>();
        for (Object configuration : (Set<?>) configurationClasses.invoke(parser)) {
            Method metadata = configuration.getClass().getDeclaredMethod("getMetadata");
            metadata.setAccessible(true);
            parsedClassNames.add(((AnnotationMetadata) metadata.invoke(configuration)).getClassName());
        }
        assertTrue(parsedClassNames.contains("com.tencent.cloud.common.metadata.config.MetadataAutoConfiguration"));
    }

    @Test
    public void supportsAllInstrumentedSpring7Methods() throws Exception {
        Class<?> parserClass = Class.forName("org.springframework.context.annotation.ConfigurationClassParser");
        Class<?> configurationClass = Class.forName("org.springframework.context.annotation.ConfigurationClass");
        assertNotNull(parserClass.getDeclaredMethod("parse", Set.class));
        assertNotNull(parserClass.getDeclaredMethod("processConfigurationClass", configurationClass, Predicate.class));
        assertNotNull(configurationClass.getDeclaredConstructor(Class.class, String.class));
        assertNotNull(Class.forName("org.springframework.context.annotation.ConfigurationClassPostProcessor")
                .getDeclaredMethod("processConfigBeanDefinitions", BeanDefinitionRegistry.class));
        assertNotNull(DefaultListableBeanFactory.class.getDeclaredMethod("registerBeanDefinition",
                String.class, org.springframework.beans.factory.config.BeanDefinition.class));
    }

    @SpringBootApplication
    static class TestApplication {
    }
}
