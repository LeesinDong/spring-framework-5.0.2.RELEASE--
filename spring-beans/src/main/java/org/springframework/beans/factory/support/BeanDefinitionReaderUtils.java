/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Utility methods that are useful for bean definition reader implementations.
 * Mainly intended for internal use.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 1.1
 * @see PropertiesBeanDefinitionReader
 * @see org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader
 */
public class BeanDefinitionReaderUtils {

	/**
	 * Separator for generated bean names. If a class name or parent name is not
	 * unique, "#1", "#2" etc will be appended, until the name becomes unique.
	 */
	public static final String GENERATED_BEAN_NAME_SEPARATOR = BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR;


	/**
	 * Create a new GenericBeanDefinition for the given parent name and class name,
	 * eagerly loading the bean class if a ClassLoader has been specified.
	 * @param parentName the name of the parent bean, if any
	 * @param className the name of the bean class, if any
	 * @param classLoader the ClassLoader to use for loading bean classes
	 * (can be {@code null} to just register bean classes by name)
	 * @return the bean definition
	 * @throws ClassNotFoundException if the bean class could not be loaded
	 */
	public static AbstractBeanDefinition createBeanDefinition(
			@Nullable String parentName, @Nullable String className, @Nullable ClassLoader classLoader) throws ClassNotFoundException {

		GenericBeanDefinition bd = new GenericBeanDefinition();
		bd.setParentName(parentName);
		if (className != null) {
			if (classLoader != null) {
				bd.setBeanClass(ClassUtils.forName(className, classLoader));
			}
			else {
				bd.setBeanClassName(className);
			}
		}
		return bd;
	}

	/**
	 * Generate a bean name for the given top-level bean definition,
	 * unique within the given bean factory.
	 * @param beanDefinition the bean definition to generate a bean name for
	 * @param registry the bean factory that the definition is going to be
	 * registered with (to check for existing bean names)
	 * @return the generated bean name
	 * @throws BeanDefinitionStoreException if no unique name can be generated
	 * for the given bean definition
	 * @see #generateBeanName(BeanDefinition, BeanDefinitionRegistry, boolean)
	 */
	public static String generateBeanName(BeanDefinition beanDefinition, BeanDefinitionRegistry registry)
			throws BeanDefinitionStoreException {

		return generateBeanName(beanDefinition, registry, false);
	}

	/**
	 * Generate a bean name for the given bean definition, unique within the
	 * given bean factory.
	 * @param definition the bean definition to generate a bean name for
	 * @param registry the bean factory that the definition is going to be
	 * registered with (to check for existing bean names)
	 * @param isInnerBean whether the given bean definition will be registered
	 * as inner bean or as top-level bean (allowing for special name generation
	 * for inner beans versus top-level beans)
	 * @return the generated bean name
	 * @throws BeanDefinitionStoreException if no unique name can be generated
	 * for the given bean definition
	 */
	public static String generateBeanName(
			BeanDefinition definition, BeanDefinitionRegistry registry, boolean isInnerBean)
			throws BeanDefinitionStoreException {

		String generatedBeanName = definition.getBeanClassName();
		if (generatedBeanName == null) {
			if (definition.getParentName() != null) {
				generatedBeanName = definition.getParentName() + "$child";
			}
			else if (definition.getFactoryBeanName() != null) {
				generatedBeanName = definition.getFactoryBeanName() + "$created";
			}
		}
		if (!StringUtils.hasText(generatedBeanName)) {
			throw new BeanDefinitionStoreException("Unnamed bean definition specifies neither " +
					"'class' nor 'parent' nor 'factory-bean' - can't generate bean name");
		}

		String id = generatedBeanName;
		if (isInnerBean) {
			// Inner bean: generate identity hashcode suffix.
			id = generatedBeanName + GENERATED_BEAN_NAME_SEPARATOR + ObjectUtils.getIdentityHexString(definition);
		}
		else {
			// Top-level bean: use plain class name.
			// Increase counter until the id is unique.
			int counter = -1;
			while (counter == -1 || registry.containsBeanDefinition(id)) {
				counter++;
				id = generatedBeanName + GENERATED_BEAN_NAME_SEPARATOR + counter;
			}
		}
		return id;
	}

	/**
	 * Register the given bean definition with the given bean factory.
	 * @param definitionHolder the bean definition including name and aliases
	 * @param registry the bean factory to register with
	 * @throws BeanDefinitionStoreException if registration failed
	 */
	/*让我们继续跟踪程序的执行顺序，接下来我们来分析 DefaultBeanDefinitionDocumentReader 对
	Bean 定 义转 换的 Document 对 象解 析的 流程 中， 在其 parseDefaultElement() 方 法中 完成对
	Document 对 象 的 解 析 后 得 到 封 装 BeanDefinition 的 BeanDefinitionHold 对 象 ， 然 后 调 用
	BeanDefinitionReaderUtils 的 registerBeanDefinition() 方 法 向 IOC 容 器 注 册 解 析 的 Bean ，
	BeanDefinitionReaderUtils 的注册的源码如下：*/
	/*当调用 BeanDefinitionReaderUtils 向 IOC 容器注册解析的 BeanDefinition 时，真正完成注册功能的
	是 DefaultListableBeanFactory。*/
	/*BeanDefinitionReaderUtils 主要是校验 BeanDefinition 信息，然后将 Bean 添加到容器中一个管理
	BeanDefinition 的 HashMap 中。*/
	//将解析的BeanDefinitionHold注册到容器中
	public static void registerBeanDefinition(
			BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry)
			throws BeanDefinitionStoreException {

		// Register bean definition under primary name.
		//获取解析的BeanDefinition的名称
		String beanName = definitionHolder.getBeanName();
		//向IOC容器注册BeanDefinition
		registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());

		// Register aliases for bean name, if any.
		//如果解析的BeanDefinition有别名，向容器为其注册别名
		String[] aliases = definitionHolder.getAliases();
		if (aliases != null) {
			for (String alias : aliases) {
				registry.registerAlias(beanName, alias);
			}
		}
	}

	/**
	 * Register the given bean definition with a generated name,
	 * unique within the given bean factory.
	 * @param definition the bean definition to generate a bean name for
	 * @param registry the bean factory to register with
	 * @return the generated bean name
	 * @throws BeanDefinitionStoreException if no unique name can be generated
	 * for the given bean definition or the definition cannot be registered
	 */
	public static String registerWithGeneratedName(
			AbstractBeanDefinition definition, BeanDefinitionRegistry registry)
			throws BeanDefinitionStoreException {

		String generatedName = generateBeanName(definition, registry, false);
		registry.registerBeanDefinition(generatedName, definition);
		return generatedName;
	}

}
