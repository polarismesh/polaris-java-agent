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

import cn.polarismesh.agent.core.common.utils.ClassUtils;
import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test for {@link ConfigurationParserInterceptor}.
 *
 * @author Hoatian Zhang
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationParserInterceptorTest {

    private ConfigurationParserInterceptor interceptor;

    @Mock
    private Object target;

    @Mock
    private BeanDefinitionRegistry registry;

    @Mock
    private Environment environment;

    @Mock
    private Constructor<?> constructor;

    @Mock
    private Method processConfigurationClass;

    private Class<?> configurationClass;

    @Before
    public void setUp() {
        interceptor = new ConfigurationParserInterceptor();
        // 尝试加载真实的 ConfigurationClass，如果不存在则使用一个替代类
        try {
            configurationClass = Class.forName("org.springframework.context.annotation.ConfigurationClass");
        } catch (ClassNotFoundException e) {
            // 如果类不存在，使用 Object.class 作为替代
            configurationClass = Object.class;
        }
    }

    /**
     * 测试 bootstrap 启动时的 bean 注入
     */
    @Test
    public void testAfter_BootstrapStartup() {
        // 准备测试数据
        BeanDefinitionHolder holder = mock(BeanDefinitionHolder.class);
        when(holder.getBeanName()).thenReturn("bootstrapImportSelectorConfiguration");

        Set<BeanDefinitionHolder> candidates = new HashSet<>();
        candidates.add(holder);

        Object[] args = new Object[]{candidates};

        // Mock 静态方法和反射工具
        try (MockedStatic<ClassUtils> classUtilsMock = mockStatic(ClassUtils.class);
             MockedStatic<ReflectionUtils> reflectionUtilsMock = mockStatic(ReflectionUtils.class)) {

            // 配置 mock 行为
            classUtilsMock.when(() -> ClassUtils.getClazz(
                            eq("org.springframework.context.annotation.ConfigurationClass"), isNull()))
                    .thenReturn(configurationClass);

            reflectionUtilsMock.when(() -> ReflectionUtils.accessibleConstructor(
                            eq(configurationClass), eq(Class.class), eq(String.class)))
                    .thenReturn(constructor);

            reflectionUtilsMock.when(() -> ReflectionUtils.findMethod(
                            any(), eq("processConfigurationClass"), eq(configurationClass), eq(Predicate.class)))
                    .thenReturn(processConfigurationClass);

            reflectionUtilsMock.when(() -> ReflectionUtils.makeAccessible(processConfigurationClass))
                    .thenAnswer(invocation -> null);

            reflectionUtilsMock.when(() -> ReflectionUtils.getObjectByFieldName(target, "registry"))
                    .thenReturn(registry);

            reflectionUtilsMock.when(() -> ReflectionUtils.getObjectByFieldName(target, "environment"))
                    .thenReturn(environment);

            // 执行测试
            interceptor.after(target, args, null, null);

            // 验证方法调用
            classUtilsMock.verify(() -> ClassUtils.getClazz(
                    eq("org.springframework.context.annotation.ConfigurationClass"), isNull()), times(1));

            reflectionUtilsMock.verify(() -> ReflectionUtils.getObjectByFieldName(target, "registry"), times(1));
            reflectionUtilsMock.verify(() -> ReflectionUtils.getObjectByFieldName(target, "environment"), times(1));
        }
    }

    /**
     * 测试主应用启动时的 bean 注入
     */
    @Test
    public void testAfter_ApplicationStartup() {
        // 准备测试数据 - 创建带有 @SpringBootApplication 注解的 Bean 定义
        // 使用真实的 AnnotatedGenericBeanDefinition 实例而不是 mock
        AnnotatedGenericBeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(TestApplication.class);

        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, "testApplication");

        Set<BeanDefinitionHolder> candidates = new HashSet<>();
        candidates.add(holder);

        Object[] args = new Object[]{candidates};

        // Mock 静态方法和反射工具
        try (MockedStatic<ClassUtils> classUtilsMock = mockStatic(ClassUtils.class);
             MockedStatic<ReflectionUtils> reflectionUtilsMock = mockStatic(ReflectionUtils.class)) {

            // 配置 mock 行为
            classUtilsMock.when(() -> ClassUtils.getClazz(
                            eq("org.springframework.context.annotation.ConfigurationClass"), isNull()))
                    .thenReturn(configurationClass);

            reflectionUtilsMock.when(() -> ReflectionUtils.accessibleConstructor(
                            eq(configurationClass), eq(Class.class), eq(String.class)))
                    .thenReturn(constructor);

            reflectionUtilsMock.when(() -> ReflectionUtils.findMethod(
                            any(), eq("processConfigurationClass"), eq(configurationClass), eq(Predicate.class)))
                    .thenReturn(processConfigurationClass);

            reflectionUtilsMock.when(() -> ReflectionUtils.makeAccessible(processConfigurationClass))
                    .thenAnswer(invocation -> null);

            reflectionUtilsMock.when(() -> ReflectionUtils.getObjectByFieldName(target, "registry"))
                    .thenReturn(registry);

            reflectionUtilsMock.when(() -> ReflectionUtils.getObjectByFieldName(target, "environment"))
                    .thenReturn(environment);

            // 执行测试
            interceptor.after(target, args, null, null);

            // 验证方法调用
            classUtilsMock.verify(() -> ClassUtils.getClazz(
                    eq("org.springframework.context.annotation.ConfigurationClass"), isNull()), times(1));

            reflectionUtilsMock.verify(() -> ReflectionUtils.getObjectByFieldName(target, "registry"), times(1));
            reflectionUtilsMock.verify(() -> ReflectionUtils.getObjectByFieldName(target, "environment"), times(1));
        }
    }

    /**
     * 测试非主 Bean 定义的情况（不应触发注入）
     */
    @Test
    public void testAfter_NonMainBeanDefinition() {
        // 准备测试数据 - 创建不带 @SpringBootApplication 注解的 Bean 定义
        // 使用真实的 AnnotatedGenericBeanDefinition 实例而不是 mock
        AnnotatedGenericBeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(TestWithoutAnnotationApplication.class);

        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, "testWithoutAnnotationApplication");

        Set<BeanDefinitionHolder> candidates = new HashSet<>();
        candidates.add(holder);

        Object[] args = new Object[]{candidates};

        // 执行测试
        interceptor.after(target, args, null, null);

        // 验证不会调用反射工具（因为不是主 Bean 定义）
        verifyNoInteractions(registry);
        verifyNoInteractions(environment);
    }

    /**
     * 测试应用类 - 带有 @SpringBootApplication 注解
     */
    @SpringBootApplication
    static class TestApplication {
    }

    /**
     * 测试应用类 - 不带有 @SpringBootApplication 注解
     */
    static class TestWithoutAnnotationApplication {
    }
}