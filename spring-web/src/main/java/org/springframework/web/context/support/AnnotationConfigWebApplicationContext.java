/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.web.context.support;

import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.*;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ContextLoader;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@link org.springframework.web.context.WebApplicationContext WebApplicationContext}
 * implementation which accepts annotated classes as input - in particular
 * {@link org.springframework.context.annotation.Configuration @Configuration}-annotated
 * classes, but also plain {@link org.springframework.stereotype.Component @Component}
 * classes and JSR-330 compliant classes using {@code javax.inject} annotations. Allows
 * for registering classes one by one (specifying class names as config location) as well
 * as for classpath scanning (specifying base packages as config location).
 *
 * <p>This is essentially the equivalent of
 * {@link org.springframework.context.annotation.AnnotationConfigApplicationContext
 * AnnotationConfigApplicationContext} for a web environment.
 *
 * <p>To make use of this application context, the
 * {@linkplain ContextLoader#CONTEXT_CLASS_PARAM "contextClass"} context-param for
 * ContextLoader and/or "contextClass" init-param for FrameworkServlet must be set to
 * the fully-qualified name of this class.
 *
 * <p>As of Spring 3.1, this class may also be directly instantiated and injected into
 * Spring's {@code DispatcherServlet} or {@code ContextLoaderListener} when using the
 * new {@link org.springframework.web.WebApplicationInitializer WebApplicationInitializer}
 * code-based alternative to {@code web.xml}. See its Javadoc for details and usage examples.
 *
 * <p>Unlike {@link XmlWebApplicationContext}, no default configuration class locations
 * are assumed. Rather, it is a requirement to set the
 * {@linkplain ContextLoader#CONFIG_LOCATION_PARAM "contextConfigLocation"}
 * context-param for {@link ContextLoader} and/or "contextConfigLocation" init-param for
 * FrameworkServlet.  The param-value may contain both fully-qualified
 * class names and base packages to scan for components. See {@link #loadBeanDefinitions}
 * for exact details on how these locations are processed.
 *
 * <p>As an alternative to setting the "contextConfigLocation" parameter, users may
 * implement an {@link org.springframework.context.ApplicationContextInitializer
 * ApplicationContextInitializer} and set the
 * {@linkplain ContextLoader#CONTEXT_INITIALIZER_CLASSES_PARAM "contextInitializerClasses"}
 * context-param / init-param. In such cases, users should favor the {@link #refresh()}
 * and {@link #scan(String...)} methods over the {@link #setConfigLocation(String)}
 * method, which is primarily for use by {@code ContextLoader}.
 *
 * <p>Note: In case of multiple {@code @Configuration} classes, later {@code @Bean}
 * definitions will override ones defined in earlier loaded files. This can be leveraged
 * to deliberately override certain bean definitions via an extra Configuration class.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.0
 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
 */
public class AnnotationConfigWebApplicationContext extends AbstractRefreshableWebApplicationContext
		implements AnnotationConfigRegistry {

	@Nullable
	private BeanNameGenerator beanNameGenerator;

	@Nullable
	private ScopeMetadataResolver scopeMetadataResolver;

	private final Set<Class<?>> annotatedClasses = new LinkedHashSet<>();

	private final Set<String> basePackages = new LinkedHashSet<>();


	/**
	 * Set a custom {@link BeanNameGenerator} for use with {@link AnnotatedBeanDefinitionReader}
	 * and/or {@link ClassPathBeanDefinitionScanner}.
	 * <p>Default is {@link org.springframework.context.annotation.AnnotationBeanNameGenerator}.
	 * @see AnnotatedBeanDefinitionReader#setBeanNameGenerator
	 * @see ClassPathBeanDefinitionScanner#setBeanNameGenerator
	 */
	public void setBeanNameGenerator(@Nullable BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = beanNameGenerator;
	}

	/**
	 * Return the custom {@link BeanNameGenerator} for use with {@link AnnotatedBeanDefinitionReader}
	 * and/or {@link ClassPathBeanDefinitionScanner}, if any.
	 */
	@Nullable
	protected BeanNameGenerator getBeanNameGenerator() {
		return this.beanNameGenerator;
	}

	/**
	 * Set a custom {@link ScopeMetadataResolver} for use with {@link AnnotatedBeanDefinitionReader}
	 * and/or {@link ClassPathBeanDefinitionScanner}.
	 * <p>Default is an {@link org.springframework.context.annotation.AnnotationScopeMetadataResolver}.
	 * @see AnnotatedBeanDefinitionReader#setScopeMetadataResolver
	 * @see ClassPathBeanDefinitionScanner#setScopeMetadataResolver
	 */
	public void setScopeMetadataResolver(@Nullable ScopeMetadataResolver scopeMetadataResolver) {
		this.scopeMetadataResolver = scopeMetadataResolver;
	}

	/**
	 * Return the custom {@link ScopeMetadataResolver} for use with {@link AnnotatedBeanDefinitionReader}
	 * and/or {@link ClassPathBeanDefinitionScanner}, if any.
	 */
	@Nullable
	protected ScopeMetadataResolver getScopeMetadataResolver() {
		return this.scopeMetadataResolver;
	}


	/**
	 * Register one or more annotated classes to be processed.
	 * <p>Note that {@link #refresh()} must be called in order for the context
	 * to fully process the new classes.
	 * @param annotatedClasses one or more annotated classes,
	 * e.g. {@link org.springframework.context.annotation.Configuration @Configuration} classes
	 * @see #scan(String...)
	 * @see #loadBeanDefinitions(DefaultListableBeanFactory)
	 * @see #setConfigLocation(String)
	 * @see #refresh()
	 */
	public void register(Class<?>... annotatedClasses) {
		Assert.notEmpty(annotatedClasses, "At least one annotated class must be specified");
		this.annotatedClasses.addAll(Arrays.asList(annotatedClasses));
	}

	/**
	 * Perform a scan within the specified base packages.
	 * <p>Note that {@link #refresh()} must be called in order for the context
	 * to fully process the new classes.
	 * @param basePackages the packages to check for annotated classes
	 * @see #loadBeanDefinitions(DefaultListableBeanFactory)
	 * @see #register(Class...)
	 * @see #setConfigLocation(String)
	 * @see #refresh()
	 */
	public void scan(String... basePackages) {
		Assert.notEmpty(basePackages, "At least one base package must be specified");
		this.basePackages.addAll(Arrays.asList(basePackages));
	}


	/**
	 * Register a {@link org.springframework.beans.factory.config.BeanDefinition} for
	 * any classes specified by {@link #register(Class...)} and scan any packages
	 * specified by {@link #scan(String...)}.
	 * <p>For any values specified by {@link #setConfigLocation(String)} or
	 * {@link #setConfigLocations(String[])}, attempt first to load each location as a
	 * class, registering a {@code BeanDefinition} if class loading is successful,
	 * and if class loading fails (i.e. a {@code ClassNotFoundException} is raised),
	 * assume the value is a package and attempt to scan it for annotated classes.
	 * <p>Enables the default set of annotation configuration post processors, such that
	 * {@code @Autowired}, {@code @Required}, and associated annotations can be used.
	 * <p>Configuration class bean definitions are registered with generated bean
	 * definition names unless the {@code value} attribute is provided to the stereotype
	 * annotation.
	 * @param beanFactory the bean factory to load bean definitions into
	 * @see #register(Class...)
	 * @see #scan(String...)
	 * @see #setConfigLocation(String)
	 * @see #setConfigLocations(String[])
	 * @see AnnotatedBeanDefinitionReader
	 * @see ClassPathBeanDefinitionScanner
	 */
	/*AnnotationConfigWebApplicationContext 是 AnnotationConfigApplicationContext 的 Web 版，
	它们对于注解 Bean 的注册和扫描是基本相同的，但是 AnnotationConfigWebApplicationContext
	对注解 Bean 定义的载入稍有不同，AnnotationConfigWebApplicationContext 注入注解 Bean 定义
	源码如下：*/
	//载入注解Bean定义资源
	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) {
		//为容器设置注解Bean定义读取器
		AnnotatedBeanDefinitionReader reader = getAnnotatedBeanDefinitionReader(beanFactory);
		//为容器设置类路径Bean定义扫描器
		ClassPathBeanDefinitionScanner scanner = getClassPathBeanDefinitionScanner(beanFactory);

		//获取容器的Bean名称生成器
		BeanNameGenerator beanNameGenerator = getBeanNameGenerator();
		//为注解Bean定义读取器和类路径扫描器设置Bean名称生成器
		if (beanNameGenerator != null) {
			reader.setBeanNameGenerator(beanNameGenerator);
			scanner.setBeanNameGenerator(beanNameGenerator);
			beanFactory.registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator);
		}

		//获取容器的作用域元信息解析器
		ScopeMetadataResolver scopeMetadataResolver = getScopeMetadataResolver();
		//为注解Bean定义读取器和类路径扫描器设置作用域元信息解析器
		if (scopeMetadataResolver != null) {
			reader.setScopeMetadataResolver(scopeMetadataResolver);
			scanner.setScopeMetadataResolver(scopeMetadataResolver);
		}

		if (!this.annotatedClasses.isEmpty()) {
			if (logger.isInfoEnabled()) {
				logger.info("Registering annotated classes: [" +
						StringUtils.collectionToCommaDelimitedString(this.annotatedClasses) + "]");
			}
			reader.register(this.annotatedClasses.toArray(new Class<?>[this.annotatedClasses.size()]));
		}

		if (!this.basePackages.isEmpty()) {
			if (logger.isInfoEnabled()) {
				logger.info("Scanning base packages: [" +
						StringUtils.collectionToCommaDelimitedString(this.basePackages) + "]");
			}
			scanner.scan(this.basePackages.toArray(new String[this.basePackages.size()]));
		}

		//获取容器定义的Bean定义资源路径
		String[] configLocations = getConfigLocations();
		//如果定位的Bean定义资源路径不为空
		if (configLocations != null) {
			for (String configLocation : configLocations) {
				try {
					//使用当前容器的类加载器加载定位路径的字节码类文件
					Class<?> clazz = ClassUtils.forName(configLocation, getClassLoader());
					if (logger.isInfoEnabled()) {
						logger.info("Successfully resolved class for [" + configLocation + "]");
					}
					reader.register(clazz);
				}
				catch (ClassNotFoundException ex) {
					if (logger.isDebugEnabled()) {
						logger.debug("Could not load class for config location [" + configLocation +
								"] - trying package scan. " + ex);
					}
					//如果容器类加载器加载定义路径的Bean定义资源失败
					//则启用容器类路径扫描器扫描给定路径包及其子包中的类
					int count = scanner.scan(configLocation);
					if (logger.isInfoEnabled()) {
						if (count == 0) {
							logger.info("No annotated classes found for specified class/package [" + configLocation + "]");
						}
						else {
							logger.info("Found " + count + " annotated classes in package [" + configLocation + "]");
						}
					}
				}
			}
		}
	}


	/**
	 * Build an {@link AnnotatedBeanDefinitionReader} for the given bean factory.
	 * <p>This should be pre-configured with the {@code Environment} (if desired)
	 * but not with a {@code BeanNameGenerator} or {@code ScopeMetadataResolver} yet.
	 * @param beanFactory the bean factory to load bean definitions into
	 * @since 4.1.9
	 * @see #getEnvironment()
	 * @see #getBeanNameGenerator()
	 * @see #getScopeMetadataResolver()
	 */
	protected AnnotatedBeanDefinitionReader getAnnotatedBeanDefinitionReader(DefaultListableBeanFactory beanFactory) {
		return new AnnotatedBeanDefinitionReader(beanFactory, getEnvironment());
	}

	/**
	 * Build a {@link ClassPathBeanDefinitionScanner} for the given bean factory.
	 * <p>This should be pre-configured with the {@code Environment} (if desired)
	 * but not with a {@code BeanNameGenerator} or {@code ScopeMetadataResolver} yet.
	 * @param beanFactory the bean factory to load bean definitions into
	 * @since 4.1.9
	 * @see #getEnvironment()
	 * @see #getBeanNameGenerator()
	 * @see #getScopeMetadataResolver()
	 */
	protected ClassPathBeanDefinitionScanner getClassPathBeanDefinitionScanner(DefaultListableBeanFactory beanFactory) {
		return new ClassPathBeanDefinitionScanner(beanFactory, true, getEnvironment());
	}

}

		/*现在通过上面的代码，总结一下 IOC 容器初始化的基本步骤：
		1、初始化的入口在容器实现中的 refresh()调用来完成。
		2、对 Bean 定义载入 IOC 容器使用的方法是 loadBeanDefinition(),
		其中的大致过程如下：通过 ResourceLoader 来完成资源文件位置的定位，DefaultResourceLoader
		是默认的实现，同时上下文本身就给出了 ResourceLoader 的实现，可以从类路径，文件系统,URL 等
		方式来定为资源位置。如果是 XmlBeanFactory 作为 IOC 容器，那么需要为它指定 Bean 定义的资源，
		也 就 是 说 Bean 定 义 文 件 时 通 过 抽 象 成 Resource 来 被 IOC 容 器 处 理 的 ， 容 器 通 过
		BeanDefinitionReader 来 完 成 定 义 信 息 的 解 析 和 Bean 信 息 的 注 册 , 往 往 使 用 的 是
		XmlBeanDefinitionReader 来 解 析 Bean 的 XML 定 义 文 件 - 实 际 的 处 理 过 程 是 委 托 给
		BeanDefinitionParserDelegate 来完成的，从而得到 bean 的定义信息，这些信息在 Spring 中使用
		BeanDefinition对象来表示-这个名字可以让我们想到loadBeanDefinition(),registerBeanDefinition()
		这些相关方法。它们都是为处理 BeanDefinitin 服务的，容器解析得到 BeanDefinition 以后，需要把
		它在 IOC 容器中注册，这由 IOC 实现 BeanDefinitionRegistry 接口来实现。注册过程就是在 IOC 容器
		内部维护的一个 HashMap 来保存得到的 BeanDefinition 的过程。这个 HashMap 是 IOC 容器持有
		Bean 信息的场所，以后对 Bean 的操作都是围绕这个 HashMap 来实现的。
		然后我们就可以通过 BeanFactory 和 ApplicationContext 来享受到 Spring IOC 的服务了,在使用 IOC
		容器的时候，我们注意到除了少量粘合代码，绝大多数以正确 IOC 风格编写的应用程序代码完全不用关
		心如何到达工厂，因为容器将把这些对象与容器管理的其他对象钩在一起。基本的策略是把工厂放到已
		知的地方，最好是放在对预期使用的上下文有意义的地方，以及代码将实际需要访问工厂的地方。Spring
		本身提供了对声明式载入 web 应用程序用法的应用程序上下文,并将其存储在 ServletContext 中的框架
		实现*/