/**
 * Copyright 2010 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */
package de.dhbw.rahmlab.nativelibloader.impl.jogamp.other;

/**
 * A generic <i>unchecked exception</i> for Jogamp errors used throughout the
 * binding as a substitute for {@link RuntimeException}.
 */
@SuppressWarnings("serial")
public class JogampRuntimeException extends RuntimeException {

    /**
     * Constructs a JogampRuntimeException object.
     */
    public JogampRuntimeException() {
        super();
    }

    /**
     * Constructs a JogampRuntimeException object with the specified detail
     * message.
     */
    public JogampRuntimeException(final String message) {
        super(message);
    }

    /**
     * Constructs a JogampRuntimeException object with the specified detail
     * message and root cause.
     */
    public JogampRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a JogampRuntimeException object with the specified root cause.
     */
    public JogampRuntimeException(final Throwable cause) {
        super(cause);
    }
}
