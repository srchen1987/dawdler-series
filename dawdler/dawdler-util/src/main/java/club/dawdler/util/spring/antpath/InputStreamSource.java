/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package club.dawdler.util.spring.antpath;

import java.io.IOException;
import java.io.InputStream;

public interface InputStreamSource {

	/**
	 * Return an {@link InputStream} for the content of an underlying resource.
	 * <p>
	 * It is expected that each call creates a <i>fresh</i> stream.
	 * <p>
	 * This requirement is particularly important when you consider an API such as
	 * JavaMail, which needs to be able to read the stream multiple times when
	 * creating mail attachments. For such a use case, it is <i>required</i> that
	 * each {@code getInputStream()} call returns a fresh stream.
	 * 
	 * @return the input stream for the underlying resource (must not be
	 *         {@code null})
	 * @throws java.io.FileNotFoundException if the underlying resource doesn't
	 *                                       exist
	 * @throws IOException                   if the content stream could not be
	 *                                       opened
	 */
	InputStream getInputStream() throws IOException;

}
