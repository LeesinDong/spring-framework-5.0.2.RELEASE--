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

package org.springframework.jmx.export.annotation;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.jmx.IJmxTestBean;

/**
 * @author Juergen Hoeller
 */
		/*FactoryBean 的实现类有非常多，比如：Proxy、RMI、JNDI、ServletContextFactoryBean 等等，
		FactoryBean 接口为 Spring 容器提供了一个很好的封装机制，具体的 getObject()有不同的实现类根
		据不同的实现策略来具体提供，我们分析一个最简单的 AnnotationTestFactoryBean 的实现源码
		其他的 Proxy，RMI，JNDI 等等，都是根据相应的策略提供 getObject()的实现。这里不做一一分析，
		这已经不是 Spring 的核心功能，感兴趣的小伙可以再去深入研究*/
public class AnnotationTestBeanFactory implements FactoryBean<FactoryCreatedAnnotationTestBean> {

	private final FactoryCreatedAnnotationTestBean instance = new FactoryCreatedAnnotationTestBean();

	public AnnotationTestBeanFactory() {
		this.instance.setName("FACTORY");
	}

	@Override
	public FactoryCreatedAnnotationTestBean getObject() throws Exception {
		return this.instance;
	}

	//AnnotationTestBeanFactory产生Bean实例对象的实现
	@Override
	public Class<? extends IJmxTestBean> getObjectType() {
		return FactoryCreatedAnnotationTestBean.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
