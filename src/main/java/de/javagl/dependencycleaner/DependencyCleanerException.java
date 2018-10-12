/*
 * www.javagl.de - DependencyCleaner
 *
 * Copyright (c) 2018 Marco Hutter - http://www.javagl.de
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package de.javagl.dependencycleaner;

/**
 * A class indicating an exception in this library
 */
final class DependencyCleanerException extends RuntimeException
{
    /**
     * Serial UID
     */
    private static final long serialVersionUID = -3376932928438924066L;

    /**
     * Creates a new instance
     * 
     * @param message The message
     */
    DependencyCleanerException(String message)
    {
        super(message);
    }

    /**
     * Creates a new instance
     * 
     * @param cause The cause
     */
    DependencyCleanerException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates a new instance
     * 
     * @param message The message
     * @param cause The cause
     */
    DependencyCleanerException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
