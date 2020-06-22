/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.web.servlet.handler;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.util.ObjectUtils;

/**
 * Abstract implementation of the {@link org.springframework.web.servlet.HandlerMapping}
 * interface, detecting URL mappings for handler beans through introspection of all
 * defined beans in the application context.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see #determineUrlsForHandler
 */
public abstract class AbstractDetectingUrlHandlerMapping extends AbstractUrlHandlerMapping {

	private boolean detectHandlersInAncestorContexts = false;


	/**
	 * Set whether to detect handler beans in ancestor ApplicationContexts.
	 * <p>Default is "false": Only handler beans in the current ApplicationContext
	 * will be detected, i.e. only in the context that this HandlerMapping itself
	 * is defined in (typically the current DispatcherServlet's context).
	 * <p>Switch this flag on to detect handler beans in ancestor contexts
	 * (typically the Spring root WebApplicationContext) as well.
	 */
	public void setDetectHandlersInAncestorContexts(boolean detectHandlersInAncestorContexts) {
		this.detectHandlersInAncestorContexts = detectHandlersInAncestorContexts;
	}


	/**
	 * Calls the {@link #detectHandlers()} method in addition to the
	 * superclass's initialization.
	 */
	/*接下来，我们来看url和Controller
	的 关 系 是 如 何 建 立 的 呢 ？ HandlerMapping 的 子 类
	AbstractDetectingUrlHandlerMapping 实现了 initApplicationContext()方法，所以
	我们直接看子类中的初始化容器方法。*/
	@Override
	public void initApplicationContext() throws ApplicationContextException {
		super.initApplicationContext();
		detectHandlers();
	}

	/**
	 * Register all handlers found in the current ApplicationContext.
	 * <p>The actual URL determination for a handler is up to the concrete
	 * {@link #determineUrlsForHandler(String)} implementation. A bean for
	 * which no such URLs could be determined is simply not considered a handler.
	 * @throws org.springframework.beans.BeansException if the handler couldn't be registered
	 * @see #determineUrlsForHandler(String)
	 */
	/**
	 * 建立当前ApplicationContext中的所有controller和url的对应关系
	 */
	protected void detectHandlers() throws BeansException {
		ApplicationContext applicationContext = obtainApplicationContext();
		if (logger.isDebugEnabled()) {
			logger.debug("Looking for URL mappings in application context: " + applicationContext);
		}
		// 获取ApplicationContext容器中所有bean的Name
		String[] beanNames = (this.detectHandlersInAncestorContexts ?
				BeanFactoryUtils.beanNamesForTypeIncludingAncestors(applicationContext, Object.class) :
				applicationContext.getBeanNamesForType(Object.class));

		// Take any bean name that we can determine URLs for.
		// 遍历beanNames,并找到这些bean对应的url
		for (String beanName : beanNames) {
			/*determineUr
			lsForHandler(String beanName)方法的作用是获取每个 Controller 中的
			url，不同的子类有不同的实现，这是一个典型的模板设计模式。因为开发中我们用的最
			多 的 就 是 用 注 解 来 配 置 Controller 中 的 url ， BeanNameUrlHandlerMapping 是
			AbstractDetectingUrlHandlerMapping 的子类,处理注解形式的 url 映射.所以我们这里
			以 BeanNameUrlHandlerMapping 来 进 行 分 析 。 我 们 看
			BeanNameUrlHandlerMapping 是如何查 beanName 上所有映射的 url*/
			// 找bean上的所有url(controller上的url+方法上的url),该方法由对应的子类实现
			//===============关键位置
			String[] urls = determineUrlsForHandler(beanName);
			if (!ObjectUtils.isEmpty(urls)) {
				// URL paths found: Let's consider it a handler.
				// 保存urls和beanName的对应关系,put it to Map<urls,beanName>,该方法在父类AbstractUrlHandlerMapping中实现

				//
				registerHandler(urls, beanName);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Rejected bean name '" + beanName + "': no URL paths identified");
				}
			}
		}
	}


	/**
	 * Determine the URLs for the given handler bean.
	 * @param beanName the name of the candidate bean
	 * @return the URLs determined for the bean, or an empty array if none
	 */
	/** 获取controller中所有方法的url,由子类实现,典型的模板模式 ,虽然这里我只看到了一个子类**/
	protected abstract String[] determineUrlsForHandler(String beanName);

}
