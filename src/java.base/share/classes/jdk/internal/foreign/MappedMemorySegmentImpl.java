/*
 * Copyright (c) 2020, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.internal.foreign;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySession;
import java.nio.ByteBuffer;
import jdk.internal.access.foreign.UnmapperProxy;
import jdk.internal.misc.ScopedMemoryAccess;

/**
 * Implementation for a mapped memory segments. A mapped memory segment is a native memory segment, which
 * additionally features an {@link UnmapperProxy} object. This object provides detailed information about the
 * memory mapped segment, such as the file descriptor associated with the mapping. This information is crucial
 * in order to correctly reconstruct a byte buffer object from the segment (see {@link #makeByteBuffer()}).
 */
public class MappedMemorySegmentImpl extends NativeMemorySegmentImpl {

    private final UnmapperProxy unmapper;

    static ScopedMemoryAccess SCOPED_MEMORY_ACCESS = ScopedMemoryAccess.getScopedMemoryAccess();

    public MappedMemorySegmentImpl(long min, UnmapperProxy unmapper, long length, boolean isReadOnly, MemorySession session) {
        super(min, length, isReadOnly, session);
        this.unmapper = unmapper;
    }

    @Override
    ByteBuffer makeByteBuffer() {
        return nioAccess.newMappedByteBuffer(unmapper, min, (int)length, null,
                session == MemorySessionImpl.GLOBAL ? null : this);
    }

    @Override
    MappedMemorySegmentImpl dup(long offset, long size, boolean isReadOnly, MemorySession session) {
        return new MappedMemorySegmentImpl(min + offset, unmapper, size, isReadOnly, session);
    }

    // mapped segment methods

    @Override
    public MappedMemorySegmentImpl asSlice(long offset, long newSize) {
        return (MappedMemorySegmentImpl)super.asSlice(offset, newSize);
    }

    @Override
    public boolean isMapped() {
        return true;
    }

    // support for mapped segments

    public MemorySegment segment() {
        return MappedMemorySegmentImpl.this;
    }

    public void load() {
        SCOPED_MEMORY_ACCESS.load(session().state(), min, unmapper.isSync(), length);
    }

    public void unload() {
        SCOPED_MEMORY_ACCESS.unload(session().state(), min, unmapper.isSync(), length);
    }

    public boolean isLoaded() {
        return SCOPED_MEMORY_ACCESS.isLoaded(session().state(), min, unmapper.isSync(), length);
    }

    public void force() {
        SCOPED_MEMORY_ACCESS.force(session().state(), unmapper.fileDescriptor(), min, unmapper.isSync(), 0, length);
    }

    public static class EmptyMappedMemorySegmentImpl extends MappedMemorySegmentImpl {

        public EmptyMappedMemorySegmentImpl(boolean isReadOnly, MemorySession session) {
            super(0, null, 0, isReadOnly, session);
        }

        @Override
        public void load() {
            // do nothing
        }

        @Override
        public void unload() {
            // do nothing
        }

        @Override
        public boolean isLoaded() {
            return true;
        }

        @Override
        public void force() {
            // do nothing
        }
    }
}
