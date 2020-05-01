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

package org.springframework.aop.framework.adapter;

import java.io.Serializable;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;

import org.springframework.aop.Advisor;
import org.springframework.aop.MethodBeforeAdvice;

/**
 * Adapter to enable {@link org.springframework.aop.MethodBeforeAdvice}
 * to be used in the Spring AOP framework.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
		/*DefaultAdvisorAdapterRegistry 设置了一系列的是配置，正是这些适配器的实现，为
		Spring AOP 提供了编织能力。下面以 MethodBeforeAdviceAdapter 为例，看具体的
		实现：*/
class MethodBeforeAdviceAdapter implements AdvisorAdapter, Serializable {

	@Override
	public boolean supportsAdvice(Advice advice) {
		return (advice instanceof MethodBeforeAdvice);
	}

	/*
	这个方法可以点到DefaultAdvisorChainFactory类中的
	//将Advisor转化成Interceptor
	MethodInterceptor[] interceptors = registry.getInterceptors(advisor);
	* */
	@Override
	public MethodInterceptor getInterceptor(Advisor advisor) {
		MethodBeforeAdvice advice = (MethodBeforeAdvice) advisor.getAdvice();
		/*Spring AOP 为了实现 advice 的织入，设计了特定的拦截器对这些功能进行了封装。我
		们接着看 MethodBeforeAdviceInterceptor 如何完成封装的？*/
		return new MethodBeforeAdviceInterceptor(advice);
	}

}
