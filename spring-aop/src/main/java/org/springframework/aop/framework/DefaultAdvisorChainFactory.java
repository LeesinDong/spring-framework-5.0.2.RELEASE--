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

package org.springframework.aop.framework;

import org.aopalliance.intercept.Interceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.support.MethodMatchers;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple but definitive way of working out an advice chain for a Method,
 * given an {@link Advised} object. Always rebuilds each advice chain;
 * caching can be provided by subclasses.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @author Adrian Colyer
 * @since 2.0.3
 */
@SuppressWarnings("serial")

public class DefaultAdvisorChainFactory implements AdvisorChainFactory, Serializable {

	/**
	 * 从提供的配置实例config中获取advisor列表,遍历处理这些advisor.如果是IntroductionAdvisor,
	 * 则判断此Advisor能否应用到目标类targetClass上.如果是PointcutAdvisor,则判断
	 * 此Advisor能否应用到目标方法method上.将满足条件的Advisor通过AdvisorAdaptor转化成Interceptor列表返回.
	 */

	/*这里获取调用链的逻辑其实比较简单，其最终的目的就是将Advisor数组一个一个的封装为Interceptor对象。
	在进行Advisor封装的时候，这里分为了三种类型：
	1.如果目标切面逻辑是一般的切面逻辑，即PointcutAdvisor，则会在运行时对目标方法进行动态匹配，
	因为前面可能存在还不能确认的是否应该应用切面逻辑的方法；

	2.如果切面逻辑是IntroductionAdvisor的，则将其封装为Interceptor类型的数组；

	3.如果以上两个都不是，说明切面逻辑可能是用户自定义的切面逻辑，这里就通过注册的AdvisorAdapter进行匹配，
	如果某个Adapter能够支持当前Advisor的转换，则调用其getInterceptor()方法将Advisor转换为MethodInterceptor返回。*/

	/*在为 AopProxy 代理对象配置拦截器的实现中，有一个取得拦截器的配置过程，这个过
	程是由 DefaultAdvisorChainFactory 实现的，这个工厂类负责生成拦截器链，在它的
	getInterceptorsAndDynamicInterceptionAdvice 方法中，有一个适配器和注册过程，
	通过配置 Spring 预先设计好的拦截器，Spring 加入了它对 AOP 实现的处理*/
	@Override
	public List<Object> getInterceptorsAndDynamicInterceptionAdvice(
			Advised config, Method method, @Nullable Class<?> targetClass) {

		// This is somewhat tricky... We have to process introductions first,
		// but we need to preserve order in the ultimate list.
		List<Object> interceptorList = new ArrayList<>(config.getAdvisors().length);
		Class<?> actualClass = (targetClass != null ? targetClass : method.getDeclaringClass());
		//查看是否包含IntroductionAdvisor
		// 判断切面逻辑中是否有IntroductionAdvisor类型的Advisor
		boolean hasIntroductions = hasMatchingIntroductions(config, actualClass);
		//这里实际上注册一系列AdvisorAdapter,用于将Advisor转化成MethodInterceptor
		AdvisorAdapterRegistry registry = GlobalAdvisorAdapterRegistry.getInstance();

		for (Advisor advisor : config.getAdvisors()) {
			if (advisor instanceof PointcutAdvisor) {
				// Add it conditionally.
				PointcutAdvisor pointcutAdvisor = (PointcutAdvisor) advisor;
				// 这里判断切面逻辑的调用链是否提前进行过过滤，如果进行过，则不再进行目标方法的匹配，
				// 如果没有，则再进行一次匹配。这里我们使用的AnnotationAwareAspectJAutoProxyCreator
				// 在生成切面逻辑的时候就已经进行了过滤，因而这里返回的是true，本文最开始也对这里进行了讲解
				if (config.isPreFiltered() || pointcutAdvisor.getPointcut().getClassFilter().matches(actualClass)) {
					//这个地方这两个方法的位置可以互换下
					//将Advisor转化成Interceptor
					// 将Advisor对象转换为MethodInterceptor数组
					MethodInterceptor[] interceptors = registry.getInterceptors(advisor);
					//检查当前advisor的pointcut是否可以匹配当前方法
					MethodMatcher mm = pointcutAdvisor.getPointcut().getMethodMatcher();
					// 这里进行匹配的时候，首先会检查是否为IntroductionAwareMethodMatcher类型的
					// Matcher，如果是，则调用其定义的matches()方法进行匹配，如果不是，则直接调用
					// 当前切面的matches()方法进行匹配。这里由于前面进行匹配时可能存在部分在静态匹配时
					// 无法确认的方法匹配结果，因而这里调用是必要的，而对于能够确认的匹配逻辑，这里调用
					// 也是非常迅速的，因为前面已经对匹配结果进行了缓存
					if (MethodMatchers.matches(mm, method, actualClass, hasIntroductions)) {
						// 判断如果是动态匹配，则使用InterceptorAndDynamicMethodMatcher对其进行封装
						if (mm.isRuntime()) {
							// Creating a new object instance in the getInterceptors() method
							// isn't a problem as we normally cache created chains.
							for (MethodInterceptor interceptor : interceptors) {
								interceptorList.add(new InterceptorAndDynamicMethodMatcher(interceptor, mm));
							}
						}
						else {
							// 如果是静态匹配，则直接将调用链返回
							interceptorList.addAll(Arrays.asList(interceptors));
						}
					}
				}
			}
			else if (advisor instanceof IntroductionAdvisor) {
				IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
				// 判断如果为IntroductionAdvisor类型的Advisor，则将调用链封装为Interceptor数组
				if (config.isPreFiltered() || ia.getClassFilter().matches(actualClass)) {
					Interceptor[] interceptors = registry.getInterceptors(advisor);
					interceptorList.addAll(Arrays.asList(interceptors));
				}
			}
			else {
				// 这里是提供的使用自定义的转换器对Advisor进行转换的逻辑，因为getInterceptors()方法中
				// 会使用相应的Adapter对目标Advisor进行匹配，如果能匹配上，通过其getInterceptor()方法
				// 将自定义的Advice转换为MethodInterceptor对象
				Interceptor[] interceptors = registry.getInterceptors(advisor);
				interceptorList.addAll(Arrays.asList(interceptors));
			}
		}

		return interceptorList;
	}

	/**
	 * Determine whether the Advisors contain matching introductions.
	 */
	private static boolean hasMatchingIntroductions(Advised config, Class<?> actualClass) {
		for (int i = 0; i < config.getAdvisors().length; i++) {
			Advisor advisor = config.getAdvisors()[i];
			if (advisor instanceof IntroductionAdvisor) {
				IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
				if (ia.getClassFilter().matches(actualClass)) {
					return true;
				}
			}
		}
		return false;
	}

}
