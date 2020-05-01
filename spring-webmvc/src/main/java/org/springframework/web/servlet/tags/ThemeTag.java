/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.web.servlet.tags;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

/**
 * The {@code <theme>} tag looks up a theme message in the scope of this page.
 * Messages are looked up using the ApplicationContext's ThemeSource,
 * and thus should support internationalization.
 *
 * <p>Regards a HTML escaping setting, either on this tag instance,
 * the page level, or the web.xml level.
 *
 * <p>If "code" isn't set or cannot be resolved, "text" will be used
 * as default message.
 *
 * <p>Message arguments can be specified via the {@link #setArguments(Object)
 * arguments} attribute or by using nested {@code <spring:argument>} tags.
 *
 * <table>
 * <caption>Attribute Summary</caption>
 * <thead>
 * <tr>
 * <th class="colFirst">Attribute</th>
 * <th class="colOne">Required?</th>
 * <th class="colOne">Runtime Expression?</th>
 * <th class="colLast">Description</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr class="altColor">
 * <td>arguments</p></td>
 * <td>false</p></td>
 * <td>true</p></td>
 * <td>Set optional message arguments for this tag, as a (comma-)delimited
 * String (each String argument can contain JSP EL), an Object array (used as
 * argument array), or a single Object (used as single argument).</p></td>
 * </tr>
 * <tr class="rowColor">
 * <td>argumentSeparator</p></td>
 * <td>false</p></td>
 * <td>true</p></td>
 * <td>The separator character to be used for splitting the arguments string
 * value; defaults to a 'comma' (',').</p></td>
 * </tr>
 * <tr class="altColor">
 * <td>code</p></td>
 * <td>false</p></td>
 * <td>true</p></td>
 * <td>The code (key) to use when looking up the message. If code is not
 * provided, the text attribute will be used.</p></td>
 * </tr>
 * <tr class="rowColor">
 * <td>htmlEscape</p></td>
 * <td>false</p></td>
 * <td>true</p></td>
 * <td>Set HTML escaping for this tag, as boolean value. Overrides the default
 * HTML escaping setting for the current page.</p></td>
 * </tr>
 * <tr class="altColor">
 * <td>javaScriptEscape</p></td>
 * <td>false</p></td>
 * <td>true</p></td>
 * <td>Set JavaScript escaping for this tag, as boolean value.
 * Default is false.</p></td>
 * </tr>
 * <tr class="rowColor">
 * <td>message</p></td>
 * <td>false</p></td>
 * <td>true</p></td>
 * <td>A MessageSourceResolvable argument (direct or through JSP EL).</p></td>
 * </tr>
 * <tr class="altColor">
 * <td>scope</p></td>
 * <td>false</p></td>
 * <td>true</p></td>
 * <td>The scope to use when exporting the result to a variable. This attribute
 * is only used when var is also set. Possible values are page, request, session
 * and application.</p></td>
 * </tr>
 * <tr class="rowColor">
 * <td>text</p></td>
 * <td>false</p></td>
 * <td>true</p></td>
 * <td>Default text to output when a message for the given code could not be
 * found. If both text and code are not set, the tag will output null.</p></td>
 * </tr>
 * <tr class="altColor">
 * <td>var</p></td>
 * <td>false</p></td>
 * <td>true</p></td>
 * <td>The string to use when binding the result to the page, request, session
 * or application scope. If not specified, the result gets outputted to the
 * writer (i.e. typically directly to the JSP).</p></td>
 * </tr>
 * </tbody>
 * </table>
 * 
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 * @see org.springframework.ui.context.Theme
 * @see org.springframework.ui.context.ThemeSource
 * @see #setCode
 * @see #setText
 * @see #setHtmlEscape
 * @see HtmlEscapeTag#setDefaultHtmlEscape
 * @see org.springframework.web.util.WebUtils#HTML_ESCAPE_CONTEXT_PARAM
 * @see ArgumentTag
 */
@SuppressWarnings("serial")
public class ThemeTag extends MessageTag {

	/**
	 * Use the theme MessageSource for theme message resolution.
	 */
	@Override
	protected MessageSource getMessageSource() {
		return getRequestContext().getTheme().getMessageSource();
	}

	/**
	 * Return exception message that indicates the current theme.
	 */
	@Override
	protected String getNoSuchMessageExceptionDescription(NoSuchMessageException ex) {
		return "Theme '" + getRequestContext().getTheme().getName() + "': " + ex.getMessage();
	}

}
